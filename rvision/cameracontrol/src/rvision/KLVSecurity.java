package rvision;



import java.io.*;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * <p>This KLV set would in turn have KLV sets within it
 * with more information about the digital signature.
 * Inside, there will be three KLV sets:</p>
 * 
 * <ol>
 *  <li>A payload wrapper:
 *   <ol>
 *    <li>The original data</li>
 *    <li>A 4-byte counter, on a per-public key basis</li>
 *   </ol>
 *  </li>
 *  <li>A string naming the signature type (e.g. "SHA1withDSA")</li>
 *  <li>The digital signature itself</li>
 * </ol>
 * @author robert.harder
 */
public class KLVSecurity {
    
    private final static Logger LOGGER = Logger.getLogger( KLVSecurity.class.getName() );
    
    
    /** KLV key lengths are one byte. */
    public final static KLV.KeyLength KEY_LENGTH = KLV.KeyLength.OneByte;
    
    /** KLV length encoding is BER. */
    public final static KLV.LengthEncoding LENGTH_ENCODING = KLV.LengthEncoding.BER;
    


    /**
     * This key (0xFF01) comes inside a {@link #DIGITAL_SIGNATURE_KLV_KEY}
     * KLV set, and its value is a string representing the type of
     * signature provided such as <tt>SHA1withDSA</tt>.
     */
    public final static int PAYLOAD_WRAPPER_KEY = 1;

        /** The original data to be wrapped up and signed (key = 1). */
        public final static int PAYLOAD_WRAPPER_ORIGINAL_DATA_KEY = 1;

        /**
         * A monotonically-increasing counter, tracked on a per-public key basis,
         * that is added to the original data (key = 2). 
         * In order to avoid a replay attack where the data is captured
         * and resent, signature and all and thereby "spoofing" the sender, 
         * the counter ensures that a packet that has already been received 
         * will not be re-evaluated.
         */
        public final static int PAYLOAD_WRAPPER_COUNTER_KEY = 2;

    /** The signature type, such as "SHA1withDSA" (key = 2) */
    public final static int SIGNATURE_TYPE_KEY = 2;

    /** The actual signature bytes (key = 3). */
    public final static int DIGITAL_SIGNATURE_KEY = 3;
    
    
    private Map<PublicKey,Integer> publicKeysToCounters = new HashMap<PublicKey,Integer>();
    private Map<PrivateKey,Integer> privateKeysToCounters = new HashMap<PrivateKey,Integer>();
    
    private final static String SIGNATURE_TYPE_DEFAULT = "SHA1withDSA";
    private String signatureType;
    private Signature signature;                    // Cached
    
    
    /**
     * Creates a new KLVSecurity object to hold authorized public keys.
     */
    protected KLVSecurity( String sigType, Signature sig ){
        this.signatureType = sigType;
        this.signature = sig;
    }
    
    
    
    public static KLVSecurity getInstance() throws NoSuchAlgorithmException{
        return getInstance( SIGNATURE_TYPE_DEFAULT );
    }
    
    
    
    /**
     * Attempts to create a KLVSecurity object using the given
     * signature type, such as <tt>SHA1withDSA</tt>. This value
     * is used in the <tt>java.security.Signature</tt> method
     * <tt>Signature.getInstance( sigType )</tt>.
     * @param signatureType
     * @return
     */
    public static KLVSecurity getInstance( String sigType ) throws NoSuchAlgorithmException{
        Signature sig = Signature.getInstance(sigType);
        return new KLVSecurity (sigType, sig );
    }
    
    
    
    /**
     * Adds a java.security.PublicKey to the list of 
     * authorized keys whose private pair can sign a command.
     * @param key
     */
    public void addAuthorizedPublicKey( PublicKey key ){
        synchronized( this.publicKeysToCounters ){
            this.publicKeysToCounters.put( key, 0 );
        }
    }
    
    
    
    
    /**
     * Removes a java.security.PublicKey from the list of 
     * authorized keys whose private pair can sign a command.
     * @param key
     */
    public void removeAuthorizedPublicKey( PublicKey key ){
        synchronized( this.publicKeysToCounters ){
            this.publicKeysToCounters.remove( key );
        }
    }
    
    

    
    
    
    
