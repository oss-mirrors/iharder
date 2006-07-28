
package net.iharder.xmlizable;

/**
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
 * @version 1.3
 */
public class Base64ContentsHandler extends StringContentsHandler
{

    /** Creates new Base64ContentsHandler */
    public Base64ContentsHandler() 
    {
    }
    
    public void setObject( Object base64EncodedString )
    {
        byte[] encoded = ((String)base64EncodedString).getBytes();
        byte[] decoded = Base64.decode( encoded, 0, encoded.length );
        Object obj = null;
        
        java.io.ByteArrayInputStream bais = null;
        java.io.ObjectInputStream     ois = null;
        try
        {   
            bais = new java.io.ByteArrayInputStream( decoded );
            ois  = new java.io.ObjectInputStream( bais );
            obj  = ois.readObject();
        }   // end try
        catch( java.io.IOException e )
        {   // Bummer.
            e.printStackTrace();
        }   // end catch
        catch( java.lang.ClassNotFoundException e )
        {   // Bummer.
            e.printStackTrace();
        }   // end catch
        finally
        {
            try{   ois.close(); }catch( Exception e ){}
            try{  bais.close(); }catch( Exception e ){}
        }   // end finally
        
        super.setObject( obj );
    }   // end setObject

}
