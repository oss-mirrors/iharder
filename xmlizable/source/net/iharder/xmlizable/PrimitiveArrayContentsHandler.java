package net.iharder.xmlizable;

/**
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
public class PrimitiveArrayContentsHandler extends StringContentsHandler 
{
    private String type;
    private int    length;
    
    public PrimitiveArrayContentsHandler( String type, int length )
    {
        super();
        
        this.type   = type;
        this.length = length;
    }   // end constructor

    
    public void setObject( Object commaSepString )
    {
        java.util.StringTokenizer stok = new java.util.StringTokenizer(
            (String)commaSepString, "," );
        String type = getType();
        int length  = getLength();
        Object obj  = null;
        
        
        
        if( BOOLEAN.equals( type ) || BOOLEAN_C.equals( type ) )
        {
            boolean[] arr = new boolean[ length ];
            for( int i = 0; stok.hasMoreTokens(); i++ ) 
                arr[i] = new Boolean( stok.nextToken() ).booleanValue();
            obj = arr;
        }   // end if: boolean
        
        else if( BYTE.equals( type ) || BYTE_C.equals( type ) )
        {
            byte[] arr = new byte[ length ];
            for( int i = 0; stok.hasMoreTokens(); i++ ) 
                arr[i] = new Byte( stok.nextToken() ).byteValue();
            obj = arr;
        }   // end if: byte
        
        else if( CHAR.equals( type ) || CHAR_C.equals( type ) )
        {
            char[] arr = new char[ length ];
            for( int i = 0; stok.hasMoreTokens(); i++ ) 
                arr[i] = new Character( stok.nextToken().charAt(0) ).charValue();
            obj = arr;
        }   // end if: char
        
        else if( SHORT.equals( type ) || SHORT_C.equals( type ) )
        {
            short[] arr = new short[ length ];
            for( int i = 0; stok.hasMoreTokens(); i++ ) 
                arr[i] = new Short( stok.nextToken() ).shortValue();
            obj = arr;
        }   // end if: short
        
        else if( INT.equals( type ) || INT_C.equals( type ) )
        {
            int[] arr = new int[ length ];
            for( int i = 0; stok.hasMoreTokens(); i++ ) 
                arr[i] = new Integer( stok.nextToken() ).intValue();
            obj = arr;
        }   // end if: int
        
        else if( LONG.equals( type ) || LONG_C.equals( type ) )
        {
            long[] arr = new long[ length ];
            for( int i = 0; stok.hasMoreTokens(); i++ ) 
                arr[i] = new Long( stok.nextToken() ).longValue();
            obj = arr;
        }   // end if: long
        
        else if( FLOAT.equals( type ) || FLOAT_C.equals( type ) )
        {
            float[] arr = new float[ length ];
            for( int i = 0; stok.hasMoreTokens(); i++ ) 
                arr[i] = new Float( stok.nextToken() ).floatValue();
            obj = arr;
        }   // end if: float
        
        else if( DOUBLE.equals( type ) || DOUBLE_C.equals( type ) )
        {
            double[] arr = new double[ length ];
            for( int i = 0; stok.hasMoreTokens(); i++ ) 
                arr[i] = new Double( stok.nextToken() ).doubleValue();
            obj = arr;
        }   // end if: double
        
        else
            obj = null;
        
        super.setObject( obj );
    }   // end setObject
    
    
    public String getType()
    {   return type;
    }   // end getType
    
    public int getLength()
    {   return length;
    }   // end getLength
    
}   // end StringHandler
