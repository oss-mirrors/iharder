package rvision;

import java.io.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Primary control point for manipulating the RVision Camera.
 * 
 * @author robert.harder
 */
public class Camera {

    public static void main( String[] args ) throws Exception {
        //for( String s : SerialStream.getPortNames() ){
        //    System.out.println(s);
        //}//System.exit(0);
        Camera c = new Camera( "COM12" );
        Thread.sleep(1000);
        
        //c.panRight(1).delay(250).panTiltStop();
        c.panTiltRelative(90,1, 15,1);
        //c.zoomOutStart();
        //c.tiltDown(1).delay(3000);//.tiltUp(.5, 1000);
        /*
        c.zoomInStart().delay(1000);
        c.panLeft(1).delay(1000).panRight(.5).delay(1000).panTiltStop();
        
        c.zoomOutStart().delay(1000);
        c.tiltUp(.25).delay(1000).tiltDown(.5).delay(1000);
        */
        c.delay(3000); 
        System.exit(0);
    }
    
/* ********  P U B L I C   S T A T I C  ******** */
    
    public final static String SELECT_SONY      = "Select_CAM_Sony";
    public final static String POWER_ON         = "CAM_Power_On";
    public final static String POWER_OFF        = "CAM_Power_Off";    
    
    public final static String TITLE_SET_POSITION  = "CAM_Title_Set_1";  
    public final static String TITLE_SET_LINE_1    = "CAM_Title_Set_2";  
    public final static String TITLE_SET_LINE_2    = "CAM_Title_Set_3";  
    public final static String TITLE_ON            = "CAM_Display_Control_On";
    public final static String TITLE_OFF           = "CAM_Display_Control_Off";
    
    public final static String TITLE_WHITE          ="CAM_Title_White";
    public final static String TITLE_YELLOW         ="CAM_Title_Yellow";
    public final static String TITLE_VIOLET         ="CAM_Title_Violet";
    public final static String TITLE_RED            ="CAM_Title_Red";
    public final static String TITLE_CYAN           ="CAM_Title_Cyan";
    public final static String TITLE_GREEN          ="CAM_Title_Green";
    public final static String TITLE_BLUE           ="CAM_Title_Blue";
    
    public final static String STABILIZER_ON    = "CAM_Stabilizer_On";
    public final static String STABILIZER_OFF   = "CAM_Stabilizer_Off";    
    
    public final static String ZOOM_STOP        = "CAM_Zoom_Stop";    
    public final static String ZOOM_IN          = "CAM_Zoom_Tele_Standard";
    public final static String ZOOM_OUT         = "CAM_Zoom_Wide_Standard";
    public final static String ZOOM_IN_VARIABLE = "CAM_Zoom_Tele_Variable";
    public final static String ZOOM_OUT_VARIABLE= "CAM_Zoom_Wide_Variable";
    public final static String ZOOM_DIRECT      = "CAM_Zoom_Direct";
    public final static String DIGITAL_ZOOM_ON  = "CAM_Digital_Zoom_On";
    public final static String DIGITAL_ZOOM_OFF = "CAM_Digital_Zoom_Off";
    
    public final static String TILT_UP          = "PTD_Up";
    public final static String TILT_DOWN        = "PTD_Down";
    public final static String PAN_LEFT         = "PTD_Left";
    public final static String PAN_RIGHT        = "PTD_Right";
    
    public final static String PAN_TILT_LEFT_UP     = "PTD_UpLeft";
    public final static String PAN_TILT_RIGHT_UP    = "PTD_UpRight";
    public final static String PAN_TILT_LEFT_DOWN   = "PTD_DownLeft";
    public final static String PAN_TILT_RIGHT_DOWN  = "PTD_DownRight";
    public final static String PAN_TILT_RELATIVE    = "PTD_Relative";
    
    public final static String PAN_TILT_STOP    = "PTD_Stop";
    
    
    
    
    /**
     * Camera type - only Sony supported for now (not IR).
     */
    public static enum CameraType { SONY };
    
    
/* ********  S T A T I C  ******** */    
    
