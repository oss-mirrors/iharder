
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
    
    
    
    
    /** Indicates key length of one byte. Equal to decimal 1. */
    public final static int KEY_LENGTH_ONE_BYTE = 1;
    
    /** Indicates key length of two bytes.  Equal to decimal 2. */
    public final static int KEY_LENGTH_TWO_BYTES = 2;
    
    /** Indicates key length of four bytes.  Equal to decimal 4. */
    public final static int KEY_LENGTH_FOUR_BYTES = 4;
    
    /** Indicates key length of 16 bytes.  Equal to decimal 16. */
    public final static int KEY_LENGTH_SIXTEEN_BYTES = 16;
    
    
    
    /** Default character set encoding to use is UTF-8. */
    public final static String DEFAULT_CHARSET_NAME = "UTF-8";


    /**
     * Number of bytes in key. 
     */
    protected int keyLength;
    
    
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
     * The default key length to use when adding KLV sets
     * with {@link addKLV}.
     * This can be overridden with the expanded {@link addKLV}
     * method, but do so at your own risk: having sub KLV sets
     * with different key lengths and encoding types will
     * generally lead to confusion and lost data.
     * Defaults to four bytes.
     *
     * @see #setSubKeyLengthDefault
     * @see #getSubKeyLengthDefault
     */
    protected int subKeyLengthDefault = KLV.KEY_LENGTH_FOUR_BYTES;
    
    
    /**
     * The default length field encoding to use when adding KLV sets
     * with {@link addKLV}.
     * This can be overridden with the expanded {@link addKLV}
     * method, but do so at your own risk: having sub KLV sets
     * with different key lengths and encoding types will
     * generally lead to confusion and lost data.
     * Defaults to Basic Encoding Rules (BER) (decimal 8).
     *
     * @see #setSubLengthFieldEncodingDefault
     * @see #getSubLengthFieldEncodingDefault
     */
    protected int subLengthFieldEncodingDefault = KLV.LENGTH_FIELD_BER;
    
    
