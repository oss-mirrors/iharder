
package net.iharder.gauge;

import java.awt.Image;
import java.awt.Polygon;
import javax.swing.ImageIcon;

/**
 * Image
 * <img src="doc-files/sleek-roll.png" />
 * @author robert.harder
 */
public class RollGauge extends DefaultGauge {


    /** Half of the X values defining the plane shape. */
    private final static int[] HAND_X = { 0,1,2,3,6,4,5,21,20,   10,8,8,9,8,7,7,4,2,0 };


    /** Half of the Y values defining the plane shape. */
    private final static int[] HAND_Y = {
        8,5,5,2,5,2,1,1,0,    0,-1,-3,-4,-5,-4,-1,-1,-2,-2
    };

    
    private final static float[] MAJOR_TICKS = 
      new float[]{ 240,270,300, 60,90,120 };
    private final static float[] MINOR_TICKS = 
      new float[]{ 250,260,280,290, 70,80,100,110 };
    private final static int[][] MAJOR_NUMERALS = 
      new int[][]{
          { 240, -30 },
          { 270,   0 },
          { 300,  30 },
          {  60, -30 },
          {  90,   0 },
          { 120,  30 }
    };
    
    private Image image;
    private float[] roll = new float[1];
    
    
    public RollGauge(){
        
        java.net.URL imgURL = this.getClass().getResource("sleek-roll.png");
        //java.net.URL imgURL = this.getClass().getResource("Airplane_silhouette.png");

        setHandsImage(new ImageIcon(imgURL).getImage());

        assert HAND_X.length == HAND_Y.length;
        int[] x = new int[ HAND_X.length * 2];
        int[] y = new int[ HAND_Y.length * 2];
        System.arraycopy( HAND_X,0, x,0,HAND_X.length );
        System.arraycopy( HAND_Y,0, y,0,HAND_Y.length );
        for( int i = 0; i < HAND_X.length; i++ ){
            x[ HAND_X.length + i ] = -HAND_X[ HAND_X.length - 1 - i ];
            y[ HAND_Y.length + i ] =  HAND_Y[ HAND_Y.length - 1 - i ];
        }
        setHand( new Polygon(x,y,x.length) );
        setHandScale(1.5f);

        
        setMajorTickDegrees( MAJOR_TICKS );
        setMinorTickDegrees( MINOR_TICKS );
        setMajorNumerals( MAJOR_NUMERALS );
        setLabel("ROLL");
        setLabelAdjustment(60);
        setRoll(0);
    }
    
    public void setRoll( float deg ){
        if( Float.isNaN(deg) ) return;
        
        roll[0] = deg;
        setValues( roll );
    }

    
}
