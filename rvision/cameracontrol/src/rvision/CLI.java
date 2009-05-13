
package rvision;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author robert.harder
 */
public class CLI {

    private static String[] USAGE = {
        "Camera commands",
        "\tU\tTilt up",
        "\tD\tTilt down",
        "\tL\tPan left",
        "\tR\tPan right",
        "\tI\tZoom in",
        "\tO\tZoom out",
        "\tZ\tZoom to this amount (0..100), mostly wide ex: Z10",
        "\tT\tTitle, ex: TFront Room",
        "\t[..]Extended commands:",
        "\t\tudp=port|off\t Turns on/off a UDP server to receive commands on 'port'",
        "\t\ttitle=The Title\t Sets the on-video title"
    };
    private final static Pattern THIS_EQUALS_THAT = Pattern.compile("(.+)=(.*)");
    private static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    private static Camera cam;
    private static UdpCameraServer udp;
    private static int howLong = 50;
    
    public static void main(String[] args) throws Exception{

        if( args.length == 3 ){
            if( args[0].contains("-u") ){
                String host = args[1];
                int port = Integer.parseInt(args[2]);
                cam = new UdpCameraClient( host, port );
            }
        }

        //args = new String[]{"/dev/cu.PL2303-0000103D"};
        if( args.length ==  0 ){
            String[] names = SerialStream.getPortNames();
            System.out.println("Serial ports:");
            System.out.println(String.format("  %2d: " + "Connect to UDP camera server", 0 ) );
            for( int i = 0; i < names.length; i++ ){
                System.out.println(String.format("  %2d: " + names[i], (i+1) ) );
            }
            System.out.print( String.format("Select a port (1-%d): ", (names.length)));
            BufferedReader br = new BufferedReader( new InputStreamReader( System.in) );
            String resp = br.readLine();
            int respI = Integer.parseInt(resp);

            if( respI == 0 ){
                System.out.println("Connect to host and port (default: localhost 4000): ");
                String in = br.readLine();
                in = in.equals("") ? "localhost 4000" : in;
                String[] hp = in.split(" ");
                cam = new UdpCameraClient( hp[0], Integer.parseInt(hp[1]));
            } else {
                args = new String[]{ names[respI-1] };
                cam = new Camera(args[0]);
            }
        }

        if( cam == null ){
            System.err.println("No camera is set up." );
            System.exit(1);
        }
        //cam = new UdpCameraClient( "localhost", 8001 );
        //System.out.println("Serial port: " + cam.serialPort);
        while( true ){
            System.out.print("Camera command: " );
            String cmd = in.readLine();
            process( cmd );
        }
    }


    private static void process(String in) throws Exception{
        System.out.println("Input [" + in + "]");
        if( in == null || in.length() == 0 ){
            printUsage();
            return;
        }
        for( int i = 0; i < in.length(); i++ ){
            char c = in.charAt(i);
            switch( c ){
                case 'U': case 'u': cam.tiltUp(.5,howLong);     break;
                case 'D': case 'd': cam.tiltDown(.5,howLong);   break;
                case 'L': case 'l': cam.panLeft(.5,howLong);    break;
                case 'R': case 'r': cam.panRight(.5,howLong);   break;
                case 'I': case 'i': cam.zoomIn(.5, howLong);    break;
                case 'O': case 'o': cam.zoomOut(.5, howLong);   break;
                case 'Z': case 'z':
                    if( i < in.length() -1 ){
                        int amt = -1;
                        try{ amt = Integer.parseInt( in.substring(i+1) ); }
                        catch( Exception exc ){}
                        if( amt < 0 || amt > 100 ){
                            System.err.println("Invalid zoom command. Should be 0..100: " + in );
                        } else {
                            cam.setZoom(amt * 0.01);
                        }
                    }   // end if: text follows
                    i = in.length();
                    break;
                case 'T': case 't':
                    if( i < in.length() -1 ){
                        cam.setTitle( in.substring(i+1) );
                        cam.setShowTitle(true);
                    } else {
                        cam.setShowTitle(false);
                    }
                    i = in.length();
                    break;
                case '.': cam.delay(howLong);                   break;
                case '[':
                    int end = in.indexOf("]",i);
                    String extended = in.substring(i+1, end);
                    Matcher m = THIS_EQUALS_THAT.matcher(extended);
                    if( m.matches() ){
                        String key = m.group(1);
                        String val = m.group(2);
                        if( key.equalsIgnoreCase("udp") ){
                            if( val.equalsIgnoreCase("off") ){
                                stopUdp();
                            } else {
                                try{
                                    startUdp( Integer.parseInt(val) );
                                } catch( Exception exc ){
                                    System.err.println(exc.getMessage());
                                }
                            }
                        }   // end if: udp

                        else if( key.equalsIgnoreCase("title") ){
                            if( val.equals("") ){
                                cam.setShowTitle(false);
                            } else {
                                cam.setTitle(val);
                                cam.setShowTitle(true);
                            }
                        }
                    }   // end if: key=val
                    i = end;
                    break;
                case 'Q': case 'q': System.exit(0);             break;
                default: printUsage();                          break;
            }   // end switch
        }   // end for:
    }
    
    
    private static void printUsage() {
        for( String s : USAGE ){
            System.out.println(s);
        }
    }


    private static synchronized void startUdp(int port){
        if( udp != null ){
            udp.setPort(port);
        } else {
            try {
                udp = new UdpCameraServer(cam, port);
                udp.start();
            } catch (IOException ex) {
                Logger.getLogger(CLI.class.getName()).log(Level.SEVERE, null, ex);
                udp = null;
            }
        }
    }

    private static synchronized void stopUdp(){
        udp.stop();
        udp = null;
    }

    

}
