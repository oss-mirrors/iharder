package net.iharder.xmlizable;

/**
 *
 *
 * <p>
 * I am placing this code in the Public Domain. Do with it as you will.
 * This software comes with no guarantees or warranties but with
 * plenty of well-wishing instead!
 * Please visit <a href="http://iharder.net/xmlizable">http://iharder.net/xmlizable</a>
 * periodically to check for updates or to contribute improvements.
 * </p>
 *
 * <p>
 * Thanks to John Morrison of Printronix for adding the 
 * code to handle boolean primitives.
 *
 * @author Robert Harder
 * @author rharder@usa.net
 * @version 1.3.1
 */
public class Main
{
    private static final String[] credits =
    { "Java XML Utilities",
      "Version 1.3.1",
      "Robert Harder",
      "rharder@usa.net",
      "",
      "To use these XML utilities, put the",
      "xmlizable.jar file in your classpath."
    };
    
    public static void main( String[] args )
    {   

        
        System.out.println("");
        for( int i = 0; i < credits.length; i++ )
            System.out.println( credits[i] );
        System.out.println("");
        
        try
        {   java.awt.Frame f = new java.awt.Frame( credits[0] );
            f.setLayout( new java.awt.GridLayout(credits.length,1) );
            for( int i = 0; i < credits.length; i++ )
                f.add( new java.awt.Label( credits[i], java.awt.Label.CENTER ) );
            f.pack();
            java.awt.Toolkit t = f.getToolkit();
            f.setLocation( (t.getScreenSize().width-f.getSize().width)/2, (t.getScreenSize().height-f.getSize().height)/2 );
            f.addWindowListener( new java.awt.event.WindowAdapter()
            {   public void windowClosing( java.awt.event.WindowEvent e )
                {   System.exit(0);
                }   // end windowClosed
            }); // end WindowAdapter
            f.show();
            
        }   // end try
        catch( Exception e )
        {   e.printStackTrace();
        }
        
    }   // end main 
}   // end class Main
