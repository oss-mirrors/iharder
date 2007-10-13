
import java.util.*;

//LEFT OFF: Create tests for constructors


/**
 * <p>A public domain class for working with Key-Length-Value (KLV)
 * byte-packing and unpacking. Supports 1-, 2-, 4-byte, and BER-encoded
 * length fields and 1-, 2-, 4-, and 16-byte key fields. Provides
 * auto-mapping of KLV elements within a payload to the
 * <code>java.util.Map</code> interface.</p>
 *
 * <p>KLV has been used for years as a repeatable, no-guesswork technique
 * for byte-packing data, that is, sending data in a binary format
 * with two bytes for this integer, four bytes for that float,  and
 * so forth. KLV is used in broadcast television and is defined in
 * SMPTE 336M-2001, but it also greatly eases the burden of non-TV-related
 * applications for an easy, interchangeable binary format.</p>
 *
 * <p>The underlying byte array is always king. If you change the key
 * length ({@link #setKeyLength}) or change the length encoding
 * ({@link #setLengthEncoding}), you only change how the underlying
 * byte array is interpreted on subsequent calls.</p>
 *
 * <p>Everything in KLV is Big Endian.</p>
 *
 * <p>All <tt>getValue...</tt> methods will return up to the number
 * of bytes specified in the length fields ({@link #getLength}) unless
 * there are fewer bytes actually given than are intended. This is to make
 * the code more robust for reading corrupted data. Too few bytes: those
 * bytes are considered the value. Too many bytes: only up to {@link #getLength}
 * bytes are condidered the value.</p>
 *
 * <p>This code is released into the Public Domain. Enjoy.</p>
 *
 * @author Robert Harder
 * @author rharder@users.sourceforge.net
 * @version 0.3
 */
public class KLV {
    

/* ********  E N U M  ******** */    
    
    /**
     * The encoding style for the length field can be fixed at
     * one byte, two bytes, four bytes, or variable with 
     * Basic Encoding Rules (BER).
     */
    public static enum LengthEncoding {
        OneByte     (1),
        TwoBytes    (2),
        FourBytes   (4),
        BER         (0);
        private int value;
        LengthEncoding( int value ){ this.value = value; }
        
        /** 
         * Returns the number of bytes used to encode length,
         * or zero if encoding is <code>BER</code>
         */
        public int value(){ return this.value; }
        
        /** 
         * Returns the LengthEncoding matching <code>value</code>
         * with zero mapping to BER or null if no match.
         * @param value the matching length encoding
         */
        public static LengthEncoding valueOf( int value ){
            switch( value ){
                case 1 : return OneByte;
                case 2 : return TwoBytes;
                case 4 : return FourBytes;
                case 0 : return BER;
                default: return null;
            }   // end switch
        }   // end valueOf
        
    }   // end enum LengthEncoding
    
    
    
    
    
    /**
     * The number of bytes in the key field can be
     * one byte, two bytes, four bytes, or sixteen bytes.
     */
    public static enum KeyLength {
        OneByte     (1),
        TwoBytes    (2),
        FourBytes   (4),
        SixteenBytes(16);
        
        private int value;
        KeyLength( int value ){ this.value = value; }
        
        /** Returns the number of bytes used in the key. */
        public int value(){ return this.value; }
        
        
        /** 
         * Returns the KeyLength matching <code>value</code>
         * or null if no match is found.
         * @param value the matching key length
         */
        public static KeyLength valueOf( int value ){
            switch( value ){
                case  1 : return OneByte;
                case  2 : return TwoBytes;
                case  4 : return FourBytes;
                case 16 : return SixteenBytes;
                default : return null;
            }   // end switch
        }   // end valueOf
    }   // end enum KeyLength


    // These are left over from before I switched to enums
    // although enums require a more modern JVM. -Rob
    /** Indicates length field is one byte. Equal to decimal 1. */
    //public final static int LENGTH_FIELD_ONE_BYTE = 1;
    
    /** Indicates length field is two bytes.  Equal to decimal 2. */
    //public final static int LENGTH_FIELD_TWO_BYTES = 2;
    
    /** Indicates length field is four bytes.  Equal to decimal 4. */
    //public final static int LENGTH_FIELD_FOUR_BYTES = 4;
    
    /** Indicates length field uses basic encoding rules (BER). Equal to decimal 8.  */
    //public final static int LENGTH_FIELD_BER = 8;
    
    /** Indicates key length of one byte. Equal to decimal 1. */
    //public final static int KEY_LENGTH_ONE_BYTE = 1;
    
    /** Indicates key length of two bytes.  Equal to decimal 2. */
    //public final static int KEY_LENGTH_TWO_BYTES = 2;
    
    /** Indicates key length of four bytes.  Equal to decimal 4. */
    //public final static int KEY_LENGTH_FOUR_BYTES = 4;
    
    /** Indicates key length of 16 bytes.  Equal to decimal 16. */
    //public final static int KEY_LENGTH_SIXTEEN_BYTES = 16;
    
    
/* ********  S T A T I C   F I E L D S  ******** */    
    
    
    
    /**
     * Default <code>KeyLength</code> value (four bytes) when
     * not otherwise specified.
     */
    public final static KeyLength DEFAULT_KEY_LENGTH = KeyLength.FourBytes;
    
    
    /**
     * Default <code>LengthEncoding</code> value (BER) when
     * not otherwise specified.
     */
    public final static LengthEncoding DEFAULT_LENGTH_ENCODING = LengthEncoding.BER;
    
    
    
    /** Default character set encoding to use is UTF-8. */
    public final static String DEFAULT_CHARSET_NAME = "UTF-8";


/* ********  I N S T A N C E   F I E L D S  ******** */    
    
    
    
    /**
     * Number of bytes in key. 
     */
    private KeyLength keyLength;
    
    
    /**
     * The bytes from which the KLV set is made up.
     * May include irrelevant bytes so that byte arrays
     * with offset and length specified separately so arrays
     * can be passed around with a minimum of copying.
     */
    private byte[] klvBytes;
    
    
    /**
     * Offset from beginning of <tt>byte[] klvBytes</tt>
     * where this KLV data begins.
     */
    private int klvBytesOffset;
    
    
    