    private static Logger LOGGER = Logger.getLogger(Camera.class.getName());
    
    private static Properties COMMANDS = new Properties();
    private static Properties ALPHABET = new Properties();
    static {
        try{
            COMMANDS.load(Camera.class.getResourceAsStream("commands.properties"));
        } catch( Exception exc ){
            LOGGER.warning("Could not load commands.properties file. Functionality will be severely limited. " + exc.getMessage());
        }   // end catch
        try{
            ALPHABET.load(Camera.class.getResourceAsStream("alphabet.properties"));
        } catch( Exception exc ){
            LOGGER.warning("Could not load alphabet.properties file. Titling will not work properly. " + exc.getMessage());
        }   // end catch
        System.out.println( "Commands: " + COMMANDS );
        System.out.println( "Alphabet: " + ALPHABET );
    }   // end static
    
    
    
    protected Exception         lastException;
    protected java.util.Timer   timer = new java.util.Timer("RVision Camera Timer",true);
    
    private InputStream         inFromCamera;   // Input stream from camera
    private OutputStream        outToCamera;    // Output stream to camera
    private CameraType          type = CameraType.SONY;
    private java.util.TimerTask panTiltTask;
    private java.util.TimerTask zoomTask;
    
    protected String serialPort;    //TEMP
    
    
    protected Camera(){}
    
    
    /**
     * Creates a Camera object and tries to connect to the
     * named serial port using the gnu.io RX/TX classes
     * (via {@link SerialStream}).
     * @param serialPort
     * @throws java.io.IOException
     */
    public Camera( String serialPort ) throws IOException{
        if( serialPort == null ){
            String[] names = SerialStream.getPortNames();
            if( names.length == 0 ){
                throw new IOException( "No serial ports found." );
            } else {
                serialPort = names[names.length-1];
            }
        }   // end if: no port provided
            
        SerialStream stream = new SerialStream( serialPort, 9600 );
        this.serialPort = serialPort;
        LOGGER.info("Connected to serial port " + serialPort );
        this.inFromCamera = stream.getInputStream();
        this.outToCamera  = stream.getOutputStream();
        
        /*switch( this.type ){
            case SONY:
                sendHex("81 01 06 23 02 FF");
                break;
            default:
                assert false : this.type;
        }   // end switch */
    }   // end camera
    
    
    
    /**
     * Closes the serial port associated with this camera.
     * This object should not be used again.
     */
    public void close(){
        try{ this.inFromCamera.close(); }
        catch( Exception exc ){
            LOGGER.warning( "Error while closing camera input stream: " + exc.getMessage() );
        }   // end catch
        try{ this.outToCamera.close(); }
        catch( Exception exc ){
            LOGGER.warning( "Error while closing camera output stream: " + exc.getMessage() );
        }   // end catch
    }
    
    
    
    /**
     * Turns the camera on (true) or off (false).
     * @param on
     * @return the camera object for chaining commands.
     */
    public synchronized Camera setPower( boolean on ){
        if( on ){
            sendCommand( POWER_ON );
        } else {
            sendCommand( POWER_OFF );
        }   // end else
        
        return this;
    }   // end set Power
    
    
    
    
    /**
     * Turns the camera stabilizer on (true) or off (false).
     * @param on
     * @return the camera object for chaining commands.
     */
    public synchronized Camera setStabilizer( boolean on ){
        if( on ){
            sendCommand( STABILIZER_ON );
        } else {
            sendCommand( STABILIZER_OFF );
        }   // end else
        
        return this;
    }   // end setStabilizer
    
    
/* ********  T I T L E  ******** */
    
