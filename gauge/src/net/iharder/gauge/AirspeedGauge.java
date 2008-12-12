package net.iharder.gauge;

/**
 * Approximates an airspeed indicator on an airplane.
 * 
 * @author Robert Harder, rob@iharder.net
 */
public class AirspeedGauge extends DefaultGauge {

    /** The "airspeed" property. */
    public final static String AIRSPEED_PROP = "airspeed";
    private float airspeed;
    private float[] hands = new float[1];

    /** The "min_airspeed" property. */
    public final static String MIN_AIRSPEED_PROP = "min_airspeed";
    private int minAirspeed = 0;

    /** The "max_airspeed" property */
    public final static String MAX_AIRSPEED_PROP = "max_airspeed";
    private int maxAirspeed = 30;
    private float[] majorTicks = new float[2];
    private float[] minorTicks = new float[0];

    /** The "redline" property. */
    public final static String REDLINE_PROP = "redline";
    private float redline = 27.5f;

    /** The "yellowline property. */
    public final static String YELLOWLINE_PROP = "yellowline";
    private float yellowline = 25;

    /** The "greenline" property. */
    public final static String GREENLINE_PROP = "greenline";
    private float greenline = 15;
    

    private final static int GREEN_BAND_INDEX = 0;
    private final static int YELLOW_BAND_INDEX = 1;
    private final static int RED_BAND_INDEX = 2;
    private float minDegrees = 30;
    private float maxDegrees = 360-30;
    private Object[][] bands = new Object[][]{
        {null,null,java.awt.Color.green},
        {null,null,java.awt.Color.yellow},
        {null,null,java.awt.Color.red}
    };
    
    
    private int[][] numerals = new int[2][2];
    
    
    /**
     * Creates an airspeed gauge.
     */
    public AirspeedGauge(){
        setLabel("SPEED");
        update();
    }
    
    
    /**
     * Sets the minimum airspeed displayed on the gauge.
     * @param min the min airspeed to display
     */
    public void setMinAirspeed( int min ){
        int oldVal = this.minAirspeed;
        this.minAirspeed = min;
        update();
        firePropertyChange( MIN_AIRSPEED_PROP, oldVal, min  );
    }
    
    /**
     * Returns the minimum airspeed displayed on the gauge.
     * @return the min airspeed that will be displayed
     */
    public int getMinAirspeed(){
        return this.minAirspeed;
    }
    
    
    
    /**
     * Sets the maximum airspeed displayed on the gauge.
     * Must be an integer.
     * @param max the max airspeed to display
     */
    public void setMaxAirspeed( int max ){
        int oldVal = this.maxAirspeed;
        this.maxAirspeed = max;
        update();
        firePropertyChange( MAX_AIRSPEED_PROP, oldVal, max  );
    }
    
    
    /**
     * Returns the maximum airspeed displayed on the gauge.
     * @return the max airspeed that will be displayed
     */
    public int getMaxAirspeed(){
        return this.maxAirspeed;
    }
    
    
    /**
     * Sets the current airspeed to display
     * @param speed the current airspeed
     */
    public void setAirspeed( float speed ){
        float oldVal = this.airspeed;
        this.airspeed = speed;
        if( speed < this.minAirspeed ){
            hands[0] = this.minDegrees;
        } else if( speed > this.maxAirspeed ){
            hands[0] = this.maxDegrees;
        } else {
            float degPerSpeed = (maxDegrees - minDegrees) / (maxAirspeed - minAirspeed);
            hands[0] = this.minDegrees + (speed - this.minAirspeed) * degPerSpeed;
        }
        setHands(hands);
        firePropertyChange( AIRSPEED_PROP, oldVal, speed );
    }
    
    
    /**
     * Sets the airspeed at which the redline begins.
     * @param redline beginning of redline
     */
    public void setRedline( float redline ){
        float oldVal = this.redline;
        this.redline = redline;
        update();
        firePropertyChange(REDLINE_PROP,oldVal,redline);
    }
    
    /**
     * Sets the airspeed at which the redline begins.
     * @return beginning of redline
     */
    public float getRedline(){
        return this.redline;
    }
    
    
    
    /**
     * Sets the airspeed at which the yellowline begins.
     * @param yellowline beginning of yellow line
     */
    public void setYellowline( float yellowline ){
        float oldVal = this.yellowline;
        this.yellowline = yellowline;
        update();
        firePropertyChange(YELLOWLINE_PROP,oldVal,yellowline);
    }
    
