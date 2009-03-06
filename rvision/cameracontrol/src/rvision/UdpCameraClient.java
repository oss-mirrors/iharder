package rvision;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * </p>A subclass of {@link Camera}, the UdpCameraClient sends
 * all of its RVision commands over a UDP datagram to
 * a {@link UdpCameraServer} which hands them off to
 * an actual {@link Camera}. The actual commands are
 * wrapped in a KLV set as described here.</p>
 * 
 * 
 * 
 * @author robert.harder
 */
public class UdpCameraClient extends Camera{

    private final static Logger LOGGER = Logger.getLogger( UdpCameraClient.class.getName());
    
    private InetAddress host;
    private int port;
    
    private DatagramSocket socket;
    private SocketAddress destination;
    
    //KLVSecurity klvSec;
    
    
    
    public static void main( String[] args ){
        args = new String[]{ "128.236.40.164", "1235" };
        if( args.length != 2 ){
            System.out.println("Usage: java " + UdpCameraClient.class.getName() + " host port" );
            System.exit(1);
        } else {
            UdpCameraClient cam = null;
            try{
                cam = new UdpCameraClient(args[0],Integer.parseInt(args[1]));
            } catch( Exception exc ){
                System.err.println("Could not set up camera client: " + exc.getMessage() );
                System.exit(2);
            }   // end catch
            
            cam.panLeft(.5).delay(1000).panRight(1000).delay(1000);
            System.out.println("Done moving camera");
            //System.exit(0);
        }   // end else
    }   // end main
    
    
    
    public void setHostname( String host ){
        try {
            this.host = InetAddress.getByName(host);
            this.destination = new InetSocketAddress( host, port );
        } catch (UnknownHostException ex) {
            Logger.getLogger(UdpCameraClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public synchronized void setPort( int port ){
        this.port = port;
        this.destination = new InetSocketAddress( host, port );
    }
    
   
    
    
    
    
    /**
     * Create a UdpCameraClient object that sends commands to the
     * specified hostname and port via UDP.
     * @param hostname
     * @param port
     * @throws java.io.IOException
     */
    public UdpCameraClient( String hostname, int port ) throws IOException {
        InetAddress host = InetAddress.getByName(hostname);
        this.destination = new InetSocketAddress( host, port );
        this.socket = new DatagramSocket();
    }
    
    @Override
    public void close(){
        this.socket.close();
        super.close();
    }
    
    
    @Override
    protected boolean sendBytes( byte[] data, int offset, int length ){
        KLV klv = new KLV();//0,KLV.KeyLength.SixteenBytes,KLV.LengthEncoding.BER); // Outer wrapper
        klv.setKeyLength(KLV.KeyLength.SixteenBytes);
        klv.setLengthEncoding(KLV.LengthEncoding.FourBytes);
        klv.setKey(UdpCameraServer.UDP_CAMERA_SERVER_GLOBAL_KEY);               // Sixteen byte key
        KLV sub = new KLV(                                                      // Insert raw command
          UdpCameraServer.CAMERA_RAW_COMMAND_KLV_KEY,                           // Key for "raw"
          UdpCameraServer.KEY_LENGTH,                                           // Key length
          UdpCameraServer.LENGTH_ENCODING,                                      // Length encoding
          data, offset, length );                                               // Raw bytes
        klv.addSubKLV(sub);                                                     // Add to outer wrapper
        byte[] klvBytes = klv.toBytes();                                        // Convert to bytes
        
        try {
            DatagramPacket packet = new DatagramPacket(klvBytes, 0, klvBytes.length, destination);
            synchronized( this.socket ){
                this.socket.send(packet);
            }   // end sync
        } catch (Exception ex) {
            Logger.getLogger(UdpCameraClient.class.getName()).log(Level.SEVERE, null, ex);
            this.lastException = ex;
            return false;
        }   // end catch
        return true;
    }
    
    
    
}
