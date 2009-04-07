/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import junit.framework.TestCase;

/**
 *
 * @author rob
 */
public class Base64Test extends TestCase {
    
    public Base64Test(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of encode method, of class Base64.
     */
    public void testEncode_ByteBuffer_ByteBuffer() {
        System.out.println("encode");
        ByteBuffer raw = null;
        ByteBuffer encoded = null;
        Base64.encode(raw, encoded);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of encode method, of class Base64.
     */
    public void testEncode_ByteBuffer_CharBuffer() {
        System.out.println("encode");
        ByteBuffer raw = null;
        CharBuffer encoded = null;
        Base64.encode(raw, encoded);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of encodeObject method, of class Base64.
     */
    @SuppressWarnings("unchecked")
    public void testEncodeObject_Serializable() throws Exception {
        System.out.println("testEncodeObject_Serializable and testDecodeToObject");
        java.util.Vector vec = new java.util.Vector();
        vec.add(1);
        vec.add("hello world");
        String result = Base64.encodeObject(vec);
        java.util.Vector vec2 = (java.util.Vector)Base64.decodeToObject(result);

        assertEquals( vec.get(0), vec2.get(0) );
        assertEquals( vec.get(1), vec2.get(1) );

    }

    /**
     * Test of encodeObject method, of class Base64.
     */

    @SuppressWarnings("unchecked")
    public void testEncodeObject_Serializable_int() throws Exception {
        System.out.println("testEncodeObject_Serializable_int and testDecodeToObject");
        java.util.Vector vec = new java.util.Vector();
        vec.add(1);
        vec.add("hello world");
        String result = Base64.encodeObject(vec, Base64.GZIP );
        java.util.Vector vec2 = (java.util.Vector)Base64.decodeToObject(result);

        assertEquals( vec.get(0), vec2.get(0) );
        assertEquals( vec.get(1), vec2.get(1) );

    }

    /**
     * Test of encodeBytes method, of class Base64.
     */

    public void testEncodeBytes_byteArr() throws Exception {
        System.out.println("encodeBytes");

        // Trivial values, small arrays
        byte[] source = new byte[]{ };
        String expResult = "";
        String result = Base64.encodeBytes(source);
        assertEquals(expResult, result);


        source = new byte[]{ 0 };
        expResult = "AA==";
        result = Base64.encodeBytes(source);
        assertEquals(expResult, result);

        source = new byte[]{ 0,0};
        expResult = "AAA=";
        result = Base64.encodeBytes(source);
        assertEquals(expResult, result);

        source = new byte[]{ 0,0,0 };
        expResult = "AAAA";
        result = Base64.encodeBytes(source);
        assertEquals(expResult, result);

        source = new byte[]{ 0,0,0, 0,0 };
        expResult = "AAAAAAA=";
        result = Base64.encodeBytes(source);
        assertEquals(expResult, result);

        // Longer sequence
        source = new byte[30];
        for( int i = 0; i < source.length; i++ ){
            source[i] = (byte)i;
        }   // end for
        expResult = "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwd";
        result = Base64.encodeBytes(source);
        assertEquals(expResult, result);


        // Some repeatable random numbers.
        // I wrote these random values to a file
        // and ran them through another Base64 encoder
        // (which presumably worked properly too!)
        java.util.Random rand = new java.util.Random(1234); // Seed
        source = new byte[30];
        rand.nextBytes(source);
        expResult = "qGiGpdKXgULaLYzzeOvIPPXqitsyfzBRIsdOdZOI";
        result = Base64.encodeBytes(source);
        assertEquals(expResult, result);

        rand = new java.util.Random(1234); // Seed
        source = new byte[31];
        rand.nextBytes(source);
        expResult = "qGiGpdKXgULaLYzzeOvIPPXqitsyfzBRIsdOdZOIug==";
        result = Base64.encodeBytes(source);
        assertEquals(expResult, result);

        rand = new java.util.Random(1234); // Seed
        source = new byte[32];
        rand.nextBytes(source);
        expResult = "qGiGpdKXgULaLYzzeOvIPPXqitsyfzBRIsdOdZOIuow=";
        result = Base64.encodeBytes(source);
        assertEquals(expResult, result);


    }

    /**
     * Test of encodeBytes method, of class Base64.
     */

    public void testEncodeBytes_byteArr_int() throws Exception {
        System.out.println("testEncodeBytes_byteArr_int");
        // Trivial values, small arrays
        byte[] source = new byte[]{ 0 };
        String result = Base64.encodeBytes(source, Base64.GZIP);
        byte[] src2 = Base64.decode( result );
        assertEquals( source.length, src2.length );
        for( int i = 0; i < source.length; i++ ){
            assertEquals( source[i], src2[i] );
        }


        // Longer sequence
        source = new byte[30];
        for( int i = 0; i < source.length; i++ ){
            source[i] = (byte)i;
        }   // end for
        result = Base64.encodeBytes(source);
        src2 = Base64.decode( result );
        assertEquals( source.length, src2.length );
        for( int i = 0; i < source.length; i++ ){
            assertEquals( source[i], src2[i] );
        }
    }

    /**
     * Test of encodeBytes method, of class Base64.
     */

    public void testEncodeBytes_3args() throws Exception {

        System.out.println("testEncodeBytes_3args");

        // Trivial values, small arrays
        byte[] source = new byte[]{ -1, 0, 1 };
        String expResult = "AA==";
        String result = Base64.encodeBytes(source,1,1);
        assertEquals(expResult, result);

        source = new byte[]{ -1, 0, 0 };
        expResult = "AAA=";
        result = Base64.encodeBytes(source,1,2);
        assertEquals(expResult, result);

        source = new byte[]{ -1, 0,0,0, 2, 3 };
        expResult = "AAAA";
        result = Base64.encodeBytes(source, 1, 3);
        assertEquals(expResult, result);

        try{
            source = null;
            Base64.encodeBytes( source, 0, 0 );
            fail("Should have thrown NullPointerException.");
        } catch(NullPointerException exc ){}

        try{
            source = new byte[3];
            Base64.encodeBytes( source, -1, 0 );
            fail("Should have thrown IllegalArgumentException.");
        } catch(IllegalArgumentException exc ){}

        try{
            source = new byte[3];
            Base64.encodeBytes( source, 0, -1 );
            fail("Should have thrown IllegalArgumentException.");
        } catch(IllegalArgumentException exc ){}

        try{
            source = new byte[3];
            Base64.encodeBytes( source, 0, 4 );
            fail("Should have thrown IllegalArgumentException.");
        } catch(IllegalArgumentException exc ){}

        try{
            source = new byte[3];
            Base64.encodeBytes( source, 4, 0 );
            fail("Should have thrown IllegalArgumentException.");
        } catch(IllegalArgumentException exc ){}

        try{
            source = new byte[3];
            Base64.encodeBytes( source, 3, 1 );
            fail("Should have thrown IllegalArgumentException.");
        } catch(IllegalArgumentException exc ){}
    }

    /**
     * Test of encodeBytes method, of class Base64.
     */

    public void testEncodeBytes_4args() throws Exception {
        System.out.println("TODO encodeBytes");
//        String result = Base64.encodeBytes(source, off, len, options);
    }

    /**
     * Test of decode method, of class Base64.
     */

    public void testDecode_4args() throws Exception {
        System.out.println("TODO decode");
        byte[] source = null;
        int off = 0;
        int len = 0;
        int options = 0;
        byte[] expResult = null;
        //byte[] result = Base64.decode(source, off, len, options);
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of decode method, of class Base64.
     */

    public void testDecode_String() throws Exception {
        System.out.println("testDecode_String");
        String s = "";
        byte[] expResult = null;

        // Trivial values, small arrays
        byte[] source = new byte[]{ };
        String encoded = Base64.encodeBytes(source);
        byte[] result = Base64.decode(encoded);
        assertEquals(source.length,result.length);


        source = new byte[]{ 42 };
        encoded = Base64.encodeBytes(source);
        result = Base64.decode(encoded);
        assertEquals(source.length,result.length);
        for( int i = 0; i < source.length; i++ ){
            assertEquals(source[i],result[i]);
        }

        source = new byte[]{ 42,23 };
        encoded = Base64.encodeBytes(source);
        result = Base64.decode(encoded);
        assertEquals(source.length,result.length);
        for( int i = 0; i < source.length; i++ ){
            assertEquals(source[i],result[i]);
        }

        source = new byte[]{ 42,23,86 };
        encoded = Base64.encodeBytes(source);
        result = Base64.decode(encoded);
        assertEquals(source.length,result.length);
        for( int i = 0; i < source.length; i++ ){
            assertEquals(source[i],result[i]);
        }

        source = new byte[]{ 42,23,86, 1,2 };
        encoded = Base64.encodeBytes(source);
        result = Base64.decode(encoded);
        assertEquals(source.length,result.length);
        for( int i = 0; i < source.length; i++ ){
            assertEquals(source[i],result[i]);
        }

        // Longer sequence
        source = new byte[30];
        for( int i = 0; i < source.length; i++ ){
            source[i] = (byte)i;
        }   // end for
        encoded = Base64.encodeBytes(source);
        result = Base64.decode(encoded);
        assertEquals(source.length,result.length);
        for( int i = 0; i < source.length; i++ ){
            assertEquals(source[i],result[i]);
        }


        // Some repeatable random numbers.
        // I wrote these random values to a file
        // and ran them through another Base64 encoder
        // (which presumably worked properly too!)
        java.util.Random rand = new java.util.Random(1234); // Seed
        source = new byte[30];
        rand.nextBytes(source);
        encoded = Base64.encodeBytes(source);
        result = Base64.decode(encoded);
        assertEquals(source.length,result.length);
        for( int i = 0; i < source.length; i++ ){
            assertEquals(source[i],result[i]);
        }

        rand = new java.util.Random(1234); // Seed
        source = new byte[31];
        rand.nextBytes(source);
        encoded = Base64.encodeBytes(source);
        result = Base64.decode(encoded);
        assertEquals(source.length,result.length);
        for( int i = 0; i < source.length; i++ ){
            assertEquals(source[i],result[i]);
        }


    }

    /**
     * Test of decode method, of class Base64.
     */

    public void testDecode_String_int() throws Exception {
        System.out.println("testDecode_String_int");
        String s = "";
        int options = 0;
        byte[] expResult = null;


        // Trivial values, small arrays
        byte[] source = new byte[]{ };
        String encoded = Base64.encodeBytes(source, Base64.GZIP);
        byte[] result = Base64.decode(encoded);
        assertEquals(source.length,result.length);


        source = new byte[]{ 42 };
        encoded = Base64.encodeBytes(source, Base64.GZIP);
        result = Base64.decode(encoded);
        assertEquals(source.length,result.length);
        for( int i = 0; i < source.length; i++ ){
            assertEquals(source[i],result[i]);
        }
        source = new byte[]{ 42 };
        encoded = Base64.encodeBytes(source, Base64.URL_SAFE);
        result = Base64.decode(encoded);
        assertEquals(source.length,result.length);
        for( int i = 0; i < source.length; i++ ){
            assertEquals(source[i],result[i]);
        }



        // Some repeatable random numbers.
        // I wrote these random values to a file
        // and ran them through another Base64 encoder
        // (which presumably worked properly too!)
        java.util.Random rand = new java.util.Random(1234); // Seed
        source = new byte[300];
        rand.nextBytes(source);
        encoded = Base64.encodeBytes(source, Base64.GZIP);
        result = Base64.decode(encoded);
        assertEquals(source.length,result.length);
        for( int i = 0; i < source.length; i++ ){
            assertEquals(source[i],result[i]);
        }

        rand = new java.util.Random(1234); // Seed
        source = new byte[3000];
        rand.nextBytes(source);
        encoded = Base64.encodeBytes(source, Base64.URL_SAFE);
        result = Base64.decode(encoded, Base64.URL_SAFE);
        assertEquals(source.length,result.length);
        for( int i = 0; i < source.length; i++ ){
            assertEquals(source[i],result[i]);
        }

        rand = new java.util.Random(1234); // Seed
        source = new byte[3000];
        rand.nextBytes(source);
        encoded = Base64.encodeBytes(source, Base64.ORDERED);
        result = Base64.decode(encoded, Base64.ORDERED);
        assertEquals(source.length,result.length);
        for( int i = 0; i < source.length; i++ ){
            assertEquals(source[i],result[i]);
        }
    }


    /**
     * Test of encodeToFile method, of class Base64.
     */

    public void testEncodeToFile() throws Exception {
        System.out.println("TODO encodeToFile");
        byte[] dataToEncode = null;
        String filename = "";
        //Base64.encodeToFile(dataToEncode, filename);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of decodeToFile method, of class Base64.
     */

    public void testDecodeToFile() throws Exception {
        System.out.println("TODO decodeToFile");
        String dataToDecode = "";
        String filename = "";
        //Base64.decodeToFile(dataToDecode, filename);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of decodeFromFile method, of class Base64.
     */

    public void testDecodeFromFile() throws Exception {
        System.out.println("TODO decodeFromFile");
        String filename = "";
        byte[] expResult = null;
        //byte[] result = Base64.decodeFromFile(filename);
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of encodeFromFile method, of class Base64.
     */

    public void testEncodeFromFile() throws Exception {
        System.out.println("TODO encodeFromFile");
        String filename = "";
        String expResult = "";
        //String result = Base64.encodeFromFile(filename);
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of encodeFileToFile method, of class Base64.
     */

    public void testEncodeFileToFile() throws Exception {
        System.out.println("TODO encodeFileToFile");
        String infile = "";
        String outfile = "";
        //Base64.encodeFileToFile(infile, outfile);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of decodeFileToFile method, of class Base64.
     */

    public void testDecodeFileToFile() throws Exception {
        System.out.println("TODO decodeFileToFile");
        String infile = "";
        String outfile = "";
        //Base64.decodeFileToFile(infile, outfile);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of encodeBytesToBytes method, of class Base64.
     */
    public void testEncodeBytesToBytes_byteArr() {
        System.out.println("encodeBytesToBytes");
        byte[] source = null;
        byte[] expResult = null;
        byte[] result = Base64.encodeBytesToBytes(source);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of encodeBytesToBytes method, of class Base64.
     */
    public void testEncodeBytesToBytes_4args() throws Exception {
        System.out.println("encodeBytesToBytes");
        byte[] source = null;
        int off = 0;
        int len = 0;
        int options = 0;
        byte[] expResult = null;
        byte[] result = Base64.encodeBytesToBytes(source, off, len, options);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of decode method, of class Base64.
     */
    public void testDecode_byteArr() {
        System.out.println("decode");
        byte[] source = null;
        byte[] expResult = null;
        byte[] result = Base64.decode(source);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of decodeToObject method, of class Base64.
     */
    public void testDecodeToObject() throws Exception {
        System.out.println("decodeToObject");
        String encodedObject = "";
        Object expResult = null;
        Object result = Base64.decodeToObject(encodedObject);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


}