    /**
     * Sets the title (up to 20 characters) 
     * that can be displayed on screen.
     * @param title
     * @return
     */
    public synchronized Camera setTitle( String title ){
        LOGGER.info("Setting title: " + title);
        // Breaking the title up into 10-character chunks,
        // set the title with the two setTitle1 and setTitle2 methods.
        if( title == null ){
            this.setTitle1("").setTitle2("");
        } else if( title.length() <= 10 ){
            this.setTitle1(title).setTitle2("");
        } else {
            this.setTitle1(title).setTitle2(title.substring(10));
        }
        
        return this;
    }
    
    
    /**
     * Sets line one of the title.
     * @param line
     * @return
     */
    private synchronized Camera setTitle1( String line ){
        String rawHex = getHex( TITLE_SET_LINE_1 );     // From commands file
        if( rawHex == null ) return this;               // No command loaded?
        
        String[] orig = new String[]{ 
          "mm", "nn", "pp", "qq", "rr", "ss", "tt", "uu", "vv", "ww" };
        
        String commandHex = rawHex;
        for( int i = 0; i < orig.length; i++ ){
            commandHex = commandHex.replace( 
              orig[i],
              //Long.toHexString(i+35));
              i < line.length()
                ? getAlphabetHex( line.charAt(i) )
                : "1B" );   // Hex for 'space'
        }   // end for: replace each hex set
        sendHex( commandHex );                          // Send command
        
        return this;
    }
    
    
    /**
     * Sets line two of the title.
     * @param line
     * @return
     */
    private synchronized Camera setTitle2( String line ){
        String rawHex = getHex( TITLE_SET_LINE_2 );     // From commands file
        if( rawHex == null ) return this;               // No command loaded?
        
        String[] orig = new String[]{ 
          "mm", "nn", "pp", "qq", "rr", "ss", "tt", "uu", "vv", "ww" };
        
        String commandHex = rawHex;
        for( int i = 0; i < orig.length; i++ ){
            commandHex = commandHex.replace( 
              orig[i],
              //Long.toHexString(i+35));
              i < line.length()
                ? getAlphabetHex( line.charAt(i) )
                : "1B" );   // Hex for 'space'
        }   // end for: replace each hex set
        sendHex( commandHex );                          // Send command
        
        return this;
    }
    
    
    /**
     * Sets whether or not to overlay the title on the video image.
     * @param display
     * @return
     */
    public synchronized Camera setShowTitle( boolean display ){
        if( display ){
            sendCommand( TITLE_ON );
        } else {
            sendCommand( TITLE_OFF );
        }   // end else
        
        return this;
    }
    
    
    
    
    
    
    
    
/* ********  Z O O M  ******** */    
    
    
    
    
    
    /**
     * Turns the camera's digital zoom capability on or off.
     * @param on
     * @return the camera object for chaining commands.
     */
    public synchronized Camera setDigitalZoom( boolean on ){
        if( on ){
            sendCommand( DIGITAL_ZOOM_ON );
        } else {
            sendCommand( DIGITAL_ZOOM_OFF );
        }   // end else
        
        return this;
    }   // end 
    
    
    
    
    /**
     * Stops the zoom operations on the camera.
     * @return the Camera object, to aid in chaining commands
     */
    public synchronized Camera zoomStop(){
        sendCommand(ZOOM_STOP);
        return this;
    }
    
    /**
     * Starts the camera zooming in at a standard rate.
     * @return the Camera object, to aid in chaining commands
     */
    public synchronized Camera zoomIn(){
        sendCommand(ZOOM_IN);
        return this;
    }
    
