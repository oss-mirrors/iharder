package rvision;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.logging.*;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

/**
 * A subclass of {@link Camera}, UdpCameraServer 
 * @author robert.harder
 */
public class UdpCameraServer extends UdpServer implements UdpServer.Listener, PropertyChangeListener {

    private final static Logger LOGGER = Logger.getLogger(UdpCameraServer.class.getName());
    
    /** KLV key lengths are one byte. */
    public final static KLV.KeyLength KEY_LENGTH = KLV.KeyLength.OneByte;
    
    /** KLV length encoding is two bytes. */
    public final static KLV.LengthEncoding LENGTH_ENCODING = KLV.LengthEncoding.TwoBytes;
    
    /**
     * This 16-byte key is the globally-unique, registered KLV key
     * that identifies this KLV set as belonging to this RVision
     * Camera code. Right now, I just made up this key. I need to
     * find out how to request a key.
     */
    public final static byte[] UDP_CAMERA_SERVER_GLOBAL_KEY = new byte[]{
        (byte)0x00, (byte)0x01, (byte)0x02, (byte)0x03, 
        (byte)0x04, (byte)0x05, (byte)0x06, (byte)0x07, 
        (byte)0x08, (byte)0x09, (byte)0x0A, (byte)0x0B, 
        (byte)0x0C, (byte)0x0D, (byte)0x0E, (byte)0x0F
    };
    
    /**
     * This key (1) is the key for the actual camera command to be given
     * to the {@link Camera}. 
     */
    public final static int CAMERA_RAW_COMMAND_KLV_KEY = 1;

    public final static String MDNS_UDP_RVISION_TYPE = "_rvision._udp.local.";
    
    /**
     * <p>This key (0xFF) is the key indicating a KLV set
     * was replaced with a digital signature set which will in turn
     * contain the original KLV set.</p>
     * 
     */
    //public final static int DIGITAL_SIGNATURE_INSIDE_KLV_KEY = 0xFF;
    
        
    private final static String[] USAGE = new String[]{
        "Usage: java -cp rvision.jar rvision.UdpCameraServer options",
        "   -s serialPort   The serial port for connecting to the camera",
        "   -p udpPort      The UDP port on which to listen for commands",
        "   -g group        The multicast group on which to listen",
    //    "   -k publicKey    The public key file against which to verify signed packets",
        "   -l              List available serial ports",
        "   -h              This help message"
    };
    
        
    private final static int PORT_DEFAULT = 8001;
    private boolean             dispose = false;
    private Camera              camera;
    

    private JmDNS jmdns;
    private ServiceInfo jmdnsSI;

