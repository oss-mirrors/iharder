
package net.iharder.gauge;


/**
 * This is probably the most useful class for building simple gauges.
 * It supports displaying a single value, and it's easy to change
 * the parameters in an IDE such as NetBeans or Eclipse.
 * 
 * @author Robert Harder, rob@iharder.net
 */
public class OneValueGauge extends DefaultGauge {

    /** The property "value" corresponding to property change events. */
    public final static String VALUE_PROP = "value";
    private float value = 50;
    private float[] hands = new float[1];

    /** The property "maxValue" corresponding to property change events. */
    public final static String MAX_VALUE = "maxValue";
    private float maxValue = 100;

    /** The property "minValue" corresponding to property change events. */
    public final static String MIN_VALUE = "minValue";
    private float minValue = 0;

    /** The property "intermediateSteps" corresponding to property change events. */
    public final static String INTERMEDIATE_STEPS = "intermediateSteps";
    private int intermediateSteps = 4;

    /** The property "redMin" corresponding to property change events. */
    public final static String RED_MIN_PROP = "redMin";
    private float redMin = 95;

    /** The property "redMax" corresponding to property change events. */
    public final static String RED_MAX_PROP = "redMax";
    private float redMax = 100;

    /** The property "yellowMin" corresponding to property change events. */
    public final static String YELLOW_MIN_PROP = "yellowMin";
    private float yellowMin = 90;

    /** The property "yellowMax" corresponding to property change events. */
    public final static String YELLOW_MAX_PROP = "yellowMax";
    private float yellowMax = 95;

    /** The property "greenMin" corresponding to property change events. */
    public final static String GREEN_MIN_PROP = "greenMin";
    private float greenMin = 70;

    /** The property "greenMax" corresponding to property change events. */
    public final static String GREEN_MAX_PROP = "greenMax";
    private float greenMax = 90;

    /** The property "degreesStart" corresponding to property change events. */
    public final static String DEGREES_START_PROP = "degreesStart";
    private int degreesStart = 225;

    /** The property "degreesRange" corresponding to property change events. */
    public final static String DEGREES_RANGE_PROP = "degreesRange";
    private int degreesRange = 270;

    /** The property "minorTickSpacing" corresponding to property change events. */
    public final static String MINOR_TICK_SPACING_PROP = "minorTickSpacing";
    private float minorTickSpacing = 5;

    /** The property "minorTickValues" corresponding to property change events. */
    public final static String MINOR_TICK_VALUES_PROP = "minorTickValues";
    private float[] minorTickValues = null;
    

    private int greenBandIndex = 0;
    private int yellowBandIndex = 1;
    private int redBandIndex = 2;
    private Object[][] bands =  new Object[][]{
        {120,125,java.awt.Color.green},
        {125,130,java.awt.Color.yellow},
        {130,135,java.awt.Color.red}
    };

    /**
     * Creates a OneValueGauge with default values.
     */
    public OneValueGauge(){
        setBands(bands);
        setValue(this.value);
        updateMarkings();
    }


    /**
     * Sets the color (or gradient or texture, etc) for
     * the green line band, if present.
     * @param pnt a java.awt.Paint object
     */
    public void setGreenPaint( java.awt.Paint pnt ){
        this.bands[greenBandIndex][2] = pnt;
        updateMarkings();
    }

    /**
     * Returns the color (or gradient or texture, etc)
     * for the green line band, if present.
     * @return the java.awt.Paint for the green band
     */
    public java.awt.Paint getGreenPaint(){
        return (java.awt.Paint)this.bands[greenBandIndex][2];
    }



    /**
     * Sets the color (or gradient or texture, etc) for
     * the yellow line band, if present.
     * @param pnt a java.awt.Paint object
     */
    public void setYellowPaint( java.awt.Paint pnt ){
        this.bands[yellowBandIndex][2] = pnt;
        updateMarkings();
    }

    /**
     * Returns the color (or gradient or texture, etc)
     * for the yellow line band, if present.
     * @return the java.awt.Paint for the yellow band
     */
    public java.awt.Paint getYellowPaint(){
        return (java.awt.Paint)this.bands[yellowBandIndex][2];
    }


