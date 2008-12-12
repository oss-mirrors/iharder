package mil2525b;

import java.lang.ref.SoftReference;
import java.util.logging.Logger;
import java.util.*;
import java.util.logging.Level;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.*;

/**
 *
 * @author robert.harder
 */
public class CotHelper {
    private final static Logger LOGGER = Logger.getLogger(CotHelper.class.getName());
    private final static String COT_TYPES_XML = "CoTtypes.xml";
    
    private static org.w3c.dom.Document COT_TYPES_DOC = null;  // XML file with packet type info
    private static javax.xml.xpath.XPath XPATH = null;      // XPath helper
    private static Map<String,SoftReference<Object>> XPATH_CACHE = new HashMap<String,SoftReference<Object>>();   // Cache xpath queries
    static{ 
        try{
            readCotTypesXml(CotHelper.class.getResource(COT_TYPES_XML));
        //readCotTypesXml(GarminPacket.class.getResource(COT_TYPES_XML)); 
        } catch(Exception exc){
            LOGGER.log(Level.SEVERE,"Could not load CotTypes.xml",exc);
        }
    }   // end static

    
    
/* ******** S T A T I C  ******** */
    

    
    
    /**
     * Read in GarminPacketTypes.xml, or similar, that has information
     * about all the Packet types, their offsets, byte packing, etc.
     */
    private static void readCotTypesXml( java.net.URL config ){
        
        java.io.InputStream in = null;
        try{
            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
            in = config.openStream();
            COT_TYPES_DOC = db.parse( in );
            XPATH = javax.xml.xpath.XPathFactory.newInstance().newXPath(); 
        } catch( javax.xml.parsers.ParserConfigurationException exc ){
            LOGGER.log(Level.WARNING,exc.getMessage(),exc);
        } catch( org.xml.sax.SAXException exc ){
            LOGGER.log(Level.WARNING,exc.getMessage(),exc);
        } catch( java.io.IOException exc ){
            LOGGER.log(Level.WARNING,exc.getMessage(),exc);
        } catch( RuntimeException exc ){
            LOGGER.log(Level.WARNING,exc.getMessage(),exc);
        } 
        finally{
            try{ in.close(); }
            catch( Exception exc ){}
        }   // end finally
        
    }
    
    
    /** 
     * Returns xpath expression and caches result for future use.
     * Assumes the underlying config document is not changed.
     */
    private static Object getXPath( String expr, javax.xml.namespace.QName type ){
        SoftReference soft = XPATH_CACHE.get(expr);
        Object obj = null;
        if( soft != null ) obj = soft.get();
        if( obj == null ){
            try {
                obj = XPATH.evaluate(expr, COT_TYPES_DOC, type);
                soft = new SoftReference<Object>( obj );               
                XPATH_CACHE.put(expr,soft);  // Put in cache
            } catch (XPathExpressionException ex) {
                if( LOGGER.isLoggable(Level.FINE) )
                    LOGGER.log(Level.FINE,ex.getMessage(),ex);
            }   // end catch
        }   // end if: not in cache
        
        return obj;
    }
    
    
    /**
     * Searches GarminPacketTypes.xml or similar for the given xpath expression
     * and returns the matching list of nodes or null if not found.
     *
     * @param expr The xpath expression to match
     * @return The result or null if not found
     */
    public static org.w3c.dom.NodeList getXPathNodeList( String expr ){
        Object obj = getXPath(expr, javax.xml.xpath.XPathConstants.NODESET);
        return obj instanceof org.w3c.dom.NodeList ? (org.w3c.dom.NodeList)obj : null;
    }
    
    
    /**
     * Searches KestrelPacketTypes.xml or similar for the given xpath expression
     * and returns the first matching node or null if not found.
     *
     * @param expr The xpath expression to match
     * @return The result or null if not found
     */
    public static org.w3c.dom.Node getXPathNode( String expr ){        
        org.w3c.dom.NodeList list = getXPathNodeList(expr);
        return list.getLength() == 0 ? null : list.item(0);
    }
    
    
    /**
     * Searches GarminPacketTypes.xml or similar for the given xpath expression
     * and returns text content of the result or null if not found.
     *
     * @param expr The xpath expression to match
     * @return The result or null if not found
     */
    public static String getXPathString( String expr ){
        Object obj = getXPath(expr, javax.xml.xpath.XPathConstants.STRING);
        return obj != null ? obj.toString() : null;
    }
    
    /**
     * Searches GarminPacketTypes.xml or similar for the given xpath expression
     * and returns the number or null if not found.
     *
     * @param expr The xpath expression to match
     * @return The result or null if not found
     */
    public static Number getXPathNumber( String expr ){
        Object obj = getXPath(expr, javax.xml.xpath.XPathConstants.NUMBER);
        return obj instanceof Number ? (Number)obj : null;
    }
    
    
    
    public static Collection<String> isWhat( String type ){
        List<String> list = new LinkedList<String>();
        
        NodeList nodes = getXPathNodeList( "/types/is" );   // All "is" nodes
        for( int i = 0; i < nodes.getLength(); i++ ){       // Loop over
            Node node = nodes.item(i);                      // Get "is" node
            NamedNodeMap attrs = node.getAttributes();      // Attributes
            String match = attrs.getNamedItem("match").getNodeValue();  // "match" attribute
            if( type.matches(match + ".*") ){
                list.add( attrs.getNamedItem("what").getNodeValue() );
            }   // end if: match
        }   // end for: each node
        
        return list;
    }
    
    
    public static Collection<List<String>> cotTypes( String type ){
        List<List<String>> list = new LinkedList<List<String>>();
        
        NodeList nodes = getXPathNodeList( "/types/cot" );   // All "is" nodes
        for( int i = 0; i < nodes.getLength(); i++ ){       // Loop over
            Node node = nodes.item(i);                      // Get "is" node
            NamedNodeMap attrs = node.getAttributes();      // Attributes
            String match = attrs.getNamedItem("cot").getNodeValue();  // "match" attribute
            match = match + "-G";
            System.out.println(match);
            if( type.matches(match) ){
                String full = attrs.getNamedItem("full").getNodeValue();
                String descr = attrs.getNamedItem("desc").getNodeValue();
                ArrayList<String> subs = new ArrayList<String>(3);
                subs.add(match);
                subs.add(full);
                subs.add(descr);
                list.add( subs);//.toString() );
            }   // end if: match
        }   // end for: each node
        
        return list;
    }
    

}