    /**
     * Starts the camera zooming out at a standard rate.
     * @return the Camera object, to aid in chaining commands
     */
    public synchronized Camera zoomOut(){
        sendCommand(ZOOM_OUT);
        return this;
    }
    
    
    /**
     * Starts the camera zooming in at a given speed between zero and one.
     * There are only six actual steps in the zoom speed.
     * @param speed
     * @return the Camera object, to aid in chaining commands
     */
    public synchronized Camera zoomIn( double speed){
        return zoom( ZOOM_IN_VARIABLE, speed );
    }
    
    
    /**
     * Starts the camera zooming in at a given speed between zero and one
     * and sends a zoomStop after <tt>howLong</tt> milliseconds.
     * There are only six actual steps in the zoom speed.
     * @param speed
     * @return the Camera object, to aid in chaining commands
     */
    public synchronized Camera zoomIn( double speed, int howLong ){
        return zoom( ZOOM_IN_VARIABLE, speed, howLong );
    }
    
    
    
    
    /**
     * Starts the camera zooming out at a given speed between zero and one.
     * There are only six actual steps in the zoom speed.
     * @param speed
     * @return the Camera object, to aid in chaining commands
     */
    public synchronized Camera zoomOut( double speed){
        return zoom( ZOOM_OUT_VARIABLE, speed );
    }
    
    
    
    
    /**
     * Starts the camera zooming out at a given speed between zero and one
     * and sends a zoomStop after <tt>howLong</tt> milliseconds.
     * There are only six actual steps in the zoom speed.
     * @param speed
     * @return the Camera object, to aid in chaining commands
     */
    public synchronized Camera zoomOut( double speed, int howLong ){
        return zoom( ZOOM_OUT_VARIABLE, speed, howLong );
    }
    
    
    
    /**
     * Starts the camera zooming out (negative) or in (positive)
     * at a speed between negative one and positive one.
     * There are only six actual steps in the zoom speed.
     * @param speed
     * @return the Camera object, to aid in chaining commands
     */
    public synchronized Camera zoom( double speedDir ){
        return speedDir < 0 ? zoomOut( -speedDir ) : zoomIn( speedDir );
    }
    
    
    
    /**
     * Starts the camera zooming in or out at a given speed between zero and one.
     * The <tt>command</tt> should be the value of the 
     * variable ZOOM_IN_VARIABLE or ZOOM_OUT_VARIABLE.
     * There are only six actual steps in the zoom speed.
     * @param speed
     * @return the Camera object, to aid in chaining commands
     */
    public synchronized Camera zoom( String command, double speed ){
        String rawHex = getHex( command );              // From commands file
        if( rawHex == null ) return this;               // No command loaded?
        
        speed = Math.min(1,Math.max(0,speed));          // Speed capped at 0..1
        
        int speedI = 2 + (int)(5 * speed + 0.5);        // Speed from 2..7
        String speedH = "" + speedI;                    // Speed as string
        
        String commandHex = rawHex.replace("Z",speedH); // Replace Z with 2..7
        sendHex( commandHex );                          // Send command
        
        return this;
    }
    
    
    
    /**
     * Starts the camera zooming in or out at a given speed between zero and one
     * and sends a zoomStop command after <tt>howLong</tt> milliseconds.
     * The <tt>command</tt> should be the value of the 
     * variable ZOOM_IN_VARIABLE or ZOOM_OUT_VARIABLE.
     * There are only six actual steps in the zoom speed.
     * @param speed
     * @return the Camera object, to aid in chaining commands
     */
    public synchronized Camera zoom( String command, double speed, int howLong ){
        if( this.zoomTask != null ){
            this.zoomTask.cancel();
        }   // end if: cancel previous
        
        this.zoomTask = new java.util.TimerTask(){
            @Override
            public void run() {
                zoomStop();
            }   // end run
        };  // end panTiltTask
        this.timer.schedule(this.zoomTask, howLong);
        
        return zoom( command, speed );
    }
    
    
    /**
     * Zooms the camera to a specific amount between zero
     * (wide, zoomed out) and one (telephoto, zoomed in).
     * There are 4000 steps in the zoom settings, not
     * counting the digital zoom, which is not accessible
     * through this method.
     * @param pos
     * @return
     */
    public synchronized Camera setZoom( double pos ){
        String rawHex = getHex( ZOOM_DIRECT );          // From commands file
        if( rawHex == null ) return this;               // No command loaded?
        
        pos = Math.min(1,Math.max(0,pos));              // Position capped at 0..1
        int posI = (int)(4000 * pos + 0.5);             // Position from 0..4000
        String posH = "" + posI;                        // Position as string
        
        String commandHex = rawHex
          .replace( "P", "" + ( posI/1000) )
          .replace( "Q", "" + ((posI%1000) / 100) )
          .replace( "R", "" + ((posI%100 ) / 10 ) )
          .replace( "S", "" + ( posI%10  ) );
        sendHex( commandHex );                          // Send command
        return this;
    }
    
    
    
/* ********  P A N   /   T I L T  ******** */    
    
    
    
