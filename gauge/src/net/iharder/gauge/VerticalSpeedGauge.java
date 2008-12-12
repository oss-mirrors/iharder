/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.iharder.gauge;

/**
 *
 * @author robert.harder
 */
public class VerticalSpeedGauge extends DefaultGauge {

    
    
    private static int LAST_PEGGED_TICK = 80;
    
    /** Major tick marks. */
    private static float[] MAJOR_TICKS;
    static{
        MAJOR_TICKS = new float[9];
        MAJOR_TICKS[0] = -90; // 9 o'clock position
        for( int i = 1; i <= 4; i++ ){ // Top ticks
            MAJOR_TICKS[i]   = -90 + i*(LAST_PEGGED_TICK+90)/4;
            MAJOR_TICKS[i+4] = -90 - i*(LAST_PEGGED_TICK+90)/4;
        }
    }
    
    
    /** Minor tick marks. */
    private static float[] MINOR_TICKS;
    static{
        MINOR_TICKS = new float[ 20 ];
        for( int i = 0; i < 10; i++ ){ // Top ticks
            MINOR_TICKS[i] = -90 + i*(LAST_PEGGED_TICK+90)/20;
        }
        for( int i = 0; i < 10; i++ ){ // Top ticks
            MINOR_TICKS[i+10] = -90 - i*(LAST_PEGGED_TICK+90)/20;
        }
    }   // end static
    
    
    private int[][] majorNumerals;                          // Numerals on gauge
    private float[] altitudes = new float[10];              // Most recent values at the beginning
    private long[]  timestamps = new long[altitudes.length];// Timestamps when altitudes[] were recorded         
    private float   verticalVelocity;                       // Positive is rising 
    //private float   vvScale = 5;                            // Scale to map velocity to degrees
    private int     maxVVMarking;
    private float[] hands = new float[1];                   // Location of hand
    
    
    
    /**
     * Creates a new VerticalSpeedGauge.
     */
    public VerticalSpeedGauge(){
        initComponents();
    }
    
    /** Initializes components. */
    private void initComponents(){
        setMajorTickDegrees(MAJOR_TICKS);
        setMinorTickDegrees(MINOR_TICKS);
        setLabel("VERT");
        this.setMaxVerticalSpeedMarking(100);
        
    }
    
    
    
    
    /**
     * Sets the current altitude and computes the
     * vertical velocity. A positive
     * vertical velocity indicates rising.
     *
     * @param alt the new altitude
     */
    public void setAltitude( float alt ){
        
        // Copy historical values
        System.arraycopy( altitudes,0, altitudes,1,altitudes.length-1 );
        System.arraycopy( timestamps,0, timestamps,1,timestamps.length-1 );
        //for( int i = altitudes.length-2; i >= 0; i-- ){
        //    altitudes[i+1] = altitudes[i];
        //    timestamps[i+1] = timestamps[i];
        //}
        // Set new values
        altitudes[0] = alt;
        timestamps[0] = System.currentTimeMillis();
        
        // Calculate vertical velocity
        // Simple case: most recent two
        verticalVelocity = 0;
        for( int i = 0; i < altitudes.length-1; i++ ){
            verticalVelocity +=
              (altitudes[i] - altitudes[i+1]) / 
              (float)(timestamps[i] - timestamps[i+1]) * 1000f / altitudes.length;
        }
//        verticalVelocity = (altitudes[0]-altitudes[1]) / (float)(timestamps[0]-timestamps[1]) * 1000f;
        
        // Reflect change
        hands[0] = -90 + Math.min(verticalVelocity / maxVVMarking,1) * (90+LAST_PEGGED_TICK);
        setHands(hands);
    }
    
    
    /**
     * Returns the maximum value represented by the gauge.
     * This number appears on the right side of the gauge as
     * both ascent and descent values converge from zero
     * (at 9 o'clock) to this maximum (at 3 o'clock).
     * @return maximum value on gauge
     */
    public int getMaxVerticalSpeedMarking(){
        return this.maxVVMarking;
    }
    
    
    
    /**
     * Sets the maximum value represented by the gauge.
     * This number appears on the right side of the gauge as
     * both ascent and descent values converge from zero
     * (at 9 o'clock) to this maximum (at 3 o'clock).
     * @param max maximum value on gauge
     */
    public void setMaxVerticalSpeedMarking( int max ){
        maxVVMarking = max;
        int quarter = max / 4;
        majorNumerals = new int[8][2];
        
        majorNumerals[0][0] = -90; // Zero
        majorNumerals[0][1] = -0;
        
        majorNumerals[7][0] = 90;   // Max
        majorNumerals[7][1] = max;
        
        for( int i = 1; i < 4; i++ ){
            majorNumerals[i][0] = (int)MAJOR_TICKS[i];   // Top
            majorNumerals[i][1] = quarter*i;
            
            majorNumerals[i+3][0] = (int)MAJOR_TICKS[i+4];   // Bottom
            majorNumerals[i+3][1] = quarter*i;
        }
        super.setMajorNumerals(this.majorNumerals);
    }
    
    
    
}
