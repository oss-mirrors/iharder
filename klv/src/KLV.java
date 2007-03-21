
import java.util.*;


/**
 *
 *
 * <p>All <tt>getValue...</tt> methods will return up to the number
 * of bytes specified in the length fields ({@link #getLength}) unless
 * there are fewer bytes actually given than are intended. This is to make
 * the code more robust for reading corrupted data. Too few bytes: those
 * bytes are considered the value. Too many bytes: only up to {@link #getLength}
 * bytes are condidered the value.</p>
 *
 * @author Robert Harder
 * @author rob@iharder.net
 */
public class KLV {


    /** Indicates length field is one byte. Equal to decimal 1. */
    public final static int LENGTH_FIELD_ONE_BYTE = 1;
    
    /** Indicates length field is two bytes.  Equal to decimal 2. */
    public final static int LENGTH_FIELD_TWO_BYTES = 2;
    
    /** Indicates length field is four bytes.  Equal to decimal 4. */
    public final static int LENGTH_FIELD_FOUR_BYTES = 4;
    
    /** Indicates length field uses basic encoding rules (BER). Equal to decimal 8.  */
    public final static int LENGTH_FIELD_BER = 8;
    
    /** Default character set encoding to use is UTF-8. */
    public final static String DEFAULT_CHARSET_NAME = "UTF-8";
    
    
    /** Indicates key length of one byte. Equal to decimal 1. */
    public final static int KEY_LENGTH_ONE_BYTE = 1;
    
    /** Indicates key length of two bytes.  Equal to decimal 2. */
    public final static int KEY_LENGTH_TWO_BYTES = 2;
    
    /** Indicates key length of four bytes.  Equal to decimal 4. */
    public final static int KEY_LENGTH_FOUR_BYTES = 4;
    
    /** Indicates key length of 16 bytes.  Equal to decimal 16. */
    public final static int KEY_LENGTH_SIXTEEN_BYTES = 16;


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
     * The bytes from which the KLV set is made up.
     * May include irrelevant bytes so that byte arrays
     * can be passed around with a minimum of copying.
     */
    protected byte[] klvBytes;
    
    /**
     * Offset from beginning of <tt>byte[] klvBytes</tt>
     * where this KLV data begins.
     */
    protected int klvBytesOffset;
    
    
    /**
     * Total number of bytes from <tt>byte[] klvBytes</tt>
     * to consider as part of this KLV set.
     */
    protected int klvBytesLength;
    
    /**
     * The kind of length encoding used. Possible values are
     * the constants {@link #ONE_BYTE}, {@link #TWO_BYTES},
     * {@link #FOUR_BYTES}, or {@link #BER}
     */
    protected int lengthFieldEncoding;
    
    /**
     * Used to cache parsing of payload into further KLV sets.
     * This Map looks up a key length and returns another Map.
     * The second Map looks up a length field encoding type and
     * returns a final Map. The final Map looks up a shortKey
     * and returns a KLV.
     */
    protected Map<Integer,Map> subKLVCache = new HashMap<Integer,Map>();
    
    
    
    /**
     * Creates a KLV set with a one-byte key of zero, a length of zero, and no payload.
     */
    public KLV(){
        this.keyLength = 1;
        this.lengthFieldEncoding = LENGTH_FIELD_ONE_BYTE;
        this.klvBytes = new byte[]{ 0, 0 };
        this.klvBytesOffset = 0;
        this.klvBytesLength = this.klvBytes.length;
    }
    
    
    