    /**
     * Starts the camera tilting up at a speed between zero and one.
     * @param speed
     * @return the Camera object, to aid in chaining commands
     */
    public synchronized Camera tiltUp( double speed ){
        return panTilt( TILT_UP, 0, speed  );
    }
    
    
    /**
     * Starts the camera tilting up at a speed between zero and one
     * and schedules a panTiltStop for <tt>howLong> millis.
     * @param speed
     * @return the Camera object, to aid in chaining commands
     */
    public synchronized Camera tiltUp( double speed, int howLong  ){
        return panTilt( TILT_UP, 0, speed, howLong    );
    }
    
    
    
    
    /**
     * Starts the camera tilting down at a speed between zero and one.
     * @param speed
     * @return the Camera object, to aid in chaining commands
     */
    public synchronized Camera tiltDown( double speed ){
        return panTilt( TILT_DOWN, 0, speed  );
    }
    
    
    /**
     * Starts the camera tilting down at a speed between zero and one
     * and schedules a panTiltStop for <tt>howLong> millis.
     * @param speed
     * @return the Camera object, to aid in chaining commands
     */
    public synchronized Camera tiltDown( double speed, int howLong ){
        return panTilt( TILT_DOWN, 0, speed, howLong  );
    }
    
    
    /**
     * Starts the camera tilting up (positive) or down (negative)
     * at a speed between negative one and positive one.
     * @param speed
     * @return the Camera object, to aid in chaining commands
     */
    public synchronized Camera tilt( double speedDir ){
        return speedDir < 0 ? tiltDown( -speedDir ) : tiltUp( speedDir );
    }
    
    
    /**
     * Starts the camera tilting up (positive) or down (negative)
     * at a speed between negative one and positive one
     * and schedules a panTiltStop for <tt>howLong> millis.
     * @param speed
     * @return the Camera object, to aid in chaining commands
     */
    public synchronized Camera tilt( double speedDir, int howLong ){
        return speedDir < 0 ? tiltDown( -speedDir, howLong ) : tiltUp( speedDir, howLong );
    }
    
    
    
    /**
     * Starts the camera panning left at a speed between zero and one.
     * @param speed
     * @return the Camera object, to aid in chaining commands
     */
    public synchronized Camera panLeft( double speed ){
        return panTilt( PAN_LEFT, speed, 0 );
    }
    
    
    
    /**
     * Starts the camera panning left at a speed between zero and one
     * and schedules a panTiltStop for <tt>howLong> millis.
     * @param speed
     * @return the Camera object, to aid in chaining commands
     */
    public synchronized Camera panLeft( double speed, int howLong ){
        return panTilt( PAN_LEFT, speed, 0, howLong );
    }
    
    /**
     * Starts the camera panning right at a speed between zero and one.
     * @param speed
     * @return the Camera object, to aid in chaining commands
     */
    public synchronized Camera panRight( double speed ){
        return panTilt( PAN_RIGHT, speed, 0 );
    }
    
    
    
    /**
     * Starts the camera panning right at a speed between zero and one
     * and schedules a panTiltStop for <tt>howLong> millis.
     * @param speed
     * @return the Camera object, to aid in chaining commands
     */
    public synchronized Camera panRight( double speed, int howLong ){
        return panTilt( PAN_RIGHT, speed, 0, howLong );
    }
    
    
    /**
     * Starts the camera panning left (negative) or right (positive)
     * at a speed between negative one and positive one.
     * @param speed
     * @return the Camera object, to aid in chaining commands
     */
    public synchronized Camera pan( double speedDir ){
        return speedDir < 0 ? panLeft( -speedDir ) : panRight( speedDir );
    }
    
    
    /**
     * Starts the camera panning left (negative) or right (positive)
     * at a speed between negative one and positive one
     * and schedules a panTiltStop for <tt>howLong> millis.
     * @param speed
     * @return the Camera object, to aid in chaining commands
     */
    public synchronized Camera pan( double speedDir, int howLong ){
        return speedDir < 0 ? panLeft( -speedDir, howLong ) : panRight( speedDir, howLong );
    }
    
    
    
