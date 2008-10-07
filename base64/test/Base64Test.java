import java.io.Serializable;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * A set of JUnit tests to give Base64.java a real workout. This should have
 * been done sooner, but I'll do my best to beef it up quickly.
 * 
 * @author rob@iharder.net
 * @since 2.2.3
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
        System.out.println("main");
        String[] args = null;
        Base64.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of encodeObject method, of class Base64.
     */
    @Test
    public void testEncodeObject_Serializable() throws Exception {
        System.out.println("encodeObject");
        Serializable serializableObject = null;
        String expResult = "";
        String result = Base64.encodeObject(serializableObject);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of encodeObject method, of class Base64.
     */
    @Test
    public void testEncodeObject_Serializable_int() throws Exception {
        System.out.println("encodeObject");
        Serializable serializableObject = null;
        int options = 0;
        String expResult = "";
        String result = Base64.encodeObject(serializableObject, options);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of encodeBytes method, of class Base64.
     */
    @Test
    public void testEncodeBytes_byteArr() throws Exception {
        System.out.println("encodeBytes");
        byte[] source = null;
        String expResult = "";
        String result = Base64.encodeBytes(source);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of encodeBytes method, of class Base64.
     */
    @Test
    public void testEncodeBytes_byteArr_int() throws Exception {
        System.out.println("encodeBytes");
        byte[] source = null;
        int options = 0;
        String expResult = "";
        String result = Base64.encodeBytes(source, options);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of encodeBytes method, of class Base64.
     */
    @Test
    public void testEncodeBytes_3args() throws Exception {
        System.out.println("encodeBytes");
        byte[] source = null;
        int off = 0;
        int len = 0;
        String expResult = "";
        String result = Base64.encodeBytes(source, off, len);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of encodeBytes method, of class Base64.
     */
    @Test
    public void testEncodeBytes_4args() throws Exception {
        System.out.println("encodeBytes");
        byte[] source = null;
        int off = 0;
        int len = 0;
        int options = 0;
        String expResult = "";
        String result = Base64.encodeBytes(source, off, len, options);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of decode method, of class Base64.
     */
    @Test
    public void testDecode_4args() throws Exception {
        System.out.println("decode");
        byte[] source = null;
        int off = 0;
        int len = 0;
        int options = 0;
        byte[] expResult = null;
        byte[] result = Base64.decode(source, off, len, options);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of decode method, of class Base64.
     */
    @Test
    public void testDecode_String() throws Exception {
        System.out.println("decode");
        String s = "";
        byte[] expResult = null;
        byte[] result = Base64.decode(s);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of decode method, of class Base64.
     */
    @Test
    public void testDecode_String_int() throws Exception {
        System.out.println("decode");
        String s = "";
        int options = 0;
        byte[] expResult = null;
        byte[] result = Base64.decode(s, options);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of decodeToObject method, of class Base64.
     */
    @Test
    public void testDecodeToObject() throws Exception {
        System.out.println("decodeToObject");
        String encodedObject = "";
        Object expResult = null;
        Object result = Base64.decodeToObject(encodedObject);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of encodeToFile method, of class Base64.
     */
    @Test
    public void testEncodeToFile() throws Exception {
        System.out.println("encodeToFile");
        byte[] dataToEncode = null;
        String filename = "";
        Base64.encodeToFile(dataToEncode, filename);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of decodeToFile method, of class Base64.
     */
    @Test
    public void testDecodeToFile() throws Exception {
        System.out.println("decodeToFile");
        String dataToDecode = "";
        String filename = "";
        Base64.decodeToFile(dataToDecode, filename);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of decodeFromFile method, of class Base64.
     */
    @Test
    public void testDecodeFromFile() throws Exception {
        System.out.println("decodeFromFile");
        String filename = "";
        byte[] expResult = null;
        byte[] result = Base64.decodeFromFile(filename);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of encodeFromFile method, of class Base64.
     */
    @Test
    public void testEncodeFromFile() throws Exception {
        System.out.println("encodeFromFile");
        String filename = "";
        String expResult = "";
        String result = Base64.encodeFromFile(filename);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of encodeFileToFile method, of class Base64.
     */
    @Test
    public void testEncodeFileToFile() throws Exception {
        System.out.println("encodeFileToFile");
        String infile = "";
        String outfile = "";
        Base64.encodeFileToFile(infile, outfile);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of decodeFileToFile method, of class Base64.
     */
    @Test
    public void testDecodeFileToFile() throws Exception {
        System.out.println("decodeFileToFile");
        String infile = "";
        String outfile = "";
        Base64.decodeFileToFile(infile, outfile);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}