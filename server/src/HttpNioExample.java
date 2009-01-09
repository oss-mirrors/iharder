
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
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
 *
 * @author Student
 */
public class HttpNioExample implements NioServer.Listener {

    private int port;
    private File root;
    private NioServer server;
    private Properties mimeTypes;

    private final static Charset charset = Charset.forName("US-ASCII");
    private final static CharsetEncoder encoder = charset.newEncoder();
    private final static CharsetDecoder decoder = charset.newDecoder();
    private final CharBuffer cbuff = CharBuffer.allocate(1024);


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

    
    public void nioServerNewConnectionReceived(NioServer.Event evt) {
        
    }

    public void nioServerTcpDataReceived(NioServer.Event evt) {
        ByteBuffer inBuff = evt.getInputBuffer();                                      // Buffer with data in it
        Object att = evt.getKey().attachment();
        if( att == null || att instanceof StringBuilder ){
            StringBuilder request = (StringBuilder)evt.getKey().attachment();       // Read characters from input
            if( request == null ){                                                  // Will be null first time through
                request = new StringBuilder();                                      // Lazily create
                evt.getKey().attach(request);
            }   // end if: lazy create

            cbuff.clear();
            while( decoder.decode(inBuff, cbuff, false) == CoderResult.OVERFLOW ){
                cbuff.flip();
                request.append(cbuff);
                cbuff.clear();
            }
            cbuff.flip();
            request.append(cbuff);

            // http://localhost:1234/build.xml
            // Have we received the GET /filename HTTP/1.0 line yet?
            Pattern patt = Pattern.compile("^GET /(.+) HTTP.*",Pattern.DOTALL);
            Matcher mat = patt.matcher(request);
            if( mat.matches() ){
                String filename = mat.group(1);
                System.out.println("match " + filename );
                File file = new File( this.root, filename );
                if( file.isFile() ){
                    try {
                        FileInputStream fis = new FileInputStream(file);
                        FileChannel fc = fis.getChannel();
                        evt.getKey().attach(fc);
                        evt.setNotifyOnTcpWritable(true);
                        String resp = "Content-type: text/plain\r\nContent-length: " + fc.size() + "\r\n\r\n";
                        cbuff.clear();
                        cbuff.put(resp).flip();
                        ByteBuffer outBuff = evt.getOutputBuffer();
                        outBuff.clear();
                        encoder.encode(cbuff, outBuff, true);
                        outBuff.flip();
//                        ((SocketChannel)evt.getKey().channel()).write(buff);
//                        buff.clear().flip();
                    } catch (IOException ex) {
                        Logger.getLogger(HttpNioExample.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    System.err.println("Not a file: " + file );
                }
            }

            System.out.println(request);
        }   // end if: still looking for GET
        
    }

    public void nioServerUdpDataReceived(NioServer.Event evt) {
        throw new UnsupportedOperationException("UDP not supported.");
    }

    public void nioServerTcpReadyToWrite(NioServer.Event evt) {
        try {
            ByteBuffer buff = evt.getOutputBuffer(); // Buffer to leave data in
            buff.clear();
            FileChannel fc = (FileChannel)evt.getKey().attachment();
            
            if( fc != null && fc.read(buff) < 0 ){
                fc.close();
                evt.close();
            }
            buff.flip();
        } catch (IOException ex) {
            Logger.getLogger(HttpNioExample.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void nioServerConnectionClosed(NioServer.Event evt) {
        System.out.println("Closed " + evt.getKey() );
    }


}