    /**
     * Stops the camera's pan/tilt operations.
     * @param speed
     * @return the Camera object, to aid in chaining commands
     */
    public synchronized Camera panTiltStop(){
        return panTilt( PAN_TILT_STOP, 0, 0  );
    }
    
    
    /**
     * General purpose pan/tilt command that takes a command such
     * as TILT_UP_START and a pan/tilt speed. 
     * @param command
     * @param panSpeed
     * @param tiltSpeed
     * @return
     */
    public synchronized Camera panTilt( String command, double panSpeed, double tiltSpeed ){
        String rawHex = getHex( command );              // From commands file
        if( rawHex == null ) return this;               // No command loaded?
        
        panSpeed = Math.min(1,Math.max(0,panSpeed));    // Speed capped at 0..1
        tiltSpeed = Math.min(1,Math.max(0,tiltSpeed));  // Speed capped at 0..1
        
        int pSpeedI = (int)(0xFE * panSpeed + 0.5);     // Speed from 0..254
        int tSpeedI = (int)(0xFE * tiltSpeed + 0.5);    // Speed from 0..254
        
        String pSpeedH = Long.toHexString(pSpeedI);     // Speed from 00..FE
        String tSpeedH = Long.toHexString(tSpeedI);     // Speed from 00..FE
        
        String commandHex = rawHex                      // Final command
          .replace("VV",pSpeedH)                        // VV: Pan
          .replace("WW",tSpeedH);                       // WW: Tilt
        sendHex( commandHex );                             // Send command
        
        return this;
    }
    
    
    
    /**
     * General purpose pan/tilt command that takes a command such
     * as TILT_UP_START and a pan/tilt speed and will issue
     * a panTiltStop command after <tt>howLong</tt> milliseconds.
     * @param command
     * @param panSpeed
     * @param tiltSpeed
     * @return
     */
    public synchronized Camera panTilt( String command, double panSpeed, double tiltSpeed, int howLong  ){
        if( this.panTiltTask != null ){
            this.panTiltTask.cancel();
        }   // end if: cancel previous
        
        this.panTiltTask = new java.util.TimerTask(){
            @Override
            public void run() {
                LOGGER.info("Entering panTiltTask run");
                panTiltStop();
                LOGGER.info("Leaving panTiltTask run");
            }   // end run
        };  // end panTiltTask

        Camera c = panTilt( command, panSpeed, tiltSpeed );
        this.timer.schedule(this.panTiltTask, howLong);
        return c;
    }
    
    
    
    /**
     * Pans or tilts the camera according to the speeds and directions
     * given for panning and tilting. 
     * Negative values are left and down.
     * Positive values are right and up.
     * @param panSpeedDir
     * @param tiltSpeedDir
     */
    public synchronized Camera panTilt( double panSpeedDir, double tiltSpeedDir ){
        if( panSpeedDir < 0 ){          // Left?
            return tiltSpeedDir < 0     // Down?
              ? panTilt( PAN_TILT_LEFT_DOWN, -panSpeedDir, -tiltSpeedDir )
              : panTilt( PAN_TILT_LEFT_UP,   -panSpeedDir,  tiltSpeedDir );
        } else {                        // Right.
            return tiltSpeedDir < 0     // Down?
              ? panTilt( PAN_TILT_RIGHT_DOWN,  panSpeedDir, -tiltSpeedDir )
              : panTilt( PAN_TILT_RIGHT_UP,    panSpeedDir,  tiltSpeedDir );
        }   // end else
    }
    
    
    /**
     * Pans or tilts the camera according to the speeds and directions
     * given for panning and tilting and will issue
     * a panTiltStop command after <tt>howLong</tt> milliseconds.
     * Negative values are left and down.
     * Positive values are right and up.
     * @param panSpeedDir
     * @param tiltSpeedDir
     */
    public synchronized Camera panTilt( double panSpeedDir, double tiltSpeedDir, int howLong ){
        if( panSpeedDir < 0 ){          // Left?
            return tiltSpeedDir < 0     // Down?
              ? panTilt( PAN_TILT_LEFT_DOWN, -panSpeedDir, -tiltSpeedDir, howLong )
              : panTilt( PAN_TILT_LEFT_UP,   -panSpeedDir,  tiltSpeedDir, howLong );
        } else {                        // Right.
            return tiltSpeedDir < 0     // Down?
              ? panTilt( PAN_TILT_RIGHT_DOWN,  panSpeedDir, -tiltSpeedDir, howLong )
              : panTilt( PAN_TILT_RIGHT_UP,    panSpeedDir,  tiltSpeedDir, howLong );
        }   // end else
    }
    
    
    
