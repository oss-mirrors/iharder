
import java.io.Serializable;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 
 * Thanks to http://www.motobit.com/util/base64-decoder-encoder.asp
 * @author Robert Harder
 * @author rob@iharder.net
 */
public class Base64Test {

    public Base64Test() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of main method, of class Base64.
     */
    @Test
    public void testMain() throws Exception {
        System.out.println("TODO main");
        String[] args = {};
        //Base64.main(args);
        // TODO review the generated test code and remove the default call to fail.
        ////fail("The test case is a prototype.");
    }

    /**
     * Test of encodeObject method, of class Base64.
     */
    @Test
    public void testEncodeObject_Serializable() throws Exception {
        System.out.println("TODO encodeObject");
        Serializable serializableObject = null;
        String expResult = "";
        String result = Base64.encodeObject(serializableObject);
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of encodeObject method, of class Base64.
     */
    @Test
    public void testEncodeObject_Serializable_int() throws Exception {
        System.out.println("TODO encodeObject");
        Serializable serializableObject = null;
        int options = 0;
        String expResult = "";
        String result = Base64.encodeObject(serializableObject, options);
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of encodeBytes method, of class Base64.
     */
    @Test
    public void testEncodeBytes_byteArr() throws Exception {
        System.out.println("encodeBytes");
        
        // Trivial values, small arrays
        byte[] source = new byte[]{ 0 };
        String expResult = "AA==";
        String result = Base64.encodeBytes(source);
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
    @Test
    public void testEncodeBytes_byteArr_int() throws Exception {
        System.out.println("TODO encodeBytes");
        byte[] source = null;
        int options = 0;
        String expResult = "";
        //String result = Base64.encodeBytes(source, options);
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of encodeBytes method, of class Base64.
     */
    @Test
    public void testEncodeBytes_3args() throws Exception {
        System.out.println("TODO encodeBytes");
        byte[] source = null;
        int off = 0;
        int len = 0;
        String expResult = "";
        String result = Base64.encodeBytes(source, off, len);
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of encodeBytes method, of class Base64.
     */
    @Test
    public void testEncodeBytes_4args() throws Exception {
        System.out.println("TODO encodeBytes");
        byte[] source = null;
        int off = 0;
        int len = 0;
        int options = 0;
        String expResult = "";
        String result = Base64.encodeBytes(source, off, len, options);
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of decode method, of class Base64.
     */
    @Test
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
    @Test
    public void testDecode_String() throws Exception {
        System.out.println("TODO decode");
        String s = "";
        byte[] expResult = null;
        //byte[] result = Base64.decode(s);
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of decode method, of class Base64.
     */
    @Test
    public void testDecode_String_int() throws Exception {
        System.out.println("TODO decode");
        String s = "";
        int options = 0;
        byte[] expResult = null;
        //byte[] result = Base64.decode(s, options);
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of decodeToObject method, of class Base64.
     */
    @Test
    public void testDecodeToObject() throws Exception {
        System.out.println("TODO decodeToObject");
        String encodedObject = "";
        Object expResult = null;
        //Object result = Base64.decodeToObject(encodedObject);
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of encodeToFile method, of class Base64.
     */
    @Test
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
    @Test
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
    @Test
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
    @Test
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
    @Test
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
    @Test
    public void testDecodeFileToFile() throws Exception {
        System.out.println("TODO decodeFileToFile");
        String infile = "";
        String outfile = "";
        //Base64.decodeFileToFile(infile, outfile);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

}