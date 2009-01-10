
import java.io.*;
import java.nio.*;
import java.net.*;
import java.beans.*;
import java.nio.charset.*;
import java.nio.channels.*;
import java.util.logging.*;


public class FileServerNioExample {

    public static void main(String[] args) throws Exception{
        NioServer ns = new NioServer();

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
            private CharBuffer nack = CharBuffer.wrap("\r\nFilename too long. Try again: ");
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
                    nack.rewind();
                    encoder.reset().encode(nack,outBuff,true);
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
                        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    inBuff.flip();                                              // Else leave all the data in the buffer
                }
            }
            


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
        });

        ns.setSingleTcpPort(1234);
        ns.start();
        //Thread.sleep(5000);
    }


}
