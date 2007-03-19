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
        assertEquals(0, klv.getLength() );
        
        // One byte payload
        klv = new KLV( new byte[]{ 0, 1, 1 }, 1, KLV.LENGTH_FIELD_ONE_BYTE );
        assertEquals(1, klv.getLength() );
        
        // Length field encoding: One Byte
        for( int i = 0; i <= 255; i++ ){
            byte[] bytes = new byte[ 1 + 1 + i ];
            bytes[0] = 42; // Arbitrary key
            bytes[1] = (byte)i; // Length, unsigned byte
            for( int j = 2; j < bytes.length; j++ ){
                bytes[j] = (byte)j; // Arbitrary payload
            }   // end for: j
            klv = new KLV( bytes, 1, KLV.LENGTH_FIELD_ONE_BYTE );
            assertTrue( klv.getLength() >= 0 );
            assertEquals(i, klv.getLength() );
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
            klv = new KLV( bytes, 1, KLV.LENGTH_FIELD_TWO_BYTES );
            assertTrue( klv.getLength() >= 0 );
            assertEquals(i, klv.getLength() );
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
            klv = new KLV( bytes, 1, KLV.LENGTH_FIELD_FOUR_BYTES );
            assertTrue( klv.getLength() >= 0 );
            assertEquals(i, klv.getLength() );
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
            klv = new KLV( bytes, 1, KLV.LENGTH_FIELD_BER );
            assertTrue( klv.getLength() >= 0 );
            assertEquals(i, klv.getLength() );
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
            klv = new KLV( bytes, 1, KLV.LENGTH_FIELD_BER );
            assertTrue( klv.getLength() >= 0 );
            assertEquals(i, klv.getLength() );
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
            klv = new KLV( bytes, 1, KLV.LENGTH_FIELD_BER );
            assertTrue( klv.getLength() >= 0 );
            assertEquals(i, klv.getLength() );
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
            klv = new KLV( bytes, 1, KLV.LENGTH_FIELD_BER );
            assertTrue( klv.getLength() >= 0 );
            assertEquals(i, klv.getLength() );
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
            klv = new KLV( bytes, 1, KLV.LENGTH_FIELD_BER );
            assertTrue( klv.getLength() >= 0 );
            assertEquals(i, klv.getLength() );
        }   // end for: i
        
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
            klv = new KLV( bytes, 1, KLV.LENGTH_FIELD_ONE_BYTE );
            assertEquals(i, klv.getShortKey());
        }   // end for: i
        
        // Two byte keys and no payload, one byte length field
        for( int i = 0; i < 65535; i++ ){
            byte[] bytes = new byte[]{ (byte)(i>>8), (byte)i, 0  };
            klv = new KLV( bytes, 2, KLV.LENGTH_FIELD_ONE_BYTE );
            assertEquals(i, klv.getShortKey());
        }   // end for: i
        
        // Three byte keys and no payload, one byte length field
        for( int i = 0; i < (1<<24)-1; i+=10 ){
            byte[] bytes = new byte[]{ (byte)(i>>16), (byte)(i>>8), (byte)i, 0  };
            klv = new KLV( bytes, 3, KLV.LENGTH_FIELD_ONE_BYTE );
            assertEquals(i, klv.getShortKey());
        }   // end for: i
        
        // Four byte keys and no payload, one byte length field
        for( int i = 0; i < (1<<32)-1; i+=100 ){
            byte[] bytes = new byte[]{ (byte)(i>>24), (byte)(i>>16), (byte)(i>>8), (byte)i, 0  };
            klv = new KLV( bytes, 4, KLV.LENGTH_FIELD_ONE_BYTE );
            assertEquals(i, klv.getShortKey());
        }   // end for: i
        
        // Five byte key will fail on getShortKey()
        {   byte[] bytes = new byte[5 + 1];
            klv = new KLV( bytes, 5, KLV.LENGTH_FIELD_ONE_BYTE );
            try{
                klv.getShortKey();
                assertTrue(false);
            } catch( IllegalStateException exc ){
                assertTrue(true);
            }   // end catch
        }
        
        
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
        
        // Key lengths of 1..1000 bytes, no payload, one byte length field
        for( int i = 1; i <= 1000; i++ ){
            byte[] bytes = new byte[ i + 1 ];
            for( int j = 0; j < i; j++ ){
                bytes[j] = (byte)0xFF; // Arbitrary key values
            }
            klv = new KLV( bytes, i, KLV.LENGTH_FIELD_ONE_BYTE );
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
            klv = new KLV( bytes, 1, KLV.LENGTH_FIELD_ONE_BYTE );
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
            klv = new KLV( bytes, 1, KLV.LENGTH_FIELD_TWO_BYTES );
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
            klv = new KLV( bytes, 1, KLV.LENGTH_FIELD_FOUR_BYTES );
            byte[]value = klv.getValue();
            assertEquals(i,value.length);
            assertEquals(5,klv.getValueOffset());
            for( int j = 0; j < i; j+=10 ){
                assertEquals(j%255,(value[j]&0xFF)%255);
            }   // end for: j
        }   // end for: i
        
        // NEED TO TEST GETVALUE WITH BER
        fail( "Need to test getValue with BER" );
        
    }   // end testGetValue
    
    
    /**
     * Test of toBytes method, of class KLV.
     */
    public void testToBytes() {
        System.out.println("toBytes");
        
        KLV klv;
    
        fail( "Still need to test toBytes().");
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
