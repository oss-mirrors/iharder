package net.iharder.gauge;

import java.beans.PropertyChangeEvent;
import java.awt.*;
import java.beans.PropertyChangeListener;

/**
 * An altimeter gauge that displays altitude with two hands:
 * the little hand being thousands, and the big hand being hundreds.
 * @author rob
 */
public class AltimeterGauge extends DefaultGauge {
    
    
    /** Major tick marks. */
    private static float[] MAJOR_TICKS;
    static{
        MAJOR_TICKS = new float[10];
        for( int i = 0; i < MAJOR_TICKS.length; i++ ){
            MAJOR_TICKS[i] = i * 360/MAJOR_TICKS.length;
        }
    }
    
    
    /** Minor tick marks. */
    private static float[] MINOR_TICKS;
    static{
        MINOR_TICKS = new float[ 50 ];
        for( int i = 1; i < MINOR_TICKS.length; i++ ){
            MINOR_TICKS[i] = i * 360/MINOR_TICKS.length;
        }
    }   // end static
    
    
    /** Major numerals */
    private static int[][] MAJOR_NUMERALS;
    static{
        MAJOR_NUMERALS = new int[10][2];
        for( int i = 0; i < MAJOR_TICKS.length; i++ ){
            MAJOR_NUMERALS[i][0] = i * 360/MAJOR_NUMERALS.length;
            MAJOR_NUMERALS[i][1] = i;
        }
    }
    
    public final static String ALTITUDE_PROP = "altitude";
    private float altitude;
    private float[] hands = new float[2];
    
    
    public AltimeterGauge(){
        initComponents();
    }
    
    private void initComponents(){
        super.setMajorTickDegrees(MAJOR_TICKS);
        super.setMinorTickDegrees(MINOR_TICKS);
        super.setMajorNumerals(MAJOR_NUMERALS);
        super.setLabel("ALT");
    }
    
    /** Sets the altitude. */
    public void setAltitude( float alt ){
        float oldVal = this.altitude;
        this.altitude = alt;
        hands[0] = (alt%1000)*.001f*360;   // Hundreds
        hands[1] = (alt%10000)*.0001f*360; // Thousands
        setValues(hands);
        firePropertyChange( ALTITUDE_PROP, oldVal, alt );
    }
    
    /** Returns the altitude. */
    public float getAltitude(){
        return this.altitude;
    }
    
}