    /**
     * Simple command line server that listens on specified port and
     * sends data to camera on specified serial port, or default
     * serial port if not specified
     * @param args
     */
    public static void main( String[] args ){
        //args = new String[]{ "-s", "COM1", "-p", "4000" };
        
        try{
            int port = PORT_DEFAULT;
            String serialPort = null;
            String group = null;
            String keyFile = null;

            if( args.length == 0 ){

                String[] names = SerialStream.getPortNames();
                System.out.println("Serial ports:");
                for( int i = 0; i < names.length; i++ ){
                    System.out.println(String.format("  %2d: " + names[i], (i+1) ) );
                }
                System.out.print( String.format("Select a port (1-%d): ", (names.length)));
                BufferedReader br = new BufferedReader( new InputStreamReader( System.in) );
                String resp = br.readLine();
                int respI = Integer.parseInt(resp);
                String portName = names[respI-1];

                System.out.print("List on UDP port number: ");
                resp = br.readLine();
                int portResp = Integer.parseInt(resp);

                args = new String[]{ "-s", portName, "-p", "" + portResp };

            }

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
                
                // Public Key File
                //else if( "-k".equals( args[i] ) ){
                //    keyFile = args[++i];
                //    System.out.println( "Requested public key file: " + keyFile );
                //}   // end if: -k
                
            }   // end for: each arg
            
            // No serial port?
            if( serialPort == null ){
                System.err.println( "No serial port requested." );
                for( String s : USAGE ){
                    System.out.println(s);
                }   // end for: usage
                System.exit(2);
            }   // end if: serialPort == null
            
            // Make Pololu device
            Camera cam = new Camera( serialPort );
            System.out.println( "Connected to camera on port: " + serialPort );

            // Make server
            UdpCameraServer ucs = null;
            if( group == null ){
                ucs = new UdpCameraServer( cam, port );
                System.out.println( "Receiving commands on UDP port: " + port );
            } else {
                System.out.println( "Joining multicast group: " + group );
                ucs = new UdpCameraServer( cam, port, group );
                System.out.println( "Receiving commands on UDP port: " + port );
            }
            
            // Signatures?
            /*if( keyFile != null ){
                PublicKey key = null;
                try{
                    key = KLVSecurity.readPublicKey( new File( keyFile ) );
                } catch( Exception exc ){
                    System.err.println(
                      "Could not read public key file " + keyFile + ": " + exc.getMessage() );
                }   // end catch
                
                if( key != null ){
                    ucs.addAuthorizedPublicKey(key);
                    ucs.setSignatureRequired(true);
                    System.out.println( "Public key file: " + keyFile );
                }
            }*/   // end keyfile

            // Start
            ucs.start();
            
        } catch( Exception exc ){
            System.err.println( "Could not set up server: " + exc.getMessage() );
            for( String s : USAGE ){
                System.out.println(s);
            }   // end for: usage
            System.exit(1);
        }
        
        
    }   // end main
    
    
    
    /**
     * Attempts to create a new UdpCameraServer listening on the
     * given UDP port and attached to a {@link Camera} on the
     * given serial port.
     * 
     * @param port
     * @param serialPort
     * @throws java.io.IOException
     */
    public UdpCameraServer( Camera cam, int port ) throws IOException{
        this.camera = cam;
        setPort( port );            // In superclass
        addUdpServerListener(this); // In superclass
        addPropertyChangeListener(this);
    }
    
    
    public UdpCameraServer( Camera cam, int port, String group ) throws IOException{
        this.camera = cam;
        setPort( port );            // In superclass
        //setGroup( group );          // In superclass
        addUdpServerListener(this); // In superclass
        addPropertyChangeListener(this);
    }
    
    
    /**
     * Sets the {@link Camera} used by the server to give the
     * commands that are received remotely.
     * 
     * @param cam
     */
    public void setCamera( Camera cam ){
        this.camera = cam;
    }
    
    
    /**
     * Returns the {@link Camera} used by the server.
     * @return
     */
    public Camera getCamera(){
        return this.camera;
    }
    
    

    
    
    /**
     * Processes a KLV request from a {@link UdpCameraClient}.
     * @param req the request from over UDP
     */
    private synchronized void processRequest(KLV req) {
        // Verify against global key
        if( !req.isFullKey( UDP_CAMERA_SERVER_GLOBAL_KEY ) ) {
            LOGGER.warning("Received KLV set with wrong global key: " + req );
            return;
        } else {
            LOGGER.info( "Received KLV set for UDP Camera Server: " + req );
        }
        
        List<KLV> subs = req.getSubKLVList(KEY_LENGTH, LENGTH_ENCODING);
        
        // Process commands
        for( KLV k : subs ){
            switch( k.getShortKey() ){
                case CAMERA_RAW_COMMAND_KLV_KEY:
                    byte[] rawCommand = k.getValue();
                    Camera cam = this.camera;
                    if( cam != null ){
                        cam.sendBytes(rawCommand,0,rawCommand.length);
                    }   // end if: cam != null
                    break;
                default:
                    LOGGER.warning( 
                      "Unknown KLV key 0x" + 
                      Long.toHexString(k.getShortKey() & 0xFFFFFFFF) + 
                      " inside Udp Camera Server KLV set." );
            }   // end switch
        }   // end for: each sub
    }

    
    /**
     * Nothing done in response to this event.
     * @param evt
     */
    public void udpServerStateChanged(UdpServer.Event evt) {
        /*switch( evt.getState() ){
            case STARTED:
                if( this.jmdns == null ){
                    try {
                        this.jmdns = JmDNS.create();
                    } catch (IOException ex) {
                        LOGGER.log(Level.WARNING, null, ex);
                    }
                }   // end if: not yet created
                if( this.jmdns != null ){
                    this.jmdnsSI = ServiceInfo.create(MDNS_UDP_RVISION_TYPE, "rvision", getPort(), "RVision Camera Controller" );
                    try {
                        this.jmdns.registerService(this.jmdnsSI);
                    } catch (IOException ex) {
                        LOGGER.log(Level.WARNING, null, ex);
                    }
                        }
                break;

            case STOPPED:
                if( this.jmdns != null ){
                    this.jmdns.unregisterService( this.jmdnsSI );
                }
                this.jmdns = null;
                this.jmdnsSI = null;
                break;
                
            case STARTING:
            case STOPPING:
            default:
                break;
        }   // end switch
        */
    }

    /**
     * Process incoming packets.
     * @param evt
     */
    public void packetReceived(UdpServer.Event evt) {
        
        DatagramPacket udpPacket = evt.getPacket();
        if( udpPacket == null ){
            return;
        }
        
        KLV klv = null;
        try{ 
            klv = new KLV(                              // Extract KLV
              udpPacket.getData(), udpPacket.getOffset(),     // Data
              KLV.KeyLength.SixteenBytes,
              KLV.LengthEncoding.FourBytes );           // Outer wrapper is 16x4
        } catch( Exception exc ){
            LOGGER.warning("Could not make KLV set from packet: " + exc.getMessage() );
        }
        if( klv != null ){                          // Got something?
            processRequest( klv );                  // Process it
        }
                        
    }

    public void propertyChange(PropertyChangeEvent evt) {
        Object src = evt.getSource();
        String prop = evt.getPropertyName();
        Object oldVal = evt.getOldValue();
        Object newVal = evt.getNewValue();

        if( src == this && UdpServer.STATE_PROP.equals( prop ) && newVal instanceof UdpServer.State ){

            switch( (UdpServer.State)newVal ){
                case STARTED:
                    if( this.jmdns == null ){
                        try {
                            this.jmdns = JmDNS.create();
                        } catch (IOException ex) {
                            LOGGER.log(Level.WARNING, null, ex);
                        }
                    }   // end if: not yet created
                    if( this.jmdns != null ){
                        this.jmdnsSI = ServiceInfo.create(
                                MDNS_UDP_RVISION_TYPE,
                                "rvision",
                                getPort(), 
                                "RVision Camera Controller" );
                        try {
                            this.jmdns.registerService(this.jmdnsSI);
                        } catch (IOException ex) {
                            LOGGER.log(Level.WARNING, null, ex);
                        }
                            }
                    break;

                case STOPPED:
                    if( this.jmdns != null ){
                        this.jmdns.unregisterService( this.jmdnsSI );
                    }
                    this.jmdns = null;
                    this.jmdnsSI = null;
                    break;

                case STARTING:
                case STOPPING:
                default:
                    break;
            }   // end switch

        }   // end if: state changed


    }   //end property change
    
    
    
    
}
