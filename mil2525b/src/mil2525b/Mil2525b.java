package mil2525b;

import java.io.*;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.*;

/**
 * <p>Helper class for accessing the 3,000+ images in this archive.</p>
 * 
 * <div style="float:right;border:solid black 1px; padding:1em; margin:1em; font-size:10px;text-align:center">
 * <img src="doc-files/Mil2525b-1.png" /><br />
 * Friendly UAV<br />
 * Mil2525b: sfapmfq--------<br />
 * Cot: a-f-A-m-f-q
 * </div>
 * 
 * <p>Since it is so unwieldy to deal with the loose image files within
 * the mil2525b.jar file, you'll probably want to leave it as a standalone
 * jar file that you add to the classpath. If there's a chance, in your
 * application deployment scenario, that the jar file may be missing,
 * you might consider using reflection to access some methods. This could
 * also be helpful if you don't have the jar file in your classpath at
 * development time.</p>
 * 
 * <p>Here is a sample method for getting an image (byte array) from a
 * given MIL2525b identifier without needing the jar file to be in the
 * classpath at compile time and failing gracefully if not available
 * at run time. Note that <tt>Method</tt> refers to
 * <tt>java.lang.reflect.Method</tt>.</p>
 * <pre>
    private static byte[] getMil2525bImage( String m25 ){
        byte[] img = null;
        try{
            Class  c = Class.forName("mil2525b.Mil2525b");
            Object o = c.newInstance();
            Method m = c.getMethod("getBytesFromMil2525b", String.class);
            img = (byte[])m.invoke(o,m25);
        } catch( Throwable t ){
            //System.err.println("May or may not want to report error here.");
        }   // end catch
        return img;
    }   // end getMil2525bImage
 * </pre>
 * 
 * <p>Of course you may want to do one for {@link #getBytesFromCotType} too.</p>
 * 
 * @see HttpServer
 * @see HttpServerGui
 * @author robert.harder
 */
public class Mil2525b {
    
    private final static Logger LOGGER = Logger.getLogger(Mil2525b.class.getName());
    
    // From mil2525b.pdf spec doc
    // These are the meanings of the 15 fields.
    private final static int CODING_SCHEME_INDEX = 0;
    private final static int AFFILIATION_INDEX = 1;
    private final static int BATTLE_DIMENSION_INDEX = 2;
    private final static int STATUS_INDEX = 3;
    private final static int FUNCTION_START_INDEX = 4;
    private final static int SYMBOL_MODIFIER_START_INDEX = 10;
    private final static int COUNTRY_CODE_START_INDEX = 12;
    private final static int ORDER_OF_BATTLE_INDEX = 14;
    
    public static void main(String[] args){
        System.out.println(convertCotTypeToMil2525b("a-f-A-m-f-q"));
    }
    
    
    private static Map<String,SoftReference<byte[]>> cachedImages = new HashMap<String,SoftReference<byte[]>>();
    
    
    /**
     * Returns a URL to the image file for the given Mil2525b identifer.
     * @param mil2525b
     * @return url to image within classpath or null
     */
    public static URL getUrlFromMil2525b( String mil2525b ){
        URL url = null;
        try{
            url = Mil2525b.class.getResource(mil2525b+".png");
        } catch( Exception exc ){
            LOGGER.warning("Error with " + mil2525b + ".png: " + exc.getMessage());
        }   // end catch
        return url;
    }
    
    /**
     * Returns a URL to the image file for the given cot type.
     * @param cotType
     * @return url for image or null
     */
    public static URL getUrlFromCotType( String cotType ){
        return getUrlFromMil2525b( convertCotTypeToMil2525b( cotType ) );
    }
    
    
    /**
     * Returns an InputStream to the mil2525b given, or null
     * if unable to do so. Filenames are expected to be the
     * png icon files such as <tt>g-fpaa--------x.png</tt>.
     * @param mil2525b
     * @return input stream for image or null
     */
    public static InputStream getInputStreamFromMil2525b( String mil2525b  ){
        InputStream in = null;
        try{
            in = Mil2525b.class.getResourceAsStream(mil2525b+".png");
        } catch( Exception exc ){
            LOGGER.warning("Error with " + mil2525b + ".png: " + exc.getMessage());
        }   // end catch
        return in;
    }
    
    
    /**
     * Returns an input stream for the given cot type, or null
     * if icon cannot be found.
     * @param cotType
     * @return input stream for image or null
     */
    public static InputStream getInputStreamFromCotType( String cotType  ){
        return getInputStreamFromMil2525b( convertCotTypeToMil2525b( cotType ) );
    }
    
    /**
     * Returns a mil2525b mil2525b (such as <tt>g-fpaa--------x.png</tt>)
     * based on the cot event type given.
     * 
     * @param cotType
     * @return mil2525b indicator
     */
    public static String convertCotTypeToMil2525b( String cotType ){
        if( cotType == null ){
            throw new NullPointerException("Cot type must not be null.");
        }   // end catch
        char[] mil2525b = "---------------".toCharArray();
        
        if( cotType.startsWith("a") ){                      // Atoms only for now
            char affil =                                    // Affiliation
              cotType.length() >= 2 ?                       // 'f' in a-f
              Character.toLowerCase(cotType.charAt(2)) : 'O';
            char space =                                    // Battlespace arena
              cotType.length() >= 4 ?                       // 'g' in a-f-g
              Character.toLowerCase(cotType.charAt(4)) : 'X';
            
            // Details about the thing
            for( int i = 6; i < cotType.length() && i < 17; i += 2 ){
                mil2525b[ FUNCTION_START_INDEX + (i-6)/2 ] = 
                  Character.toLowerCase(cotType.charAt(i));
            }   // end for: detail fields
            
            
            mil2525b[CODING_SCHEME_INDEX] = 's';            // "Warfighting" only for now
            mil2525b[AFFILIATION_INDEX] = affil;
            mil2525b[BATTLE_DIMENSION_INDEX] = space;
            mil2525b[STATUS_INDEX] = 'p';                   // "Present" only for now
            
        }   // end if: atoms
        
        return new String(mil2525b);
    }  
    
    
    public static byte[] getBytesFromCotType( String cotType ){
        return getBytesFromMil2525b( convertCotTypeToMil2525b(cotType) );
    }
    
    /**
     * Returns the bytes for the image for the requested Mil2525b indicator.
     * This method caches the returned byte arrays using SoftReferences.
     * @param mil2525b
     * @return image bytes or null
     */
    public static byte[] getBytesFromMil2525b( String mil2525b ){
        synchronized( cachedImages ){
            SoftReference<byte[]> ref = cachedImages.get(mil2525b);
            if( ref == null ){
                byte[] data  = readBytes(mil2525b);
                cachedImages.put( mil2525b, new SoftReference(data) );
                return data;
            } else {
                byte[] data = ref.get();
                if( data == null ){
                    data  = readBytes(mil2525b);
                    cachedImages.put( mil2525b, new SoftReference(data) );
                }   // end if: dumped from cache
                return data;
            }   // end else: never in cache
        }   // end sync
    }
     
    private static byte[] readBytes( String mil2525b ){
        byte[] data = null;
        InputStream in = getInputStreamFromMil2525b( mil2525b );
        if( in != null ){
            BufferedInputStream bin = new BufferedInputStream(in);
            try{
                data = new byte[ bin.available() ];
                bin.read(data);
            } catch( IOException exc ){
                LOGGER.warning("Could not read image from " + in + ": " + exc.getMessage());
            }
        }
        
        return data;
    }
    
    
}
