
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author rob
 */
public class TestEfficiency {

    public static void main(String[] args) throws IOException{

        for( int i = 55; i < 57; i++ ){
            testWithBytes(i);
        }


        //testWithBytes(  40<<20 );
        //testWithBytes(  40<<20 );
        //testWithBytes( 140<<20 );
        //testWithString( 90<<20 );



    }

    private static void testWithString( int numBytes ) {
        System.gc();Thread.yield();
        System.out.println("\nTesting using Base64.encodeBytes(..)" );
        System.out.println( "Memory at start: " + ((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()) >> 20) + "MB" );
        byte[] raw = new byte[numBytes];
        System.out.println( "Memory in use from raw data: " + ((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()) >> 20) + "MB" );
        Random r = new Random();
        r.nextBytes( raw );
        long start = System.currentTimeMillis();
        String enc = Base64.encodeBytes( raw );
        long end = System.currentTimeMillis();
        System.gc();Thread.yield();
        System.out.println( "Memory in use from raw and encoded: " + ((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()) >> 20) + "MB" );
        System.out.println("Speed: " + ((numBytes >> 20) * 0.001 * (end-start) ) + " MB/sec");
    }

    private static void testWithBytes( int numBytes ) throws IOException {
        System.gc();Thread.yield();
        System.out.println("\nTesting using Base64.encodeBytesToBytes(" + numBytes + ")" );
        System.out.println( "Memory at start: " + ((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()) >> 20) + "MB" );
        byte[] raw = new byte[numBytes];
        System.out.println( "Memory in use from raw data: " + ((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()) >> 20) + "MB" );
        Random r = new Random();
        r.nextBytes( raw );
        long start = System.currentTimeMillis();
        byte[] enc = null;
        //try {
            enc = Base64.encodeBytesToBytes( raw, 0, raw.length, Base64.DO_BREAK_LINES );
        //} catch( IOException ex ) {
        //    ex.printStackTrace();
        //}
        long end = System.currentTimeMillis();
        System.gc();Thread.yield();
        System.out.println( "Memory in use from raw and encoded: " + ((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()) >> 20) + "MB" );
        System.out.println("Speed: " + ((numBytes >> 20) * 0.001 * (end-start) ) + " MB/sec");
    }

}
