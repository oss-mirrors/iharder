
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;



/**
 * <p>A simple server that echoes UDP or TCP data to the console
 * and back to the source. You can connect to the server with telnet:</p>
 *
 * <pre>telnet localhost 1234</pre>
 *
 * <p>or netcat (for UDP experiments):</p>
 *
 * <pre>nc -u localhost 1234</pre>
 *
 * <p>Because the NioServer can listen on multiple ports simultaneously,
 * you can invoke this echo program with as many ports listed as you want:</p>
 *
 * <pre>telnet localhost 1234 8000 42000</pre>
 *
 * @author Robert Harder
 * @author rharder@users.sourceforge.net
 * @version 0.1
 * @see NioServer
 */
public class NioEchoExample {


    public static void main(String[] args) throws Exception{

        if( args.length == 0 ){
            System.out.println("\nNo ports provided. Using port 1234 as default.");
            args = new String[]{ "1234" };
        }

        // Parse command line port requests
        NioServer ns = new NioServer();                                 // New server
        for( String s : args ){
            try{
                int port = Integer.parseInt(args[0]);
                SocketAddress addr = new InetSocketAddress(port);
                ns.addTcpBinding(addr).addUdpBinding(addr);             // Bind to TCP and UDP
                System.out.println("  Listening on port " + port );
            } catch( Exception exc ){
                System.out.println("To specify a port, include it as the first argument.");
            }
        }   // end for
        

        ns.addNioServerListener(new NioServer.Adapter() {               // Listener
            Charset charset = Charset.forName( "US-ASCII" );            // Only print ASCII text
            
            /**
             * Echo all TCP data as it is received.
             */
            @Override
            public void tcpDataReceived(NioServer.Event evt) {
                ByteBuffer inBuff = evt.getInputBuffer();               // Input buffer

                inBuff.mark();                                          // Remember where we started
                System.out.print( charset.decode( inBuff ) );           // Echo to console
                inBuff.reset();                                         // Back to the mark

                ByteBuffer outBuff = evt.getOutputBuffer();             // Output buffer
                outBuff.clear();                                        // Clear output
                outBuff.put( inBuff );                                  // Copy input into output
                outBuff.flip();                                         // Prepare output for playback
            }

            /**
             * Echo all UDP data as it is received.
             * This code is identical to the TCP code above
             * but is crammed into one line for illustration only.
             */
            @Override
            public void udpDataReceived(NioServer.Event evt) {          // Same as TCP method!
                ByteBuffer inBuff = evt.getInputBuffer();               // Input buffer

                inBuff.mark();                                          // Remember where we started
                System.out.print( charset.decode( inBuff ) );           // Echo to console
                inBuff.reset();                                         // Back to the mark

                ByteBuffer outBuff = evt.getOutputBuffer();             // Output buffer
                outBuff.clear();                                        // Clear output
                outBuff.put( inBuff );                                  // Copy input into output
                outBuff.flip();                                         // Prepare output for playback
            }
        });

        ns.start();
    }   // end main
    
}   // end NioEchoExample