    /**
     * Sets the color (or gradient or texture, etc) for
     * the red line band, if present.
     * @param pnt a java.awt.Paint object
     */
    public void setRedPaint( java.awt.Paint pnt ){
        this.bands[redBandIndex][2] = pnt;
        updateMarkings();
    }

    /**
     * Returns the color (or gradient or texture, etc)
     * for the red line band, if present.
     * @return the java.awt.Paint for the red band
     */
    public java.awt.Paint getRedPaint(){
        return (java.awt.Paint)this.bands[redBandIndex][2];
    }
    
    
    /**
     * Sets the value and updates the hands.
     * @param val the value to set
     */
    public void setValue( float val ){
        float oldVal = this.value;
        this.value = val;
        if( val < this.minValue ){
            hands[0] = this.degreesStart;
        } else if( val > this.maxValue ){
            hands[0] = this.degreesStart + this.degreesRange;
        } else {
            hands[0] = degreeForValue(val);
        }
        setValues(hands);
        firePropertyChange(VALUE_PROP,oldVal,val);
    }
    
    /**
     * Returns the current value.
     * @return the current value
     */
    public float getValue(){
        return this.value;
    }


    /**
     * Sets the minimum value to display on the gauge.
     * This will correspond to where you set the
     * minimum degrees.
     * If <tt>min</tt> is greater than the max value,
     * then <tt>min</tt> will be changed to max value.
     * @param min the minimum value
     */
    public void setMinValue( float min ){
        if( min > this.maxValue ) {
            min = this.maxValue;
        }
        float oldVal = this.minValue;
        this.minValue = min;
        updateMarkings();
        firePropertyChange( MIN_VALUE, oldVal, min );
    }


    /**
     * Gets the minimum value to display on the gauge.
     * This will correspond to where you set the
     * minimum degrees.
     * @return the minimum value
     */
    public float getMinValue(){
        return this.minValue;
    }



    /**
     * Sets the maximum value to display on the gauge.
     * This will correspond to where you set the
     * maximum degrees.
     * If <tt>max</tt> is less than the min value,
     * then <tt>max</tt> will be changed to min value.
     * @param max the maximum value
     */
    public void setMaxValue( float max ){
        if( max < this.minValue ) {
            max = this.minValue;
        }
        float oldVal = this.maxValue;
        this.maxValue = max;
        updateMarkings();
        firePropertyChange( MAX_VALUE, oldVal, max );
    }


    /**
     * Gets the maximum value to display on the gauge.
     * This will correspond to where you set the
     * maximum degrees.
     * @return the maximum value
     */
    public float getMaxValue(){
        return this.maxValue;
    }




    /**
     * Sets the point in degrees (north is zero) where
     * the minimum value will be represented.
     * @param start the starting point of the gauge
     */
    public void setDegreesStart( int start ){
        int oldVal = this.degreesStart;
        this.degreesStart = start;
        updateMarkings();
        firePropertyChange( DEGREES_START_PROP, oldVal, start );
    }


    /**
     * Gets the point in degrees (north is zero) where
     * the minimum value will be represented.
     * @return the starting point of the gauge
     */
    public int setDegreesStart(){
        return this.degreesStart;
    }




    /**
     * Sets how many degrees the gauge should span.
     * A typical car fuel gauge, for example, might have
     * a range of 60 degrees, whereas a speedometer that
     * takes up most of the circle might have a 270 degree range.
     * @param range The range, in degrees, of the gauge, min to max
     */
    public void setDegreesRange( int range ){
        int oldVal = this.degreesRange;
        this.degreesRange = range;
        updateMarkings();
        firePropertyChange( DEGREES_RANGE_PROP, oldVal, range );
    }


    /**
     * Gets how many degrees the gauge should span.
     * A typical car fuel gauge, for example, might have
     * a range of 60 degrees, whereas a speedometer that
     * takes up most of the circle might have a 270 degree range.
     * @return The range, in degrees, of the gauge, min to max
     */
    public int setDegreesRange(){
        return this.degreesRange;
    }



