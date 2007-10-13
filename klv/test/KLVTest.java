import junit.framework.*;
import java.util.*;


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
        assertEquals(0, klv.getFullKey()[0]  );
        
        // Key lengths of 1,2,4,16 bytes, no payload, one byte length field
        //for( int i = 1; i <= 1000; i++ ){
        for( int i : new int[]{ 1, 2, 4, 16} ){    
            byte[] bytes = new byte[ i + 1 ];
            for( int j = 0; j < i; j++ ){
                bytes[j] = (byte)0xFF; // Arbitrary key values
            }
            klv = new KLV( bytes, KLV.KeyLength.valueOf(i), KLV.LengthEncoding.OneByte );
            byte[] key = klv.getFullKey();
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
    
    
    
/* ********  8   B I T   I N T  ******** */ 
    
    /**
     * Test of getValueAs8bitInt method, of class KLV.
     */
    public void testGetValueAs8bitUnsignedInt() {
        System.out.println("getValueAs8bitUnsignedInt");
        
        KLV klv;
    
        // Trivial KLV
        klv = new KLV();
        assertEquals(0, klv.getValueAs8bitUnsignedInt() );
    
        // Length field encoding: One byte
        // Payload: One byte
        for( int i = 0; i <= 255; i++ ){
            byte[] bytes = new byte[ 1 + 1 + 1 ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = 1;  // Payload size
            bytes[2] = (byte)i;
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
            assertEquals(i,klv.getValueAs8bitUnsignedInt());
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
            if( i <= 255 )  
                assertEquals(0,klv.getValueAs8bitUnsignedInt());
            else
                assertEquals( (i>>8)&0xFF, klv.getValueAs8bitUnsignedInt() );
        }   // end for: i
    
        
        // Length field encoding: Two bytes
        // Payload: Two bytes
        for( int i = 0; i <= 65535; i++ ){
            byte[] bytes = new byte[ 2 + 1 + 2 ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = 0;  // Payload size
            bytes[2] = 2;  // Payload size
            bytes[3] = (byte)(i>>8);
            bytes[4] = (byte)i;
            klv = new KLV( bytes, KLV.KeyLength.TwoBytes, KLV.LengthEncoding.OneByte );
            if( i <= 255 )  
                assertEquals(0,klv.getValueAs8bitUnsignedInt());
            else
                assertEquals( (i>>8)&0xFF, klv.getValueAs8bitUnsignedInt() );
        }   // end for: i
    
    }   // end testGetValueAs8bitUnsignedInt
    
    
    
    
    /**
     * Test of getValueAs8bitInt method, of class KLV.
     */
    public void testGetValueAs8bitSignedInt() {
        System.out.println("getValueAs8bitSignedInt");
        
        KLV klv;
    
        // Trivial KLV
        klv = new KLV();
        assertEquals(0, klv.getValueAs8bitSignedInt() );
    
        // Length field encoding: One byte
        // Payload: One byte
        for( int i = 0; i <= 255; i++ ){
            byte[] bytes = new byte[ 1 + 1 + 1 ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = 1;  // Payload size
            bytes[2] = (byte)i;
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
            if( i <= (1<<7)-1 )
                assertEquals(i,klv.getValueAs8bitSignedInt());
            else
                assertEquals(i-(1<<8),klv.getValueAs8bitSignedInt());
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
            if( i <= (1<<8)-1 )  
                assertEquals(0,klv.getValueAs8bitSignedInt());
            else{
                int temp = (i>>8);
                if( temp <= (1<<7)-1 )
                    assertEquals(temp,klv.getValueAs8bitSignedInt());
                else
                    assertEquals(temp-(1<<8),klv.getValueAs8bitSignedInt());
            }
        }   // end for: i
    
        
        // Length field encoding: Two bytes
        // Payload: Two bytes
        for( int i = 0; i <= 65535; i++ ){
            byte[] bytes = new byte[ 2 + 1 + 2 ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = 0;  // Payload size
            bytes[2] = 2;  // Payload size
            bytes[3] = (byte)(i>>8);
            bytes[4] = (byte)i;
            klv = new KLV( bytes, KLV.KeyLength.TwoBytes, KLV.LengthEncoding.OneByte );
            if( i <= 255 )  
                assertEquals(0,klv.getValueAs8bitSignedInt());
            else{
                int temp = (i>>8);
                if( temp <= 127 )
                    assertEquals(temp,klv.getValueAs8bitSignedInt());
                else
                    assertEquals(temp-256,klv.getValueAs8bitSignedInt());
            }
        }   // end for: i
    
    }   // end testGetValueAs8bitSignedInt
    
    
    
/* ********  1 6   B I T   I N T  ******** */   
    
    
    /**
     * Test of getValueAs16bitInt method, of class KLV.
     */
    public void testGetValueAs16bitUnsignedInt() {
        System.out.println("getValueAs16bitUnsignedInt");
        
        KLV klv;
    
        // Trivial KLV
        klv = new KLV();
        assertEquals(0, klv.getValueAs16bitUnsignedInt() );
    
        // Length field encoding: One byte
        // Payload: One byte
        for( int i = 0; i <= 255; i++ ){
            byte[] bytes = new byte[ 1 + 1 + 1 ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = 1;  // Payload size
            bytes[2] = (byte)i;
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
            assertEquals(i,klv.getValueAs16bitUnsignedInt());
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
            assertEquals(i,klv.getValueAs16bitUnsignedInt());
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
            if( i <= 65535 )  
                assertEquals(0,klv.getValueAs16bitUnsignedInt());
            else
                assertEquals( (i>>8)&0xFFFF, klv.getValueAs16bitUnsignedInt() );
        }   // end for: i
        
    }   // end testGetValueAs16bitUnsignedInt
    
    
    /**
     * Test of getValueAs16bitInt method, of class KLV.
     */
    public void testGetValueAs16bitSignedInt() {
        System.out.println("getValueAs16bitSignedInt");
        
        KLV klv;
    
        // Trivial KLV
        klv = new KLV();
        assertEquals(0, klv.getValueAs16bitSignedInt() );
    
        // Length field encoding: One byte
        // Payload: One byte
        for( int i = 0; i <= 255; i++ ){
            byte[] bytes = new byte[ 1 + 1 + 1 ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = 1;  // Payload size
            bytes[2] = (byte)i;
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
            assertEquals(i,klv.getValueAs16bitSignedInt());
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
            if( i <= (1<<15)-1 )
                assertEquals(i,klv.getValueAs16bitSignedInt());
            else
                assertEquals(i-(1<<16),klv.getValueAs16bitSignedInt());
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
            if( i <= (1<<16)-1 )  
                assertEquals(0,klv.getValueAs16bitSignedInt());
            else{
                int temp = (i>>8);
                if( temp <= (1<<15)-1 )
                    assertEquals(temp,klv.getValueAs16bitSignedInt());
                else
                    assertEquals(temp-(1<<16),klv.getValueAs16bitSignedInt());
            }
        }   // end for: i
        
    }   // end testGetValueAs16bitSignedInt
    
    
    
    
    
/* ********  3 2   B I T   I N T  ******** */    
    
    /**
     * Test of getValueAs32bitSignedInt method, of class KLV.
     */
    public void testGetValueAs32bitInt() {
        System.out.println("getValueAs32bitInt");
        
        KLV klv;
    
        // Trivial KLV
        klv = new KLV();
        assertEquals(0, klv.getValueAs32bitInt() );
    
        // Length field encoding: One byte
        // Payload: One byte
        for( int i = 0; i <= 255; i++ ){
            byte[] bytes = new byte[ 1 + 1 + 1 ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = 1;  // Payload size
            bytes[2] = (byte)i;
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
            assertEquals(i,klv.getValueAs32bitInt());
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
            assertEquals(i,klv.getValueAs32bitInt());
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
            assertEquals(i,klv.getValueAs32bitInt());
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
            assertEquals(i,klv.getValueAs32bitInt());
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
            assertEquals(i,klv.getValueAs32bitInt());
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
            assertEquals(value,klv.getValueAs32bitInt());
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
            assertEquals(value,klv.getValueAs32bitInt());
        }
        
        
    }   // end testGetValueAsInt
    
    
    
/* ********  6 4   B I T   L O N G  ******** */   
    
    /**
     * Test of getValueAs32bitSignedInt method, of class KLV.
     */
    public void testGetValueAs64bitLong() {
        System.out.println("getValueAs64bitLong");
        
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
            assertEquals(i,klv.getValueAs32bitInt());
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
    
    
    
    
/* ********  F L O A T  ******** */   
    
    /**
     * Test of getValueAsFloat method, of class KLV.
     */
    public void testGetValueAsFloat() {
        System.out.println("getValueAsFloat");
        
        KLV klv;
    
        // Trivial KLV: NaN if not enough bytes exist
        klv = new KLV();
        assertEquals(Float.NaN, klv.getValueAsFloat() );
        
        // Load Float - Unload same float
        // Key length: one byte
        // Length field: one byte
        for( float f = (float)Math.PI; f < 1000; f += 1.234f ){
            int bits = Float.floatToIntBits(f);
            byte[] bytes = new byte[ 1 + 1 + 4 ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = 4;  // Payload size
            for( int i = 0; i < 4; i++ )
                bytes[i+2] = (byte)((bits >> (3-i)*8)&0xFF);
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
            assertEquals(f,klv.getValueAsFloat());
        }
        
        // Some constants
        // Key length: one byte
        // Length field: one byte
        {
            float[] ff = new float[]{
                Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY,
                Float.MAX_VALUE,
                Float.MIN_VALUE,
                Float.NaN
            };
            for( float f : ff ){
                int bits = Float.floatToIntBits(f);
                byte[] bytes = new byte[ 1 + 1 + 4 ];
                bytes[0] = 42; // Arbitrary key
                bytes[1] = 4;  // Payload size
                for( int i = 0; i < 4; i++ )
                    bytes[i+2] = (byte)((bits >> (3-i)*8)&0xFF);
                klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
                assertEquals(f,klv.getValueAsFloat());
            }   // end for: each float
        }
        
        
        // Known Positive Infinity: 0x7f800000
        // Key length: one byte
        // Length field: one byte
        {
            byte[] bytes = new byte[ 1 + 1 + 4 ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = 4;  // Payload size
            bytes[2] = (byte)0x7F;
            bytes[3] = (byte)0x80;
            bytes[4] = 0;
            bytes[5] = 0;
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
            assertEquals(Float.POSITIVE_INFINITY,klv.getValueAsFloat());
        }
        
        // Known Negative Infinity: 0xff800000
        // Key length: one byte
        // Length field: one byte
        {
            byte[] bytes = new byte[ 1 + 1 + 4 ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = 4;  // Payload size
            bytes[2] = (byte)0xFF;
            bytes[3] = (byte)0x80;
            bytes[4] = 0;
            bytes[5] = 0;
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
            assertEquals(Float.NEGATIVE_INFINITY,klv.getValueAsFloat());
        }
    
    }   // end testGetValueAsFloat
    
    
    
    
/* ********  D O U B L E  ******** */   
    
    /**
     * Test of getValueAsDouble method, of class KLV.
     */
    public void testGetValueAsDouble() {
        System.out.println("getValueAsDouble");
        
        KLV klv;
    
        // Trivial KLV
        klv = new KLV();
        assertEquals(Double.NaN, klv.getValueAsDouble() );
        
        // Load double - Unload same double
        // Key length: one byte
        // Length field: one byte
        for( double d = Math.PI; d < 1000; d += 1.234 ){
            long bits = Double.doubleToLongBits(d);
            byte[] bytes = new byte[ 1 + 1 + 8 ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = 8;  // Payload size
            for( int i = 0; i < 8; i++ )
                bytes[i+2] = (byte)((bits >> (7-i)*8)&0xFF);
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
            assertEquals(d,klv.getValueAsDouble());
        }
        
        // Some constants
        // Key length: one byte
        // Length field: one byte
        {
            double[] dd = new double[]{
                Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY,
                Double.MAX_VALUE,
                Double.MIN_VALUE,
                Double.NaN
            };
            for( double d : dd ){
                long bits = Double.doubleToLongBits(d);
                byte[] bytes = new byte[ 1 + 1 + 8 ];
                bytes[0] = 42; // Arbitrary key
                bytes[1] = 8;  // Payload size
                for( int i = 0; i < 8; i++ )
                    bytes[i+2] = (byte)((bits >> (7-i)*8)&0xFF);
                klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
                assertEquals(d,klv.getValueAsDouble());
            }   // end for: each float
        }
        
        
        // Known Positive Infinity: 0x7FF00000 00000000
        // Key length: one byte
        // Length field: one byte
        {
            byte[] bytes = new byte[ 1 + 1 + 8 ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = 8;  // Payload size
            bytes[2] = (byte)0x7F;
            bytes[3] = (byte)0xF0; // Remaining bytes are zero
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
            assertEquals(Double.POSITIVE_INFINITY,klv.getValueAsDouble());
        }
        
        // Known Negative Infinity: 0xFFF00000 00000000
        // Key length: one byte
        // Length field: one byte
        {
            byte[] bytes = new byte[ 1 + 1 + 8 ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = 8;  // Payload size
            bytes[2] = (byte)0xFF;
            bytes[3] = (byte)0xF0; // Remaining bytes are zero
            klv = new KLV( bytes, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
            assertEquals(Double.NEGATIVE_INFINITY,klv.getValueAsDouble());
        }
    
    }   // end testGetValueAsDouble
    
    
    
    
/* ********  S T R I N G  ******** */  
    
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
            KLV k1 = klv.getSubKLV( 43, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte);
            KLV k2 = klv.getSubKLV( 44, KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte);
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
    public void testAddSubKLV() {
        System.out.println("addSubKLV");
        
        KLV klv;
    
        // Add one-byte subKLV
        for( int i = 0; i < 255; i++ ){
            klv = new KLV();
            klv.addSubKLV(42, (byte)i);
            klv.addSubKLV(23, (byte)((i+10)%255));
            KLV k42 = klv.getSubKLV(42);
            KLV k23 = klv.getSubKLV(23);
            assertEquals(i,k42.getValueAs8bitUnsignedInt());
            assertEquals((i+10)%255,k23.getValueAs8bitUnsignedInt());
        }
        
        
        // Add two-byte subKLV
        for( int i = 0; i < 65535; i++ ){
            klv = new KLV();
            klv.addSubKLV(42, (short)i);
            klv.addSubKLV(23, (short)((i+10)%65535));
            KLV k42 = klv.getSubKLV(42);
            KLV k23 = klv.getSubKLV(23);
            assertEquals(i,k42.getValueAs16bitUnsignedInt());
            assertEquals((i+10)%65535,k23.getValueAs16bitUnsignedInt());
        }
        
        // Add four-byte subKLV
        for( int i = 0; i <= (1<<32)-1; i+=10000000 ){
            klv = new KLV();
            klv.addSubKLV(42, i);
            klv.addSubKLV(23, ((i+10)%987654321));
            KLV k42 = klv.getSubKLV(42);
            KLV k23 = klv.getSubKLV(23);
            assertEquals(i,k42.getValueAs32bitInt());
            assertEquals((i+10)%987654321,k23.getValueAs32bitInt());
        }
        
        // Add String subKLV
        {
            String[] words = new String[]{ null, "", "a", "cat" };
            for( String word : words ){
                klv = new KLV();
                klv.addSubKLV(42,word);
                klv.addSubKLV(23,word+word);
                KLV k42 = klv.getSubKLV(42);
                KLV k23 = klv.getSubKLV(23);
                if( word == null ){
                    assertEquals("",k42.getValueAsString());
                    assertEquals("nullnull",k23.getValueAsString());
                } else {
                    assertEquals(word,k42.getValueAsString());
                    assertEquals(word+word,k23.getValueAsString());
                }
            }
        }
        
    }   // end testAddKLV
    
    
    
    
/* ********  S E T   K E Y  ******** */
    
    
    
    public void testSetKey() {
        System.out.println("setKey");
        
        KLV klv;
        
        // Same key length
        for( int i = 1; i < (1<<31); i = (i << 1) + i ){
            klv = new KLV(); // Four byte key by default
            //System.out.println("Setting key to " + i );
            klv.setKey(i);
            assertEquals(i,klv.getShortKey());
        }
        
        // Change from four- to one-byte key
        for( int i = 0; i < (1<<7)-1; i++ ){
            klv = new KLV();
            klv.setKey(i, KLV.KeyLength.OneByte );
            assertEquals( i, klv.getShortKey() );
        }
        
        // Change from four- to two-byte key
        for( int i = 1; i < (1<<15)-1; i = (i<<1)+i ){
            klv = new KLV();
            klv.setKey(i, KLV.KeyLength.TwoBytes );
            assertEquals( i, klv.getShortKey() );
        }
        
        
        // Change from one- to two-byte key
        for( int i = 1; i < (1<<15)-1; i = (i<<1)+i ){
            klv = new KLV( new byte[]{ (byte)42, 1, 23 }, 
                    KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
            klv.setKey(i, KLV.KeyLength.TwoBytes );
            assertEquals( i, klv.getShortKey() );
        }
        
        // Change from one- to four-byte key
        for( int i = 1; i < (1<<31); i = (i << 1) + i ){
            klv = new KLV( new byte[]{ (byte)42, 1, 23 }, 
                    KLV.KeyLength.OneByte, KLV.LengthEncoding.OneByte );
            klv.setKey(i, KLV.KeyLength.FourBytes );
            assertEquals( i, klv.getShortKey() );
        }
        
        // Change from four- to sixteen-byte key
        {
            klv = new KLV();
            byte[] key = new byte[16];
            for( int i = 0; i < 16; i++ ){
                key[i] = (byte)((i+23)%255);
            }
            klv.setKey(key);
            byte[] returnedKey = klv.getFullKey();
            assertEquals(16,returnedKey.length);
            for( int i = 0; i < 16; i++ ){
                assertEquals((i+23)%255,returnedKey[i]);
            }
        }
        
        
    }   // end testSetKey
    
    
    
    
/* ********  S E T   V A L U E  ******** */
    
    
    
    public void testSetValue() {
        System.out.println("setValue");
        
        KLV klv;
        
        
        
        // Add one byte
        {
            klv = new KLV();
            klv.setValue( new byte[]{ (byte)42 } );
            assertEquals( 42, klv.getValueAs32bitInt() );
            assertEquals( 1, klv.getActualValueLength() );
            assertEquals( 1, klv.getDeclaredValueLength() );
        }
        
        
        // Add one byte, then clear
        {
            klv = new KLV();
            klv.setValue( new byte[]{ (byte)42 } );
            klv.setValue( new byte[0] );
            assertEquals( 0, klv.getValueAs32bitInt() );
            assertEquals( 0, klv.getActualValueLength() );
            assertEquals( 0, klv.getDeclaredValueLength() );
        }
        
        // Add many bytes
        for( int i = 0; i < 1000; i+=10 ){
            byte[] bytes = new byte[i];
            for( int j = 0; j < i; j++ ){
                bytes[j] = (byte)((j + 23) % 255); // Arbitrary
            }   // end for: fill array
            klv = new KLV();
            klv.setValue(bytes);
            byte[] value = klv.getValue();
            for( int j = 0; j < i; j++ ){
                assertEquals(bytes[j],value[j]);
            }
        }
        
        
    }
    
    
    
    
    
    /**
     * Test of toBytes method, of class KLV.
     */
    public void testToBytes() {
        System.out.println("toBytes - Not Yet Implemented");
        
        KLV klv;
    
        //fail( "Still need to test toBytes().");
    }   // end testToBytes
    
    
    
    /**
     * Test of toString method, of class KLV.
     */
    public void testToString() {
        System.out.println("toString - Not Yet Implemented");
        
        KLV instance = new KLV();
        
        String expResult = "";
        String result = instance.toString();
        //assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
    
}
