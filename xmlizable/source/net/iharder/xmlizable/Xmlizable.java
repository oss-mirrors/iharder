package net.iharder.xmlizable;


/**
 * Implementations of this interface provide a way to "Xml-ize" themselves into SAX2 events
 * and to rebuild themselves from SAX2 events. Unlike {@link java.io.Serializable} merely
 * implementing this interface is not enough. The necessary methods must be coded. If your
 * object can be encapsulated in a {@link java.util.Map} then you can "Xml-ize" your object
 * with the following sample code.
 *
 * <p>
 *
 * <pre><code>
 *     public void toXml( org.xml.sax.ContentHandler handler ) 
 *     throws org.xml.sax.SAXException, NotXmlizableException
 *     {    ParseUtilities.parseMap( myMap, handler );
 *     }   // end toXml
 *
 *     public org.xml.sax.ContentHandler fromXml() 
 *     {   return new MapHandler( myMap );
 *     }   // end fromXml
 * </code></pre>
 *
 *
 *
 * <p>
 * I am placing this code in the Public Domain. Do with it as you will.
 * This software comes with no guarantees or warranties but with
 * plenty of well-wishing instead!
 * Please visit <a href="http://iharder.net/xmlizable">http://iharder.net/xmlizable</a>
 * periodically to check for updates or to contribute improvements.
 * </p>
 *
 * @author Robert Harder
 * @author rharder@usa.net
 * @version 1.2
 */
public interface Xmlizable
{
    
    /**
     * Convert the object to XML via SAX2 events according to whatever
     * DTD or Schema the object likes. Your object may get its own
     * XML document or it may become part of a larger document in which
     * case the {@link org.xml.sax.ContentHandler#startDocument startDocument()}
     * and {@link org.xml.sax.ContentHandler#endDocument endDocument()}
     * calls will be ignored, if you call them.
     *
     * @param handler The SAX2 content handler that will receive SAX2 events.
     * @since 1.2
     */
    public abstract void toXml( org.xml.sax.ContentHandler handler )
    throws org.xml.sax.SAXException, NotXmlizableException;
    
    
    /**
     * Returns a SAX document handler that is cabable of receiving SAX events
     * and rebuilding the object. Your class should have a null constructor to
     * instantiate your object, and then this method will be called.
     *
     * <p>
     *
     * If your class's properties can be contained entirely in a
     * {@link java.util.Map} then you can use the {@link MapHandler}
     * with the constructor where you provide the {@link java.util.Map}
     * to build.
     *
     * @return A document handler that will rebuild the object from SAX events
     * @see MapHandler
     * @since 1.2
     */
    public abstract org.xml.sax.ContentHandler fromXml();

    
}   // end interface Xmlizable