/* ********  C O N S T R U C T O R S  ******** */    
    
    
    
    /**
     * Creates a KLV set with a one-byte key of zero, a length of zero, and no payload.
     * Other constructors in sub classes do not need to start with this constructor.
     */
    public KLV(){
        this.keyLength = 1;
        this.lengthFieldEncoding = LENGTH_FIELD_ONE_BYTE;
        this.klvBytes = new byte[]{ 0, 0 };
        this.klvBytesOffset = 0;
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
     * in reading corrupted data. Check {@link #isConsistent} to verify that the data is
     * consistent.</p>
     *
     * @param theBytes The bytes that make up the entire KLV set
     * @param keyLength The number of bytes in the key.
     * @param lengthFieldEncoding The length field encoding type.
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
     * in reading corrupted data. Check {@link #isConsistent} to verify that the data is
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
        if( theBytes.length < 2 )   throw new IllegalArgumentException( "KLV byte array must be at least two bytes long:" + theBytes.length ); 
        if( offset < 0 )            throw new IllegalArgumentException( "Offset must be non-negative: " + offset ); 
        if( keyLength < 1 )         throw new IllegalArgumentException( "Key length must be positive: " + keyLength );
        
        this.klvBytes = theBytes;
        this.klvBytesOffset = offset;
        this.keyLength = keyLength;
        this.lengthFieldEncoding = lengthFieldEncoding;
    }   // end constructor
    
    
    
    
    
/* ********  P U B L I C   G E T   M E T H O D S  ******** */    
    
    
    /**
     * Return a KLV that is in the payload after 
     * parsing the payload as if it contains more KLV sets with the given
     * key lengths and length field encodings.
     * This may be useful if you have unknown data and have to try
     * a few parameters to find the right set.
     * The parsing is cached to speed subsequent calls.
     *
     * @param shortKey One- to four-byte key for the sub-KLV element
     * @param keyLength Length of shortKey
     * @param lengthFieldEncoding Flag indicating encoding type
     * @return Matching KLV or null if not found
     */
    public KLV getKLV( int shortKey, int keyLength, int lengthFieldEncoding ){
        if( keyLength < 1 || keyLength > 4 ) throw new IllegalArgumentException( "Key length must be from one to four: " + keyLength );
        if( lengthFieldEncoding != KLV.LENGTH_FIELD_ONE_BYTE &&
            lengthFieldEncoding != KLV.LENGTH_FIELD_TWO_BYTES &&
            lengthFieldEncoding != KLV.LENGTH_FIELD_FOUR_BYTES &&
            lengthFieldEncoding != KLV.LENGTH_FIELD_BER )
                throw new IllegalArgumentException( "Invalid length field encoding flag: " + lengthFieldEncoding );
        
        // Map exists for this keyLength?
        Map<Integer,Map> k2l = this.subKLVCache.get( keyLength );
        if( k2l == null ){
            k2l = new HashMap<Integer,Map>();
            this.subKLVCache.put( keyLength, k2l );
        }   // end if: null
        
        // Map exists for length field encoding?
        Map<Integer,KLV> l2klv = k2l.get( lengthFieldEncoding );
        if( l2klv == null ){
            int offset = getValueOffset();              // Where payload begins
            int valueLength = getActualValueLength();   // How many bytes in payload
            l2klv = parseBytes( this.klvBytes, offset, valueLength, keyLength, lengthFieldEncoding );
            k2l.put( lengthFieldEncoding, l2klv );
        }   // end if: null
        
        // KLV exists?
        return l2klv.get( shortKey );
    }   // end getKLV
    
    
    /**
     * Return a KLV that is in the payload after 
     * parsing the payload as if it contains more KLV sets with the 
     * default sub key lengths and default sub length field encodings.
     * The parsing is cached to speed subsequent calls.
     *
     * @param shortKey One- to four-byte key for the sub-KLV element
     * @return Matching KLV or null if not found
     * @see #setSubKeyLengthDefault
     * @see #setSubLengthFieldEncodingDefault
     */
    public KLV getKLV( int shortKey ){        
        return getKLV( shortKey, this.subKeyLengthDefault, this.subLengthFieldEncodingDefault );
    }   // end getKLV
    
    

    
    /**
     * Returns the length of the key.
     *
     * @return length of key.
     */
    public int getKeyLength(){
        return this.keyLength;
    }   // end getKeyLength
    
    
    
    
    /**
     * Returns the default key lengths used by possible
     * KLV sets within the overall payload.
     *
     * @return sub key length
     * @see #setSubKeyLengthDefault
     */
    public int getSubKeyLengthDefault(){
        return this.subKeyLengthDefault;
    }   // end getSubKeyLengthDefault
    
    

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
     * Returns the length field encoding flag.
     *
     * @return length field encoding flag
     */
    public int getLengthFieldEncoding(){
        return this.lengthFieldEncoding;
    }
    
    
    
    
    
    /**
     * Returns the default length field encoding used by any sub KLV sets 
     * that might be stored in the overall payload. 
     *
     * @see #setSubLengthFieldEncodingDefault
     */
    public int getSubLengthFieldEncodingDefault(){
        return this.subLengthFieldEncodingDefault;
    }
    
    
    
    
    /**
     * Returns the declared length of the value (payload) in bytes.
     * It's possible that the payload actually has fewer bytes if
     * not enough bytes were passed in at instantiation. 
     *
     * @return Length of payload as declared in the length field
     * @see getActualValueLength
     */
    public int getDeclaredValueLength(){
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
    }   // end getDeclaredValueLength
    
    
    
    /**
     * Returns the actual length of the value (payload) in bytes.
     * It's possible that the payload actually has fewer bytes 
     * than are declared in the length field if not enough bytes
     * were passed in during instantiation. This flexibility allows
     * for possibly-corrupted data to still be read as well as possible.
     *
     * @return Actual length of payload
     * @see getDeclaredValueLength
     */
    public int getActualValueLength(){
        int declaredLength = getDeclaredValueLength();
        int availableLength = this.klvBytes.length - getValueOffset();
        return declaredLength < availableLength ? declaredLength : availableLength;
    }   // end getDeclaredValueLength
    
    
    
    
    /**
     * Determine how far into the byte array the value starts.
     *
     * @return offset to the first byte of the payload
     */
    public int getValueOffset(){
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
        int valueLength = getActualValueLength();
        byte[] value = new byte[ valueLength ];
        System.arraycopy(this.klvBytes,offset, value,0,value.length);
        return value;
    }
    
    
    
    /**
     * Returns up to the first two bytes of the value as a two-byte char.
     * Be careful that Java assumes the highest bit (the 16th) is a sign
     * bit, but that's not necessarily what is meant to be represented.
     *
     * @return the value as a char
     */
    public char getValueAsChar(){
        byte[] bytes = getValue();
        char value = 0;
        int length = bytes.length;
        int shortLen = length < 2 ? length : 2;
        for( int i = 0; i < shortLen; i++ )
            value |= (bytes[i] & 0xFF) << (shortLen*8 - i*8 - 8);
        return value;
    }   // end getValueAsInt
    
    
    
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
    
    
    
/* ********  S E T   M E T H O D S  ******** */
    
    
    /**
     * Sets the default key lengths used by possible
     * KLV sets within the overall payload.
     *
     * @param subKeyLength The default sub key length to use for KLV sets within
     * @return <tt>this</tt> to aid in stringing commands together
     * @see #getSubKeyLengthDefault
     */
    public KLV setSubKeyLengthDefault( int subKeyLength ){
        if( subKeyLength < 1 || subKeyLength > 4 )
            throw new IllegalArgumentException( "Sub key length must be from one to four: " + subKeyLength );
        
        this.subKeyLengthDefault = subKeyLength;        
        return this;
    }
    
    
    /**
     * Sets the default length field encoding used by possible
     * KLV sets within the overall payload.
     *
     * @param subKeyLength The default sub key length to use for KLV sets within
     * @return <tt>this</tt> to aid in stringing commands together
     * @see #getSubLengthFieldEncodingDefault
     */
    public KLV setSubLengthFieldEncodingDefault( int subLengthFieldEncoding ){
        if( subLengthFieldEncoding != KLV.LENGTH_FIELD_ONE_BYTE &&
            subLengthFieldEncoding != KLV.LENGTH_FIELD_TWO_BYTES &&
            subLengthFieldEncoding != KLV.LENGTH_FIELD_FOUR_BYTES &&
            subLengthFieldEncoding != KLV.LENGTH_FIELD_BER )
                throw new IllegalArgumentException( "Invalid sub length field encoding flag: " + subLengthFieldEncoding );
        
        this.subLengthFieldEncodingDefault = subLengthFieldEncoding;        
        return this;
    }
    
    
    
    
    
    
    
/* ********  A D D   M E T H O D S  ******** */
    
    
    
    
    
    /**
     * Adds a sub KLV set with the given key and the
     * single byte of data as the payload
     * using the default key length
     * and default length field encoding.
     *
     * @param key The key for the data
     * @param data The data in the payload
     * @return <tt>this</tt>, to aid in stringing commands together.
     */
    public KLV addKLV( int key, byte data ){
        return addKLV( key, new byte[]{ data } );
    }   // end addKLV
    
    
    
    /**
     * Adds a sub KLV set with the given key and the
     * single char (two bytes) of data as the payload
     * using the default key length
     * and default length field encoding.
     *
     * @param key The key for the data
     * @param data The data in the payload
     * @return <tt>this</tt>, to aid in stringing commands together.
     */
    public KLV addKLV( int key, char data ){
        return addKLV( key, new byte[]{ (byte)(data >> 8), (byte)data } );
    }   // end addKLV
    
    
    
    /**
     * Adds a sub KLV set with the given key and the
     * single int (four bytes) of data as the payload
     * using the default key length
     * and default length field encoding.
     *
     * @param key The key for the data
     * @param data The data in the payload
     * @return <tt>this</tt>, to aid in stringing commands together.
     */
    public KLV addKLV( int key, int data ){
        return addKLV( key, new byte[]{ 
            (byte)(data >> 24), 
            (byte)(data >> 16), 
            (byte)(data >>  8), 
            (byte)data } );
    }   // end addKLV
    
    
    
    /**
     * Adds a sub KLV set with the given key and the
     * single long (eight bytes) of data as the payload
     * using the default key length
     * and default length field encoding.
     *
     * @param key The key for the data
     * @param data The data in the payload
     * @return <tt>this</tt>, to aid in stringing commands together.
     */
    public KLV addKLV( int key, long data ){
        return addKLV( key, new byte[]{ 
            (byte)(data >> 56), 
            (byte)(data >> 48), 
            (byte)(data >> 40), 
            (byte)(data >> 32), 
            (byte)(data >> 24), 
            (byte)(data >> 16), 
            (byte)(data >>  8), 
            (byte)data } );
    }   // end addKLV
    
    
    /**
     * Adds a sub KLV set with the given key and the
     * string of data as the payload
     * using the default key length
     * and default length field encoding.
     * If data is <tt>null</tt>, then the corresponding
     * payload length will be zero.
     * The default charset (UTF-8) will be used unless
     * that is not supported in which case the current
     * computer's default charset will be used.
     *
     * @param key The key for the data
     * @param data The data in the payload
     * @return <tt>this</tt>, to aid in stringing commands together.
     */
    public KLV addKLV( int key, String data ){
        if( data == null ){
            return addKLV( key, new byte[0] );
        }   // end if: null
        else {
            try{
                return addKLV( key, data.getBytes(KLV.DEFAULT_CHARSET_NAME) );
            } catch( java.io.UnsupportedEncodingException exc ){
                return addKLV( key, data.getBytes() );
            }   // end catch
        }   // end else: not null
    }   // end addKLV
    
    
    /**
     * Adds a KLV set to the overall payload using the given
     * key, default sub key length, default BER length encoding, and the provided data.
     * Underlying byte array is copied and replaced.
     *
     * @param key The key for the data
     * @param data The data in the payload
     * @return <tt>this</tt>, to aid in stringing commands together.
     */
    public KLV addKLV( int key, byte[] data ){
        return addKLV( key, this.subKeyLengthDefault, this.subLengthFieldEncodingDefault, data );
    }   // end addKLV
    
    
    /**
     * Adds a KLV set to the overall payload using the given
     * key, default sub key length, default BER length encoding, and the provided data.
     * Underlying byte array is copied and replaced.
     *
     * @param key The key for the data
     * @param data The data in the payload
     * @return <tt>this</tt>, to aid in stringing commands together.
     */
    public KLV addKLV( int subKey, int subKeyLength, int subLengthFieldEncoding, byte[] subData ){
        if( subKeyLength < 0 || subKeyLength > 4 ) throw new IllegalArgumentException( "Key length must be from one to four bytes: " + subKeyLength );
        if( subLengthFieldEncoding != KLV.LENGTH_FIELD_ONE_BYTE &&
            subLengthFieldEncoding != KLV.LENGTH_FIELD_TWO_BYTES &&
            subLengthFieldEncoding != KLV.LENGTH_FIELD_FOUR_BYTES &&
            subLengthFieldEncoding != KLV.LENGTH_FIELD_BER )
                throw new IllegalArgumentException( "Invalid length field encoding flag: " + subLengthFieldEncoding );
        
        // Old outer data
        int oldOuterLength = getActualValueLength();    // Old payload length
        int oldValueOffset = getValueOffset();          // Old value offset
        int outerLengthFieldOffset = this.klvBytesOffset + this.keyLength; // Old length field starts here
        int oldOuterLengthFieldLength = oldValueOffset - outerLengthFieldOffset; // Length of old length field encoding
        
        // Bytes for inner length encoding
        byte[] subLengthBytes = makeLengthField( subLengthFieldEncoding, subData.length );
        int subOverallLength = subKeyLength + subLengthBytes.length + subData.length;
        
        // Bytes for outer length field -- drives a lot of other offset values
        byte[] outerLengthBytes = makeLengthField( this.lengthFieldEncoding, oldOuterLength + subOverallLength );
        int newOuterValueOffset = outerLengthFieldOffset + outerLengthBytes.length; // New value starts here
        int subKeyOffset = newOuterValueOffset + oldOuterLength;
        int subLengthFieldOffset = subKeyOffset + subKeyLength;
        int subValueOffset = subLengthFieldOffset + subLengthBytes.length;
        
        // Total new bytes
        byte[] newBytes = new byte[ this.keyLength + outerLengthBytes.length + oldOuterLength + subOverallLength ];
        
        // Copy outer key
        System.arraycopy(this.klvBytes,this.klvBytesOffset, newBytes,0,this.keyLength);
        
        // Copy outer length field
        System.arraycopy(outerLengthBytes,0, newBytes,this.keyLength,outerLengthBytes.length);
        
        // Copy old payload
        System.arraycopy(this.klvBytes,oldValueOffset, newBytes,newOuterValueOffset,oldOuterLength);
        
        // Add new key
        for( int i = 0; i < subKeyLength; i++ ){
            newBytes[subKeyOffset + i] = (byte)(subKey >> (subKeyLength*8 - i*8 - 8));
        }   // end for: i
        
        // Add new length field
        for( int i = 0; i < subLengthBytes.length; i++ ){
            newBytes[subLengthFieldOffset + i] = subLengthBytes[i];
        }   // end for: i
        
        // Add new data
        for( int i = 0; i < subData.length; i++ ){
            newBytes[subValueOffset + i] = subData[i];
        }   // end for: i
        
        // Replace underlying byte array
        this.klvBytes = newBytes;   // Replace underlying byte array
        purgeCache();               // Sub KLV elements use different underlying array now
        
        return this;
        
    }   // end addKLV
    
    
    
    
    
    
    
/* ********  P R O T E C T E D   M E T H O D S  ******** */
    
    
    /**
     * Purges the cache, if it exists, of any KLV values
     * inside the overall payload.
     * Calling {#link getKLV} causes the cache to be created,
     * but if the payload is changed for any reason, the
     * cache ought to be purged.
     */
    protected void purgeCache(){
        subKLVCache = new HashMap<Integer,Map>();
    }
    
    
    
    
/* ********  O B J E C T   O V E R R I D E  ******** */    
    
    
	
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        
        // Key
        sb.append("Key=");
        if( keyLength <= 4 ) sb.append( getShortKey() );
        else{ sb.append( "(long key. Code pending)" ); }
        
        // Declared Length
        sb.append(", Declared Length=");
        sb.append( getDeclaredValueLength() );
        
        // Actual Length
        sb.append(", Actual Length=");
        sb.append( getActualValueLength() );
        
        // Value
        
        return sb.toString();
    }
    
    
    
    
    
/* ********  S T A T I C   M E T H O D S  ********* */    
    
    
    
    /**
     * Return a mapping of keys (up to four bytes) to KLV
     * sets based on an assumed key length and length field
     * encoding scheme.
     *
     * @param bytes The byte array to parse
     * @param offset Where to start parsing
     * @param length How many bytes to parse
     * @param keyLength Length of keys assumed in the KLV sets
     * @param lengthFieldEncoding Flag indicating encoding type
     * @return Map of keys to KLVs
     */
    public static Map<Integer,KLV> parseBytes( byte[] bytes, int offset, int length, int keyLength, int lengthFieldEncoding ){
        Map<Integer,KLV> map = new HashMap<Integer,KLV>();
        
        int currentPos = offset;    // Keep track of where we are
        while( currentPos < offset + length ){
            try{
                KLV klv = new KLV( bytes, currentPos, keyLength, lengthFieldEncoding );
                currentPos = klv.getValueOffset() + klv.getActualValueLength(); // Skip to end of sub KLV
                map.put( klv.getShortKey(), klv );
            } catch( Exception exc ){
                // Stop trying for more?
                System.err.println("Stopped parsing with exception: " + exc.getMessage() );
                break;
            }   // end catch
            
        }   // end while
        
        return map;
    }   // end parseBytes
    
    
    
    
    /**
     * Make a byte array that represents the length field necessary to
     * indicate the given payload length. Most useful when using BER encoding.
     *
     * @param length field encoding flag
     * @param payloadLength number of bytes in value
     * @return byte array with appropriate length field bytes
     */
    public static byte[] makeLengthField( int lengthFieldEncoding, int payloadLength ){
        
        // Bytes for length encoding
        byte[] bytes = null;
        switch( lengthFieldEncoding ){
            
            // Unsigned integer, one byte long.
            case LENGTH_FIELD_ONE_BYTE: 
                if( payloadLength > 255 ) throw new IllegalArgumentException( "Too much data for one byte length field encoding." );
                bytes = new byte[]{ (byte)payloadLength };
                break;
            
            // Unsigned integer, two bytes long, big endian.    
            case LENGTH_FIELD_TWO_BYTES: 
                if( payloadLength > 65535 ) throw new IllegalArgumentException( "Too much data for two byte length field encoding." );
                bytes = new byte[]{ (byte)(payloadLength >> 8), (byte)payloadLength };
                break;
                
            // Unsigned integer, four bytes long, big endian.    
            case LENGTH_FIELD_FOUR_BYTES: 
                bytes = new byte[]{ 
                    (byte)(payloadLength >> 24), 
                    (byte)(payloadLength >> 16), 
                    (byte)(payloadLength >>  8), 
                    (byte)payloadLength };
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
                if( payloadLength <= 127 ){
                    bytes = new byte[]{ (byte)payloadLength };
                }   // end if: short form
                else {
                    if( payloadLength <= 255 ){ // One byte
                        bytes = new byte[]{ (byte)0x81, (byte)payloadLength };
                    } else if( payloadLength <= 65535 ){ // Two bytes
                        bytes = new byte[]{ (byte)0x82, (byte)(payloadLength >> 8), (byte)payloadLength };
                    } else { // Four bytes
                    bytes = new byte[]{ 
                        (byte)0x84,
                        (byte)(payloadLength >> 24), 
                        (byte)(payloadLength >> 16), 
                        (byte)(payloadLength >>  8), 
                        (byte)payloadLength };
                    }
                }   // end else: long form
                    break;
            default:
                throw new IllegalStateException( "Unknown length field encoding flag: " + lengthFieldEncoding );
        }   // end switch
        return bytes;
    }   // end makeLengthField
    
    
    
	
	
}   // end class KLV