    /**
     * <p>Creates a KLV set from the given byte array, the specified key length,
     * and the specified length field encoding. The byte array <tt>theBytes</tt>
     * is not copied or cloned, so be careful if you change the values within
     * the array.</p>
     *
     * <p>There is no checking to make sure that the bytes passed are consistent,
     * particularly in regards to the total number of bytes versus the length
     * specified in the length field. This is to make the code more robust
     * in reading corrupted data. Check {@link isConsistent} to verify that the data is
     * consistent.</p>
     *
     * @param theBytes The bytes that make up the entire KLV set
     * @param offset The offset from beginning of theBytes
     * @param length The number of bytes to use in theBytes
     * @param keyLength The number of bytes in the key.
     * @param lengthFieldEncoding The length field encoding type.
     * @see ONE_BYTE
     * @see TWO_BYTES
     * @see FOUR_BYTES
     * @see BER
     */
    public KLV( byte[] theBytes, int keyLength, int lengthFieldEncoding ){
        this( theBytes, 0, keyLength, lengthFieldEncoding );
    }
    
    
    
    
    /**
     * <p>Creates a KLV set from the given byte array, the given offset in that array,
     * the total length of the KLV set in the byte array, the specified key length,
     * and the specified length field encoding. The byte array <tt>theBytes</tt>
     * is not copied or cloned, so be careful if you change the values within
     * the array.</p>
     *
     * <p>There is no checking to make sure that the bytes passed are consistent,
     * particularly in regards to the total number of bytes versus the length
     * specified in the length field. This is to make the code more robust
     * in reading corrupted data. Check {@link isConsistent} to verify that the data is
     * consistent.</p>
     *
     * @param theBytes The bytes that make up the entire KLV set
     * @param offset The offset from beginning of theBytes
     * @param keyLength The number of bytes in the key.
     * @param lengthFieldEncoding The length field encoding type.
     * @see ONE_BYTE
     * @see TWO_BYTES
     * @see FOUR_BYTES
     * @see BER
     */
    public KLV( byte[] theBytes, int offset, int keyLength, int lengthFieldEncoding ){
        if( theBytes == null )      throw new IllegalArgumentException( "KLV byte array must not be null." ); 
        if( theBytes.length < 2 )   throw new IllegalArgumentException( "KLV byte array must be at least two bytes long." ); 
        if( offset < 0 )            throw new IllegalArgumentException( "Offset must be non-negative." ); 
        if( keyLength < 1 )         throw new IllegalArgumentException( "Key length must be positive: " + keyLength );
        
        this.klvBytes = theBytes;
        this.klvBytesOffset = offset;
        this.keyLength = keyLength;
        this.lengthFieldEncoding = lengthFieldEncoding;
        
    }
    
    
    /**
     * Return a KLV that is in the payload after 
     * parsing the payload as if it contains more KLV sets with the given
     * key lengths and length field encodings. The parsing is cached
     * to speed subsequent calls.
     *
     * @param shortKey One- to four-byte key for the sub-KLV element
     * @param keyLength Length of shortKey
     * @param lengthFieldEncoding Flag indicating encoding type
     * @return Matching KLV or null if not found
     */
    public KLV getKLV( int shortKey, int keyLength, int lengthFieldEncoding ){
        if( keyLength < 1 ) throw new IllegalArgumentException( "Key length must be positive." );
        
        // Map exists for this keyLength?
        Map<Integer,Map> k2l = this.subKLVCache.get( keyLength );
        if( k2l == null ){
            k2l = new HashMap<Integer,Map>();
            this.subKLVCache.put( keyLength, k2l );
        }   // end if: null
        
        // Map exists for length field encoding?
        Map<Integer,KLV> l2klv = k2l.get( lengthFieldEncoding );
        if( l2klv == null ){
            int offset = getValueOffset();      // Where payload begins
            int advertisedLength = getLength(); // Determine how many bytes should be parsed
            int availableLength = this.klvBytes.length - offset;
            int actualLength = availableLength > advertisedLength ? advertisedLength : availableLength;
            l2klv = parseBytes( this.klvBytes, offset, actualLength, keyLength, lengthFieldEncoding );
            k2l.put( lengthFieldEncoding, l2klv );
        }   // end if: null
        
        // KLV exists?
        return l2klv.get( shortKey );
    }   // end getKLV
    
    
    
    /**
     * Parses the payload as if it contains KLV sets and returns
     * a Map connecting shortKeys to KLVs.
     *
     * @param keyLength Length of shortKey
     * @param lengthFieldEncoding Flag indicating encoding type
     * @return Map of keys to KLVs
     */
    protected Map<Integer,KLV> parseBytes( byte[] bytes, int offset, int length, int keyLength, int lengthFieldEncoding ){
        Map<Integer,KLV> map = new HashMap<Integer,KLV>();
        
        // HARD WORK BEGINS HERE
        int currentPos = offset;
        while( currentPos < length ){
            try{
                KLV klv = new KLV( bytes, currentPos, keyLength, lengthFieldEncoding );
                currentPos = klv.getValueOffset() + klv.getLength();
                map.put( klv.getShortKey(), klv );
            } catch( Exception exc ){
                // Stop trying for more?
                System.err.println("Stopped parsing with exception: " + exc.getMessage() );
                break;
            }   // end catch
            
        }   // end while
        
        
        return map;
    }   // end parsePayload
    

    
    /**
     * Returns the length of the key.
     *
     * @return length of key.
     */
    public int getKeyLength(){
        return this.keyLength;
    }
    
    

