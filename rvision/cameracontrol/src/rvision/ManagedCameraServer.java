package rvision;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import rvision.TcpServer.Event;

/**
 * <p>Listens on a UDP port and a TCP port for commands for manipulating
 * an RVision camera. The command set is simple and uses the
 * {@link ManagedCamera} class so that pan/tilt movements can be
 * coordinated with the current zoom level.</p>
 * 
 * <p>The command set is the same for UDP and TCP. In the case of TCP,
 * it is expecting an HTTP GET request where the commands are URL parameters.</p>
 * 
 * <p>The commands are <tt>pan</tt>, <tt>tilt</tt>, and <tt>zoom</tt>.
 * If sending UDP messages,
 * you might send a datagram with the content <tt>pan=0.25</tt> which would mean
 * "pan the camera left and center on the point half way between the edge of 
 * the screen and the center."</p>
 * 
 * <p>The <tt>pan</tt> and <tt>tilt</tt> commands are coordinated with the current
 * zoom level such that zero refers to "center the camera on the left or bottom
 * edge of the screen" (pan or tilt), and one refers to "center the camera on the
 * right or top of the screen." That means a value of 0.5 is essentially a
 * "no move" command. Values outside that range are capped at zero and one,
 * and invalid values are ignored.</p>
 * 
 * <p>The <tt>zoom</tt> command can either take values from zero to one, which
 * would set the absolute zoom level with zero being zoomed out and one being
 * zoomed in, or a series of +'s and -'s to zoom in (+) or zoom out (-) in 
 * increments relative to the current position adding or subtracting 0.1 to the
 * absolute zoom value for each + or -.</p>
 * 
 * @author robert.harder
 */
public class ManagedCameraServer implements TcpServer.Listener, UdpServer.Listener {
    
    private final static Logger LOGGER = Logger.getLogger( ManagedCameraServer.class.getName() );
    
    private final static int PORT_DEFAULT = 8000;
    
    private final static byte[] OK_BYTES = "HTTP/1.1 200 OK\r\n".getBytes();
    
    /**
     * Group 1 extracts the pan value, such as 0.25 in pan=0.25.
     */
    private final static Pattern PAN_PATTERN = Pattern.compile( "^.*\\bpan=((\\d*\\.?\\d*)).*");
    
    /**
     * Group 1 extracts the tilt value, such as 0.25 in tilt=0.25.
     */
    private final static Pattern TILT_PATTERN = Pattern.compile( "^.*\\btilt=((\\d*\\.?\\d*)).*");
    
    /**
     * Group 1 extracts an absolute zoom value, such as 0.25 in zoom=0.25.
     */
    private final static Pattern ZOOM_ABS_PATTERN = Pattern.compile( "^.*\\bzoom=((\\d*\\.?\\d*)).*");
    
    /**
     * Group 1 extracts a zoom in value, such as +++ in zoom=+++.
     */
    private final static Pattern ZOOM_IN_PATTERN = Pattern.compile( "^.*\\b?zoom=((\\++)).*?");
    
    /**
     * Group 1 extracts a zoom out value, such as --- in zoom=---.
     */
    private final static Pattern ZOOM_OUT_PATTERN = Pattern.compile( "^.*\\b?zoom=((-+)).*?");
    
    
    private UdpServer udpServer;
    private TcpServer tcpServer;
    private ManagedCamera camera;
    
    
    
    
    public ManagedCameraServer(){
        initComponents();
    }
    
    private void initComponents(){
        udpServer = new UdpServer();
        tcpServer = new TcpServer();
        
        udpServer.addUdpServerListener(this);
        tcpServer.addTcpServerListener(this);
    }
    
    
    public ManagedCamera getManagedCamera(){
        return this.camera;
    }
    
    public void setManagedCamera( ManagedCamera cam ){
        this.camera = cam;
    }
    
    
    public void setPort( int port ){
        this.udpServer.setPort( port );
        this.tcpServer.setPort( port );
    }
    
    
    public void setMulticastGroup( String group ){
        this.udpServer.setGroup(group);
    }


    /**
     * Starts the server.
     */
    public synchronized void start(){
        this.udpServer.start();
        this.tcpServer.start();
    }
    
    
    /**
     * Stops the server.
     */
    public synchronized void stop(){
        this.udpServer.stop();
        this.tcpServer.stop();
    }
    
    
    
    
    public void tcpServerStateChanged(Event evt) {
        switch( evt.getState() ){
            case STOPPED:
                stop();
                break;
        }   // end switch
    }

