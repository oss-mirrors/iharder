
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * A rather crude HTTP server demonstrating some ways to interact
 * with the <code>java.nio</code> package and NioServer.
 * 
 * @author Robert Harder
 */
public class HttpNioExample implements NioServer.Listener {

    private int port;
    private File root;
    private NioServer server;
    private Properties mimeTypes;

    private final static Pattern EOH_PATTERN = Pattern.compile(".*(\r\r|\n\n|\r\n\r\n).*",Pattern.DOTALL); // End of headers
    private final Matcher eoh_matcher = EOH_PATTERN.matcher("");
    
    private final static Pattern GET_PATTERN = Pattern.compile("^GET (\\S+) HTTP/\\d\\.\\d[\\r\\n].*",Pattern.DOTALL); // GET request
    private final Matcher get_matcher = GET_PATTERN.matcher("");
    
    private final static Charset charset = Charset.forName("US-ASCII");
    private final static CharsetEncoder encoder = charset.newEncoder();
    private final static CharsetDecoder decoder = charset.newDecoder();
    private final CharBuffer cbuff = CharBuffer.allocate(1024);
    
    private final CharBuffer RESP_200_OK = CharBuffer.wrap("HTTP/1.1 200 OK\r\n");
    private final CharBuffer RESP_400_BAD_REQUEST = CharBuffer.wrap("HTTP/1.0 400 Bad Request\r\n");
    private final CharBuffer RESP_404_NOT_FOUND = CharBuffer.wrap("HTTP/1.0 404 Not Found\r\n");
    private final CharBuffer RESP_500_INTERNAL_ERROR = CharBuffer.wrap("HTTP/1.0 500 Internal Error\r\n");
    private final CharBuffer BLANK_LINE = CharBuffer.wrap("\r\n");


    public HttpNioExample( int port, String rootDir ){
        this.port = port;
        this.root = new File(rootDir);
    }


