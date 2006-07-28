package net.iharder.xmlizable;

/**
 * This exception is thrown if an object is encountered that cannot be
 * converted to XML.
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
public class NotXmlizableException extends java.lang.Exception {

    /** Creates new NotXmlizableException */
    public NotXmlizableException() 
    {   super();
    }   // end constructor
    
    /** Creates new NotXmlizableException */
    public NotXmlizableException( String message )
    {   super( message );
    }   // end constructor

}   // end class NotXmlizableException