    /**
     * Pans/tilts the camera a relative number of degrees where
     * postive is up and right, negative is down and left.
     * THIS METHOD DOES NOT YET WORK PROPERLY.
     * 
     * @param panDegrees
     * @param tiltDegrees
     * @return
     */
    public synchronized Camera panTiltRelative( 
    double panDegrees, double panSpeed, double tiltDegrees, double tiltSpeed ){
        String rawHex = getHex( PAN_TILT_RELATIVE );    // From commands file
        if( rawHex == null ) return this;               // No command loaded?
        
        // Degree conversions
        int pd = (int)(Math.min(440,Math.max(-440,panDegrees))*10); // Integer, four digits
        int td = (int)(Math.min(440,Math.max(-440,tiltDegrees))*10);// Integer, four digits
        String pds = String.format("%04d",pd);          // Zero-padded string
        String tds = String.format("%04d",td);          // Zero-padded string
        
        // Speed conversions
        panSpeed  = Math.min(1,Math.max(0,panSpeed));   // Speed capped at 0..1
        tiltSpeed = Math.min(1,Math.max(0,tiltSpeed));  // Speed capped at 0..1
        int pSpeedI = (int)(0x18 * panSpeed + 0.5);     // Speed from 0x00..0x18
        int tSpeedI = (int)(0x13 * tiltSpeed + 0.5);    // Speed from 0x00..0x13
        String pSpeedH = Long.toHexString(pSpeedI);     // Speed in hex
        String tSpeedH = Long.toHexString(tSpeedI);     // Speed in hex
        
        // Build hex string
        String commandHex = rawHex
          .replace( 'P', pds.charAt(0) )                // Pan hundreds
          .replace( 'Q', pds.charAt(1) )                // Pan tens
          .replace( 'R', pds.charAt(2) )                // Pan ones
          .replace( 'S', pds.charAt(3) )                // Pan tenths
          
          .replace( 'T', tds.charAt(0) )                // Tilt hundreds
          .replace( 'U', tds.charAt(1) )                // Tilt tens
          .replace( 'X', tds.charAt(2) )                // Tilt ones
          .replace( 'Z', tds.charAt(3) )                // Tilt tenths
          
          .replace( "GG", pSpeedH )                     // Pan speed
          .replace( "HH", tSpeedH );                    // Tilt speed
        
        sendHex( commandHex );                             // Send command
        //sendHex("81 01 06 03 0F 0F 0E 04 08 00 0E 04 08 00 FF");
                
        return this;
    }
    
    
/* ********  H E L P E R  ******** */    
    
    /**
     * Returns the last exception that was thrown.
     * @return
     */
    public synchronized Exception getLastException(){
        return this.lastException;
    }
    
    
    /**
     * Delays the given number of millis and returns
     * the Camera object to aid in chaining.
     * @param millis
     * @return the Camera object to aid in chaining.
     */
    public synchronized Camera delay( int millis ){
        try{ Thread.sleep(millis); }
        catch( InterruptedException exc ){
            LOGGER.warning("Interrupted while in a delay for " + millis );
        }   // end catch
        return this;
    }
    
    
    /**
     * Returns a hex string from the commands.properties file.
     * @param command
     * @return
     */
    private String getHex( String command ){
        String hex = COMMANDS.getProperty(command);
        if( hex == null ){
            LOGGER.warning("Could not find hex values for command " + command );
            return null;
        } else {
            return hex;
        }   // end else
    }
    
