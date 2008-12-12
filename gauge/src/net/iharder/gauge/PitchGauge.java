/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.iharder.gauge;

import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.ImageIcon;

/**
 *
 * @author robert.harder
 */
public class PitchGauge extends DefaultGauge {
    
    private final static float[] MAJOR_TICKS = 
      new float[]{ 240,270,300, 60,90,120 };
    private final static float[] MINOR_TICKS = 
      new float[]{ 250,260,280,290, 70,80,100,110 };
    private final static int[][] MAJOR_NUMERALS = 
      new int[][]{
          { 240,  30 },
          { 270,   0 },
          { 300, -30 },
          {  60,  30 },
          {  90,   0 },
          { 120, -30 }
    };
    
    private Image image;
    private float[] pitch = new float[1];
    
    
    
    public PitchGauge(){
        
        java.net.URL imgURL = this.getClass().getResource("pitch.gif");
        //if (imgURL != null) image = new ImageIcon(imgURL).getImage();
        setHandsImage(new ImageIcon(imgURL).getImage());
        setMajorTickDegrees( MAJOR_TICKS );
        setMinorTickDegrees( MINOR_TICKS );
        setMajorNumerals( MAJOR_NUMERALS );
        setPitch(0);
        setLabel("PITCH");
        setLabelAdjustment(60);
    }
    
    public void setPitch( float deg ){
        if( Float.isNaN(deg) ) return;
        
        pitch[0] = -deg;
        setHands( pitch );
    }

    
}