    public void start() throws IOException{
        
        // Mime Types
        this.mimeTypes = new Properties();
        this.mimeTypes.load(this.getClass().getResourceAsStream("mimetypes.properties"));

        // Root Directory
        if( !root.isDirectory() ){
            throw new IOException("Root not a directory: " + root );
        }

        // NIO server
        server = new NioServer();
        server.setSingleTcpPort(port);
        server.addNioServerListener(this);
        server.setOutputBufferSize(100);


        server.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {

                final String prop = evt.getPropertyName();
                final Object oldVal = evt.getOldValue();
                final Object newVal = evt.getNewValue();
                System.out.println("Property: " + prop + ", Old: " + oldVal + ", New: " + newVal );

                if( NioServer.LAST_EXCEPTION_PROP.equals( prop ) ){
                    Throwable t = (Throwable)newVal;
                    t.printStackTrace();
                }
            }   // end prop change
        });

        server.start();
    }


    public static void main(String[] args) throws IOException {
        args = new String[]{ "1234", "." };
        int port = -1;
        String rootDir = null;
        try{
            port = Integer.parseInt(args[0]);
            rootDir = args[1];
        } catch( Exception ex ){
            System.out.println("Usage: java HttpNioExample port dir");
            System.out.println("  port  The TCP port on which to listen");
            System.out.println("  dir   The root directory from which to serve files");
            System.exit(1);
        }   // end catch
        HttpNioExample hne = new HttpNioExample(port,rootDir);
        hne.start();
    }   // end main


    private String getMimeType(String filename){
        int lastDot = -1;
        if( (lastDot = filename.lastIndexOf('.')) > 0 && lastDot < filename.length()-2 ){
            String suffix = filename.substring(lastDot+1);
            String mime = mimeTypes.getProperty(suffix);
            if( mime == null ){
                return "application/octet-stream";
            } else {
                return mime;
            }
        } else {
            return "application/octet-stream";
        }
    }

    private CharBuffer contentTypeLine( String filename ){
        return CharBuffer.wrap("Content-Type: " + getMimeType(filename) + "\r\n");
    }

    private CharBuffer contentLengthLine( long size ){
        return CharBuffer.wrap("Content-Length: " + size + "\r\n");
    }

    
    public void newConnectionReceived(NioServer.Event evt) {
        System.out.println("New connection: " + evt.getKey().channel() );
    }
    

    public void tcpDataReceived(NioServer.Event evt) {
        ByteBuffer inBuff = evt.getInputBuffer();                               // Buffer with data in it
        ByteBuffer outBuff = evt.getOutputBuffer();                             // Buffer for sending data back
        cbuff.clear();                                                          // Clear our char buffer to hold input
        CoderResult cr = decoder.reset().decode(inBuff, cbuff, true);           // Decode data

        if( cr == CoderResult.OVERFLOW ){                                       // Our 1024-character buffer is too big?
            outBuff.clear();
            RESP_400_BAD_REQUEST.rewind();
            encoder.reset().encode(RESP_400_BAD_REQUEST, outBuff, true);        // Encode the "bad request" message
            outBuff.flip();                                                     // Prepare output buffer to be passed on
            evt.closeAfterWriting();                                            // Indicate that we're done after the message gets sent

        } else if( cr == CoderResult.UNDERFLOW ){                               // We've read all there is (can ignore case with multibyte chars here)
            cbuff.flip();                                                       // Prepare cbuff for reading
            System.out.println(cbuff);
            eoh_matcher.reset(cbuff);                                           // Reset end of header matcher
            if( eoh_matcher.matches() ){                                        // Done with headers (two returns in a row)
                
                get_matcher.reset(cbuff);                                       // Reset grep get_matcher with this input
                if( get_matcher.matches() ){                                    // Does it match a GET request?
                    String filename = get_matcher.group(1);                     // Filename is first set of (..)
                    File file = new File( this.root, filename );                // Make java.io.File out of request
                    // SHOULD CHECK SECURITY HERE SO AS NOT TO SHARE WHOLE HARD DRIVE
                    System.out.println("Requested filename " + filename + "(" + file + ")");

                    if( file.isFile() ){                                        // Requested a file?
                        try {
                            FileInputStream fis = new FileInputStream(file);    // Open file for reading
                            FileChannel fc = fis.getChannel();                  // Get channel for NIO
                            evt.getKey().attach(fc);                            // Save it in key's attachment (convenient!)
                            CharBuffer mimeLine = contentTypeLine( filename );  // Header with mime type
                            CharBuffer sizeLine = contentLengthLine( fc.size() );  // Header with content length

                            System.out.println("OK: " + file);
                            outBuff.clear();
                            encoder.reset();
                            RESP_200_OK.rewind();
                            BLANK_LINE.rewind();
                            encoder.encode(RESP_200_OK, outBuff, false);            // Encode OK
                            encoder.encode(mimeLine, outBuff, false);               // Encode content type
                            encoder.encode(sizeLine, outBuff, false);               // Encode content length
                            encoder.encode(BLANK_LINE, outBuff, true);          // Encode end of headers
                            outBuff.flip();

                        } catch (IOException ex) {
                            Logger.getLogger(HttpNioExample.class.getName()).log(Level.SEVERE, null, ex);
                            System.out.println("500: " + ex.getMessage());
                            outBuff.clear();
                            encoder.reset();
                            RESP_500_INTERNAL_ERROR.rewind();
                            BLANK_LINE.rewind();
                            encoder.encode(RESP_500_INTERNAL_ERROR, outBuff, false); // Encode error
                            encoder.encode(BLANK_LINE, outBuff, true);          // Encode end of headers
                            outBuff.flip();
                            evt.closeAfterWriting();
                        }
                    } else {
                        System.err.println("Not a file: " + file );
                        
                        outBuff.clear();
                        encoder.reset();
                        RESP_404_NOT_FOUND.rewind();
                        BLANK_LINE.rewind();
                        encoder.encode(RESP_404_NOT_FOUND, outBuff, false);       // Encode error
                        encoder.encode(BLANK_LINE, outBuff, true);              // Encode end of headers
                        outBuff.flip();
                        evt.closeAfterWriting();
                    }
                } else {
                    System.out.println("400: Headers done but no valid GET");
                    outBuff.clear();
                    encoder.reset();
                    RESP_400_BAD_REQUEST.rewind();
                    BLANK_LINE.rewind();
                    encoder.encode(RESP_400_BAD_REQUEST, outBuff, false);       // Encode error
                    encoder.encode(BLANK_LINE, outBuff, true);              // Encode end of headers
                    outBuff.flip();
                    evt.closeAfterWriting();
                }

            } else {
                // Not at end of headers yet.
                // Leave the data in the input buffer for next time.
                System.out.println("Headers not done: " + cbuff);
                inBuff.flip();
            }

        }   // end if: still looking for GET

    }

    
    public void udpDataReceived(NioServer.Event evt) {
        throw new UnsupportedOperationException("UDP not supported in this HTTP server.");
    }

    
    public void tcpReadyToWrite(NioServer.Event evt) {
        ByteBuffer buff = evt.getOutputBuffer(); // Buffer to leave data in
        buff.clear();
        try {
            Object att = evt.getKey().attachment();
            if( att instanceof FileChannel ){
                //FileChannel fc = (FileChannel)evt.getKey().attachment();
                ReadableByteChannel rbc = (ReadableByteChannel)evt.getKey().attachment();

                if( rbc != null && rbc.read(buff) < 0 ){
                    System.out.println("Done sending file" );
                    rbc.close();
                    evt.close();
                }
            } else if( att instanceof Boolean && !((Boolean)att) ){
                //evt.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(HttpNioExample.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            buff.flip();
        }

    }

    public void connectionClosed(NioServer.Event evt) {
        System.out.println("Connection closed: " + evt.getKey().channel() );
    }


}
