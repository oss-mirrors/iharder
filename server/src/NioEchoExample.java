
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;



/**
 * A simpel echo program.
 * 
 * @author Robert Harder
 */
public class NioEchoExample {


    public static void main(String[] args) throws Exception{

        System.out.println("Echo Example");
        System.out.println("List as many ports as your like as arguments.");
        System.out.println("The server will respond to all of them simultaneously.");
        System.out.println("Example: java NioEchoExample 1234 1235 1236");

        if( args.length == 0 ){
            System.out.println("\nNo ports provided. Using port 1234 as default.");
            args = new String[]{ "1234" };
        }


        NioServer ns = new NioServer();
        for( String s : args ){
            try{
                int port = Integer.parseInt(args[0]);
                SocketAddress addr = new InetSocketAddress(port);
                ns.addTcpBinding(addr).addUdpBinding(addr);
                System.out.println("  Listening on port " + port );
            } catch( Exception exc ){
                System.out.println("To specify a port, include it as the first argument.");
            }
        }   // end for
        

        // Add a listener to echo whatever data comes in.
        ns.addNioServerListener(new NioServer.Adapter() {
            /**
             * Echo all TCP data as it is received.
             */
            @Override
            public void tcpDataReceived(NioServer.Event evt) {
                ByteBuffer inBuff = evt.getInputBuffer();              // Input buffer
                ByteBuffer outBuff = evt.getOutputBuffer();            // Output buffer
                outBuff.clear();                                                // Clear output
                outBuff.put( inBuff );                                          // Copy input into output
                outBuff.flip();                                                 // Prepare output for playback
            }

            /**
             * Echo all UDP data as it is received.
             * This code is identical to the TCP code above
             * but is crammed into one line for illustration only.
             */
            @Override
            public void udpDataReceived(NioServer.Event evt) {
                // Same as above but crammed into one line
                ((ByteBuffer)evt.getOutputBuffer().clear()).put( evt.getInputBuffer() ).flip();
            }
        });

        ns.start();
    }   // end main
    
}   // end NioEchoExample