    /**
     * Looks for the three KLV sets that make up a KLVSecurity package
     * and extracts the original payload, throwing a SignatureException
     * if anything is wrong with the data but never returning null.
     * 
     * @param data      The byte array containing the KLVSecurity set
     * @param offset    The offset to begin
     * @param length    The number of bytes to process
     * @return          The original, verified byte array
     * @throw           SignatureException if anything is wrong
     * @throw           NullPointerException if <tt>data</tt> is null
     * @throw           IndexOutOfBoundsException if <tt>offset</tt> 
     *                  or <tt>length</tt> are invalid
     */
    public byte[] extractOriginalData( byte[] data, int offset, int length )
    throws java.security.SignatureException {
        if( data == null ){
            throw new NullPointerException( "Byte array must not be null." );
        } else if( offset < 0 || offset >= data.length ){
            throw new IndexOutOfBoundsException( "Offset out of range: " + offset );
        } else if( length < 0 || offset + length > data.length ){
            throw new IndexOutOfBoundsException( "Length out of range: " + length );
        }   // end else
        
        List<KLV> subs = KLV.bytesToList(data, offset, length, KEY_LENGTH, LENGTH_ENCODING);
        
        KLV payloadWrapperKlv = null;   // The wrapper with a counter and original data
        KLV sigTypeKlv = null;          // The signature type (e.g. "SHA1withDSA")
        KLV sigKlv = null;              // The signature itself
        
        for( KLV k : subs ){
            switch( k.getShortKey() ){
                case PAYLOAD_WRAPPER_KEY:
                    payloadWrapperKlv = k;
                    break;
                case SIGNATURE_TYPE_KEY:
                    sigTypeKlv = k;
                    break;
                case DIGITAL_SIGNATURE_KEY:
                    sigKlv = k;
                    break;
                default:
                    LOGGER.warning( 
                      "Unknown KLV key 0x" + 
                      Long.toHexString(k.getShortKey() & 0xFFFFFFFF) + 
                      " inside KLVSecurity set." );
                    break;
            }   // end switch
        }   // end for: each sub
        
        // Did we get all the components we needed?
        if( payloadWrapperKlv == null ){
            throw new SignatureException( "No payload wrapper inside KLVSecurity set." );
        } else if( sigTypeKlv == null ){
            throw new SignatureException( "No declared signature type inside KLVSecurity set." );
        } else if( sigKlv == null ){
            throw new SignatureException( "No digital signature inside KLVSecurity set." );
        }   // end if
        
        
        // Now check signature
        PublicKey verifyingKey = null;                              // Set this if we find the right key
        byte[] toVerify = payloadWrapperKlv.toBytes();              // The data to verify
        String sigType  = sigTypeKlv.getValueAsString();            // The signature type (e.g., SHA1withDSA
        byte[] sigBytes = sigKlv.getValue();                        // The signature itself
        try{
            Signature sig = Signature.getInstance(sigType);         // Signing technique
            for( PublicKey key : this.publicKeysToCounters.keySet() ){    // Loop over authorized keys
                try{
                    sig.initVerify(key);                            // Set up sig object
                    sig.update( toVerify );                         // Bytes that were signed
                    if( sig.verify(sigBytes) ){                     // Valid signature?
                        verifyingKey = key;                         // Mark as valid
                        if( LOGGER.isLoggable(Level.FINEST) ){
                            LOGGER.finest( "Found public key that verified data: " + key );
                        }   // end if: finest
                        break;                                      // Out of for loop
                    }   // end if: valid
                } catch( InvalidKeyException exc ){
                    LOGGER.warning("Invalid key, continuing: " + key );
                }   // end catch
            }   // end for: each known key
        } catch( NoSuchAlgorithmException exc ){
            throw new SignatureException( "Cannot find algorithm " + sigType, exc );
        }   // end catch: unknown algorithm
        
        // If we found a key that verifies the data, then check the counter
        // to make sure it's greater than what we've seen before.
        if( verifyingKey == null ){
            throw new SignatureException( "No public key found that could verify signature." );
        }else {
            Map<Integer,KLV> payloadSubs = payloadWrapperKlv.getSubKLVMap();
            KLV originalKlv = payloadSubs.get( this.PAYLOAD_WRAPPER_ORIGINAL_DATA_KEY );
            KLV counterKlv  = payloadSubs.get( this.PAYLOAD_WRAPPER_COUNTER_KEY );
            
            if( originalKlv == null ){
                throw new SignatureException( "No original data found." );
            } else if( counterKlv == null ){
                throw new SignatureException( "No counter found." );
            }   // end if
            
            int sigCounter = counterKlv.getValueAs32bitInt();           // Counter in packet
            int prevCounter = this.publicKeysToCounters.get(verifyingKey);    // Previous counter
            if( sigCounter <= prevCounter ){                            // Old counter?
                throw new SignatureException( "Counter out of date; possible replay attack stopped." );
            }   // end if: old counter
            this.publicKeysToCounters.put( verifyingKey, sigCounter );        // Update saved counter
                    
            return originalKlv.getValue();
        }   // end else: found key
    }   // end extract
    
    
    
    
    
    
    /**
     * Wraps bytes from an array in a KLVSecurity wrapper, signing
     * the data with the given private key. A counter, matched to the
     * private key, is added to the original data to prevent
     * replay attacks. May return null if there is an error.
     * The returned array (if not null) contains three KLV sets
     * in tandem: Payload wrapper, Signature type, and Signature.
     * The array is suitable for inserting into another KLV set
     * with key of your choosing that fits into your scheme or
     * any other binary form you choose.
     * 
     * @param data
     * @param offset
     * @param length
     * @param key
     * @return
     */
    public byte[] wrapOriginalData( byte[] data, int offset, int length, PrivateKey key ){
        if( data == null ){
            throw new NullPointerException( "Byte array must not be null." );
        } else if( offset < 0 || offset >= data.length ){
            throw new IndexOutOfBoundsException( "Offset out of range: " + offset );
        } else if( length < 0 || offset + length > data.length ){
            throw new IndexOutOfBoundsException( "Length out of range: " + length );
        }   // end else
        
        
        // KLV Payload Wrapper that will be signed
        KLV payloadWrapperKlv = new KLV( PAYLOAD_WRAPPER_KEY, KEY_LENGTH, LENGTH_ENCODING );
        
        // Add the KLV set for the original data
        payloadWrapperKlv.addSubKLV( new KLV( 
          PAYLOAD_WRAPPER_ORIGINAL_DATA_KEY, KEY_LENGTH, LENGTH_ENCODING, data, offset, length ) );
        
        // Add the KLV set for the counter
        Integer lastCounter = this.privateKeysToCounters.get( key );
        if( lastCounter == null ){
            lastCounter = 0;
        }   // end if: previously unknown key
        int newCounter = lastCounter + 1;
        this.privateKeysToCounters.put( key, newCounter );
        payloadWrapperKlv.addSubKLV(PAYLOAD_WRAPPER_COUNTER_KEY, newCounter);
        
        // Sign
        Signature sig = this.signature;
        if( sig == null ){
            return null;
        } else {
            try{
                sig.initSign(key);                                      // Set up with private key
            } catch( InvalidKeyException exc ){
                LOGGER.warning("Invalid key: " + exc.getMessage() );
                return null;
            }   // end catch
            byte[] payloadWrapperKlvBytes = payloadWrapperKlv.toBytes();// Bytes to sign
            byte[] signed = null;
            try{
                sig.update(payloadWrapperKlvBytes);                  // Add bytes to sign
                signed = sig.sign();                                 // Sign
            } catch( SignatureException exc ){
                LOGGER.warning( "Could not sign data: " + exc.getMessage() );
                return null;
            }   // end catch
            assert signed != null : signed;     // Otherwise we would have had a SignatureException
            
            KLV sigKlv  = new KLV( 
              DIGITAL_SIGNATURE_KEY, KEY_LENGTH, LENGTH_ENCODING, signed );
            KLV typeKlv = new KLV( 
              SIGNATURE_TYPE_KEY, KEY_LENGTH, LENGTH_ENCODING, this.signatureType.getBytes() );
            
            byte[] sigKlvBytes = sigKlv.toBytes();
            byte[] typeKlvBytes = typeKlv.toBytes();
            
            byte[] klvSec = new byte[               // Make new array big enough to hold
              payloadWrapperKlvBytes.length +       // Payload wrapper...
              typeKlvBytes.length +                 // Signature type...
              sigKlvBytes.length ];                 // And signature.
            
            System.arraycopy(                       // Copy payloadWrapperKlvBytes
              payloadWrapperKlvBytes,0, 
              klvSec,0,payloadWrapperKlvBytes.length);
            
            System.arraycopy(                       // Copy typeKlvBytes
              typeKlvBytes,0,
              klvSec,payloadWrapperKlvBytes.length, typeKlvBytes.length );
            
            System.arraycopy(                       // Copy sigKlvBytes
              sigKlvBytes,0,
              klvSec,payloadWrapperKlvBytes.length + typeKlvBytes.length,sigKlvBytes.length);
            
            
            return klvSec;
        }   // end else
    }
    
    
    
/* ********  S T A T I C   H E L P E R   M E T H O D S  ******** */
    
    
    /**
     * Attempts to read an X509 public key from the given file.
     * Returns null if unable to do so.
     * @param file
     * @return
     */
    public static PublicKey readPublicKey( File file ){
        PublicKey pubKey = null;
        String keyType = "DSA";
        try{
            InputStream in = new BufferedInputStream(  new FileInputStream(file) );
            byte[] pubKeyBytes = new byte[in.available()];  
            in.read(pubKeyBytes);
            in.close();
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(pubKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(keyType);
            pubKey = keyFactory.generatePublic(pubKeySpec);
        } catch( Exception exc ){
            LOGGER.warning( "Could not read public key: " + exc.getMessage() );
        }
        return pubKey;
    }
    
    
    /**
     * Attempts to read a PKCS8 private key from the given file.
     * Returns null if unable to do so.
     * @param file
     * @return
     */
    public static PrivateKey readPrivateKey( File file ){
        PrivateKey key = null;
        String keyType = "DSA";
        try{
            InputStream in = new BufferedInputStream(  new FileInputStream(file) );
            byte[] keyBytes = new byte[in.available()];  
            in.read(keyBytes);
            in.close();
            
            KeyFactory keyFactory = KeyFactory.getInstance(keyType);
            PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(keyBytes);
            key = keyFactory.generatePrivate(privKeySpec);
        } catch( Exception exc ){
            LOGGER.warning( "Could not read private key: " + exc.getMessage() );
        }
        return key;
    }
    
    
    
    /**
     * Writes a public or private key to a file.
     * @param key
     * @param file
     */
    public static void writeKey( Key key, File file ){
        OutputStream out = null;
        try{
            out = new BufferedOutputStream( new FileOutputStream( file ) );
            out.write(key.getEncoded());
        } catch( Exception exc ){
            LOGGER.warning("Could not save key to file: " + exc.getMessage() );
        } finally {
            try{ out.close(); }
            catch( Exception exc ){}
        }
    }
    
    

    
    /**
     * Creates a java.security.KeyPair with suitable Public and Private keys
     * to use with KLVSecurity.
     * @return
     */
    public static KeyPair generateKeyPair(){
        String keyType = "DSA";
        KeyPair pair = null;
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(keyType);
            kpg.initialize(1024, new SecureRandom());
            pair = kpg.genKeyPair();
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.warning( "Could not generate key pair: " + ex.getMessage() );
        }   // end catch
        return pair;
    }
    
    
}