    /**
     * The kind of length encoding used.
     */
    private LengthEncoding lengthEncoding;
    
    
    /**
     * Used to cache parsing of payload into further KLV sets.
     *
     * This map is in three layers: Map -> Map -> KLV.
     *
     * The outer map is keyed off of {@link #KeyLength} and contains
     * all attempts to parse the payload with that key length.
     *
     * The next map within is keyed off of {@link LengthEncoding}
     * and itself contains actual KLV objects that were parsed with
     * the parent key length and length encoding types.
     *
     */
    private Map<KeyLength,
                Map<LengthEncoding,
                    Map<Integer,KLV> >> subKLVCache 
            = new HashMap<KeyLength,Map<LengthEncoding,Map<Integer,KLV>>>();
    
    
    /**
     * The default key length to use when adding KLV sets
     * to the payload with {@link addKLV}.
     * This can be overridden with the expanded {@link addKLV}
     * method, but do so thoroughly: having sub KLV sets
     * with different key lengths and encoding types will
     * generally lead to confusion and lost data.
     * Defaults to four bytes.
     *
     * @see #setSubKeyLengthDefault
     * @see #getSubKeyLengthDefault
     */
    //private KeyLength subKeyLengthDefault = DEFAULT_KEY_LENGTH;
    
    
    /**
     * The default length field encoding to use when adding KLV sets
     * to the payload with {@link addKLV}.
     * This can be overridden with the expanded {@link addKLV}
     * method, but do so thoroughly: having sub KLV sets
     * with different key lengths and encoding types will
     * generally lead to confusion and lost data.
     * Defaults to Basic Encoding Rules (BER).
     * 
     * @see #setSubLengthEncodingDefault
     * @see #getSubLengthFieldEncodingDefault
     */
    //private LengthEncoding subLengthEncodingDefault = DEFAULT_LENGTH_ENCODING;
    
    
/* ********  C O N S T R U C T O R S  ******** */    
    
    
    
