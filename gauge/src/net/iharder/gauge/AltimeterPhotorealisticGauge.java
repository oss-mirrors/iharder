/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.iharder.gauge;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import javax.swing.ImageIcon;

/**
 *
 * @author robert.harder
 */
public class AltimeterPhotorealisticGauge extends AltimeterGauge {

    
    private static Image IMAGE; // Background
    private static Image[] NUMERALS;
    
    static{
        java.net.URL imgUrl = AltimeterPhotorealisticGauge.class.getResource("altimeter.jpg");
        if (imgUrl != null) IMAGE = new ImageIcon(imgUrl).getImage();
        
        
        // Numerals
        NUMERALS = new Image[10];
        for( int i = 0; i <= 9; i++ ){
            java.net.URL n_imgURL = AltimeterPhotorealisticGauge.class.getResource("altimeter." + i + ".png");
            if (n_imgURL != null) NUMERALS[i] = new ImageIcon(n_imgURL).getImage();
        }
    }
    
    
    private float[] altitudes = new float[3];          // Most recent values at the beginning
    private long[]  timestamps = new long[altitudes.length];         
    private float   verticalVelocity;   // Positive is rising
    
    
    public AltimeterPhotorealisticGauge(){
        super();
        setMajorTickDegrees(null);
        setMinorTickDegrees(null);
        setMajorNumerals(null);
    }
    
    
    
    /**
     * Sets the current altitude and computes the
     * vertical velocity. A positive
     * vertical velocity indicates rising.
     *
     * @param alt the new altitude
     */
    @Override
    public void setAltitude( float alt ){
        
        // Copy historical values
        for( int i = altitudes.length-2; i >= 0; i-- ){
            altitudes[i+1] = altitudes[i];
            timestamps[i+1] = timestamps[i];
        }
        // Set new values
        altitudes[0] = alt;
        timestamps[0] = System.currentTimeMillis();
        
        // Calculate vertical velocity
        // Simple case: most recent two
        verticalVelocity = (altitudes[0]-altitudes[1]) / (float)(timestamps[0]-timestamps[1]) * 1000f;
        
        // Reflect change
        //altitudeFTF.setValue(alt);
        //verticalVelocityFTF.setValue(verticalVelocity);
        super.setAltitude(alt);
    }
    
    
    /**
     * Returns the vertical velocity in "units" per second.
     * What you use for units does not matter. The velocity
     * is calculated each time the altitude is set. A positive
     * vertical velocity indicates rising.
     *
     * @return vertical velocity
     */
    public float getVerticalVelocity(){
        return verticalVelocity;
    }
    
    
    @Override
    protected void paintBackground( Graphics2D g2d, int squareSize ){
        float halfSquareSize = squareSize*0.5f;
        AffineTransform orig = g2d.getTransform();
        
        // Draw image
        if( IMAGE != null ){
            float imgWidth = IMAGE.getWidth(this); 
            float imgHeight = IMAGE.getHeight(this);
            float halfImgWidth = 0.5f * imgWidth;
            float halfImgHeight = 0.5f * imgHeight;
            float scale = (squareSize)/imgWidth;
            
            AffineTransform trans = new AffineTransform();
            trans.concatenate(AffineTransform.getScaleInstance(scale,scale));
            trans.concatenate( AffineTransform.getTranslateInstance(-halfImgWidth,-halfImgHeight) );
            g2d.translate(halfSquareSize,halfSquareSize);
            g2d.drawImage(IMAGE,trans,this);
            
        }
    }
    
    @Override
    protected void paintFace( Graphics2D g2d, int squareSize  ){
        
        float halfSquareSize = squareSize*.5f;
        AffineTransform at = g2d.getTransform();
        
        // Find numbers
        int alt = (int)altitudes[0];
        int ten_thousands =  alt / 10000;
        int thousands     = (alt%10000) / 1000;
        int hundreds      = (alt%1000) / 100;
        int tens          = (alt%100) / 10;
        int ones          = (alt%10);
        
        
        //StringBuilder sb = new StringBuilder();
        //sb.append( ten_thousands ).append('_');
        //sb.append( thousands ).append(',');
        //sb.append( hundreds ).append('_');
        //sb.append( tens ).append('_');
        //sb.append( ones );
        //System.out.println(sb);
        
                
        // Draw image
        float imgWidth = IMAGE.getWidth(this); 
        float imgHeight = IMAGE.getHeight(this);
        float halfImgWidth = 0.5f * imgWidth;
        float halfImgHeight = 0.5f * imgHeight;
        float scale = (squareSize)/imgWidth;
        AffineTransform trans = new AffineTransform();
        trans.concatenate(AffineTransform.getScaleInstance(scale,scale));
        trans.concatenate( AffineTransform.getTranslateInstance(-halfImgWidth,-halfImgHeight) );;
        
        
        // 10,000
        g2d.translate(halfSquareSize*1.45,halfSquareSize*1.83);
        g2d.drawImage(NUMERALS[ten_thousands],trans,this);
        g2d.setTransform(at);
        
        
        // 1,000
        g2d.translate(halfSquareSize*1.61,halfSquareSize*1.83);
        g2d.drawImage(NUMERALS[thousands],trans,this);
        g2d.setTransform(at);
        
        
        // 100
        g2d.translate(halfSquareSize*1.77,halfSquareSize*1.83);
        g2d.drawImage(NUMERALS[hundreds],trans,this);
        g2d.setTransform(at);
        
        
        // 10
        g2d.translate(halfSquareSize*1.93,halfSquareSize*1.83);
        g2d.drawImage(NUMERALS[tens],trans,this);
        g2d.setTransform(at);
        
        
        // 1
        g2d.translate(halfSquareSize*2.09,halfSquareSize*1.83);
        g2d.drawImage(NUMERALS[ones],trans,this);
        
        
    }
    
    
    
    
    /** Don't paint bezel. */
    @Override
    protected void paintBezel(Graphics2D g2d, int squareSize ){}
    
}
