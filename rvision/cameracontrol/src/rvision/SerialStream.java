package rvision;




import gnu.io.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.*;

/**
 *
 * @author robert.harder
 */
public class SerialStream implements SerialPortEventListener {

    private final static Logger LOGGER = Logger.getLogger(SerialStream.class.getName());
    
    
    // Try to load rxtx library
    static{try{ loadLibrary(); }
        catch( Throwable t ){
            //t.printStackTrace();
            LOGGER.warning( "Error while copying native code to working directory (not necessarily bad): " + t.getMessage() );
        }   // end catch: throwable
    }   // end static
    
    private static boolean loadLibrary() throws Throwable{
        
        // Get input stream from jar resource
        String[] libs = new String[]{ "rxtxSerial.dll", "librxtxSerial.so", "librxtxSerial.jnilib" };
        for( String lib : libs ){
            try{
                String[] ll = lib.split("\\.");
                String prefix = ll[0];
                String suffix = ll[1];

                File outFile = new File( lib );
                //File outFile = File.createTempFile(prefix, "."+ suffix);
                //outFile.deleteOnExit();

                if( !outFile.exists() ){
                    System.out.println("Copying " + lib + " to the working directory...");
                     InputStream inputStream =
                        SerialStream.class.getResource("/"+lib).openStream();  

                     // Copy resource to filesystem in a temp folder with a unique name
                     File rxtxDll = new File(lib);//Working directory
                     FileOutputStream outputStream = new FileOutputStream(outFile);
                     byte[] array = new byte[8192];
                     int read = 0;
                     while ( (read = inputStream.read(array)) >= 0)
                         outputStream.write(array, 0, read);
                     outputStream.close();  
                }   // end if: file does not exist

                System.load(outFile.getAbsolutePath());
            } catch( Throwable t ){
                System.err.println("SerialStream.loadLibrary: " + t.getMessage() );
            }
        }   // end for: each library

         // Delete on exit the dll
         //temporaryDll.deleteOnExit();  

         // Finally, load the dll
         //System.load(rxtxDll.getPath());

        return true;
    }

    
    
/* ********  S T A T I C   F I E L D S  ******** */    
    
    private final static String DEFAULT_PORT   = "COM1";
    private final static int DEFAULT_BAUD      = 9600;
    private final static int DEFAULT_DATA_BITS = SerialPort.DATABITS_8;
    private final static int DEFAULT_PARITY    = SerialPort.PARITY_NONE;
    private final static int DEFAULT_STOP_BITS = SerialPort.STOPBITS_1;
    private final static int DEFAULT_READ_TIMEOUT = 3000; // milliseconds
    
    
    
/* ********  I N S T A N C E   F I E L D S  ******** */    
    
    private CommPortIdentifier portId;              // The requested serial port
    private SerialPort serialPort;                  // The active serial port
    private String port;
    private int baud;
    private int dataBits;
    private int parity;
    private int stopBits;
    private int readTimeout;
    private InputStream in;
    private OutputStream out;
    
    private BlockingQueue<Integer> inputQueue;      // Thread-safe queueing of
    private static Integer[] BYTES; // Populate bytes array, a handy lookup to avoid excessive instantiation
    static{
        BYTES = new Integer[256];
        for( int i = 0; i < 255; i++ ){
            BYTES[i] = new Integer(i);
        }   // end if: 256 bytes
    }
    
    public SerialStream() throws java.io.IOException{
        this( DEFAULT_PORT, DEFAULT_BAUD, DEFAULT_DATA_BITS, DEFAULT_PARITY, DEFAULT_STOP_BITS, DEFAULT_READ_TIMEOUT);
    }
    
    
    public SerialStream( String port, int baud ) throws java.io.IOException{
        this( port, baud, DEFAULT_DATA_BITS, DEFAULT_PARITY, DEFAULT_STOP_BITS, DEFAULT_READ_TIMEOUT);
    }
    
    
    
    
    
    public SerialStream( String port, int baud, int dataBits, int parity, int stopBits, int readTimeout )
    throws java.io.IOException{
        this.port = port;
        this.baud = baud;
        this.dataBits = dataBits;
        this.parity = parity;
        this.stopBits = stopBits;
        this.readTimeout = readTimeout;
        initComponents();
    }
    