    /**
     * Creates a KLV set with default key length (four bytes), 
     * default length encoding (BER), a length of zero, and no payload.
     * Other constructors in sub classes are not required to call this constructor.
     */
    public KLV(){
        this.keyLength = DEFAULT_KEY_LENGTH;
        this.lengthEncoding = DEFAULT_LENGTH_ENCODING;
        this.klvBytes = new byte[]{
            0,0,0,0,    // Key = 0
            0           // Length = 0
        };
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
     * @param theBytes          The bytes that make up the entire KLV set
     * @param keyLength         The number of bytes in the key.
     * @param lengthEncoding    The length field encoding type.
     * @throws NullPointerException if keyLength or lengthEncoding are null
     */
    public KLV( byte[] theBytes, KeyLength keyLength, LengthEncoding lengthEncoding ){
        this( theBytes, 0, keyLength, lengthEncoding );
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
     * @param theBytes          The bytes that make up the entire KLV set
     * @param offset            The offset from beginning of theBytes
     * @param keyLength         The number of bytes in the key.
     * @param lengthEncoding    The length field encoding type.
     * @throws NullPointerException if theBytes, keyLength, or lengthEncoding are null
     */
    public KLV( byte[] theBytes, int offset, KeyLength keyLength, LengthEncoding lengthFieldEncoding ){
        if( theBytes == null )      throw new IllegalArgumentException( "KLV byte array must not be null." ); 
        if( theBytes.length < 2 )   throw new IllegalArgumentException( "KLV byte array must be at least two bytes long:" + theBytes.length ); 
        if( offset < 0 )            throw new IllegalArgumentException( "Offset must be non-negative: " + offset ); 
        
        this.klvBytes       = theBytes;
        this.klvBytesOffset = offset;
        this.keyLength      = keyLength;
        this.lengthEncoding = lengthFieldEncoding;
    }   // end constructor
    
    
    
    
    /**
     * Create a KLV set with the given key, key length, length field encoding,
     * and provided value in a byte array.
     */
    public KLV( int shortKey, KeyLength keyLength, LengthEncoding lengthFieldEncoding, byte[] value ){
        this( shortKey, keyLength, lengthFieldEncoding, value, 0, value.length );
    }
    
    
    /**
     * Create a KLV set with the given key, key length, length field encoding,
     * and provided value in a byte array.
     */
    public KLV( int shortKey, KeyLength keyLength, LengthEncoding lengthFieldEncoding, byte[] value, int offset, int length ){
        
        // Length field
        byte[] lengthField = KLV.makeLengthField(lengthFieldEncoding, length );
        
        // New byte array
        byte[] newBytes = new byte[ keyLength.value() + lengthField.length + length ];
        
        // Copy  key
        switch( keyLength ){
        case OneByte:
            newBytes[0] = (byte)shortKey;
            break;
            
        case TwoBytes:
            newBytes[0] = (byte)(shortKey>>8);
            newBytes[1] = (byte)shortKey;
            break;
            
        case FourBytes:
            newBytes[0] = (byte)(shortKey>>24);
            newBytes[1] = (byte)(shortKey>>16);
            newBytes[2] = (byte)(shortKey>>8);
            newBytes[3] = (byte)shortKey;
            break;
            
        default:
            throw new IllegalStateException( "Unknown key length: " + keyLength );
        }
        
        // Copy length field
        System.arraycopy(lengthField,0, newBytes,keyLength.value(),lengthField.length);
        
        // Copy payload
        System.arraycopy(value,offset, newBytes,keyLength.value() + lengthField.length, length);
        
        // Replace underlying byte array
        this.klvBytes = newBytes;   // Replace underlying byte array
        this.klvBytesOffset = 0;
        this.keyLength = keyLength;
        this.lengthEncoding = lengthFieldEncoding;
    }
    
    
    
    /**
     * Constructs a KLV with the given key, key length, length field encoding, and value (payload).
     * 
     */
    public KLV( byte[] key, LengthEncoding lengthFieldEncoding, byte[] value, int offset, int length ){
        
        // Length field
        byte[] lengthField = KLV.makeLengthField(lengthFieldEncoding, length );
        
        // New byte array
        byte[] newBytes = new byte[ key.length + lengthField.length + length ];
        
        // Copy  key
        System.arraycopy(key,0, newBytes,0,key.length);
        
        // Copy length field
        System.arraycopy(lengthField,0, newBytes,key.length,lengthField.length);
        
        // Copy new payload
        System.arraycopy(value,offset, newBytes,key.length + lengthField.length, length);
        
        // Replace underlying byte array
        this.klvBytes = newBytes;   // Replace underlying byte array
        this.klvBytesOffset = 0;
        this.keyLength = KLV.KeyLength.valueOf(key.length);
        this.lengthEncoding = lengthFieldEncoding;
        
    }
    
    
    
    /**
     * Return the KLV as a byte array.
     * The array is copied from the original underlying byte array.
     */
    public byte[] toBytes(){
        int size = this.getValueOffset() - this.klvBytesOffset + this.getActualValueLength();
        byte[] bytes = new byte[ size ];
        System.arraycopy(this.klvBytes,this.klvBytesOffset, bytes,0,size);
        return bytes;
    }
    
    
    
    
    
/* ********  P U B L I C   G E T   M E T H O D S  ******** */    
    
    
    /**
     * Return a KLV that is in the payload after 
     * parsing the payload as if it contains more KLV sets with the 
     * same key length and length field encoding as the parent.
     * The parsing is cached to speed subsequent calls.
     * The key should be loaded up least-significant bytes first.
     * That is, a one-byte key with the value 42 would be an <tt>int</tt>
     * with value 42 as well: <tt>00000000 00000000 00000000 00101010</tt>.
     * 
     * @param shortKey      One- to four-byte key for the sub-KLV element
     * @return Matching KLV or null if not found
     */
    public KLV getSubKLV( int shortKey ){        
        return getSubKLV( shortKey, this.keyLength, this.lengthEncoding );
    }   // end getSubKLV
    
    
    
    
    /**
     * Return a KLV that is somewhere in the payload after 
     * parsing the payload as if it contains more KLV sets with the given
     * key lengths and length field encodings.
     * The parsing is cached to speed subsequent calls.
     * The key should be loaded up least-significant bytes first.
     * That is, a one-byte key with the value 42 would be an <tt>int</tt>
     * with value 42 as well: <tt>00000000 00000000 00000000 00101010</tt>.
     * 
     * @param shortKey          One- to four-byte integer key for the sub-KLV elements
     * @param keyLength         length of shortKey
     * @param lengthEncoding    indicates encoding type
     * @return                  Matching KLV or null if not found
     */   
    //private Map<KeyLength,
    //            Map<LengthEncoding,
    //                Map<Integer,KLV> >> subKLVCache 
    public KLV getSubKLV( int shortKey, KeyLength keyLength, LengthEncoding lengthEncoding ){
        
        // Map exists for this keyLength?
        Map<LengthEncoding,Map<Integer,KLV>> k2l = this.subKLVCache.get( keyLength );
        if( k2l == null ){
            k2l = new HashMap<LengthEncoding,Map<Integer,KLV>>();
            this.subKLVCache.put( keyLength, k2l );
        }   // end if: null
        
        // Map exists for length field encoding?
        Map<Integer,KLV> l2klv = k2l.get( lengthEncoding );
        if( l2klv == null ){
            int offset = getValueOffset();              // Where payload begins
            int valueLength = getActualValueLength();   // How many bytes in payload
            l2klv = parseBytes( this.klvBytes, offset, valueLength, keyLength, lengthEncoding );
            k2l.put( lengthEncoding, l2klv );
        }   // end if: null
        
        // KLV exists?
        return l2klv.get( shortKey );
    }   // end getSubKLV
    
    

    
    /**
     * Returns the length of the key 
     * (not necessarily of the payload within,
     * if the payload is more KLV data).
     *
     * @return length of key.
     */
    public KeyLength getKeyLength(){
        return this.keyLength;
    }   // end getKeyLength
    
    
    
    
    /**
     * Returns the default key lengths used by possible
     * KLV sets within the overall payload.
     *
     * @return  sub key length
     * @see     #setSubKeyLengthDefault
     */
    //public KeyLength getSubKeyLengthDefault(){
    //    return this.subKeyLengthDefault;
    //}   // end getSubKeyLengthDefault
    
    

    /**
     * Returns up to the first four bytes of the key as an int.
     *
     * @return the key
     */
    public int getShortKey(){
        int shortKey = 0;
        int length = this.keyLength.value();
        
        // No harm if key length is 16 bytes, but wasted cycles
        for( int i = 0; i < length; i++ )
            shortKey |= (klvBytes[klvBytesOffset+i] & 0xFF) << (length*8 - i*8 - 8);
        
        
        return shortKey;
    }   // end getShortKey
    
    
    /**
     * Returns a byte array representing the key. This is a copy of the bytes
     * from the original byte set.
     *
     * @return the key
     */
    public byte[] getFullKey(){
        int length = this.keyLength.value;
        byte[] key = new byte[length];
        System.arraycopy(this.klvBytes,klvBytesOffset, key,0,length);
        return key;
    }   // end getFullKey
    
    
    /**
     * Returns the length encoding flag
     * (not necessarily of the payload within,
     * if the payload is more KLV data).
     *
     * @return length field encoding flag
     */
    public LengthEncoding getLengthEncoding(){
        return this.lengthEncoding;
    }
    
    
    
    
    
    /**
     * Returns the default length field encoding used by any sub KLV sets 
     * that might be stored in the overall payload. 
     * 
     * @see #setSubLengthEncodingDefault
     */
    //public LengthEncoding getSubLengthFieldEncodingDefault(){
    //    return this.subLengthEncodingDefault;
    //}
    
    
    
    
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
        int keyLengthVal = this.keyLength.value();
        switch( this.lengthEncoding ){
            
            // Unsigned integer, one byte long.
            case OneByte: 
                length = klvBytes[klvBytesOffset + keyLengthVal] & 0xFF;
                break;
            
            // Unsigned integer, two bytes long, big endian.    
            case TwoBytes: 
                length = (klvBytes[klvBytesOffset + keyLengthVal]     & 0xFF) << 8
                       | (klvBytes[klvBytesOffset + keyLengthVal + 1] & 0xFF);
                break;
                
            // Unsigned integer, four bytes long, big endian.    
            case FourBytes: 
                length = (klvBytes[klvBytesOffset + keyLengthVal]     & 0xFF) << 24 
                       | (klvBytes[klvBytesOffset + keyLengthVal + 1] & 0xFF) << 16
                       | (klvBytes[klvBytesOffset + keyLengthVal + 2] & 0xFF) << 8
                       | (klvBytes[klvBytesOffset + keyLengthVal + 3] & 0xFF);
                break;
                
            // Short BER form: If high bit is not set, then  
            // use the byte to determine length of payload.
            // Long BER form: If high bit is set (0x80), 
            // then use low seven bits to determine how many 
            // bytes that follow are themselves an unsigned 
            // integer specifying the length of the payload. 
            // Using more than four bytes to specify the length 
            // is not supported in this code, though it's not 
            // exactly forbidden KLV notation either, but my
            // goodness, how big a _length_ field would that be?!
            case BER:
                int berField = klvBytes[klvBytesOffset + keyLengthVal] & 0xFF;  // First byte
                if( (berField & 0x80) == 0 ) return berField;                   // Must be short BER form
                else{
                    int berLength = berField & 0x7F; // Low seven bits
                    if( berLength > 4 ) 
                        throw new UnsupportedOperationException( 
                            String.format("BER length field greater than four bytes are not supported (%d bytes declared).",berLength) );
                    for( int i = 0; i < berLength; i++ )
                        length |= (klvBytes[klvBytesOffset + keyLengthVal + 1 + i] & 0xFF) << (berLength*8 - i*8 - 8);
                    break;
                }   // end else: long BER form
            default:
                throw new IllegalStateException( "Unknown length field encoding flag: " + this.lengthEncoding );
        }   // end switch
        
        return length;
    }   // end getDeclaredValueLength
    
    
    
    /**
     * Returns the actual length of the value (payload) in bytes.
     * It's possible that the payload actually has fewer bytes 
     * than are declared in the length field if not enough bytes
     * were passed in during instantiation. This flexibility allows
     * for possibly-corrupted data to still be read.
     *
     * @return  Actual length of payload
     * @see     #getDeclaredValueLength
     */
    public int getActualValueLength(){
        int declaredLength = getDeclaredValueLength();
        int availableLength = this.klvBytes.length - getValueOffset();
        return declaredLength < availableLength ? declaredLength : availableLength;
    }   // end getDeclaredValueLength
    
    
    
    
    /**
     * Determine how far into the byte array the value starts.
     * If the KLV set was created from a larger byte array where
     * the key itself starts at some high offset, expect that the
     * offset here will also be high. In other words, this is the
     * offset from the beginning of the containing byte array, not
     * the offset from the first byte of the key.
     *
     * @return offset to the first byte of the payload
     */
    public int getValueOffset(){
        int keyLengthVal = this.keyLength.value();
        int offset = this.klvBytesOffset + keyLengthVal;
        switch( this.lengthEncoding ){
            
            // Unsigned integer, one byte long.
            case OneByte: 
                offset += 1;
                break;
            
            // Unsigned integer, two bytes long, big endian.    
            case TwoBytes: 
                offset += 2;
                break;
                
            // Unsigned integer, four bytes long, big endian.    
            case FourBytes: 
                offset += 4;
                break;
                
            // Short BER form: 1 extra byte
            // Long BER form: variable extra bytes
            case BER:
                offset++; // Always at least one BER byte
                int berField = klvBytes[ keyLengthVal ] & 0xFF;
                if( (berField & 0x80) == 0 ) break; // Short BER form
                else{
                    int berLength = berField & 0x7F; // Low seven bits
                    offset += berLength;
                }   // end else: long BER form
                break;
            default:
                throw new IllegalStateException( "Unknown length field encoding flag: " + this.lengthEncoding );
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
     * Returns up to the first byte of the value as an 8-bit signed integer.
     *
     * @return the value as an 8-bit signed integer
     */
    public int getValueAs8bitSignedInt(){
        byte[] bytes = getValue(); // Probably could be more efficient.
        byte value = 0;
        if( bytes.length > 0 )
            value = bytes[0];
        return value;
    }   // end getValueAs8bitSignedInt
    
    
    /**
     * Returns up to the first byte of the value as an 8-bit unsigned integer.
     *
     * @return the value as an 8-bit unsigned integer
     */
    public int getValueAs8bitUnsignedInt(){
        byte[] bytes = getValue();
        int value = 0;
        if( bytes.length > 0 )
            value = bytes[0] & 0xFF;
        return value;
    }   // end getValueAs8bitSignedInt
    
    
    
    /**
     * Returns up to the first two bytes of the value as a 16-bit signed integer.
     *
     * @return the value as a 16-bit signed integer
     */
    public int getValueAs16bitSignedInt(){
        byte[] bytes = getValue();
        short value = 0;
        int length = bytes.length;
        int shortLen = length < 2 ? length : 2;
        for( int i = 0; i < shortLen; i++ )
            value |= (bytes[i] & 0xFF) << (shortLen*8 - i*8 - 8);
        return value;
    }   // end getValueAs16bitSignedInt
    
    
    /**
     * Returns up to the first two bytes of the value as a 16-bit unsigned integer.
     *
     * @return the value as a 16-bit unsigned integer
     */
    public int getValueAs16bitUnsignedInt(){
        byte[] bytes = getValue();
        int value = 0;
        int length = bytes.length;
        int shortLen = length < 2 ? length : 2;
        for( int i = 0; i < shortLen; i++ )
            value |= (bytes[i] & 0xFF) << (shortLen*8 - i*8 - 8);
        return value;
    }   // end getValueAs16bitUnsignedInt
    
    
    
    /**
     * Returns up to the first four bytes of the value as a 32-bit int.
     * Since all Java ints are signed, there is no signed/unsigned option.
     * If you need a 32-bit unsigned int, try {@link #getValueAs64bitLong}.
     *
     * @return the value as an int
     */
    public int getValueAs32bitInt(){
        byte[] bytes = getValue();
        int value = 0;
        int length = bytes.length;
        int shortLen = length < 4 ? length : 4;
        for( int i = 0; i < shortLen; i++ )
            value |= (bytes[i] & 0xFF) << (shortLen*8 - i*8 - 8);
        return value;
    }   // end getValueAs32bitSignedInt
    
    
    
    /**
     * Returns up to the first eight bytes of the value as a 64-bit signed long.
     * Note if you expect a 32-bit <b>unsigned</b> int, and since Java doesn't
     * have such a thing, you could return a long instead and get the proper effect.
     *
     * @return the value as a long
     */
    public long getValueAs64bitLong(){
        byte[] bytes = getValue();
        long value = 0;
        int length = bytes.length;
        int shortLen = length < 8 ? length : 8;
        for( int i = 0; i < shortLen; i++ )
            value |= (long)(bytes[i] & 0xFF) << (shortLen*8 - i*8 - 8);
        return value;
    }   // end getValueAs64bitLong
    
    
    
    /**
     * Returns the first four bytes of the value as a float according
     * to IEEE 754 byte packing. See Java's Float class for details.
     * This method calls <code>Float.intBitsToFloat</code> with
     * {@link #getValueAs32bitInt} as the argument. However it does check
     * to see that the value has at least four bytes. If it does not,
     * then <tt>Float.NaN</tt> is returned.
     *
     * @return the value as a float
     */
    public float getValueAsFloat(){
        return this.getActualValueLength() < 4 
                ? Float.NaN 
                : Float.intBitsToFloat(getValueAs32bitInt());
    }   // end getValueAsFloat
    
    
    
    /**
     * Returns the first eight bytes of the value as a double according
     * to IEEE 754 byte packing. See Java's Double class for details.
     * This method calls <code>Double.longBitsToDouble</code> with
     * {@link #getValueAs64bitLong} as the argument. However it does check
     * to see that the value has at least eight bytes. If it does not,
     * then <tt>Double.NaN</tt> is returned.
     *
     * @return the value as a float
     */
    public double getValueAsDouble(){
        return this.getActualValueLength() < 8 
                ? Double.NaN 
                : Double.longBitsToDouble(getValueAs64bitLong());
    }   // end getValueAsDouble
    
    
    
    
    
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
     * Some of the bytes may not be part of the KLV set if an offset
     * was specified or the length field indicates fewer bytes than
     * are available.
     * Be sure to check {@link #getBytesOffset}.
     *
     * @return  all the bytes
     * @see     #getBytesOffset
     * @see     #getBytesLength
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
     * @param subKeyLength  The default sub key length to use for KLV sets within
     * @return              <tt>this</tt> to aid in stringing commands together
     * @see                 #getSubKeyLengthDefault
     */
    //public KLV setSubKeyLengthDefault( KeyLength subKeyLength ){        
    //    this.subKeyLengthDefault = subKeyLength;        
    //    return this;
    //}
    
    
    /**
     * Sets the default length encoding used by possible
     * KLV sets within the payload.
     *
     * @param subLengthEncoding The default sub length encoding to use for KLV sets within
     * @return                  <tt>this</tt> to aid in stringing commands together
     * @see                     #getSubLengthFieldEncodingDefault
     */
    //public KLV setSubLengthEncodingDefault( LengthEncoding subLengthEncoding ){        
    //    this.subLengthEncodingDefault = subLengthEncoding;        
    //    return this;
    //}
    
    
    
    /**
     * Sets the key as a sixteen-byte key.
     * The underlying byte array is not recopied unless
     * the key length would change as a result of this call.
     * If <tt>sixteenByteKey</tt> is not sixteen bytes long,
     * an <tt>IllegalArgumentException</tt> is thrown.
     * 
     * @param sixteenByteKey    the sixteen byte key
     * @return                  <tt>this</tt> to aid in stringing commands together
     */
    public KLV setKey( byte[] sixteenByteKey ){
        if( sixteenByteKey == null )
            throw new NullPointerException("New key must not be null.");
        if( sixteenByteKey.length != 16 )
            throw new IllegalArgumentException("New key must be 16 bytes long, not " + sixteenByteKey.length );
        
        int oldKeyLength = this.getKeyLength().value();
        
        if( oldKeyLength == 16 ){ // No need for new byte array
            System.arraycopy(sixteenByteKey,0, this.klvBytes,this.klvBytesOffset,16);
            
        } else {    // Need new byte array
            
            // Get the old bytes that make up the entire KLV.
            // Make a new array.
            // Copy in new key.
            // Copy in old length field and value.
            // This could be more efficient, but I'm doing it this way for
            // now because it's easy to code. If it's a bottleneck, I can
            // fix it later.
            byte[] oldBytes = this.toBytes(); 
            byte[] newBytes = new byte[ oldBytes.length + 16 - oldKeyLength ];
            
            System.arraycopy(sixteenByteKey,0, newBytes,0,16);  // Copy key
            System.arraycopy(                                   // Copy length and value
                    oldBytes,oldKeyLength,
                    newBytes,16, oldBytes.length - oldKeyLength );
            
            this.klvBytes = newBytes;
            this.klvBytesOffset = 0;
            this.keyLength = KeyLength.SixteenBytes;
        }
        
        return this;
    }
    
    
    
    /**
     * Sets the key according to the existing key length.
     * The underlying byte array is not copied - it is
     * modified in place.
     * 
     * @param shortKey      the key of one, two, or four bytes
     * @return              <tt>this</tt> to aid in stringing commands together
     */
    public KLV setKey( int shortKey ){
        return setKey( shortKey, this.keyLength );
    }
    
    
    
    /**
     * Sets the key according to the given key length.
     * The underlying byte array will be copied into a
     * new array only if <tt>keyLength</tt> is different
     * than the existing key length.
     * 
     * @param shortKey      the key of one, two, or four bytes
     * @param keyLength     the length of the key
     * @return              <tt>this</tt> to aid in stringing commands together
     */
    public KLV setKey( int shortKey, KeyLength keyLength ){
        
        if( keyLength == this.getKeyLength() ){ // No need to copy byte array
            
            // Put "shortKey" into the appropriate byte array locations
            switch( keyLength ){
            case OneByte:
                this.klvBytes[this.klvBytesOffset] = (byte)shortKey;
                break;
                
            case TwoBytes:
                this.klvBytes[this.klvBytesOffset  ] = (byte)(shortKey>>8);
                this.klvBytes[this.klvBytesOffset+1] = (byte)shortKey;
                break;
                
            case FourBytes:
                this.klvBytes[this.klvBytesOffset  ] = (byte)(shortKey>>24);
                this.klvBytes[this.klvBytesOffset+1] = (byte)(shortKey>>16);
                this.klvBytes[this.klvBytesOffset+2] = (byte)(shortKey>>8);
                this.klvBytes[this.klvBytesOffset+3] = (byte)shortKey;
                break;
                
            default:
                throw new IllegalArgumentException( "Only one-, two-, and four-byte keys are accepted in this method." );
            }
            
        } else {    // Need new byte array
            
            // Get the old bytes that make up the entire KLV.
            // Make a new array.
            // Copy in new key.
            // Copy in old length field and value.
            // This could be more efficient, but I'm doing it this way for
            // now because it's easy to code. If it's a bottleneck, I can
            // fix it later.
            byte[] oldBytes = this.toBytes(); 
            byte[] newBytes = new byte[ oldBytes.length + keyLength.value() - this.getKeyLength().value() ];
                        
            switch( keyLength ){
            case OneByte:
                newBytes[0] = (byte)shortKey;
                break;
                
            case TwoBytes:
                newBytes[0] = (byte)(shortKey>>8);
                newBytes[1] = (byte)shortKey;
                break;
                
            case FourBytes:
                newBytes[0] = (byte)(shortKey>>24);
                newBytes[1] = (byte)(shortKey>>16);
                newBytes[2] = (byte)(shortKey>>8);
                newBytes[3] = (byte)shortKey;
                break;
                
            default:
                throw new IllegalArgumentException( "Only one-, two-, and four-byte keys are accepted in this method." );
            }   // end switch
            
            // Copy from length field to end of value
            System.arraycopy(
                    oldBytes,this.getKeyLength().value(),
                    newBytes,keyLength.value(), oldBytes.length - this.getKeyLength().value() );
            this.klvBytes = newBytes;
            this.klvBytesOffset = 0;
            this.keyLength = keyLength;
        }
        
        return this;
    }   // end setKey
    
    
    
    /**
     * Sets the value of the KLV set and adjusts the length field to match.
     * The underlying byte array will be replaced with a new one.
     */
    public KLV setValue( byte[] newValue ){
        return setValue( newValue, 0, newValue.length );
    }
    
    
    /**
     * Sets the value of the KLV set and adjusts the length field to match.
     * The underlying byte array will be replaced with a new one.
     */
    public KLV setValue( byte[] newValue, int offset, int length ){
        
        
        //if( extraOffset + extraLength > extraBytes.length )
        //    throw new IllegalArgumentException( "Not enough bytes in array for requested offset and length." );
        
        // Determine length field bytes which may vary depending on encoding type.
        // Then create a new byte array to hold Key, Length field, and Value.
        // Then copy key.
        // Then copy length field.
        // Then copy value.
        
        // Length field
        byte[] lengthField = KLV.makeLengthField(this.lengthEncoding, length );
        
        // New byte array
        byte[] newBytes = new byte[ this.keyLength.value() + lengthField.length + length ];
        
        // Copy  key
        System.arraycopy(this.klvBytes,this.klvBytesOffset, newBytes,0,this.keyLength.value());
        
        // Copy length field
        System.arraycopy(lengthField,0, newBytes,this.keyLength.value(),lengthField.length);
        
        // Copy new payload
        System.arraycopy(newValue,offset, newBytes,this.keyLength.value() + lengthField.length, length);
        
        // Replace underlying byte array
        this.klvBytes = newBytes;   // Replace underlying byte array
        this.klvBytesOffset = 0;
        
        purgeCache();               // Sub KLV elements use different underlying array now
        
        return this;
    }
    
    
    
    
/* ********  A D D   M E T H O D S  ******** */
    
    
    
    
    
    /**
     * Adds a sub KLV set with the given key and the
     * single byte of data as the payload
     * using the parent's key length
     * and parent's length field encoding.
     *
     * @param key   The key for the data
     * @param subValue  The data in the payload
     * @return      <tt>this</tt>, to aid in stringing commands together.
     */
    public KLV addSubKLV( int key, byte subValue ){
        return addSubKLV( key, new byte[]{ subValue } );
    }   // end addSubKLV
    
    
    
    /**
     * Adds a sub KLV set with the given key and the
     * single short (two bytes) of data as the payload
     * using the parent's key length
     * and parent's length field encoding.
     *
     * @param key   The key for the data
     * @param subValue  The data in the payload
     * @return      <tt>this</tt>, to aid in stringing commands together.
     */
    public KLV addSubKLV( int key, short subValue ){
        return addSubKLV( key, new byte[]{ (byte)(subValue >> 8), (byte)subValue } );
    }   // end addSubKLV
    
    
    
    /**
     * Adds a sub KLV set with the given key and the
     * single int (four bytes) of data as the payload
     * using the parent's key length
     * and parent's length field encoding.
     *
     * @param key   The key for the data
     * @param subValue  The data in the payload
     * @return      <tt>this</tt>, to aid in stringing commands together.
     */
    public KLV addSubKLV( int key, int subValue ){
        return addSubKLV( key, new byte[]{ 
            (byte)(subValue >> 24), 
            (byte)(subValue >> 16), 
            (byte)(subValue >>  8), 
            (byte)subValue } );
    }   // end addSubKLV
    
    
    
    /**
     * Adds a sub KLV set with the given key and the
     * single long (eight bytes) of data as the payload
     * using the parent's key length
     * and parent's length field encoding.
     *
     * @param key   The key for the data
     * @param subValue  The data in the payload
     * @return      <tt>this</tt>, to aid in stringing commands together.
     */
    public KLV addSubKLV( int key, long subValue ){
        return addSubKLV( key, new byte[]{ 
            (byte)(subValue >> 56), 
            (byte)(subValue >> 48), 
            (byte)(subValue >> 40), 
            (byte)(subValue >> 32), 
            (byte)(subValue >> 24), 
            (byte)(subValue >> 16), 
            (byte)(subValue >>  8), 
            (byte)subValue } );
    }   // end addSubKLV
    
    
    /**
     * Adds a sub KLV set with the given key and the
     * string of data as the payload
     * using the parent's key length
     * and parent's length field encoding.
     * If data is <tt>null</tt>, then the corresponding
     * payload length will be zero.
     * The default charset (UTF-8) will be used unless
     * that is not supported in which case the current
     * computer's default charset will be used.
     *
     * @param key   The key for the data
     * @param subValue  The data in the payload
     * @return      <tt>this</tt>, to aid in stringing commands together.
     */
    public KLV addSubKLV( int key, String subValue ){
        if( subValue == null ){
            return addSubKLV( key, new byte[0] );
        }   // end if: null
        else {
            try{
                return addSubKLV( key, subValue.getBytes(KLV.DEFAULT_CHARSET_NAME) );
            } catch( java.io.UnsupportedEncodingException exc ){
                return addSubKLV( key, subValue.getBytes() );
            }   // end catch
        }   // end else: not null
    }   // end addSubKLV
    
    
    /**
     * Adds a KLV set to the overall payload using the given
     * key, parent's key length, parent's length encoding, and the provided data.
     * Underlying byte array is copied and replaced.
     *
     * @param key   The key for the data
     * @param subValue  The data in the payload
     * @return      <tt>this</tt>, to aid in stringing commands together.
     */
    public KLV addSubKLV( int key, byte[] subValue ){
        return addSubKLV( key, this.keyLength, this.lengthEncoding, subValue );
    }   // end addSubKLV
    
    
    /**
     * Adds a KLV set to the overall payload using the given
     * key, given sub key length, given length encoding, and the provided data.
     * Underlying byte array is copied and replaced.
     *
     * @param key The key for the data
     * @param data The data in the payload
     * @return <tt>this</tt>, to aid in stringing commands together.
     */
    public KLV addSubKLV( int subKey, KeyLength subKeyLength, LengthEncoding subLengthEncoding, byte[] subValue ){
    
        return addSubKLV( new KLV( subKey, subKeyLength, subLengthEncoding, subValue, 0, subValue.length ) );
        
        /*
        
        
        // Old outer data
        int oldOuterLength = getActualValueLength();    // Old payload length
        int oldValueOffset = getValueOffset();          // Old value offset
        int outerLengthOffset = this.klvBytesOffset + this.keyLength.value();   // Old length field starts here
        int oldOuterLengthFieldLength = oldValueOffset - outerLengthOffset;     // Length of old length field encoding
    
        // Bytes for inner length encoding
        byte[] subLengthBytes = makeLengthField( subLengthEncoding, subData.length );
        int subOverallLength = subKeyLength.value() + subLengthBytes.length + subData.length;
        
        // Make sub KLV
        //KLV klv = new KLV( subKey, subKeyLength, subLengthEncoding, subData );
        
        // Bytes for outer length field -- drives a lot of other offset values
        byte[] outerLengthBytes = makeLengthField( this.lengthEncoding, oldOuterLength + subOverallLength );
        int newOuterValueOffset = outerLengthOffset + outerLengthBytes.length; // New value starts here
        int subKeyOffset = newOuterValueOffset + oldOuterLength;
        int subLengthFieldOffset = subKeyOffset + subKeyLength.value();
        int subValueOffset = subLengthFieldOffset + subLengthBytes.length;
        
        // Total new bytes
        byte[] newBytes = new byte[ this.keyLength.value() + outerLengthBytes.length + oldOuterLength + subOverallLength ];
        
        // Copy outer key
        System.arraycopy(this.klvBytes,this.klvBytesOffset, newBytes,0,this.keyLength.value());
        
        // Copy outer length field
        System.arraycopy(outerLengthBytes,0, newBytes,this.keyLength.value(),outerLengthBytes.length);
        
        // Copy old payload
        System.arraycopy(this.klvBytes,oldValueOffset, newBytes,newOuterValueOffset,oldOuterLength);
        
        // Add new key
        for( int i = 0; i < subKeyLength.value(); i++ ){
            newBytes[subKeyOffset + i] = (byte)(subKey >> (subKeyLength.value()*8 - i*8 - 8));
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
        */
    }   // end addSubKLV
    
    
    /**
     * Adds the given KLV set to the payload by calling
     * <code>addPaylaod( sub.toBytes() )</code>.
     * 
     * @param sub the KLV set to add.
     * @return <tt>this</tt>, to aid in stringing commands together.
     */
    public KLV addSubKLV( KLV sub ){
        return addPayload( sub.toBytes() );
    }
    
    
    
    
    /**
     * Adds the provided bytes to the payload and adjusts the length field.
     *
     * @param extraBytes    New bytes to add
     * @return              <tt>this</tt>, to aid in stringing commands together.
     */
    public KLV addPayload( byte[] extraBytes ){
        addPayload( extraBytes, 0, extraBytes.length );
        return this;
    }
    
    /**
     * Adds the provided bytes to the payload and adjusts the length field.
     * If the length field encoding does not support payloads as large
     * as would result from adding <tt>extraBytes</tt>, 
     * then an IllegalArgumentException is thrown.
     *
     * @param extraBytes    new bytes to add
     * @param extraOffset   offset within <code>extraBytes</code>
     * @param extraLength   length of <code>extraBytes</code> to use
     * @return              <tt>this</tt>, to aid in stringing commands together.
     */
    public KLV addPayload( byte[] extraBytes, int extraOffset, int extraLength ){
        if( extraOffset + extraLength > extraBytes.length )
            throw new IllegalArgumentException( "Not enough bytes in array for requested offset and length." );
        
        
        // Old outer data
        int oldOuterLength = getActualValueLength();    // Old payload length
        int oldValueOffset = getValueOffset();          // Old value offset
        int outerLengthFieldOffset = this.klvBytesOffset + this.keyLength.value();  // Old length field starts here
        int oldOuterLengthFieldLength = oldValueOffset - outerLengthFieldOffset;    // Length of old length field encoding
        
        // Bytes for outer length field -- drives a lot of other offset values
        byte[] outerLengthBytes = makeLengthField( this.lengthEncoding, oldOuterLength + extraLength);
        int newOuterValueOffset = outerLengthFieldOffset + outerLengthBytes.length; // New value starts here
        int offsetForExtraData = newOuterValueOffset + oldOuterLength;
        
        // Total new bytes
        byte[] newBytes = new byte[ this.keyLength.value() + outerLengthBytes.length + oldOuterLength + extraLength ];
        
        // Copy outer key
        System.arraycopy(this.klvBytes,this.klvBytesOffset, newBytes,0,this.keyLength.value());
        
        // Copy outer length field
        System.arraycopy(outerLengthBytes,0, newBytes,this.keyLength.value(),outerLengthBytes.length);
        
        // Copy old payload
        System.arraycopy(this.klvBytes,oldValueOffset, newBytes,newOuterValueOffset,oldOuterLength);
        
        // Add new payload
        for( int i = 0; i < extraLength; i++ ){
            newBytes[offsetForExtraData + i] = extraBytes[i];
        }   // end for: i
        
        // Replace underlying byte array
        this.klvBytes = newBytes;   // Replace underlying byte array
        purgeCache();               // Sub KLV elements use different underlying array now
        
        return this;
    }
    
    
    
    
    
/* ********  P R O T E C T E D   M E T H O D S  ******** */
    
    
    /**
     * Purges the cache, if it exists, of any KLV values
     * inside the overall payload.
     * Calling {#link getKLV} causes the cache to be filled,
     * but if the payload is changed for any reason, the
     * cache ought to be purged.
     */
    protected void purgeCache(){
        subKLVCache = new HashMap<KeyLength,Map<LengthEncoding,Map<Integer,KLV>>>();
    }
    
    
    
    
/* ********  O B J E C T   O V E R R I D E  ******** */    
    
    
	
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        
        // Key
        sb.append("Key=");
        if( this.keyLength.value() <= 4 ) sb.append( getShortKey() );
        else{ 
            sb.append('[');
            byte[] longKey = getFullKey();
            for( byte b : longKey )
                sb.append(Long.toHexString(b & 0xFF)).append(' ');
            sb.append(']');
        }
        
        // Declared Length
        sb.append(", Declared Length=");
        sb.append( getDeclaredValueLength() );
        
        // Actual Length
        sb.append(", Actual Length=");
        sb.append( getActualValueLength() );
        
        // Value
        sb.append(", Value=[");
        byte[] value = getValue();
        for( byte b : value )
            sb.append(Long.toHexString(b & 0xFF)).append(' ');
        sb.append(']');
        
        sb.append(']');
        return sb.toString();
    }
    
    
    
    
    
/* ********  S T A T I C   M E T H O D S  ********* */    
    
    
    
    /**
     * Return a mapping of keys (up to four bytes) to KLV
     * sets based on an assumed key length and length field
     * encoding scheme.
     * 
     * @param bytes             The byte array to parse
     * @param offset            Where to start parsing
     * @param length            How many bytes to parse
     * @param keyLength         Length of keys assumed in the KLV sets
     * @param lengthEncoding    Flag indicating encoding type
     * @return                  Map of keys to KLVs
     */
    public static Map<Integer,KLV> parseBytes( byte[] bytes, int offset, int length, KeyLength keyLength, LengthEncoding lengthEncoding ){
        Map<Integer,KLV> map = new HashMap<Integer,KLV>();
        
        int currentPos = offset;    // Keep track of where we are
        while( currentPos < offset + length ){
            try{
                KLV klv = new KLV( bytes, currentPos, keyLength, lengthEncoding );
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
     * @param lengthEncoding    field encoding flag
     * @param payloadLength     number of bytes in value
     * @return                  byte array with appropriate length field bytes
     */
    public static byte[] makeLengthField( LengthEncoding lengthEncoding, int payloadLength ){
        
        // Bytes for length encoding
        byte[] bytes = null;
        switch( lengthEncoding ){
            
            // Unsigned integer, one byte long.
            case OneByte: 
                if( payloadLength > 255 ) 
                    throw new IllegalArgumentException( 
                        String.format("Too much data (%d bytes) for one-byte length field encoding.", payloadLength) );
                bytes = new byte[]{ (byte)payloadLength };
                break;
            
            // Unsigned integer, two bytes long, big endian.    
            case TwoBytes: 
                if( payloadLength > 65535 ) 
                    throw new IllegalArgumentException( 
                        String.format("Too much data (%d bytes) for two-byte length field encoding.", payloadLength) );
                bytes = new byte[]{ (byte)(payloadLength >> 8), (byte)payloadLength };
                break;
                
            // (Un?)signed integer, four bytes long, big endian.    
            case FourBytes: 
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
            case BER:
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
                throw new IllegalStateException( "Unknown length field encoding flag: " + lengthEncoding );
        }   // end switch
        return bytes;
    }   // end makeLengthField
    
    
    
	
	
}   // end class KLV