    /**
     * Returns the first four bytes of the key as an int. If the key
     * has fewer than four bytes, then as many bytes as are available
     * will be used.
     *
     * @return the key
     */
    public int getShortKey(){
        int shortKey = 0;
        for( int i = 0; i < keyLength; i++ )
            shortKey |= (klvBytes[klvBytesOffset+i] & 0xFF) << (keyLength*8 - i*8 - 8);
        
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
        System.arraycopy(this.klvBytes,klvBytesOffset, key,0,this.keyLength);
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
                length = klvBytes[klvBytesOffset + keyLength] & 0xFF;
                break;
            
            // Unsigned integer, two bytes long, big endian.    
            case LENGTH_FIELD_TWO_BYTES: 
                length = (klvBytes[klvBytesOffset + keyLength] & 0xFF) << 8
                       | (klvBytes[klvBytesOffset + keyLength + 1] & 0xFF);
                break;
                
            // Unsigned integer, four bytes long, big endian.    
            case LENGTH_FIELD_FOUR_BYTES: 
                length = (klvBytes[klvBytesOffset + keyLength] & 0xFF) << 24 
                       | (klvBytes[klvBytesOffset + keyLength + 1] & 0xFF) << 16
                       | (klvBytes[klvBytesOffset + keyLength + 2] & 0xFF) << 8
                       | (klvBytes[klvBytesOffset + keyLength + 3] & 0xFF);
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
                int berField = klvBytes[klvBytesOffset + keyLength] & 0xFF;
                if( (berField & 0x80) == 0 ) return berField; // Short BER form
                else{
                    int berLength = berField & 0x7F; // Low seven bits
                    if( berLength > 4 ) throw new UnsupportedOperationException( "BER length fields greater than four bytes are not supported." );
                    for( int i = 0; i < berLength; i++ )
                        length |= (klvBytes[klvBytesOffset + keyLength + 1 + i] & 0xFF) << (berLength*8 - i*8 - 8);
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
        int offset = this.klvBytesOffset + this.keyLength;
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
                offset++; // Always at least one BER byte
                int berField = klvBytes[ keyLength ] & 0xFF;
                if( (berField & 0x80) == 0 ) break; // Short BER form
                else{
                    int berLength = berField & 0x7F; // Low seven bits
                    offset += berLength;
                }   // end else: long BER form
                break;
            default:
                throw new IllegalStateException( "Unknown length field encoding flag: " + this.lengthFieldEncoding );
        }   // end switch
        return offset;
    }
    
    
    
    /**
     * <p>Returns up to the number of bytes specified in the length 
     * fields ({@link #getLength}) unless there are fewer bytes actually 
     * given than are intended. This is to make the code more robust for 
     * reading corrupted data. 
     * Too few bytes: whatever bytes are available are returned. 
     * Too many bytes: only up to {@link #getLength} bytes are returned.</p>
     *
     * <p>The bytes are copied from the original array.</p>
     *
     * @return the value
     */
    public byte[] getValue(){
        int offset = getValueOffset();
        int advertisedLength = getLength();
        int actualLength = this.klvBytes.length - offset;
        byte[] value = new byte[ actualLength > advertisedLength ? advertisedLength : actualLength ];
        System.arraycopy(this.klvBytes,offset, value,0,value.length);
        return value;
    }
    
    
    
    /**
     * Returns up to the first four bytes of the value as a four-byte int.
     * Be careful that Java assumes the highest bit (the 32nd) is a sign
     * bit, but that's not necessarily what is meant to be represented.
     *
     * @return the value as an int
     */
    public int getValueAsInt(){
        byte[] bytes = getValue();
        int value = 0;
        int length = bytes.length;
        int shortLen = length < 4 ? length : 4;
        for( int i = 0; i < shortLen; i++ )
            value |= (bytes[i] & 0xFF) << (shortLen*8 - i*8 - 8);
        return value;
    }   // end getValueAsInt
    
    
    
    /**
     * Returns up to the first eight bytes of the value as an eight-byte long.
     * Be careful that Java assumes the highest bit (the 64th) is a sign
     * bit, but that's not necessarily what is meant to be represented.
     *
     * @return the value as a long
     */
    public long getValueAsLong(){
        byte[] bytes = getValue();
        long value = 0;
        int length = bytes.length;
        int shortLen = length < 8 ? length : 8;
        for( int i = 0; i < shortLen; i++ )
            value |= (long)(bytes[i] & 0xFF) << (shortLen*8 - i*8 - 8);
        return value;
    }   // end getValueAsLong
    
    
    /**
     * Returns the value as a String using KLV's default character set
     * as defined by {@link #DEFAULT_CHARSET_NAME} or the computer's default
     * charset if that is not available.
     *
     * @return value as a string
     */
    public String getValueAsString(){
        try{
            return getValueAsString( DEFAULT_CHARSET_NAME );
        } catch( java.io.UnsupportedEncodingException exc ){
            return new String( getValue() );
        }   // end catch
    }   // end getValueAsString
        
    
    
    /**
     * Return the value as a String, interpreted with given encoding.
     *
     * @return value as String.
     */
    public String getValueAsString( String charsetName ) throws java.io.UnsupportedEncodingException{
        return new String( getValue(), charsetName );
    }
    
    
    
    /**
     * Returns the underlying byte array that represents this KLV set.
     * Some of the bytes may not be part of the KLV set.
     * Be sure to check {@link #getBytesOffset}.
     *
     * @return all the bytes
     * @see #getBytesOffset
     * @see #getBytesLength
     */
    public byte[] getBytes(){
        return this.klvBytes;
    }
    
    
    /**
     * Returns the number of bytes offset from {@link #getBytes}
     * where this KLV set actually begins.
     * Be sure to check {@link #getBytes}.
     *
     * @return offset of bytes
     * @see #getBytes
     */
    public int getBytesOffset(){
        return this.klvBytesOffset;
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
