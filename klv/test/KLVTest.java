import junit.framework.*;
import java.util.*;
/*
 * KLVTest.java
 * JUnit based test
 *
 * Created on March 17, 2007, 2:51 PM
 */

/**
 *
 * @author robert.harder
 */
public class KLVTest extends TestCase {
    
    public KLVTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }


    /**
     * Test of getLength method, of class KLV.
     */
    public void testGetLength() {
        System.out.println("getLength");
        
        KLV klv;
        
        // Meaningless, zero KLV
        klv= new KLV();
        assertEquals(0, klv.getActualValueLength() );
        assertEquals(klv.getDeclaredValueLength(), klv.getActualValueLength() );
        
        // One byte payload
        klv = new KLV( new byte[]{ 0, 1, 1 }, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
        assertEquals(1, klv.getActualValueLength() );
        assertEquals(klv.getDeclaredValueLength(), klv.getActualValueLength() );
        
        // Length field encoding: One Byte
        for( int i = 0; i <= 255; i++ ){
            byte[] bytes = new byte[ 1 + 1 + i ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = (byte)i; // Length, unsigned byte
            for( int j = 2; j < bytes.length; j++ ){
                bytes[j] = (byte)j; // Arbitrary payload
            }   // end for: j
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
            assertEquals(klv.getDeclaredValueLength(), klv.getActualValueLength() );
            assertTrue( klv.getDeclaredValueLength() >= 0 );
            assertEquals(i, klv.getDeclaredValueLength() );
        }   // end for: i
        
        
        // Length field encoding: Two Bytes
        for( int i = 0; i <= 65535; i+=100 ){ // Max two-byte unsigned integer
            byte[] bytes = new byte[ 1 + 2 + i ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = (byte)(i >> 8);
            bytes[2] = (byte)i;
            for( int j = 3; j < bytes.length; j+=10 ){
                bytes[j] = 23;// Arbitrary payload
            }   // end for: j
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.TwoBytes );
            assertEquals(klv.getDeclaredValueLength(), klv.getActualValueLength() );
            assertTrue( klv.getDeclaredValueLength() >= 0 );
            assertEquals(i, klv.getDeclaredValueLength() );
        }   // end for: i
        
        
        // Length field encoding: Four Bytes
        // Java arrays have a maximum size of Integer.MAX_VALUE
        // which is the max value of a four-byte SIGNED integer.
        // that is, (1<<31)-1, but you'll run out of heap space
        // long before that.
        for( int i = 0; i <= (1<<25)-1; i+=10000000 ){  // Move through tests faster
            byte[] bytes = new byte[ 1 + 4 + i ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = (byte)(i >> 24);
            bytes[2] = (byte)(i >> 16);
            bytes[3] = (byte)(i >>  8);
            bytes[4] = (byte)i;
            for( int j = 5; j < bytes.length; j+=10 ){
                bytes[j] = 23;// Arbitrary payload
            }   // end for: j
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.FourBytes );
            assertEquals(klv.getDeclaredValueLength(), klv.getActualValueLength() );
            assertTrue( klv.getDeclaredValueLength() >= 0 );
            assertEquals(i, klv.getDeclaredValueLength() );
        }   // end for: i
        
        
        // Length field encoding: Basic Encoding Rules (BER)
        // Length <= 127
        for( int i = 0; i <= 127; i++ ){
            byte[] bytes = new byte[ 1 + 1 + i ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = (byte)i; // Length, unsigned byte, High bit clear
            for( int j = 2; j < bytes.length; j+=10 ){
                bytes[j] = 23; // Arbitrary payload
            }   // end for: j
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.BER );
            assertEquals(klv.getDeclaredValueLength(), klv.getActualValueLength() );
            assertTrue( klv.getDeclaredValueLength() >= 0 );
            assertEquals(i, klv.getDeclaredValueLength() );
        }   // end for: i
        
        // Length field encoding: Basic Encoding Rules (BER)
        // Length field = 1
        // Length = 0 .. 255
        for( int i = 0; i <= 255; i++ ){
            byte[] bytes = new byte[ 1 + 1 + 1 + i ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = (byte)0x81; // 10000001: One byte follows and is the length field
            bytes[2] = (byte)(i & 0xFF); // Length of payload
            for( int j = 3; j < bytes.length; j+=10 ){
                bytes[j] = 23; // Arbitrary payload
            }   // end for: j
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.BER );
            assertEquals(klv.getDeclaredValueLength(), klv.getActualValueLength() );
            assertTrue( klv.getDeclaredValueLength() >= 0 );
            assertEquals(i, klv.getDeclaredValueLength() );
        }   // end for: i
        
        // Length field encoding: Basic Encoding Rules (BER)
        // Length field = 2
        // Length = 0 .. 65,535
        for( int i = 0; i <= 65535; i+=100 ){
            byte[] bytes = new byte[ 1 + 1 + 2 + i ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = (byte)0x82; // 10000010: Two bytes follow that are the length field
            bytes[2] = (byte)(i >> 8);  // Length of payload, high bits
            bytes[3] = (byte)i;         // Length of payload, low bits
            for( int j = 4; j < bytes.length; j+=10 ){
                bytes[j] = 23;// Arbitrary payload
            }   // end for: j
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.BER );
            assertEquals(klv.getDeclaredValueLength(), klv.getActualValueLength() );
            assertTrue( klv.getDeclaredValueLength() >= 0 );
            assertEquals(i, klv.getDeclaredValueLength() );
        }   // end for: i
        
        
        // Length field encoding: Basic Encoding Rules (BER)
        // Length field = 3 (seems obscure)
        // Length = 0 .. (2^24)-1
        for( int i = 0; i <= (1<<24)-1; i+=1000000 ){
            byte[] bytes = new byte[ 1 + 1 + 3 + i ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = (byte)0x83; // 10000011: Three bytes follow that are the length field
            bytes[2] = (byte)(i >>16);  // Length of payload, high bits
            bytes[3] = (byte)(i >> 8);  // Length of payload, medium bits
            bytes[4] = (byte)i;         // Length of payload, low bits
            for( int j = 5; j < bytes.length; j+=10 ){
                bytes[j] = 23;// Arbitrary payload
            }   // end for: j
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.BER );
            assertEquals(klv.getDeclaredValueLength(), klv.getActualValueLength() );
            assertTrue( klv.getDeclaredValueLength() >= 0 );
            assertEquals(i, klv.getDeclaredValueLength() );
        }   // end for: i
        
        // Length field encoding: Basic Encoding Rules (BER)
        // Length field = 4
        // Length = 0 .. (2^31)-1
        // Java arrays have a maximum size of Integer.MAX_VALUE
        // which is the max value of a four-byte SIGNED integer.
        // that is, (2^31)-1, but you'll run out of heap space
        // long before that.
        for( int i = 0; i <= (1<<25)-1; i+=10000000 ){
            byte[] bytes = new byte[ 1 + 1 + 4 + i ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = (byte)0x84; // 10000100: Four bytes follow that are the length field
            bytes[2] = (byte)(i >>24);  // Length of payload, highest bits
            bytes[3] = (byte)(i >>16);  // Length of payload, high middle bits
            bytes[4] = (byte)(i >> 8);  // Length of payload, low middle bits
            bytes[5] = (byte)i;         // Length of payload, low bits
            for( int j = 6; j < bytes.length; j+=10 ){
                bytes[j] = 23;// Arbitrary payload
            }   // end for: j
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.BER );
            assertEquals(klv.getDeclaredValueLength(), klv.getActualValueLength() );
            assertTrue( klv.getDeclaredValueLength() >= 0 );
            assertEquals(i, klv.getDeclaredValueLength() );
        }   // end for: i
        
        
        // Using byte offsets
        // One fake byte, one byte key, one byte length, two byte payload
        {   byte[] bytes = new byte[]{ 86, 42, 2, 23, 24 };
            klv = new KLV( bytes, 1, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
            assertEquals(klv.getDeclaredValueLength(), klv.getActualValueLength() );
            assertEquals(42,klv.getShortKey()); // Key
            assertEquals(1,klv.getBytesOffset());   // Offset
            assertEquals(2,klv.getDeclaredValueLength());    // Payload
            byte[] value = klv.getValue();
            assertEquals(2,value.length);       // Payload
            assertEquals(23,value[0]);          // Payload
            assertEquals(24,value[1]);          // Payload
        }
        
    }   // testGetLength

    
    
    
    /**
     * Test of getShortKey method, of class KLV.
     */
    public void testGetShortKey() {
        System.out.println("getShortKey");
        
        KLV klv;
        
        // Trivial KLV
        klv = new KLV();
        assertEquals(0, klv.getShortKey() );
        
        // One byte keys and no payload, one byte length field
        for( int i = 0; i < 255; i++ ){
            byte[] bytes = new byte[]{ (byte)i, 0 };
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
            assertEquals(i, klv.getShortKey());
        }   // end for: i
        
        // Two byte keys and no payload, one byte length field
        for( int i = 0; i < 65535; i++ ){
            byte[] bytes = new byte[]{ (byte)(i>>8), (byte)i, 0  };
            klv = new KLV( bytes, KLV.KeyLength.TwoBytes, KLV.LengthEncoding.OneByte );
            assertEquals(i, klv.getShortKey());
        }   // end for: i
        
        
        // Four byte keys and no payload, one byte length field
        for( int i = 0; i < (1<<32)-1; i+=100 ){
            byte[] bytes = new byte[]{ (byte)(i>>24), (byte)(i>>16), (byte)(i>>8), (byte)i, 0  };
            klv = new KLV( bytes, KLV.KeyLength.FourBytes, KLV.LengthEncoding.OneByte );
            assertEquals(i, klv.getShortKey());
        }   // end for: i
        
        
        
        
    }   // end testGetShortKey
    
    
    
    
    
    
    /**
     * Test of getLongKey method, of class KLV.
     */
    public void testGetLongKey() {
        System.out.println("getLongKey");
        
        KLV klv;
        
        // Trivial KLV
        klv = new KLV();
        assertEquals(0, klv.getLongKey()[0] );
        
        // Key lengths of 1,2,4,16 bytes, no payload, one byte length field
        //for( int i = 1; i <= 1000; i++ ){
        for( int i : new int[]{ 1, 2, 4, 16} ){    
            byte[] bytes = new byte[ i + 1 ];
            for( int j = 0; j < i; j++ ){
                bytes[j] = (byte)0xFF; // Arbitrary key values
            }
            klv = new KLV( bytes, KLV.KeyLength.valueOf(i), KLV.LengthEncoding.OneByte );
            byte[] key = klv.getLongKey();
            assertEquals(i,key.length);
            for( int j = 0; j < i; j++ ){
                assertEquals(bytes[j],key[j]);
            }
        }   // end for: i    
    }   // end testGetLongKey
    
    
    
    /**
     * Test of getValue method, of class KLV.
     */
    public void testGetValue() {
        System.out.println("getValue");
        
        KLV klv;
    
        // Trivial KLV
        klv = new KLV();
        assertEquals(0, klv.getValue().length);
        
        // Length field encoding: One Byte
        for( int i = 0; i <= 255; i++ ){
            byte[] bytes = new byte[ 1 + 1 + i ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = (byte)i; // Length, unsigned byte
            for( int j = 2; j < bytes.length; j++ ){
                bytes[j] = (byte)(j-2); // Arbitrary payload
            }   // end for: j
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
            byte[]value = klv.getValue();
            assertEquals(i,value.length);
            for( int j = 0; j < i; j++ ){
                assertEquals(j,value[j]&0xFF);
            }   // end for: j
        }   // end for: i
        
        // Length field encoding: Two Bytes
        for( int i = 0; i <= 100; i+=100 ){ // Max two-byte unsigned integer
            byte[] bytes = new byte[ 1 + 2 + i ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = (byte)(i >> 8);
            bytes[2] = (byte)i;
            for( int j = 3; j < bytes.length; j++ ){
                bytes[j] = (byte)((j-3)%255); // Arbitrary payload
            }   // end for: j
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.TwoBytes );
            byte[]value = klv.getValue();
            assertEquals(i,value.length);
            assertEquals(3,klv.getValueOffset());
            for( int j = 0; j < i; j++ ){
                assertEquals(j%255,(value[j]&0xFF)%255);
            }   // end for: j
        }   // end for: i
        
        
        // Length field encoding: Four Bytes
        for( int i = 0; i <= (1<<25)-1; i+=10000000 ){  // Move through tests faster
            byte[] bytes = new byte[ 1 + 4 + i ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = (byte)(i >> 24);
            bytes[2] = (byte)(i >> 16);
            bytes[3] = (byte)(i >>  8);
            bytes[4] = (byte)i;
            for( int j = 5; j < bytes.length; j+=10 ){
                bytes[j] = (byte)((j-5)%255); // Arbitrary payload
            }   // end for: j
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.FourBytes );
            byte[]value = klv.getValue();
            assertEquals(i,value.length);
            assertEquals(5,klv.getValueOffset());
            for( int j = 0; j < i; j+=10 ){
                assertEquals(j%255,(value[j]&0xFF)%255);
            }   // end for: j
        }   // end for: i
        
        
        // Length field encoding: Basic Encoding Rules (BER)
        // Length <= 127
        for( int i = 0; i <= 127; i++ ){
            byte[] bytes = new byte[ 1 + 1 + i ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = (byte)i; // Length, unsigned byte, High bit clear anyway
            for( int j = 2; j < bytes.length; j+=10 ){
                bytes[j] = (byte)((j-2)%255); // Arbitrary payload
            }   // end for: j
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.BER );
            byte[]value = klv.getValue();
            assertEquals(i,value.length);
            assertEquals(2,klv.getValueOffset());
            for( int j = 0; j < i; j+=10 ){
                assertEquals(j%255,(value[j]&0xFF)%255);
            }   // end for: j
        }   // end for: i
        
        
        // Length field encoding: Basic Encoding Rules (BER)
        // Length field = 1
        // Length = 0 .. 255
        for( int i = 0; i <= 255; i++ ){
            byte[] bytes = new byte[ 1 + 1 + 1 + i ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = (byte)0x81; // 10000001: One byte follows and is the length field
            bytes[2] = (byte)(i & 0xFF); // Length of payload
            for( int j = 3; j < bytes.length; j+=10 ){
                bytes[j] = (byte)((j-3)%255); // Arbitrary payload
            }   // end for: j
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.BER );
            byte[]value = klv.getValue();
            assertEquals(i,value.length);
            assertEquals(3,klv.getValueOffset());
            for( int j = 0; j < i; j+=10 ){
                assertEquals(j%255,(value[j]&0xFF)%255);
            }   // end for: j
        }   // end for: i
        
        
        // Skip BER, length field = 2,3 and jump to =4.
        // Length field encoding: Basic Encoding Rules (BER)
        // Length field = 4
        // Length = 0 .. (2^31)-1
        // Java arrays have a maximum size of Integer.MAX_VALUE
        // which is the max value of a four-byte SIGNED integer.
        // that is, (2^31)-1, but you'll run out of heap space
        // long before that.
        for( int i = 0; i <= (1<<25)-1; i+=10000000 ){
            byte[] bytes = new byte[ 1 + 1 + 4 + i ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = (byte)0x84; // 10000100: Four bytes follow that are the length field
            bytes[2] = (byte)(i >>24);  // Length of payload, highest bits
            bytes[3] = (byte)(i >>16);  // Length of payload, high middle bits
            bytes[4] = (byte)(i >> 8);  // Length of payload, low middle bits
            bytes[5] = (byte)i;         // Length of payload, low bits
            for( int j = 6; j < bytes.length; j+=10 ){
                bytes[j] = (byte)((j-6)%255); // Arbitrary payload
            }   // end for: j
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.BER );
            byte[]value = klv.getValue();
            assertEquals(i,value.length);
            assertEquals(6,klv.getValueOffset());
            for( int j = 0; j < i; j+=10 ){
                assertEquals(j%255,(value[j]&0xFF)%255);
            }   // end for: j
        }   // end for: i
        
        // Do one byte length field again, but change key size
        for( int kl : new int[]{ 1, 2, 4, 16} ){ // Key lengths 1,2,4,16 bytes
            for( int i = 0; i <= 255; i++ ){ // Payload 0..255 bytes
                byte[] bytes = new byte[ kl + 1 + i ];
                for( int j = 0; j < kl; j++ ){
                    bytes[j] = (byte)(j%255); // Arbitrary key
                }
                bytes[kl+0] = (byte)i; // Length, unsigned byte
                for( int j = kl+1; j < bytes.length; j++ ){
                    bytes[j] = (byte)(j-(kl+1)); // Arbitrary payload
                }   // end for: j
                klv = new KLV( bytes, KLV.KeyLength.valueOf(kl), KLV.LengthEncoding.OneByte);
                byte[]value = klv.getValue();
                assertEquals(kl,klv.getKeyLength().value());
                assertEquals(i,value.length);
                assertEquals(kl+1,klv.getValueOffset());
                for( int j = 0; j < i; j+=10 ){
                    assertEquals(j%255,(value[j]&0xFF)%255);
                }   // end for: j
            }   // end for: i
        }   // end for: kl
        
        
    }   // end testGetValue
    
    
    
    
    /**
     * Test of getValueAs32bitSignedInt method, of class KLV.
     */
    public void testGetValueAs32bitSignedInt() {
        System.out.println("getValueAsInt");
        
        KLV klv;
    
        // Trivial KLV
        klv = new KLV();
        assertEquals(0, klv.getValueAs32bitSignedInt() );
    
        // Length field encoding: One byte
        // Payload: One byte
        for( int i = 0; i <= 255; i++ ){
            byte[] bytes = new byte[ 1 + 1 + 1 ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = 1;  // Payload size
            bytes[2] = (byte)i;
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
            assertEquals(i,klv.getValueAs32bitSignedInt());
        }   // end for: i
        
        
        // Length field encoding: One byte
        // Payload: Two bytes
        for( int i = 0; i <= 65535; i++ ){
            byte[] bytes = new byte[ 1 + 1 + 2 ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = 2;  // Payload size
            bytes[2] = (byte)(i>>8);
            bytes[3] = (byte)i;
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
            assertEquals(i,klv.getValueAs32bitSignedInt());
        }   // end for: i
    
        
        // Length field encoding: One byte
        // Payload: Three bytes
        for( int i = 0; i <= (1<<24)-1; i+=100000 ){
            byte[] bytes = new byte[ 1 + 1 + 3 ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = 3;  // Payload size
            bytes[2] = (byte)(i>>16);
            bytes[3] = (byte)(i>>8);
            bytes[4] = (byte)i;
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
            assertEquals(i,klv.getValueAs32bitSignedInt());
        }   // end for: i
        
        
        // Length field encoding: One byte
        // Payload: Four bytes
        for( int i = 0; i <= (1<<31)-1 && i >= 0; i+=100000000 ){
            byte[] bytes = new byte[ 1 + 1 + 4 ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = 4;  // Payload size
            bytes[2] = (byte)(i>>24);
            bytes[3] = (byte)(i>>16);
            bytes[4] = (byte)(i>>8);
            bytes[5] = (byte)i;
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
            assertEquals(i,klv.getValueAs32bitSignedInt());
        }   // end for: i
        
        
        // Length field encoding: One byte
        // Payload: Five bytes
        // Only first four bytes should count
        for( int i = 0; i <= (1<<31)-1 && i >= 0; i+=100000000 ){
            byte[] bytes = new byte[ 1 + 1 + 5 ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = 5;  // Payload size
            bytes[2] = (byte)(i>>24);
            bytes[3] = (byte)(i>>16);
            bytes[4] = (byte)(i>>8);
            bytes[5] = (byte)i;
            bytes[6] = (byte)23; // Arbitrary extra payload byte
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
            assertEquals(i,klv.getValueAs32bitSignedInt());
        }   // end for: i
        
        
        // Check boundary at three bytes
        {   int value = (1<<24)-1; // 24 bits all set
            byte[] bytes = new byte[ 1 + 1 + 3 ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = 3;  // Payload size
            bytes[2] = (byte)(value>>16);
            bytes[3] = (byte)(value>>8);
            bytes[4] = (byte)value;
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
            assertEquals(value,klv.getValueAs32bitSignedInt());
        }
        
        // Check boundary at four bytes
        {   int value = -1; // 32 bits all set
            byte[] bytes = new byte[ 1 + 1 + 4 ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = 4;  // Payload size
            bytes[2] = (byte)(value>>24);
            bytes[3] = (byte)(value>>16);
            bytes[4] = (byte)(value>>8);
            bytes[5] = (byte)value;
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
            assertEquals(value,klv.getValueAs32bitSignedInt());
        }
        
        
    }   // end testGetValueAsInt
    
    
    
    
    /**
     * Test of getValueAs32bitSignedInt method, of class KLV.
     */
    public void testGetValueAsLong() {
        System.out.println("getValueAsLong");
        
        KLV klv;
    
        // Trivial KLV
        klv = new KLV();
        assertEquals(0, klv.getValueAs64bitLong() );
    
        // Length field encoding: One byte
        // Payload: One byte
        for( int i = 0; i <= 255; i++ ){
            byte[] bytes = new byte[ 1 + 1 + 1 ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = 1;  // Payload size
            bytes[2] = (byte)i;
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
            assertEquals(i,klv.getValueAs64bitLong());
        }   // end for: i
        
        
        // Length field encoding: One byte
        // Payload: Four bytes
        for( int i = 0; i <= (1<<32)-1; i+=10000000 ){
            byte[] bytes = new byte[ 1 + 1 + 4 ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = 4;  // Payload size
            bytes[2] = (byte)(i>>24);
            bytes[3] = (byte)(i>>16);
            bytes[4] = (byte)(i>>8);
            bytes[5] = (byte)i;
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
            assertEquals(i,klv.getValueAs64bitLong());
        }   // end for: i
        
        
        // Length field encoding: One byte
        // Payload: Five bytes
        for( long i = 0; i <= (1L<<40)-1; i+=100000000 ){
            byte[] bytes = new byte[ 1 + 1 + 5 ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = 5;  // Payload size
            bytes[2] = (byte)(i>>32);
            bytes[3] = (byte)(i>>24);
            bytes[4] = (byte)(i>>16);
            bytes[5] = (byte)(i>>8);
            bytes[6] = (byte)i;
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
            assertEquals(i,klv.getValueAs64bitLong());
        }   // end for: i
        
        
        // Length field encoding: One byte
        // Payload: Eight bytes
        for( long i = 0; i <= (1<<63)-1 && i >= 0; i+=10000000000L ){
            byte[] bytes = new byte[ 1 + 1 + 8 ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = 8;  // Payload size
            bytes[2] = (byte)(i>>56);
            bytes[3] = (byte)(i>>48);
            bytes[4] = (byte)(i>>40);
            bytes[5] = (byte)(i>>32);
            bytes[6] = (byte)(i>>24);
            bytes[7] = (byte)(i>>16);
            bytes[8] = (byte)(i>>8);
            bytes[9] = (byte)i;
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
            assertEquals(i,klv.getValueAs32bitSignedInt());
        }   // end for: i
        
        
        
        
        // Check boundary at four bytes
        {   long value = (1<<32)-1; // 32 bits all set
            byte[] bytes = new byte[ 1 + 1 + 4 ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = 4;  // Payload size
            bytes[2] = (byte)(value>>24);
            bytes[3] = (byte)(value>>16);
            bytes[4] = (byte)(value>>8);
            bytes[5] = (byte)value;
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
            assertEquals(value,klv.getValueAs64bitLong());
        }
        
        // Check boundary at eight bytes
        {   long value = -1; // 64 bits all set
            byte[] bytes = new byte[ 1 + 1 + 8 ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = 8;  // Payload size
            bytes[2] = (byte)(value>>56);
            bytes[3] = (byte)(value>>48);
            bytes[4] = (byte)(value>>40);
            bytes[5] = (byte)(value>>32);
            bytes[6] = (byte)(value>>24);
            bytes[7] = (byte)(value>>16);
            bytes[8] = (byte)(value>>8);
            bytes[9] = (byte)value;
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
            assertEquals(value,klv.getValueAs64bitLong());
        }
        
    }   // end testGetValueAsLong
    
    
    
    
    /**
     * Test of getValueAsString method, of class KLV.
     */
    public void testGetValueAsString() {
        System.out.println("getValueAsString");
        
        KLV klv;
    
        // Trivial KLV
        klv = new KLV();
        assertEquals("", klv.getValueAsString() );
        
        // One character: A
        {   byte[] bytes = new byte[]{ 42, 1, (byte)'A' };
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
            assertEquals("A",klv.getValueAsString());
        }
        
        // One word: Hello
        {   byte[] bytes = new byte[]{ 42, 5, (byte)'H', (byte)'e', (byte)'l', (byte)'l', (byte)'o' };
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
            assertEquals("Hello",klv.getValueAsString());
        }
        
    }   // end testGetValueAsString
    
    
    
    /**
     * Test of getKLV method, of class KLV.
     */
    public void testGetKLV() {
        System.out.println("getKLV");
        
        KLV klv;
        
        // Make two sub KLVs:
        //  Sub KLV 1: key length=1, length field=1, payload=2 bytes
        //  Sub KLV 2: key length=1, length field=1, payload=2 bytes
        {   byte[] bytes = new byte[]{
                42, 8,          // Overall KLV
                43, 2, 23, 24,  // Sub KLV 1
                44, 2, 25, 26   // Sub KLV 2
            };
            klv = new KLV(bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte);
            KLV k1 = klv.getKLV( 43, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte);
            KLV k2 = klv.getKLV( 44, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte);
            assertNotNull(k1);
            assertNotNull(k2);
            assertEquals(42,klv.getShortKey());
            assertEquals(43,k1.getShortKey());
            assertEquals(44,k2.getShortKey());
            
        }
        
    }   // end testGetKLV
    
    
    
    /**
     * Test of addKLV method, of class KLV.
     */
    public void testAddKLV() {
        System.out.println("toBytes");
        
        KLV klv;
    
        // Add payload of five bytes with one byte key
        {   klv = new KLV();
            byte[] data = new byte[]{ 0, 1, 2, 3, 4 };
            klv.setSubKeyLengthDefault(KLV.KeyLength.OneByte).setSubLengthEncodingDefault(KLV.LengthEncoding.OneByte);
            klv.addKLV(42, data);
            
            KLV k2 = klv.getKLV(42,KLV.KeyLength.OneByte,KLV.LengthEncoding.OneByte);
            assertNotNull(k2);
            byte[] data2 = k2.getValue();
            assertNotNull(data2);
            assertEquals(data.length,data2.length);
            for( int i = 0; i < data.length; i++ ){
                assertEquals(data[i],data2[i]);
            }
            
            k2 = klv.getKLV(42);
            assertNotNull(k2);
            data2 = k2.getValue();
            assertNotNull(data2);
            assertEquals(data.length,data2.length);
            for( int i = 0; i < data.length; i++ ){
                assertEquals(data[i],data2[i]);
            }
        }
    
    }   // end testAddKLV
    
    
    
    /**
     * Test of toBytes method, of class KLV.
     */
    public void testToBytes() {
        System.out.println("toBytes");
        
        KLV klv;
    
        //fail( "Still need to test toBytes().");
    }   // end testToBytes
    
    
    
    /**
     * Test of toString method, of class KLV.
     */
    public void testToString() {
        System.out.println("toString");
        
        KLV instance = new KLV();
        
        String expResult = "";
        String result = instance.toString();
        //assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
    
}
