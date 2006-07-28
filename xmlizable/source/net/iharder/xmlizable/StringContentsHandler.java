package net.iharder.xmlizable;

/**
 * Handles string contents. It interecepts the <tt>characters(...)</tt>
 * SAX2 event, makes a string out of it, and calls the 
 * {@link Object#setObject setObject(...)} method of its parent
 * {@link ObjectHandler}.
 * You would use it in the following situation. Suppose you encountered
 * an XML element for a string object: <tt>&lt;object class="java.lang.String"&gt;</tt>.
 * You would then setup this <tt>StringContentsHandler</tt> to receive subsequent
 * events, which should only be a <tt>characters(...)</tt> event, until
 * you receive the matching <tt>&lt;/object&gt;</tt> tag.
 * 
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
public class StringContentsHandler extends ObjectHandler 
{
    
    public StringContentsHandler()
    {   this.object = "";
    }   // end constructor

    public void characters( final char[] values, final int offset, final int length ) 
    throws org.xml.sax.SAXException 
    {   setObject( new String( values, offset, length ) );
    }   // end characters
    
    
}   // end StringHandler
