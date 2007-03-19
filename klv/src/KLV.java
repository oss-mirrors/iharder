//
//  KLV.java
//  KLV
//
//  Created by Robert Harder on 3/17/07.
//  Copyright (c) 2007 __MyCompanyName__. All rights reserved.
//
import java.util.*;

public class KLV {


    /** Indicates length field is one byte. Equal to decimal 1. */
    public final static int LENGTH_FIELD_ONE_BYTE = 1;
    
    /** Indicates length field is two bytes.  Equal to decimal 2. */
    public final static int LENGTH_FIELD_TWO_BYTES = 2;
    
    /** Indicates length field is four bytes.  Equal to decimal 4. */
    public final static int LENGTH_FIELD_FOUR_BYTES = 4;
    
    /** Indicates length field uses basic encoding rules (BER). Equal to decimal 8.  */
    public final static int LENGTH_FIELD_BER = 8;


    /**
     * Number of bytes in key. 
     */
    protected int keyLength;
    
    /**
     * The key, if the key is one to four bytes.
     */
    //protected int shortKey;

    /**
     * The key, if the key is longer than four bytes.
     */
    //protected byte[] longKey;
    
    /**
     * The bytes that make up the entire KLV set.
     */
    protected byte[] klvBytes;
    
    /**
     * The kind of length encoding used. Possible values are
     * the constants {@link #ONE_BYTE}, {@link #TWO_BYTES},
     * {@link #FOUR_BYTES}, or {@link #BER}
     */
    protected int lengthFieldEncoding;
    
    
    
    /**
     * Creates a KLV set with a one-byte key of zero, a length of zero, and no payload.
     */
    public KLV(){
        this.keyLength = 1;
        this.lengthFieldEncoding = LENGTH_FIELD_ONE_BYTE;
        this.klvBytes = new byte[]{ 0, 0 };
    }
    
    /**
     * Creates a KLV set from the given byte array, the specified key length,
     * and the specified length field encoding. The byte array <tt>theBytes</tt>
     * is not copied or cloned, so be careful if you change the values within
     * the array.
     *
     * @param theBytes The bytes that make up the entire KLV set
     * @param keyLength The number of bytes in the key.
     * @param lengthFieldEncoding The length field encoding type.
     * @see ONE_BYTE
     * @see TWO_BYTES
     * @see FOUR_BYTES
     * @see BER
     */
    public KLV( byte[] theBytes, int keyLength, int lengthFieldEncoding ){
        if( theBytes == null )      throw new IllegalArgumentException( "KLV byte array must not be null." ); 
        if( theBytes.length < 2 )   throw new IllegalArgumentException( "KLV byte array must be at least two bytes long." ); 
        if( keyLength < 1 )         throw new IllegalArgumentException( "Key length must be positive: " + keyLength );
        
        this.klvBytes = theBytes;
        this.keyLength = keyLength;
        this.lengthFieldEncoding = lengthFieldEncoding;
        
    }


