/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rvision;

import java.io.*;

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
        "\tT\tTitle, ex: TFront Room"
    };
    private static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    private static Camera cam;
    private static int howLong = 100;
    
    public static void main(String[] args) throws Exception{
        args = new String[]{"/dev/cu.PL2303-0000103D"};
        if( args.length ==  0 ){
            String[] names = SerialStream.getPortNames();
            System.out.println("Serial ports:");
            for( String n : names ){
                System.out.println("\t" + n );
            }
            System.exit(1);
        }
        cam = new Camera(args[0]);
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
    

}
