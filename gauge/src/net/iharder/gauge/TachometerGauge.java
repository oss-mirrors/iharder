/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.iharder.gauge;

/**
 *
 * @author robert.harder
 */
public class TachometerGauge extends DefaultGauge {

    public final static String RPM_PROP = "rpm";
    private int rpm;
    private float[] hands = new float[1];
    
    public final static String MAX_RPM_IN_THOUSANDS_PROP = "max_rpm_in_thousands";
    private int maxRpmInThousands;
    
    public final static String REDLINE_PROP = "redline";
    private int redline;
    
    public final static String YELLOWLINE_PROP = "yellowline";
    private int yellowline;
    
    private int yellowBandIndex = 0;
    private int redBandIndex = 1;
    private Object[][] bands =  new Object[][]{
        {125,130,java.awt.Color.yellow},
        {130,135,java.awt.Color.red}
    };
    
    /**
     * Creates a tachometer to show revolutions per minute.
     * @return
     */
    public TachometerGauge(){
        this.setMaxRPMInThousands(8);
        this.setRedline(7000);
        this.setYellowline(6700);
        this.setLabel("RPM");
    }
    
    
    /**
     * Sets the RPM and updates the hands.
     * @param rpm
     */
    public void setRPM( int rpm ){
        int oldVal = this.rpm;
        this.rpm = rpm;
        if( rpm < 0 ){
            hands[0] = 225;
        } else if( rpm > this.maxRpmInThousands * 1000  ){
            hands[0] = 225 + 270;
        } else {
            hands[0] = 225f + 270f / this.maxRpmInThousands / 1000f * rpm;
        }
        setValues(hands);
        
        firePropertyChange(RPM_PROP,oldVal,rpm);
    }
    
    /**
     * Returns the current RPM.
     * @return
     */
    public int getRPM(){
        return this.rpm;
    }
    
    /**
     * Sets the maximum RPM (in thousands) that should 
     * be displayed by the gauge.
     * Values after this point are "pegged."
     * For example <code>setMaxRPMInThousands(9)</code>
     * would show up to 9,000 RPM before values were pegged.
     * @param max maximum RPM
     */
    public void setMaxRPMInThousands( int max ){
        if( max <= 0 ) max = 1;
        int oldVal = this.maxRpmInThousands;
        this.maxRpmInThousands = max;
        
        // Major ticks
        float[] majorTicks = new float[ max + 1 ];
        for( int i = 0; i <= max; i++ ){
            majorTicks[i] = 225f + 270f / max * i;
        }   // end for: each major tick
        super.setMajorTickDegrees(majorTicks);
        
        // Major numerals
        int[][] majorNums = new int[max+1][2];
        for( int i = 0; i <= max; i++ ){
            majorNums[i][0] = (int)majorTicks[i];
            majorNums[i][1] = i;
        }   // end for: each major numeral
        super.setMajorNumerals(majorNums);
        
        firePropertyChange( MAX_RPM_IN_THOUSANDS_PROP, oldVal, max );
    }
    
    public int getMaxRPMInThousands(){
        return maxRpmInThousands;
    }
    
    /**
     * Sets the RPM value where the red band begins.
     * Should be in actual RPM, e.g. 6700.
     * @param rpm
     */
    public void setRedline( int rpm ){
        if( rpm > this.maxRpmInThousands * 1000 ) rpm = this.maxRpmInThousands * 1000;
        
        int oldVal = this.redline;
        this.redline = rpm;
        float start = 225f + 270f / this.maxRpmInThousands / 1000f * rpm;
        this.bands[this.redBandIndex][0] = start;
        super.setBands(this.bands);
        
        firePropertyChange( REDLINE_PROP, oldVal, rpm );
    }
    
    /**
     * Returns the current redline value.
     * @return
     */
    public int getRedline(){
        return this.redline;
    }
    
    
    /**
     * Sets the RPM value where the yellow band begins.
     * Should be in actual RPM, e.g. 6700.
     * @param rpm
     */
    public void setYellowline( int rpm ){
        if( rpm > this.redline ) rpm = this.redline;
        
        int oldVal = this.yellowline;
        this.yellowline = rpm;
        float start = 225f + 270f / this.maxRpmInThousands / 1000f * rpm;
        this.bands[this.yellowBandIndex][0] = start;
        this.bands[this.yellowBandIndex][1] = this.bands[this.redBandIndex][0];
        super.setBands(this.bands);
        
        firePropertyChange( YELLOWLINE_PROP, oldVal, rpm );
    }
    
    
    
}