    /**
     * Returns the key, if the key length is four bytes or less.
     *
     * @return the key
     * @throw IllegalStateException if the key length is greater than four.
     */
    public int getShortKey(){
        if( this.keyLength > 4 ) throw new IllegalStateException( "Key is too long to return as an int: " + this.keyLength + " bytes." );
        
        int shortKey = 0;
        for( int i = 0; i < keyLength; i++ )
            shortKey |= (klvBytes[ i ] & 0xFF) << (keyLength*8 - i*8 - 8);
        
        return shortKey;
    }   // end getShortKey
    
    
    /**
     * Returns a byte array representing the key. This is a copy of the bytes
     * from the original byte set. Unlike {@link #getShortKey} this method
     * can return a key regardless of the key length.
     *
     * @return the key
     */
    public byte[] getLongKey(){
        byte[] key = new byte[this.keyLength];
        System.arraycopy(this.klvBytes,0, key,0,this.keyLength);
        return key;
    }   // end getLongKey
    
    
    /**
     * Returns the length of the value (payload) in bytes.
     */
    public int getLength(){
        int length = 0;
        switch( this.lengthFieldEncoding ){
            
            // Unsigned integer, one byte long.
            case LENGTH_FIELD_ONE_BYTE: 
                length = klvBytes[ keyLength ] & 0xFF;
                break;
            
            // Unsigned integer, two bytes long, big endian.    
            case LENGTH_FIELD_TWO_BYTES: 
                length = (klvBytes[ keyLength ] & 0xFF) << 8
                       | (klvBytes[ keyLength+1 ] & 0xFF);
                break;
                
            // Unsigned integer, four bytes long, big endian.    
            case LENGTH_FIELD_FOUR_BYTES: 
                length = (klvBytes[ keyLength ] & 0xFF) << 24 
                       | (klvBytes[ keyLength + 1 ] & 0xFF) << 16
                       | (klvBytes[ keyLength + 2 ] & 0xFF) << 8
                       | (klvBytes[ keyLength + 3 ] & 0xFF);
                break;
                
            // Short BER form: If high bit is not set, then  
            // use the byte to determine length of payload.
            // Long BER form: If high bit is set (0x80), 
            // then use low seven bits to determine how many 
            // bytes that follow are themselves an unsigned 
            // integer specifying the length of the payload. 
            // Using more than four bytes to specify the length 
            // is not supported in this code, though it's not 
            // exactly illegal KLV notation either.
            case LENGTH_FIELD_BER:
                int berField = klvBytes[ keyLength ] & 0xFF;
                if( (berField & 0x80) == 0 ) return berField; // Short BER form
                else{
                    int berLength = berField & 0x7F; // Low seven bits
                    if( berLength > 4 ) throw new UnsupportedOperationException( "BER length fields greater than four bytes are not supported." );
                    for( int i = 0; i < berLength; i++ )
                        length |= (klvBytes[ keyLength + 1 + i ] & 0xFF) << (berLength*8 - i*8 - 8);
                    break;
                }   // end else: long BER form
            default:
                throw new IllegalStateException( "Unknown length field encoding flag: " + this.lengthFieldEncoding );
        }   // end switch
        
        return length;
    }   // end getLength
    
    
    /**
     * Determine how far into the bytes the value starts.
     */
    protected int getValueOffset(){
        int offset = this.keyLength;
        switch( this.lengthFieldEncoding ){
            
            // Unsigned integer, one byte long.
            case LENGTH_FIELD_ONE_BYTE: 
                offset += 1;
                break;
            
            // Unsigned integer, two bytes long, big endian.    
            case LENGTH_FIELD_TWO_BYTES: 
                offset += 2;
                break;
                
            // Unsigned integer, four bytes long, big endian.    
            case LENGTH_FIELD_FOUR_BYTES: 
                offset += 4;
                break;
                
            // Short BER form: 1 extra byte
            // Long BER form: variable extra bytes
            case LENGTH_FIELD_BER:
                int berField = klvBytes[ keyLength ] & 0xFF;
                if( (berField & 0x80) == 0 ) offset += 1; // Short BER form
                else{
                    int berLength = berField & 0x7F; // Low seven bits
                    offset += berLength;
                    break;
                }   // end else: long BER form
            default:
                throw new IllegalStateException( "Unknown length field encoding flag: " + this.lengthFieldEncoding );
        }   // end switch
        return offset;
    }
    
    
    
    /**
     * Returns the value (payload) as a byte array.
     * The bytes are copied from the original array.
     *
     * @return the value
     */
    public byte[] getValue(){
        int length = getLength();
        byte[] value = new byte[length];
        System.arraycopy(this.klvBytes,getValueOffset(), value,0,value.length);
        return value;
    }
    
    
    
    /**
     * Returns the underlying byte array that represents this KLV set.
     *
     * @return all the bytes
     */
    public byte[] toBytes(){
        return this.klvBytes;
    }
    
    
/* ********  O B J E C T   O V E R R I D E  ******** */    
    
    
	
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        
        // Key
        sb.append("Key=");
        if( keyLength <= 4 ) sb.append( getShortKey() );
        else{ sb.append( "(long key. Code pending)" ); }
        
        // Length
        sb.append(", Length=");
        sb.append( getLength() );
        
        // Value
        
        return sb.toString();
    }
	
	
}
