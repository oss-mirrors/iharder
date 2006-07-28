package net.iharder.xmlizable;

/**
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
public class ArrayArrayContentsHandler extends ObjectHandler implements XmlConstants
{
    
    private Object[] arrArr;
    int nextIndex = 0;
    
    public ArrayArrayContentsHandler()
    {   
        this( new Object[0] );
    }   // end constructor
    
    
    /**
     * Creates an ArrayArrayContentsHandler that will add data from the
     * SAX2 events into the passed array.
     *
     * @param arrArr The array to build
     * @since 1.3
     */
    public ArrayArrayContentsHandler(Object[] arrArr)
    {   
        setArrayArray( arrArr );
    }   // end constructor
    
    
    /**
     *
     */
    public void setArrayArray( Object[] arrArr )
    {   
        setObject( this.arrArr = arrArr );
    }   // end setArrayArray
    
    
    public Object[] getArrayArray()
    {   return arrArr;
    }   // end getArrayArray
    
    
/* ********  D O C U M E N T   H A N D L E R   M E T H O D S  ******** */    
    
    

    public void startElement( 
    final String namespaceURI, final String localName, 
    final String qName, final org.xml.sax.Attributes atts )
    throws org.xml.sax.SAXException
    {
        // Is another handler in charge now?
        if( getAltHandler() != null )
        {   // Another handler is in charge.
            getAltHandlerElementStack().push( localName );
            getAltHandler().startElement( namespaceURI, localName, qName, atts );
        }   // end if: another handler has taken over for a while
        
        // Else is it one of our elements?
        // If so, send the SAX event again, so the object
        // handler handles it properly.
        else if( NAMESPACE.equals( namespaceURI ) )
        {
            setAltHandler( new ObjectHandler() );
            resetAltHandlerElementStack().push( localName );
            getAltHandler().startElement( namespaceURI, localName, qName, atts );
        }   // end else if: our namespace
        
        // Else set up default handler to ignore the element.
        else 
        {
            setAltHandler( new org.xml.sax.helpers.DefaultHandler() );
            resetAltHandlerElementStack().push( localName );
            getAltHandler().startElement( namespaceURI, localName, qName, atts );
        }   // end else: some other element
    }   // end startElement
    
    

    public void endElement( 
    final String namespaceURI, final String localName, final String qName) 
    throws org.xml.sax.SAXException
    {
        // Are we the primary handler?
        if( getAltHandler() != null )
        {
            getAltHandlerElementStack().pop();
            getAltHandler().endElement( namespaceURI, localName, qName );
            if( getAltHandlerElementStack().isEmpty() )
            {   
                // Get the object if there's an object to be returned
                // that is, it's not the xmlizable type. If it was
                // xmlizable, then it was added to the collection
                // at the beginning.
                if( getAltHandler() instanceof ObjectHandler )
                {
                    Object obj = ((ObjectHandler)getAltHandler()).getObject();

                    // Add it to the array
                    getArrayArray()[nextIndex++] = obj;
                    
                }   // end if: handler is an ObjectHandler
                
                clearAltHandlerElementStack();
                clearAltHandler();
            }   // end if: stack empty
            //else getAltHandler().endElement( namespaceURI, localName, qName );
        }   // end if: altHandler not null
        
    }   // end endElement
 
    
}   // end class ArrayArrayContentsHandler