    private void initComponents() throws IOException{
        this.portId = getPortId(this.port);
        this.inputQueue = new LinkedBlockingQueue<Integer>();


        // Serial Port
        try { this.serialPort = (SerialPort)this.portId.open("Java Serial Stream", 2000); } // 2 second timeout
        catch (PortInUseException e){ 
            close();
            throw new java.io.IOException("The port " + portId.getName() + " was in use.",e);
        }

        // Input Stream
        final InputStream serialIn = this.serialPort.getInputStream();
        this.in = new InputStream(){
            @Override
            public int read() throws IOException {
                int b = -1;
                while( (b = serialIn.read()) < 0 ){         // Read one byte (maybe)
                    Thread.yield();
                    synchronized( serialIn ){               // Lock on stream
                        //System.out.println("Attempted read. No data. Waiting...");
                        Thread.yield();
                        try{
                            Thread.sleep(100);
                            serialIn.wait(); 
                        }             // Wait until we're told to try again
                        catch( InterruptedException exc ){
                            throw new IOException( "Interrupted while waiting on serial input.",exc);
                        }   // end catch
                    }   // end sync
                }   // end while: no data
                return b;
            }   // end read
        };  // end inputstream

        
        // Output Stream
        try { this.out = serialPort.getOutputStream(); } 
        catch (java.io.IOException e) { 
            close();
            throw new java.io.IOException("Couldn't connect to the input stream on " + portId.getName() + ".",e);
        }

        // Event Listener
        try { serialPort.addEventListener(this); } 
        catch (java.util.TooManyListenersException e) { 
            close();
            throw new java.io.IOException("Too many listeners on " + portId.getName() + ".",e);
        }
        serialPort.notifyOnDataAvailable(true);

        // Parameters
        try { this.serialPort.setSerialPortParams(this.baud, this.dataBits, this.stopBits, this.parity ); }
        catch (UnsupportedCommOperationException e) { 
            close();
            throw new java.io.IOException("Could not set parameters on serial port "+  portId.getName() + ".",e);
        }
            
        
    }   // end initComponents
    
    
    
    private CommPortIdentifier getPortId( String commPort ){
        CommPortIdentifier portId = null;
        java.util.Enumeration portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
            CommPortIdentifier someId = (CommPortIdentifier) portList.nextElement();
            if (someId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                if( commPort.equals( someId.getName() ) ){
                    portId = someId;
                    break;
                }
            }   // end if:
        }   // end while: more ports
        return portId;
    }
    
    
    public static String[] getPortNames(){
        LinkedList<String> names = new LinkedList<String>();
        java.util.Enumeration portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
            CommPortIdentifier someId = (CommPortIdentifier) portList.nextElement();
            if (someId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                names.add( someId.getName() );
            }   // end if:
        }   // end while: more ports
        return names.toArray(new String[names.size()]);
    }
    
    
    public String getPortName(){
        return this.portId == null ? null : this.portId.getName();
    }
    
    
    
    public InputStream getInputStream(){
        return this.in;
    }
    
    
    public OutputStream getOutputStream(){
        return this.out;
    }
    
    
    public void close(){
        try{ this.serialPort.close(); }
        catch(Exception exc){
            LOGGER.warning( "Error while closing serial port: " + exc.getMessage() );
        }
    }

    
/* ********  S E R I A L   E V E N T   L I S T E N E R  ******** */    
    

    public void serialEvent(SerialPortEvent event) {
        //System.out.println("Serial event detected on thread " + Thread.currentThread() + ": " + event );
	switch (event.getEventType()) {

	case SerialPortEvent.BI: // Break interrupt
            System.out.println("Break interrupt");
            break;
            
	case SerialPortEvent.OE: // Overrun error
            System.out.println("Overrun error");
            break;

	case SerialPortEvent.FE: // Framing error
            System.out.println("Framing error");
            break;

	case SerialPortEvent.PE: // Parity error
            System.out.println("Parity error");
            break;

	case SerialPortEvent.CD: // Carrier detect
            System.out.println("Carrier detect");
            break;

	case SerialPortEvent.CTS: // Clear to send
            System.out.println("Clear to send");
            break;

	case SerialPortEvent.DSR: // Data set ready
            System.out.println("Data set ready");
            break;

	case SerialPortEvent.RI: // Ring indicator
            System.out.println("Ring indicator");
            break;

	case SerialPortEvent.OUTPUT_BUFFER_EMPTY: // Output buffer is empty
            System.out.println("Output buffer is empty");
            break;
            
        // Data Available
        // Read data one byte at a time and put it in the queue.
        // Yes, I thought about using a little buffer, but for now
        // I prefer the ease of having the SerialReader read one byte
        // at a time with no "funny business" (buffers). We can always
        // change it later if we find it hurts performance on this modest
        // amount of serial port activity.
	case SerialPortEvent.DATA_AVAILABLE: // Data available at the serial port
            //System.out.println("Data available");
            Thread.yield();
            InputStream serialIn = ((RXTXPort)event.getSource()).getInputStream();
            synchronized( serialIn ){
                serialIn.notify();    // Notify
            }   // end sync
	    break;
	}   // end switch: event type
    }   // end serialEvent

    
    
}