    public void tcpServerSocketReceived(Event evt) {
        try{
            InputStream in = null;
            try{
                in = evt.getSocket().getInputStream();
            } catch( IOException exc ){
                LOGGER.warning( "Could not get input stream from socket: " + exc.getMessage() );
                return;
            }   // end catch
            BufferedReader reader = new BufferedReader( new InputStreamReader(in) );
            String line = null;
            try{
                line = reader.readLine(); // BLOCKS
            } catch( IOException exc ){
                LOGGER.warning( "Error reading a line from stream: " + exc.getMessage() );
                return;
            }   // end catch
            if( line != null ){
                processLine( line );
            }   // end if: not null
        } catch( Exception exc ){
            LOGGER.warning( "Error reading socket: " + exc.getMessage() );
        } finally{
            try{
                evt.getSocket().getOutputStream().write(OK_BYTES);
            } catch( Exception exc2 ){
                LOGGER.warning("Error responding to stream: " + exc2.getMessage() );
            }
            try{ evt.getSocket().close(); }
            catch( Exception exc2 ){
                LOGGER.warning( "Error closing socket: " + exc2.getMessage() );
            }   // end catch
        }   // end finally
    }

    
    public void udpServerStateChanged(rvision.UdpServer.Event evt) {
        switch( evt.getState() ){
            case STOPPED:
                stop();
                break;
        }   // end switch
    }

    
    public void udpServerPacketReceived(rvision.UdpServer.Event evt) {
        String msg = evt.getPacketAsString();
        if( msg == null ){
            if( LOGGER.isLoggable(Level.FINE) ){
                LOGGER.fine( "No data could be retrieved from packet " + evt.getPacket() );
            }
            return;
        }   // if null
        processLine( msg );
    }
    
    
    /**
     * Processes a line that has commands embedded in it. Uses regular
     * expressions to match pan=0.25 etc.
     * @param line
     */
    protected void processLine( String line ){
        if( LOGGER.isLoggable( Level.FINEST ) ){
            LOGGER.finest( "Processing " + line );
        }   // end if: finest
        
        if( line == null ){
            throw new NullPointerException("Cannot process null line.");
        }   // end if: null
        
        ManagedCamera cam = getManagedCamera();
        if( cam == null ){
            if( LOGGER.isLoggable( Level.FINE ) ){
                LOGGER.fine( "Dropping commands because no camera is set: " + line );
            }   // end if: fine
            return;
        }   // end if: no camera
//...TODO: NOT PROPERLY PARSING MULTIPLE COMMANDS        
        Double pan     = extractDouble( PAN_PATTERN, line );
        Double tilt    = extractDouble( TILT_PATTERN, line );
        Double zoomAbs = extractDouble( ZOOM_ABS_PATTERN, line );
        String  zoomIn  = extractString( ZOOM_IN_PATTERN, line );
        String  zoomOut = extractString( ZOOM_OUT_PATTERN, line );
        
        // Since pan/tilt commands work together, check them together
        if( pan != null || tilt != null ){
            double p = pan  == null ? 0.5 : pan;
            double t = tilt == null ? 0.5 : tilt;
            if( LOGGER.isLoggable(Level.FINEST) ){
                LOGGER.finest( String.format("pan=%f, tilt=%f",p,t) );
            }   // end if finest
            cam.panTilt(p,t);
        }   // end if: pan/tilt
        
        // Zoom
        if( zoomAbs != null ){
            if( LOGGER.isLoggable(Level.FINEST) ){
                LOGGER.finest( "zoom=" + zoomAbs );
            }   // end if finest
            cam.setZoom(zoomAbs);
        }   // end if: zoom absolute
        
        // Also zoom in?
        if( zoomIn != null ){
            int num = zoomIn.length();  // Number of +'s
            double zoom = cam.getZoom();
            for( int i = 0; i < num && zoom < 1; i++ ){
                zoom += 0.1;
            }   // end for: zoom in
            if( zoom > 1 ) zoom = 1;
            if( LOGGER.isLoggable(Level.FINEST) ){
                LOGGER.finest( "Zooming in relatively to " + zoom );
            }   // end if finest
            cam.setZoom(zoom);
        }   // end if: zoom in
        
        
        // Also zoom out?
        if( zoomOut != null ){
            int num = zoomOut.length();  // Number of -'s
            double zoom = cam.getZoom();
            for( int i = 0; i < num && zoom > 0; i++ ){
                zoom -= 0.1;
            }   // end for: zoom in
            if( zoom < 0 ) zoom = 0;
            if( LOGGER.isLoggable(Level.FINEST) ){
                LOGGER.finest( "Zooming out relatively to " + zoom );
            }   // end if finest
            cam.setZoom(zoom);
        }   // end if: zoom out
        
    }
    
    
    