    /**
     * Sets how many intermediate major tick marks will be
     * evenly distributed between the minimum and maximum values.
     * If <tt>inter</tt> is negative, it will be changed to zero.
     * @param inter Number of intermediate steps
     */
    public void setIntermediateSteps( int inter ){
        if( inter < 0 ){
            inter = 0;
        }
        int oldVal = this.intermediateSteps;
        this.intermediateSteps = inter;
        updateMarkings();
        firePropertyChange( INTERMEDIATE_STEPS, oldVal, inter );
    }


    /**
     * Gets how many intermediate major tick marks will be
     * evenly distributed between the minimum and maximum values.
     * @return Number of intermediate steps
     */
    public int getIntermediateSteps(){
        return this.intermediateSteps;
    }

    /**
     * Updates the positions of tick marks, bands, etc.
     */
    private void updateMarkings(){
        float min = this.minValue;
        float max = this.maxValue;
        float range = max - min;
        int intermediate = this.intermediateSteps;

        // Major ticks
        float[] majorTicks = new float[ intermediate + 2 ];
        for( int i = 0; i < majorTicks.length; i++ ){
            majorTicks[i] = this.degreesStart + (float)this.degreesRange * i / (majorTicks.length-1);
        }   // end for: each major tick
        super.setMajorTickDegrees(majorTicks);

        // Major numerals
        int[][] majorNums = new int[majorTicks.length][2];
        for( int i = 0; i < majorTicks.length; i++ ){
            majorNums[i][0] = (int)majorTicks[i];
            majorNums[i][1] = (int)(min + range * i / (majorTicks.length-1));
        }   // end for: each major numeral
        super.setMajorNumerals(majorNums);

        // Minor ticks
        // If minorTickValues is not null, then use that,
        // otherwise spread out ticks according to minorTickSpacing
        if( this.minorTickValues == null ){
            float[] minorTicks = new float[ (int)(range / this.minorTickSpacing)];
            for( int i = 0; i < minorTicks.length; i++ ){
                minorTicks[i] = degreeForValue(this.minValue + this.minorTickSpacing * (i+1));
            }
            super.setMinorTickDegrees(minorTicks);
        } else {
            float[] minorTicks = new float[ this.minorTickValues.length ];
            for( int i = 0; i < minorTicks.length; i++ ){
                minorTicks[i] = degreeForValue( this.minorTickValues[i] );
            }
            super.setMinorTickDegrees(minorTicks);
        }


        // Bands
        this.bands[this.greenBandIndex][0] = degreeForValue( this.greenMin );   // Green
        this.bands[this.greenBandIndex][1] = degreeForValue( this.greenMax );   // Green
        this.bands[this.yellowBandIndex][0] = degreeForValue( this.yellowMin ); // Yellow
        this.bands[this.yellowBandIndex][1] = degreeForValue( this.yellowMax ); // Yellow
        this.bands[this.redBandIndex][0] = degreeForValue( this.redMin );       // Red
        this.bands[this.redBandIndex][1] = degreeForValue( this.redMax );       // Red

        repaint();
    }

    /**
     * Handy calculation that's used repeatedly for converting a number
     * in the gauge's "value" domain to degrees in a circle.
     * @param val The value to convert
     * @return The degrees for that value
     */
    private float degreeForValue( float val ){
        float deg = this.degreesStart + (float)this.degreesRange * (val - this.minValue) / (this.maxValue - this.minValue);
        return deg;
    }


/* ********  G R E E N   B A N D  ******** */
    
    /**
     * Sets the value corresponding to the beginning of the green band.
     * If no band is desired, set the min and max to the same value.
     * @param greenMin the beginning of the green band
     */
    public void setGreenMin( float greenMin ){
        float oldVal = this.greenMin;
        this.greenMin = greenMin;
        updateMarkings();
        firePropertyChange( GREEN_MIN_PROP, oldVal, greenMin );
    }


    /**
     * Gets the value corresponding to the beginning of the green band.
     * @return the beginning of the green band
     */
    public float getGreenMin(){
        return this.greenMin;
    }


    /**
     * Sets the value corresponding to the end of the green band.
     * If no band is desired, set the min and max to the same value.
     * @param greenMax the end of the green band
     */
    public void setGreenMax( float greenMax ){
        float oldVal = this.greenMax;
        this.greenMax = greenMax;
        updateMarkings();
        firePropertyChange( GREEN_MAX_PROP, oldVal, greenMax );
    }


