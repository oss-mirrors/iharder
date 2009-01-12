



/**
 * <p>A simple server that echoes UDP packets to the console
 * and via UDP back to the source IP and port. If you have netcat
 * on your system try the following command to see the echo working too:</p>
 *
 * <pre>nc -u localhost 1234</pre>
 *
 * @author Robert Harder
 * @author rharder@users.sourceforge.net
 * @version 0.1
 * @see UdpServer
 */
public class UdpEchoExample {

    public static void main(String[] args) throws Exception{

        int port = 1234;
        try{ port = Integer.parseInt(args[0]); }
        catch( Exception exc ){
            System.out.println("No port, or bad port, provided. Will use " + port );
        }   // end catch

        UdpServer us = new UdpServer();                             // Create the server
        us.setPort( port );                                         // Set the port
        us.addUdpServerListener( new UdpServer.Listener() {         // Add listener
            @Override
            public void packetReceived( UdpServer.Event evt ) {     // Packet received
                System.out.println( evt.getPacketAsString() );      // Write to console
                try {
                    evt.send( evt.getPacket() );                    // Packet magically already contains
                                                                    // return address information
                } catch( java.io.IOException ex ) {
                    ex.printStackTrace(); // Please don't use printStackTrace in production code
                }   // end ctach
            }   // end packetReceived
        }); // end Listener
        us.start();
        System.out.println("Server started on port " + port );

    }   // end main

}   // end class UdpEchoExample