    private static String extractString( Pattern pat, String line ){
        Matcher mat = pat.matcher(line);
        if( mat.matches() && mat.groupCount() >= 2 ){
            return mat.group(1);
        } else {
            return null;
        }
    }
    
    
    /**
     * Attempts to pull an integer from the command such as pan=0.25
     * or returns null if unsuccessful. Also caps all values between zero and one.
     * @param pat
     * @param line
     * @return
     */
    private static Double extractDouble( Pattern pat, String line ){
        String sval = extractString( pat, line );
        try{
            double val = Double.parseDouble( sval);
            if( val < 0 ) val = 0;
            if( val > 1 ) val = 1;
            return val;
        } catch( Exception exc ){
            //LOGGER.warning( "Could not extract number from " + line );
            return null;
        } 
    }
    
    
    
/* ********   E X A M P L E   C O M M A N D   L I N E   S E R V E R  ******** */
    
    
    private final static String[] USAGE = new String[]{
        "Usage: java -cp rvision.jar rvision.ManagedCameraServer options",
        "   -s serialPort   The serial port for connecting to the camera",
        "   -p udpPort      The UDP port on which to listen for commands (default " + PORT_DEFAULT + ")",
        "   -g group        The multicast group on which to listen",
        "   -l              List available serial ports",
        "   -h              This help message"
    };
    
    
    /**
     * Simple command line server that listens on specified port and
     * sends data to camera on specified serial port, or default
     * serial port if not specified
     * @param args
     */
    public static void main( String[] args ){
        args = new String[]{ "-s", "COM6", "-p", "8001" };
        
        try{
            int port = PORT_DEFAULT;
            String serialPort = null;
            String group = null;
            
            if( args.length <= 1 ){
                
                if( "-h".equals(args[0]) ){
                    for( String s : USAGE ){
                        System.out.println(s);
                    }   // end for: usage
                    System.exit(0);
                }   // end if: help
                
                else if( "-l".equals( args[0] ) ){
                    String[] names = SerialStream.getPortNames();
                    System.out.println("Serial ports: " );
                    if( names.length == 0 ){
                        System.out.println( "\tNone found." );
                    } else {
                        for( String s : names ){
                            System.out.println("\t" + s );
                        }   // end for: each port
                    }   // end else
                    System.exit(0);
                }   // end if: list serial ports
            }   // end if: show usage
            
            // Parse command line
            for( int i = 0; i < args.length-1; i++ ){
                
                // Serial Port
                if( "-s".equals( args[i] ) ){
                    serialPort = args[++i];
                    System.out.println( "Requested serial port: " + serialPort );
                }   // end if: -s
                
                // UDP Port
                else if( "-p".equals( args[i] ) ){
                    try{
                        port = Integer.parseInt(args[++i]);
                        System.out.println( "Requested UDP port: " + port );
                    } catch( Exception exc ){
                        System.err.println( "Could not determine a port number from " + args[i] );
                        System.exit(1);
                    }   // end catch
                }   // end if: -p
                
                // Multicast group
                else if( "-g".equals( args[i] ) ){
                    group = args[++i];
                    System.out.println( "Requested multicast group: " + group );
                }   // end if: -g
                
                
            }   // end for: each arg
            
            // No serial port?
            if( serialPort == null ){
                System.err.println( "No serial port requested." );
                for( String s : USAGE ){
                    System.out.println(s);
                }   // end for: usage
                System.exit(2);
            }   // end if: serialPort == null
            
            // Set up Managed Camera
            Camera cam = new Camera( serialPort );
            ManagedCamera mc = new ManagedCamera(cam);
            System.out.println( "Connected to camera on serial port: " + serialPort );
            

            // Make server
            ManagedCameraServer cs = new ManagedCameraServer();
            cs.setManagedCamera(mc);
            cs.setPort( port );
            System.out.println( "Receiving commands on UDP port: " + port );
            if( group != null ){
                System.out.println( "Joining multicast group: " + group );
                cs.setMulticastGroup( group );
            }
            
            cs.start();
            
        } catch( Exception exc ){
            System.err.println( "Could not set up server: " + exc.getMessage() );
            for( String s : USAGE ){
                System.out.println(s);
            }   // end for: usage
            System.exit(1);
        }
        
        
    }   // end main
    
    

}