    /**
     * Gets the value corresponding to the end of the green band.
     * @return the end of the green band
     */
    public float getGreenMax(){
        return this.greenMax;
    }


/* ********  Y E L L O W   B A N D  ******** */


    /**
     * Sets the value corresponding to the beginning of the yellow band.
     * If no band is desired, set the min and max to the same value.
     * @param yellowMin the beginning of the yellow band
     */
    public void setYellowMin( float yellowMin ){
        //if( yellowMin > this.maxValue ) yellowMin = this.maxValue;
        float oldVal = this.yellowMin;
        this.yellowMin = yellowMin;
        updateMarkings();
        firePropertyChange( YELLOW_MIN_PROP, oldVal, yellowMin );
    }


    /**
     * Gets the value corresponding to the beginning of the yellow band.
     * @return the beginning of the yellow band
     */
    public float getYellowMin(){
        return this.yellowMin;
    }


    /**
     * Sets the value corresponding to the end of the yellow band.
     * If no band is desired, set the min and max to the same value.
     * @param yellowMax the end of the yellow band
     */
    public void setYellowMax( float yellowMax ){
        //if( yellowMax > this.maxValue ) yellowMax = this.maxValue;
        float oldVal = this.yellowMax;
        this.yellowMax = yellowMax;
        updateMarkings();
        firePropertyChange( YELLOW_MAX_PROP, oldVal, yellowMax );
    }


    /**
     * Gets the value corresponding to the end of the yellow band.
     * @return the end of the yellow band
     */
    public float getYellowMax(){
        return this.yellowMax;
    }


/* ********  R E D   B A N D  ******** */
    



    /**
     * Sets the value corresponding to the beginning of the red band.
     * If no band is desired, set the min and max to the same value.
     * @param redMin the beginning of the red band
     */
    public void setRedMin( float redMin ){
        //if( redMin > this.maxValue ) redMin = this.maxValue;
        float oldVal = this.redMin;
        this.redMin = redMin;
        updateMarkings();
        firePropertyChange( RED_MIN_PROP, oldVal, redMin );
    }


    /**
     * Gets the value corresponding to the beginning of the red band.
     * @return the beginning of the red band
     */
    public float getRedMin(){
        return this.redMin;
    }


    /**
     * Sets the value corresponding to the end of the red band.
     * If no band is desired, set the min and max to the same value.
     * @param redMax the end of the red band
     */
    public void setRedMax( float redMax ){
        //if( redMax > this.maxValue ) redMax = this.maxValue;
        float oldVal = this.redMax;
        this.redMax = redMax;
        updateMarkings();
        firePropertyChange( RED_MAX_PROP, oldVal, redMax );
    }


    /**
     * Gets the value corresponding to the end of the red band.
     * @return the end of the red band
     */
    public float getRedMax(){
        return this.redMax;
    }



    /**
     * Sets how often minor tick marks should be placed.
     * This is only used if explicit markings are not provided
     * by the {@link #setMinorTickValues(float[])} method.
     * @param interval spacing for minor ticks
     */
    public void setMinorTickSpacing( float interval ){
        float oldVal = this.minorTickSpacing;
        this.minorTickSpacing = interval;
        updateMarkings();
        firePropertyChange( MINOR_TICK_SPACING_PROP, oldVal, interval );
    }


    /**
     * Gets how often minor tick marks should be placed.
     * @return spacing for minor ticks
     */
    public float getMinorTickSpacing(){
        return this.minorTickSpacing;
    }


    /**
     * Sets exact positions of minor tick marks.
     * This overrides the behavior set by
     * {@link #setMinorTickSpacing(float)}
     * @param values array of values for minor tick marks
     */
    public void setMinorTickValues( float[] values ){
        float[] oldVal = this.minorTickValues;
        this.minorTickValues = values;
        updateMarkings();
        firePropertyChange( MINOR_TICK_VALUES_PROP, oldVal, values );
    }


    /**
     * Gets exact positions of minor tick marks.
     * @return array of values for minor tick marks
     */
    public float[] getMinorTickValues(){
        return this.minorTickValues;
    }



}   // end class OneValueGauge
