
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;




/**
 * <p>A simple server that echoes TCP data to the console
 * and back to the source. You can connect to the server with telnet:</p>
 *
 * <pre>telnet localhost 1234</pre>
 *
 * @author Robert Harder
 * @author rharder@users.sourceforge.net
 * @version 0.1
 * @see TcpServer
 */
public class TcpEchoExample {

    public static void main(String[] args) throws Exception{

        int port = 1234;
        try{ port = Integer.parseInt(args[0]); }
        catch( Exception exc ){
            System.out.println("No port, or bad port, provided. Will use " + port );
        }   // end catch

        TcpServer ts = new TcpServer();                                     // Create the server
        ts.setPort( port );                                                 // Set the port
        ts.addTcpServerListener( new TcpServer.Listener() {                 // Add listener

            @Override
            public void socketReceived( TcpServer.Event evt ) {             // New stream
                try {
                    InputStream in = evt.getSocket().getInputStream();      // Input
                    OutputStream out = evt.getSocket().getOutputStream();   // Output
                    byte[] buff = new byte[64];                             // Buffer
                    int num = -1;                                           // Bytes read
                    while( (num = in.read(buff)) >= 0 ){                    // Not EOS yet
                        System.out.print(new String(buff,0,num) );          // Echo to console
                        out.write( buff, 0, num );                          // Echo to source
                        out.flush();                                        // Flush stream
                    }   // end while
                } catch( IOException exc ) {
                    exc.printStackTrace();
                } finally {
                    try {
                        evt.getSocket().close();
                    } catch( IOException exc2 ) {
                        exc2.printStackTrace();
                    }
                }
            }   // end socketReceived
        }); // end Listener
        ts.start();
        System.out.println("Server started on port " + port );

    }   // end main

}   // end class TcpEchoExample
