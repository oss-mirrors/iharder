import java.io.*;
import java.util.*;

public class TestEncodeFromFile {
	public static void main(String[] args) throws Exception{
		for( int i = 0; i < 1024; i++ ){
//		for( int i : new int[]{31,34,37} ){
			try{
				testFileOfSize(i);
			} catch( Exception e ){
				e.printStackTrace();
				Thread.sleep(250);
				Thread.yield();
			}
		}
	}
	
	public static void testFileOfSize(int size) throws Exception{
		System.out.println("Testing file of size " + size);
		// Generate data
		byte[] data = new byte[size];
		new Random().nextBytes(data);
		
		// Write to file
		File f = File.createTempFile("base64-test-file",null);
		OutputStream out = new FileOutputStream(f);
		out.write(data);
		out.close();
		
		// Read and encode
		String encoded = Base64.encodeFromFile(f.getAbsolutePath());
		
		// Decode
		byte[] decoded = Base64.decode(encoded);
		
		// Test
		assertEquals(data.length, decoded.length);
		for( int i = 0; i < data.length; i++ ){
			assertEquals(data[i],decoded[i]);
		}
	}
	
	private static void assertEquals(int a, int b) throws Exception{
		if( a != b ){
			throw new Exception("Should be " + a + " but was " + b + " instead.");
		}
	}
	private static void assertEquals(byte a, byte b) throws Exception{
		if( a != b ){
			throw new Exception("Should be " + a + " but was " + b + " instead.");
		}
	}
}