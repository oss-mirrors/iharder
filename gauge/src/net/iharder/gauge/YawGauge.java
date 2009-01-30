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
public class YawGauge extends DefaultGauge {
    
    private final static float[] MAJOR_TICKS = 
      new float[]{ 0,90,180,270 };
    private final static float[] MINOR_TICKS = 
      new float[]{ 30,60, 120,150, 210,240, 300,330 };
    private final static int[][] MAJOR_NUMERALS = 
      new int[][]{
          {   0,   0 },
          {  90,  90 },
          { 180, 180 },
          { 270, 270 }
    };
    private final static int[][] MINOR_NUMERALS = 
      new int[][]{
          {  30,  30 },
          {  60,  60 },
          { 120, 120 },
          { 150, 150 },
          { 210, 210 },
          { 240, 240 },
          { 300, 300 },
          { 330, 330 }
    };
    
    private Image image;
    private float[] yaw = new float[1];
    
    
    
    public YawGauge(){
        
        java.net.URL imgURL = this.getClass().getResource("sleek-yaw.png");
        setHandScale(1f);
        //if (imgURL != null) image = new ImageIcon(imgURL).getImage();
        setHandsImage(new ImageIcon(imgURL).getImage());
        setYaw(0);
        setMajorTickDegrees( MAJOR_TICKS );
        setMinorTickDegrees( MINOR_TICKS );
        setMajorNumerals( MAJOR_NUMERALS );
        setMinorNumerals( MINOR_NUMERALS );
        setLabel("YAW");
        setLabelAdjustment(60);
    }
    
    public void setYaw( float deg ){
        if( Float.isNaN(deg) ) return;
        
        yaw[0] = deg;
        setValues( yaw );
    }

    
}
