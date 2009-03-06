package rvision;

import java.net.*;
import java.io.*;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Listens for simple HTTP commands to manipulate a camera.
 * Commands are one character each and can be strung together.
 * For example, to pan right a little, use the command 'R' but
 * to pan right a lot, use "RRRRRRRR."
 * 
 * <table>
 * <caption>The Commands</caption>
 * <thead>
 *  <tr><th>Character</th><th>Command</th></tr>
 * </thead>
 * <tbody>
 *  <tr><th>U</th><td>Tilt Up</td></tr>
 *  <tr><th>D</th><td>Tilt Down</td></tr>
 *  <tr><th>L</th><td>Pan Left</td></tr>
 *  <tr><th>R</th><td>Pan Right</td></tr>
 *  <tr><th>I</th><td>Zoom In</td></tr>
 *  <tr><th>O ("Oh")</th><td>Zoom Out</td></tr>
 *  <tr><th>_ (underscore)</th><td>Delay 1/2 second</td></tr>
 * </tbody>
 * </table>
 * @author robert.harder
 */
public class HttpCameraServer extends TcpServer implements TcpServer.Listener {
    private final static Logger LOGGER = Logger.getLogger(HttpCameraServer.class.getName());
    
    private final static Pattern COMMAND_PATTERN = Pattern.compile( "^GET .*cmd=(([\\w]+)).*" );
    private final static Pattern RAW_PATTERN = Pattern.compile( "^GET .*raw=(([\\w\\d\\-]+)).*" );
    
    //public final static String PORT_PROP = "port";
    //private final static int PORT_DEFAULT = 8080;
    //private int port = PORT_DEFAULT;
    
    private Camera camera;
    //private boolean dispose;
    //private Thread ioThread;
    //private ServerSocket tcpServer;
    private int howLong = 250;
    
    
    public static void main( String[] args ) {
        //args = new String[]{"4000","4002"};
        try {
            HttpCameraServer serv = new HttpCameraServer();
            
            UdpCameraClient cam = new UdpCameraClient("localhost", Integer.parseInt(args[0]));
            //Camera cam = new Camera( args[0] );
            
            serv.setPort(Integer.parseInt(args[1]));
            serv.setCamera( cam );
            serv.start();
        } catch (Exception ex) {
            Logger.getLogger(HttpCameraServer.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Usage: java [-cp classpath] rvision.HttpCameraServer udpPort httpPort ");
            System.out.println("Serial Ports:" );
            for( String s : SerialStream.getPortNames() ){
                System.out.println("  " + s );
            }
        }
        
    }
    
    
    
    
    
    public HttpCameraServer(){
        addTcpServerListener(this);
    }
    
    
    /**
     * Sets the {@link Camera} to control.
     * @param cam
     */
    public synchronized void setCamera( Camera cam ){
        this.camera = cam;
    }
    

    

    
    /**
     * Parses the line and returns the commands. Never returns null
     * but may return an empty string ("").
     * @param line
     * @return
     */
    private static String parseCommandFromGET( String line ){
        if( line == null ) return null;
        if( LOGGER.isLoggable(Level.FINEST) ){
            LOGGER.finest( "Parsing request: " + line );
        }   // end if: finest
        System.out.println( "Parsing request: " + line );
        Matcher mat = COMMAND_PATTERN.matcher(line);
        if( mat.matches() ){
            int gc = mat.groupCount();  // Should be two
            assert gc == 2 : gc;        // Assert two
            System.out.println("Got command " + mat.group(1));
            return mat.group(1);
        }   // end if: matches
        return null;
    }
    
    protected void processCommand( String cmd ){
        if( camera == null ) return;
        for( int i = 0; i < cmd.length(); i++ ){
            char c = cmd.charAt(i);
            switch( c ){
                case 'U': case 'u': camera.tiltUp(.5,howLong);      break;
                case 'D': case 'd': camera.tiltDown(.5,howLong);    break;
                case 'L': case 'l': camera.panLeft(.5,howLong);     break;
                case 'R': case 'r': camera.panRight(.5,howLong);    break;
                case 'I': case 'i': camera.zoomIn(.5,howLong);      break;
                case 'O': case 'o': camera.zoomOut(.5,howLong);     break;
                case '_': camera.delay(howLong);                    break;
                default:
                    LOGGER.warning("Unknown command '" + c + "' in sequence '" + cmd + "'" );
                    break;
            }   // end switch
        }   // end for:
    }
    
    
    
    
    /**
     * Looks for ?aw=urlencodedvalues in the URL which is
     * the raw bytes to send to the camera.
     * @param line
     * @return
     */
    private static byte[] parseRawFromGET( String line ){
        if( line == null ) return null;
        if( LOGGER.isLoggable(Level.FINEST) ){
            LOGGER.finest( "Parsing request for raw commands: " + line );
        }   // end if: finest
        Matcher mat = RAW_PATTERN.matcher(line);
        if( mat.matches() ){
            int gc = mat.groupCount();  // Should be two
            assert gc == 2 : gc;        // Assert two
            System.out.println("Got command " + mat.group(1));
            String rawEnc = mat.group(1);
            byte[] bytes = null;
            try{
                String[] pairs = rawEnc.split("-");
                bytes = new byte[pairs.length];
                for( int i = 0; i < pairs.length; i++ ){
                    bytes[i] = (byte)Long.parseLong(pairs[i],16);
                }   // end for: each byte
                return bytes;
            } catch( Exception exc ){
                return null;
            }   // end catch
            
        }   // end if: matches
        return null;
    }
    
    
    private void processRaw( byte[] bytes ){
        camera.sendBytes(bytes,0,bytes.length);
    }
    
    

    public void tcpServerStateChanged(Event evt) {
        // NOTHING TO DO
    }

    public void tcpServerSocketReceived(Event evt) {
        Socket sock = evt.getSocket();
        try{
            BufferedReader in = new BufferedReader( 
              new InputStreamReader( 
                sock.getInputStream()));
            // Example:
            // GET /foo?cmd=RRRRUUUU HTTP/1.0
            String firstLine = in.readLine();               // First line
            String cmd = parseCommandFromGET( firstLine );  // Command    
            if( cmd != null ){
                processCommand( cmd );                          // Process
            }   // end if: cmd=
            
            byte[] raw = parseRawFromGET( firstLine );
            if( raw != null){
                processRaw( raw  );
            }
        } catch( IOException exc ){
            exc.printStackTrace();
        }
        try{
            OutputStream out = sock.getOutputStream();
            out.write( "HTTP/1.1 200 OK\r\n".getBytes() );
            out.write( "Content-type: text/plain\r\n".getBytes() );
            out.write( "Content-length: 0\r\n".getBytes() );
        } catch( IOException exc ){
            exc.printStackTrace();
        }
        try{
            sock.close();
        } catch( IOException exc ){
            exc.printStackTrace();
        }
    }
    
}
