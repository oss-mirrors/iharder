
import java.io.*;
import java.nio.*;
import java.net.*;
import java.beans.*;
import java.nio.charset.*;
import java.nio.channels.*;
import java.util.logging.*;

/**
 * A simple example of the {@link NioServer} that listens on a TCP port and
 * sends a requested file. Run <code>java FileServerNioExample [port]</code>
 * where [port] is optionally the port to listen on (default is 1234).
 * Telnet to this machine on that port (<code>telnet localhost 1234</code>)
 * and type the name of a file in the current directory where you ran
 * this Java code.
 *
 * <p>Public Domain</p>
 * 
 * @author Robert Harder
 */
public class FileServerNioExample {

    public static void main(String[] args) throws Exception{

        int port = 1234;
        try{ port = Integer.parseInt(args[0]); }
        catch( Exception exc ){
            System.out.println("To specify a port, include it as the first argument.");
        } finally {
            System.out.println("Telnet to this computer on port " + port + " and type the name of a file in this directory.");
        }

        NioServer ns = new NioServer();
        ns.setSingleTcpPort(port).setSingleUdpPort(port);

        // Listen for property change events for general interest only
        ns.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String prop = evt.getPropertyName();
                Object oldVal = evt.getOldValue();
                Object newVal = evt.getNewValue();
                System.out.println("Property: " + prop + ", Old: " + oldVal + ", New: " + newVal );
                
                if( newVal instanceof Throwable ){
                    ((Throwable)newVal).printStackTrace();
                }
            }
        });

        // Listen for NioServer events
        ns.addNioServerListener(new NioServer.Listener() {
            private Charset charset = Charset.forName("US-ASCII");
            private CharsetEncoder encoder = charset.newEncoder();
            private CharsetDecoder decoder = charset.newDecoder();
            private CharBuffer greeting = CharBuffer.wrap("Greetings. Enter filename: ");
            private CharBuffer ack = CharBuffer.wrap("Sending...\r\n");
            private CharBuffer nackTooLong = CharBuffer.wrap("\r\nFilename too long. Try again: ");
            private CharBuffer nackNotFound = CharBuffer.wrap("\r\nFile not found. Try again: ");
            private CharBuffer request = CharBuffer.allocate(100);

            @Override
            public void newConnectionReceived(NioServer.Event evt) {
                SocketAddress local = evt.getLocalSocketAddress();
                SocketAddress remote = evt.getRemoteSocketAddress();
                System.out.println("New connection from " + remote + " to " + local);

                ByteBuffer buff = evt.getOutputBuffer();                        // Reusable output buffer
                buff.clear();                                                   // Clear buffer
                greeting.rewind();                                              // Prepare greeting
                encoder.reset().encode( greeting, buff, true);                  // Write greeting to buffer
                buff.flip();                                                    // Prepare buffer to be read back
            }

            
            @Override
            public void connectionClosed(NioServer.Event evt) {
                System.out.println("Sorry to see you go: " + evt.getKey().channel() );
            }

            @Override
            public void tcpDataReceived(NioServer.Event evt) {
                ByteBuffer inBuff = evt.getInputBuffer();                       // Reusable buffer
                ByteBuffer outBuff = evt.getOutputBuffer();                     // Reusable buffer
                
                request.clear();                                                // Clear reusable request buffer
                CoderResult cr = decoder.reset().decode(inBuff, request, true );  // Write buffer into request
                request.flip();                                                 // Prepare request buffer for reading
                String s = request.toString();                                  // Make simple String of the request
                
                if( cr == CoderResult.OVERFLOW ){                               // 100 characters isn't big enough?
                    // Bail out, showing that we read the data
                    // and want nothing more to do with it.
                    // The next bit of data we receive will
                    // not have this old data as part of it.
                    outBuff.clear();
                    encoder.reset().encode((CharBuffer)nackTooLong.rewind(),outBuff,true);
                    outBuff.flip();
                    inBuff.clear().flip();
                    return;
                }
                
                if( s.contains("\r") || s.contains("\n") ){                     // Someone hit return yet?
                    FileInputStream fis = null;
                    try {
                        s = s.trim();                                           // Remove whitespace (like newline)
                        fis = new FileInputStream(s);                           // Input stream
                        FileChannel fc = fis.getChannel();                      // Associated channel
                        evt.getKey().attach( fc );                              // Save channel in key's attachment
                        evt.setNotifyOnTcpWritable(true);                       // Tell server we have something to write
                        outBuff.clear();
                        ack.rewind();
                        encoder.reset().encode(ack, outBuff, true);
                        outBuff.flip();
                    } catch (IOException ex) {
                        Logger.getLogger(this.getClass().getName()).warning(ex.getMessage());
                        outBuff.clear();
                        encoder.reset().encode((CharBuffer)nackNotFound.rewind(),outBuff,true);
                        outBuff.flip();
                        inBuff.clear().flip();
                    }
                } else {
                    inBuff.flip();                                              // Else leave all the data in the buffer
                }
            }   // end tcpDataReceived
            

            @Override
            public void udpDataReceived(NioServer.Event evt) {}        // Not using UDP


            @Override
            public void tcpReadyToWrite(NioServer.Event evt) {
                ByteBuffer dst = evt.getOutputBuffer();                         // Leave data on this buffer to have it written
                FileChannel fc = (FileChannel) evt.getKey().attachment();       // We previously attached the FileChannel
                if( fc == null ){
                    return;
                }
                dst.clear();                                                    // Clear the buffer
                try {
                    if( fc.read(dst) < 0 ){                                     // Read from FileChannel into buffer and check for EOF
                        fc.close();                                             // Close FileChannel
                        evt.close();                                            // Close SocketChannel
                    }   // end if: EOF
                } catch (IOException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                }
                dst.flip();                                                     // Show that buffer has data in it to be written
            }
            

        }); // end listener

        ns.start();
        //Thread.sleep(5000);
    }




}
