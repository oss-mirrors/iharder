package net.iharder.xmlizable;



public class Test implements XmlConstants 
{

    public static void main (String args[]) {
        
        System.out.println( "String.isArray(): " + (new String().getClass().isArray()) );
        
        int[] intarray = { 42, 23, 35 };
        boolean[] boolarray = { true, false, true, false };
        System.out.println( "int[]: " + intarray.getClass().getComponentType().getName() );
        String[] stringarray = { "String number 1", "My name is Rob", "Who are you?" };
        System.out.println( "String[]: " + stringarray.getClass() );
        
        boolean[] boolarr = { true, true, false };
        
        try
        {
            com.megginson.sax.DataWriter dw = new com.megginson.sax.DataWriter(
                new java.io.OutputStreamWriter( new java.io.BufferedOutputStream(
                new java.io.FileOutputStream( "d:\\test.xml" ) ) ) );
            dw.setIndentStep( 2 );
            dw.setPrefix( NAMESPACE, "h" );
            
            //ObjectHandler oh = new ObjectHandler();
            //ParserUtilities.parseObject( System.getProperties(), oh, true );
            
            java.util.Collection coll = new java.util.LinkedList();
            //coll.add( new java.util.HashMap() );
            coll.add( "hi" );
        //    coll.add( boolarr );
            coll.add( intarray );
            coll.add( stringarray );
            coll.add( boolarray );
            
            java.util.Collection c2 = new java.util.ArrayList();
            java.util.Iterator iter = System.getProperties().keySet().iterator();
            int i = 0;
            while( iter.hasNext() && (i++) < 4 )
                c2.add( iter.next() );
            
            java.util.Map h1 = new java.util.HashMap();
            h1.put( "System property names", c2 );
            //coll.add( h1 );
            //coll.add( oh.getObject() );
            coll.add( c2 );
            
            coll.add( new javax.swing.JLabel("I'm a label!") );
            coll.add( System.getProperties() );
            
            ObjectHandler oh2 = new ObjectHandler();
            //ParserUtilities.parseObject( c2, oh2, false );
            ParserUtilities.parseObject( coll, oh2, false );
            //ParserUtilities.parseObject( "hello, world", oh2, false );
            System.out.println( "\n\n\n" + oh2.getObject() + "\n\n\n" );
            
            //ParserUtilities.parseObject( h1, oh2 );
            
            //String piData = "type=\"text/xsl\" href=\"d:/java/Projects/XML Projects/XML Utilities 2/xsl/default.xsl\""; 
            //String piData = "type=\"text/xsl\" href=\"c:/java/XML Utilities 2/xsl/default.xsl\""; 
            String piData = "type=\"text/xsl\" href=\"" + DEFAULT_XSL_URL + "\""; 

            
            dw.startDocument();
            dw.processingInstruction( "xml-stylesheet", piData );
                //ParserUtilities.parseInt( 42, dw, false );
                ParserUtilities.parseObject( oh2.getObject(), dw, false );
                //ParserUtilities.parseObject( "yo", dw, false );
                //ParserUtilities.parseObject( coll, dw, false );
            dw.endDocument();
            
            java.io.File f = new java.io.File( "D:\\file.xml.gz" );
            ParserUtilities.saveObject( oh2.getObject(), f );
            System.out.println( "\n\nRead from file:\n" + ParserUtilities.readObject( f ) );
            ParserUtilities.saveObject( ParserUtilities.readObject( f ), new java.io.File("D:\\file2.xml") );
            
        }   // end try
        catch( Exception e )
        {   e.printStackTrace();
        }   // end catch
        
        
        
        
    }

}