    /**
     * Sets the airspeed at which the yellowline begins.
     * @return beginning of yellow line
     */
    public float getYellowline(){
        return this.yellowline;
    }
    
    
    
    
    /**
     * Updates the markings on the gauge.
     */
    private void update(){
        updateTicks();
        updateNumerals();
        updateBands();
    }
    
    
    /**
     * Updates the tick marks on the gauge.
     */
    private void updateTicks(){
        float degPerSpeed = (maxDegrees - minDegrees) / (maxAirspeed - minAirspeed);
        
        // How many ticks?
        // Less than 30? Try divisible by 5
        int range = maxAirspeed - minAirspeed;
        if( range <= 30 && range % 5 == 0 ){
            
            int numTicks = range / 5 + 1;
            if( this.majorTicks.length != numTicks ){
                this.majorTicks = new float[numTicks];
            }   // end if: resize
            this.majorTicks[0] = minDegrees;
            this.majorTicks[numTicks-1] = maxDegrees;
            for( int i = 1; i < numTicks-1; i++ ){
                this.majorTicks[i] = minDegrees + 5 * i * degPerSpeed;
            }
            
            // Minor ticks: integer airspeeds between min and max
            int numMinTicks = this.maxAirspeed - this.minAirspeed- 1;
            if( numMinTicks < 0 ){
                this.minorTicks = null;
            } else {
                if( this.minorTicks.length != numMinTicks ){
                    this.minorTicks = new float[ numMinTicks ];
                }   // end if: need to resize array
                for( int i = 0; i < numMinTicks; i++ ){
                    this.minorTicks[i] = this.minDegrees + (i+1) * degPerSpeed;
                }   // end for: each minor tick
            }
            
        } else if( range <= 100 && range % 10 == 0 ){
            int numTicks = range / 10 + 1;
            if( this.majorTicks.length != numTicks ){
                this.majorTicks = new float[numTicks];
            }   // end if: resize
            this.majorTicks[0] = minDegrees;
            this.majorTicks[numTicks-1] = maxDegrees;
            for( int i = 1; i < numTicks-1; i++ ){
                this.majorTicks[i] = minDegrees + 10 * i * degPerSpeed;
            }
            
            // Minor ticks: integer airspeeds between min and max
            int numMinTicks = (this.maxAirspeed - this.minAirspeed) / 5;
            if( numMinTicks < 0 ){
                this.minorTicks = null;
            } else {
                if( this.minorTicks.length != numMinTicks ){
                    this.minorTicks = new float[ numMinTicks ];
                }   // end if: need to resize array
                for( int i = 0; i < numMinTicks; i++ ){
                    this.minorTicks[i] = this.minDegrees + 5 * (i+1) * degPerSpeed;
                }   // end for: each minor tick
            }
            
        } else { // Two major ticks: min and max
            if( this.majorTicks.length != 2 ){
                this.majorTicks = new float[2];
            }   // end if: resize
            this.majorTicks[0] = this.minDegrees;
            this.majorTicks[1] = this.maxDegrees;
            
            // Minor ticks: integer airspeeds between min and max
            int numMinTicks = this.maxAirspeed - this.minAirspeed- 1;
            if( numMinTicks < 0 ){
                this.minorTicks = null;
            } else {
                if( this.minorTicks.length != numMinTicks ){
                    this.minorTicks = new float[ numMinTicks ];
                }   // end if: need to resize array
                for( int i = 0; i < numMinTicks; i++ ){
                    this.minorTicks[i] = this.minDegrees + (i+1) * degPerSpeed;
                }   // end for: each minor tick
            }
        }
        
        super.setMajorTickDegrees(this.majorTicks);
        super.setMinorTickDegrees(this.minorTicks);
    }
    
    
    /**
     * Updates the min and max airspeed numerals.
     */
    private void updateNumerals(){
        // Should match major ticks
        if( this.numerals.length != this.majorTicks.length ){
            this.numerals = new int[this.majorTicks.length][2];
        }   // end if: resize
        
        // Min and max
        this.numerals[0][0] = (int)minDegrees;
        this.numerals[0][1] = minAirspeed;
        this.numerals[this.numerals.length-1][0] = (int)maxDegrees;
        this.numerals[this.numerals.length-1][1] = maxAirspeed;
        
        // In between
        for( int i = 1; i < this.numerals.length-1; i++ ){
            this.numerals[i][0] = (int)this.majorTicks[i];
            this.numerals[i][1] = minAirspeed + (maxAirspeed - minAirspeed) / (this.numerals.length-1) * i;
        }
        
        setMajorNumerals(this.numerals);
    }
    
    
    
    /**
     * Update the bands on the gauge.
     */
    private void updateBands(){
        float degPerSpeed = (maxDegrees - minDegrees) / (maxAirspeed - minAirspeed);
        
        float greenStartDeg = minDegrees + (greenline - minAirspeed) * degPerSpeed;
        float greenStopDeg = greenStartDeg + (yellowline - greenline) * degPerSpeed;
        float yellowStopDeg = greenStopDeg + (redline - yellowline) * degPerSpeed;
        
        bands[GREEN_BAND_INDEX][0] = greenStartDeg;
        bands[GREEN_BAND_INDEX][1] = greenStopDeg;
        
        bands[YELLOW_BAND_INDEX][0] = greenStopDeg;
        bands[YELLOW_BAND_INDEX][1] = yellowStopDeg;
        
        bands[RED_BAND_INDEX][0] = yellowStopDeg;
        bands[RED_BAND_INDEX][1] = maxDegrees;
        
        
        
        setBands(bands);
    }
    
    
    
    
}