    /**
     * Returns a hex string from the commands.properties file.
     * @param command
     * @return
     */
    private String getAlphabetHex( char c ){
        String hex = ALPHABET.getProperty( (""+c).toUpperCase() );
        if( hex == null ){
            LOGGER.warning("Could not find hex title value for character " + c );
            return "1B"; // Hex value for a space
        } else {
            return hex;
        }   // end else
    }
    
    
    /**
     * Sends a named command from the commands.properties file.
     * Some commands require preprocessing and will probably
     * fail, returning false and saving the exception.
     * @param command
     * @return
     */
    public synchronized boolean sendCommand( String command ){
        String hex = getHex(command);
        return hex == null ? false : sendHex(hex);
    }   // end sendCommand
    
    
    
    /**
     * Interprets the string as hexadecimal values and
     * sends those bytes to <tt>outToCamera</tt> stream.
     * If an exception occurs, <tt>false</tt> will be
     * returned, and 
     * @param hex
     */
    protected boolean sendHex( String hex ){
        byte[] bytes = null;
        try{
            String[] pairs = hex.split(" ");
            bytes = new byte[pairs.length];
            for( int i = 0; i < pairs.length; i++ ){
                bytes[i] = (byte)Long.parseLong(pairs[i],16);
            }   // end for: each byte
            
        } catch( Exception exc ){
            this.lastException = exc;
            return false;
        }   // end catch
        
        if( LOGGER.isLoggable(Level.FINER) ){
            LOGGER.finer( "Sending hex " + hex );
        }
        return sendBytes(bytes,0,bytes.length);
    }   // end sendHex
    
    
    
    /**
     * Final step for outgoing data to camera.
     * @param hex
     */
    protected boolean sendBytes( byte[] outgoing, int offset, int length ){
        LOGGER.info("sendBytes called on thread " + Thread.currentThread() );
        try{
            // Write
            this.outToCamera.write(outgoing,offset,length);
            if( LOGGER.isLoggable(Level.FINER) ){
                LOGGER.finer( "Sent " + length + " bytes to camera.");
            }   // end finer
            
            // Wait for reply
            // We set up a timer to interrupt the thread after
            // a timeout in case there's a problem receiving
            // a reply from the camera.
            // We want to wait for a reply though, because 
            // it seems that the camera ignores commands that 
            // are sent to it before it has finished replying,
            // so we don't want to flood the camera.
            byte[] reply = new byte[1];                             // Replies typically 6 bytes
            int read = -1;                                          // Count how many bytse were received
            final Thread t = Thread.currentThread();                // Current thread to interrupt
            Thread inter = new Thread( new Runnable(){
                public void run(){
                    try{ 
                        Thread.sleep(250);
                        //LOGGER.finer("Interrupting read thread...");
                        t.interrupt();
                        LOGGER.finer("Interrupted read thread.");
                    }
                    catch( InterruptedException exc ){
                        //LOGGER.finer("Interrupting thread was interrupted - this is standard behavior.");
                    }
                }
            });
            inter.start();
            try{
                //LOGGER.finer("Reading from camera...");
                read = this.inFromCamera.read(reply);               // Read response. 
                                                                    // We don't really care what it is.
                //LOGGER.finer("Done reading from camera.");
                Thread.sleep(100);
                inter.interrupt();
            } catch( Exception exc ){
                LOGGER.warning("Timeout cancelled reading from camera after 100 ms.");
            }   // end catch
            
        } catch( Exception exc ){
            this.lastException = exc;
            return false;
        }   // end catch
        return true;
    }   // end sendHex
    
    
}   // end class Camera
