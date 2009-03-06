package rvision;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * </p>A subclass of {@link Camera}, the HttpCameraClient sends
 * all of its RVision commands over an HTTP connection
 * an {@link HttpCameraServer} which hands them off to
 * an actual {@link Camera}. </p>
 * 
 * 
 * 
 * @author robert.harder
 */
public class HttpCameraClient extends Camera{

    private final static Logger LOGGER = Logger.getLogger( HttpCameraClient.class.getName());
    
    //private InetAddress host;
    //private int port;
    
    //private DatagramSocket socket;
    //private SocketAddress destination;
    private URL endpoint = null;   // The URL up to the question mark, ex:
                                   // http://localhost:8000/camera
    
    //KLVSecurity klvSec;
    
    
    
    public static void main( String[] args ){
        if( args.length < 1 ){
            System.out.println("Usage: java " + HttpCameraClient.class.getName() + " endpoint"  );
            System.out.println("   ex: java " + HttpCameraClient.class.getName() + " http://localhost:8080/"  );
            System.exit(1);
        } else {
            HttpCameraClient cam = null;
            try{
                cam = new HttpCameraClient(args[0]);
            } catch( Exception exc ){
                System.err.println("Could not set up camera client: " + exc.getMessage() );
                System.exit(2);
            }   // end catch
            
            cam.panLeft(.5).delay(1000).panRight(1000).delay(1000);
            System.out.println("Done moving camera");
            //System.exit(0);
        }   // end else
    }   // end main
    
    
    
    public synchronized void setEndpoint( URL end ){
        this.endpoint = end;
    }
    
    
    public synchronized URL getEndpoint(){
        return this.endpoint;
    }
   
    
    
    
    public HttpCameraClient(){
        super();
    }
    
    
    /**
     * Create a HttpCameraClient object that sends commands to the
     * specified endpoint
     * @param endpoint
     * @throws java.io.IOException
     */
    public HttpCameraClient(String endpoint) {
        super();
        try {
            this.endpoint = new URL(endpoint);
        } catch (MalformedURLException ex) {
            Logger.getLogger(HttpCameraClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
    @Override
    protected boolean sendBytes( byte[] data, int offset, int length ){
        try {
            String hex = toHex(data,offset,length);
            
            // send via http
            URL url = new URL(this.endpoint,"?raw=" + hex);
            System.out.println("Connecting to " + url );
            URLConnection conn = url.openConnection();
            conn.connect();
            Object content = conn.getContent();
               
            return true;
        } catch (IOException ex) {
            //Logger.getLogger(HttpCameraClient.class.getName()).log(Level.SEVERE, null, ex);
            LOGGER.warning(ex.getMessage());
            return false;
        }
    }
    
    private static String toHex(byte[] data, int offset, int length ){
        StringBuilder sb = new StringBuilder();
        for( int i = 0; i < length; i++ ){
            sb.append(Long.toHexString(data[offset+i]&0xFF));
            if( i < length-1 ){
                sb.append("-");
            }
        }
        return sb.toString();
    }
    
}
