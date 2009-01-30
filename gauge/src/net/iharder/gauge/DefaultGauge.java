package net.iharder.gauge;
import java.beans.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.*;
import java.util.logging.Logger;


/**
 * This is a handy class for creating round airframe gauges such as altimeters,
 * compasses, and the like. It can be configured externally with the get
 * and set methods, and it can be subclassed to create ready-made gauges
 * of specific types and with specific extra features.
 *
 * @author rob
 */
public class DefaultGauge extends JComponent { 



/* ********  S T A T I C   F I E L D S  ******** */

    /** The X values defining the hand (like a clock) polygon. */
    private final static int[] HAND_X = {0,-2,-1,0,1,2};


    /** The Y values defining the hand (like a clock) polygon. */
    private final static int[] HAND_Y = {0,2,37,40,37,2};

    /** The largest Y value defining the hand (like a clock) polygon. */
    private final static int HAND_MAX_Y = 40;

    /** The paint for the hand. */
    private final static Paint HAND_PAINT = Color.white;

    /** The paint for the button over the values. */
    //private final static Paint HAND_BUTTON_PAINT = Color.black.brighter().brighter();

    /** The hand (like a clock) polygon. */
    private final static Polygon HAND = new Polygon(HAND_X,HAND_Y,HAND_X.length);


    /** The border paint of the gauge. */
    private final static Paint BORDER_PAINT = Color.black.brighter();

    /** The paint for tick marks. */
    private final static Paint TICK_PAINT = new Color(250,250,250);



    private final static Logger LOGGER = Logger.getLogger(DefaultGauge.class.getName());
    
/** ********  P R O P E R T I E S  ******** */    
    
    
    /** The property name <tt>values</tt> referring to the array of values. */
    public final static String VALUES_PROP = "values";
    private float[] values;


    /** The property name <tt>hand</tt> referring to the hand shape. */
    public final static String HAND_PROP = "hand";
    private Shape hand = HAND;


    /** The property name <tt>hand_scale</tt> referring to a scaling factor when drawing the hand shape. */
    public final static String HAND_SCALE_PROP = "hand_scale";
    private float handScale = 1.0f;


    /** The property name <tt>major_tick_degrees</tt>. */
    public final static String MAJOR_TICK_DEGREES_PROP = "major_tick_degrees";
    private float[] majorTicks;
    
    
    /** The property name <tt>minor_tick_degrees</tt>. */
    public final static String MINOR_TICK_DEGREES_PROP = "minor_tick_degrees";
    private float[] minorTicks;
    
    
    /** The property name <tt>major_numerals</tt>. */
    public final static String MAJOR_NUMERALS_PROP = "major_numerals";
    private int[][] majorNumerals;
    
    
    /** The property name <tt>minor_numerals</tt>. */
    public final static String MINOR_NUMERALS_PROP = "minor_numerals";
    private int[][] minorNumerals;
    
    
    /** The property name <tt>bands</tt>. */
    public final static String BANDS_PROP = "bands";
    private Object[][] bands;
    
    
    /** The property name <tt>label</tt>. */
    public final static String LABEL_PROP = "label";
    private String label;

    public final static String LABEL_ADJUSTMENT_PROP = "label_adjustment";
    private int labelAdjustment = 40;


    /** The background of the gauge. */
    public final static String BACKGROUND_PAINT_PROP = "background_paint";
    private Paint defaultBackgroundPaint;   // Updated as component resizes
    private Paint backgroundPaint;

    private Paint handButtonPaint;

    /** Optional image to draw instead of the normal values. */
    public final static String HANDS_IMAGE_PROP = "hands_image";
    private Image handsImage;
    
    
    
/* ********  O B J E C T   F I E L D S  ******** */    
    
    /** Passed from subclasses via interestedProperties() */
    //private String[] registeredPropertyNames = EMPTY_STRING_ARRAY;
    
    
    private Graphics2D g2d;
    private AffineTransform originalTransform;
    private AffineTransform baseTransform;
    private int squareSize;
    private float halfSquareSize;
    private float scale;
    private Stroke originalStroke;
    private Paint originalPaint;

    public DefaultGauge(){
        initComponents();
    }
    
    private void initComponents(){
        setPreferredSize(new java.awt.Dimension(250,250));
        updateElementsForSize();

        // Update gradient for the new size, if background
        // paint is not already overridden.
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateElementsForSize();
            }
        });

    }

    /**
     * Updates the background gradient with the new
     * component size, since the gradient should
     * appear "constant" as the component resizes.
     */
    private void updateElementsForSize(){
        int size = Math.min( getWidth(),getHeight());
        setBackgroundPaint( new GradientPaint( 0,0, Color.gray, size,size, Color.darkGray));


        if( size > 0 ){
            float buttonOffset = size * .04f * .03f;
            float buttonRadius = size * .04f * 2;
            this.handButtonPaint = new RadialGradientPaint(
                -buttonOffset,-buttonOffset, buttonRadius, new float[]{0f,.5f},new Color[]{Color.darkGray.darker(),Color.black});
        }
    }
    
    
    
    /**
     * Returns an array of floats that correspond to where major tick marks
     * should be drawn. The values are degrees, clockwise, starting at
     * 12 o'clock. For example, returning <code>new float[] {0, 90, 180, 270};</code>
     * would put major tick marks at north, south, east, and west.
     * 
     * @return array of degrees where major tick marks will be drawn.
     */
    public float[] getMajorTickDegrees(){ 
        return this.majorTicks;
    }
    
    
    /**
     * Sets an array of floats that correspond to where major tick marks
     * should be drawn. The values are degrees, clockwise, starting at
     * 12 o'clock. For example, returning <code>new float[] {0, 90, 180, 270};</code>
     * would put major tick marks at north, south, east, and west.
     * @param degrees array of degrees where major tick marks will be drawn.
     */
    public void setMajorTickDegrees( float[] degrees ){
        float[] oldVal = this.majorTicks;
        this.majorTicks = degrees;
        firePropertyChange( MAJOR_TICK_DEGREES_PROP, oldVal, degrees );
        repaint();
    }
    
    
    
    
    /**
     * Returns an array of floats that correspond to where minor tick marks
     * should be drawn. The values are degrees, clockwise, starting at
     * 12 o'clock. For example, returning <code>new int[] {15,30,45,60,75};</code>
     * would put minor tick marks in 15 degree increments from north to east.
     *
     * @return array of degrees where minor tick marks will be drawn.
     */
    protected float[] getMinorTickDegrees(){ 
        return this.minorTicks; 
    }
    
    
    /**
     * Returns an array of floats that correspond to where minor tick marks
     * should be drawn. The values are degrees, clockwise, starting at
     * 12 o'clock. For example, returning <code>new int[] {15,30,45,60,75};</code>
     * would put minor tick marks in 15 degree increments from north to east.
     *
     * @param degrees array of degrees where minor tick marks will be drawn.
     */
    public void setMinorTickDegrees( float[] degrees ){
        float[] oldVal = this.minorTicks;
        this.minorTicks = degrees;
        firePropertyChange( MINOR_TICK_DEGREES_PROP, oldVal, degrees );
        repaint();
    }
    
    
    /**
     * <p>Returns a two-dimensional array that represents colored bands
     * that may be drawn around the edge, just inside the bezel.
     * This is a more complex structure than the Major or Minor Ticks.
     * The first index in the array refers to a band.
     * The elements within that array refer to the definition of a band:</p>
     * <ul>
     *   <li>0: Start of band, in degrees from North</li>
     *   <li>1: End of band, painting clockwise, in degrees from North</li>
     *   <li>2: The color of the band, in the form of a <tt>java.awt.Paint</tt></li>
     * </ul>
     * <p>For the more adventurous, note that {@link java.awt.Paint} supports
     * more than just colors; it also supports gradients and textures.</p>
     * <p>The following example would define two bands, one that is green
     * from "noon to 3" and one that is red from "3 to 6":</p>
     * <pre><code>new Object[][]{
     *   { 0, 90, java.awt.Color.green },
     *   { 90, 180, java.awt.Color.red }
     * }</code></pre>
     * 
     * @return the bands
     */
    public Object[][] getBands(){ 
        return this.bands;
    }
    
    
    /**
     * <p>Sets a two-dimensional array that represents colored bands
     * that may be drawn around the edge, just inside the bezel,
     * or <tt>null</tt> if no bands are required.
     * This is a more complex structure than the Major or Minor Ticks.
     * The first index in the array refers to a band.
     * The elements within that array refer to the definition of a band:</p>
     * <ul>
     *   <li>0: Start of band, in degrees from North</li>
     *   <li>1: End of band, painting clockwise, in degrees from North</li>
     *   <li>2: The color of the band, in the form of a <tt>java.awt.Paint</tt></li>
     * </ul>
     * <p>For the more adventurous, note that {@link java.awt.Paint} supports
     * more than just colors; it also supports gradients and textures.</p>
     * <p>The following example would define two bands, one that is green
     * from "noon to 3" and one that is red from "3 to 6":</p>
     * <pre><code>new Object[][]{
     *   { 0, 90, java.awt.Color.green },
     *   { 90, 180, java.awt.Color.red }
     * }</code></pre>
     * @param newBands the new bands, or null if no bands
     */
    public void setBands( Object[][] newBands ){
        Object[][] oldVal = this.bands;
        this.bands = newBands;
        firePropertyChange( BANDS_PROP, oldVal, newBands );
        repaint();
    }
    
    
    
    /**
     * <p>Returns a two-dimensional 2xn array of minor numerals and where those should be
     * drawn, in degrees, clockwise, starting at 12 o'clock "north."
     * This array of arrays should be such that the second array is two ints long
     * with the first int being the degrees and the second int being the value to show.</p>
     * 
     * <p>For instance, to show the values 12, 3, 6, and 9 as on a clock, you would
     * return this array:</p>
     *
     * <code>return new int[][] { {0,12}, {90,3}, {180,6}, {270,9} };</code>
     *
     * @returns numerals and their locations
     */
    public int[][] getMajorNumerals(){ 
        return this.majorNumerals;
    }
    
    
    
    
    /**
     * <p>Sets a two-dimensional 2xn array of minor numerals and where those should be
     * drawn, in degrees, clockwise, starting at 12 o'clock "north."
     * This array of arrays should be such that the second array is two ints long
     * with the first int being the degrees and the second int being the value to show.</p>
     * 
     * <p>For instance, to show the values 12, 3, 6, and 9 as on a clock, you would
     * return this array:</p>
     *
     * <code>return new int[][] { {0,12}, {90,3}, {180,6}, {270,9} };</code>
     *
     * @param newNums numerals and their locations
     */
    public void setMajorNumerals( int[][] newNums ){
        int[][] oldVal = this.majorNumerals;
        this.majorNumerals = newNums;
        firePropertyChange( MAJOR_NUMERALS_PROP, oldVal, newNums );
        repaint();
    }
    
    
    
    /**
     * <p>Returns a two-dimensional 2xn array of amjor numerals and where those should be
     * drawn, in degrees, clockwise, starting at 12 o'clock "north."
     * This array of arrays should be such that the second array is two ints long
     * with the first int being the degrees and the second int being the value to show.</p>
     * 
     * <p>For instance, to show the values 12, 3, 6, and 9 as on a clock, you would
     * return this array:</p>
     *
     * <code>return new int[][] { {0,12}, {90,3}, {180,6}, {270,9} };</code>
     *
     * <p>Returns empty array. It is not necessary to call
     * <code>super.propertyChange(evt)</code> if you override
     * this method.</p>
     *
     * @returns numerals and their locations
     */
    protected int[][] getMinorNumerals(){ 
        return this.minorNumerals;
    }
    
    
    /**
     * <p>Sets a two-dimensional 2xn array of amjor numerals and where those should be
     * drawn, in degrees, clockwise, starting at 12 o'clock "north."
     * This array of arrays should be such that the second array is two ints long
     * with the first int being the degrees and the second int being the value to show.</p>
     * 
     * <p>For instance, to show the values 12, 3, 6, and 9 as on a clock, you would
     * return this array:</p>
     *
     * <code>return new int[][] { {0,12}, {90,3}, {180,6}, {270,9} };</code>
     *
     * <p>Returns empty array. It is not necessary to call
     * <code>super.propertyChange(evt)</code> if you override
     * this method.</p>
     *
     * @param newNums numerals and their locations
     */
    public void setMinorNumerals( int[][] newNums ){
        int[][] oldVal = this.minorNumerals;
        this.minorNumerals = newNums;
        firePropertyChange( MINOR_NUMERALS_PROP, oldVal, newNums );
        repaint();
    }
    
    /**
     * Returns an array of degrees (clockwise, starting at 12 o'clock "north") 
     * where values should be drawn.
     * The first element in the array will be the longest hand,
     * the second will be shorter, and so forth. Eventually your gauge
     * will be illegible, so don't go crazy here.
     * <p>Returns empty array. It is not necessary to call
     * <code>super.propertyChange(evt)</code> if you override
     * this method.</p>
     *
     * @return array of degrees where values should be drawn
     */
    public float[] getValues(){
        return this.values;
    }
    
    
    /**
     * After getting a new value, either by intercepting setValue()
     * or by listening for a property change event, compute the new
     * positions of the hand or values and set the value here.
     * This will automatically generate a repaint().
     * @param newVal
     */
    public void setValues( float[] newVal ){
        float[] oldVal = this.values;
        this.values = newVal;
        firePropertyChange( VALUES_PROP, oldVal, newVal );
        repaint();
    }
    
    
    /**
     * Sets an image to use in place of drawing the first hand.
     * @param img
     */
    public void setHandsImage( Image newVal ){
        Image oldVal = this.handsImage;
        this.handsImage = newVal;
        firePropertyChange( HANDS_IMAGE_PROP, oldVal, newVal );
        repaint();
    }
    
    /**
     * Returns the image used for drawing the first hand.
     * @return
     */
    public Image getHandsImage(){
        return this.handsImage;
    }


    /**
     * Sets the shape to draw for each hand (scaled down if
     * using multiple values). The presence (not null) of
     * an image set by {@link #setHandsImage(java.awt.Image)}
     * will override this shape setting.
     * 
     * @param hand
     */
    public void setHand( Shape hand ){
        Shape oldVal = this.hand;
        this.hand = hand;
        firePropertyChange( HAND_PROP, oldVal, hand );
        repaint();
    }

    /**
     * Returns the shape to draw for each hand (scaled down if
     * using multiple values). The presence (not null) of
     * an image set by {@link #setHandsImage(java.awt.Image)}
     * will override this shape setting.
     *
     * @return hand
     */
    public Shape getHand(){
        return this.hand;
    }


    /**
     * Sets a scaling factor for drawing the hands when using a shape.
     * The default value is 1.0 which works for the built-in hands.
     * You will need to use trial and error to find the right value
     * if you provide a custom hand shape.
     * @param scale
     */
    public void setHandScale( float scale ){
        float oldVal = this.handScale;
        this.handScale = scale;
        firePropertyChange( HAND_SCALE_PROP, oldVal, scale );
        repaint();
    }

    /**
     * Returns a scaling factor for drawing the hands when using a shape.
     * The default value is 1.0 which works for the built-in hands.
     * You will need to use trial and error to find the right value
     * if you provide a custom hand shape.
     * @return scale
     */
    public float getHandScale(){
        return this.handScale;
    }


    /**
     * Sets an image to use in place of drawing the first hand.
     * @param img
     */
    public void setBackgroundPaint( Paint newVal ){
        Paint oldVal = this.backgroundPaint;
        this.backgroundPaint = newVal;
        firePropertyChange( BACKGROUND_PAINT_PROP, oldVal, newVal );
        repaint();
    }

    /**
     * Returns the image used for drawing the first hand.
     * @return
     */
    public Paint getBackgroundPaint(){
        return this.backgroundPaint;
    }

    
    /**
     * Sets the label that will appear on the gauge.
     * @param text
     */
    public void setLabel( String text ){
        String oldVal = this.label;
        this.label = text;
        repaint();
        firePropertyChange( LABEL_PROP, oldVal, text );
    }
    
    /**
     * Returns the label that appears on the gauge.
     * @return
     */
    public String getLabel(){
        return this.label;
    }
    

    /**
     * Adjusts vertical position of label. Default is 40.
     * Larger values move label further from the center.
     * @return
     */
    public int getLabelAdjustment(){
        return this.labelAdjustment;
    }

    public void setLabelAdjustment( int adj ){
        int oldVal = this.labelAdjustment;
        this.labelAdjustment = adj;
        repaint();
        firePropertyChange( LABEL_ADJUSTMENT_PROP, oldVal, adj );
    }

    
/* ********  P A I N T   H E L P E R S  ******** */    
    
    
    protected void paintRotatedImage( Graphics2D g2d, Image image, float degrees ){
        
        // Draw image
        if( image != null ){
            float imgWidth = image.getWidth(this); 
            float imgHeight = image.getHeight(this);
            float halfImgWidth = 0.5f * imgWidth;
            float halfImgHeight = 0.5f * imgHeight;
            float halfSquareSize = squareSize * 0.5f;
            float scale = (squareSize-20)/Math.max(imgWidth,imgHeight)*0.7f*this.handScale;
            AffineTransform at = g2d.getTransform(); // Record what user came in with
            g2d.setTransform(baseTransform);         // We want upper left referencing
            
            AffineTransform trans = new AffineTransform();
            trans.concatenate(AffineTransform.getScaleInstance(scale,scale));
            trans.concatenate( AffineTransform.getTranslateInstance(-halfImgWidth,-halfImgHeight) );
            
            g2d.translate(halfSquareSize,halfSquareSize);
            g2d.rotate(degrees * Math.PI / 180);
            g2d.drawImage(image,trans,this);
            g2d.setTransform(at);
            
        }   // end if: image loaded
    }   // end paintRotatedImage
    
    
    protected void paintRotatedShape( Graphics2D g2d, Shape shape, Stroke stroke, Paint paint, float shapeScale, float degrees ){
        
        // Draw image
        if( shape != null ){
            Rectangle2D bounds = shape.getBounds2D();;
            bounds.getWidth();
            bounds.getHeight();
            
            //float width =  image.getWidth(this); 
            //float height = image.getHeight(this);
            //float halfImgWidth = 0.5f * imgWidth;
            //float halfImgHeight = 0.5f * imgHeight;
            float halfSquareSize = squareSize * 0.5f;
            //float scale = (squareSize-20)/imgWidth*0.7f;
            AffineTransform prevTransform = g2d.getTransform(); // Record what user came in with
            Stroke prevStroke = g2d.getStroke();
            Paint prevPaint = g2d.getPaint();
            
            g2d.setTransform(baseTransform);         // We want upper left referencing
            g2d.setStroke(stroke);
            g2d.setPaint(paint);
            
            g2d.translate(halfSquareSize,halfSquareSize);
            g2d.rotate(degrees * Math.PI / 180);
            g2d.scale(scale*shapeScale,scale*shapeScale);
           // g2d.drawImage(image,trans,this);
            g2d.draw(shape);
            
            g2d.setTransform(prevTransform);
            g2d.setStroke(prevStroke);
            g2d.setPaint(prevPaint);
            
        }   // end if: image loaded
    }   // end paintRotatedShape
    
    
    
    
/* ********  P A I N T  ******** */    
    
    
    
    
    /** Paints the gauge. */
    public void paint( Graphics g ){
        super.paint(g);
        if( g instanceof Graphics2D ){
            g2d = (Graphics2D)g;
            int width = this.getWidth();
            int height = this.getHeight();
            squareSize = Math.min(width,height);
            halfSquareSize = squareSize*0.5f;
            scale = halfSquareSize / 100;
            originalTransform = g2d.getTransform();
            originalPaint = g2d.getPaint();
            originalStroke = g2d.getStroke();


            // Center and make square
            if( width > squareSize ){
                g2d.translate((width-squareSize)*.5,0);
            } else if( height > squareSize) {
                g2d.translate(0,(height-squareSize)*.5);
            }
            baseTransform = g2d.getTransform();
            
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Background of gauge
            paintBackground(g2d,squareSize);
            g2d.setTransform(baseTransform);
            
            // Numerals and tick marks
            paintFace(g2d,squareSize);
            g2d.setTransform(baseTransform);
            
            // Hands
            paintHands(g2d,squareSize);
            g2d.setTransform(baseTransform);
            
            // Top coating
            paintBezel(g2d,squareSize);
            g2d.setTransform(baseTransform);
            
        
            // Reset
            g2d.setStroke(originalStroke);
            g2d.setPaint(originalPaint);
            g2d.setTransform(originalTransform);
        }   // end if: graphics2d
    }   // end paint
    
    
    
    protected void paintBackground( Graphics2D g2d, int squareSize ){
        
        // Background fill
        g2d.setPaint(this.backgroundPaint == null ? this.defaultBackgroundPaint : this.backgroundPaint );
        g2d.fill( new Ellipse2D.Float(squareSize*.01f,squareSize*.01f,squareSize*.98f,squareSize*.98f));
        
    }
    
    protected void paintFace( Graphics2D g2d, int squareSize ){
        
        // Colored bands below tick marks
        // Major index is a band definition
        // Minor index:
        //   0 = Start degrees
        //   1 = End degrees
        //   2 = Color
        Object[][] bands = getBands();
        if( bands != null ){
            Rectangle2D bounds = new Rectangle2D.Float(squareSize*.03f,squareSize*.03f,squareSize*.94f,squareSize*.94f);
            Arc2D arc = new Arc2D.Float(bounds,0,0, Arc2D.OPEN);
            g2d.setStroke(new BasicStroke(9*scale, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
            for( int i = 0; i < bands.length; i++ ){
                if( bands[i].length >= 3 ){
                    if( bands[i][0] instanceof Number && bands[i][1] instanceof Number && bands[i][2] instanceof Paint ){
                        double start = ((Number)bands[i][0]).doubleValue(); // Requested start degrees
                        double end = ((Number)bands[i][1]).doubleValue();   // Requested end degrees
                        if( end < start ){                                  // Handle wrapping right
                            end += 360;
                        }
                        double extent = -(end - start); // Arcs are weird.
                        arc.setAngleStart( -start+90 ); // Arcs are weird. 
                        arc.setAngleExtent( extent ); 
                        g2d.setPaint((Paint)bands[i][2]);
                        g2d.draw(arc);
                    }   // end if: propery element types
                }   // end if: 3 elements in array
            }   // end for: each band
        }   // end if: bands != null
        
        // Big tick marks
        float[] bigTicks = getMajorTickDegrees();
        if( bigTicks != null ){
            g2d.setStroke(new BasicStroke(4*scale, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.setPaint(TICK_PAINT);
            for( int i = 0; i < bigTicks.length; i++ ){
                g2d.rotate( (bigTicks[i]%360) * Math.PI / 180 + Math.PI * .5, halfSquareSize, halfSquareSize );
                g2d.draw( new Line2D.Float(.02f*squareSize,halfSquareSize, .08f*squareSize,halfSquareSize));
                g2d.setTransform(baseTransform);
            }
        }   // end if: bitTicks not null
        
        // Little tick marks
        float[] littleTicks = getMinorTickDegrees();
        if( littleTicks != null ){
            g2d.setStroke(new BasicStroke(2*scale, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for( int i = 0; i < littleTicks.length; i++ ){
                g2d.rotate( (littleTicks[i]%360) * Math.PI / 180 + Math.PI * .5, halfSquareSize, halfSquareSize );
                g2d.draw( new Line2D.Float(.02f*squareSize,halfSquareSize, .05f*squareSize,halfSquareSize));
                g2d.setTransform(baseTransform);
            }
        }   // end if: littleTicks not null
        
        
        // Draw a label on the gauge
        String label = getLabel();
        if( label != null ){
            g2d.translate(halfSquareSize,halfSquareSize);   // Move to center
            g2d.translate(-label.length()*7*scale, this.labelAdjustment*scale);
            g2d.scale(scale*2,2*scale);
            g2d.drawString(label,0,0);
            g2d.setTransform(baseTransform);
        }
        
        
        // Draw Major Numerals
        int[][] numerals;
        numerals = getMajorNumerals();
        if( numerals != null ){
            for( int i = 0; i < numerals.length; i++ ){
                int deg = numerals[i][0];
                int val = numerals[i][1];
                int numDigits = val == 0 ? 1 : (int)Math.ceil(Math.log10(Math.abs(val)));
                if( val < 0 ){
                    numDigits++;    // Count a negative sign
                }
                float degAdj = deg;//deg < 181 ? deg - 90 + 180 : deg - 90+180;
                g2d.translate(halfSquareSize,halfSquareSize);   // Move to center
                if( (deg % 360) <= 180 ){
                    g2d.translate(-(numDigits-1)*scale*10,0);        // On right side of dial, nudge numbers left
                }
                g2d.rotate(degAdj * Math.PI / 180 +Math.PI*.5); // Rotate
                g2d.translate(-halfSquareSize*.7f,0);           // Move left
                g2d.rotate(-degAdj * Math.PI / 180 -Math.PI*.5);// Rotate back
                g2d.translate(-7*scale, 10*scale);
                g2d.scale(scale*2,2*scale);
                g2d.drawString(""+val,0,0);
                g2d.setTransform(baseTransform);
            }   // end for: each numeral
        }   // end if: numerals not null
        
        // Draw Minor Numerals
        numerals = getMinorNumerals();
        if( numerals != null ){
            for( int i = 0; i < numerals.length; i++ ){
                int deg = numerals[i][0];
                int val = numerals[i][1];
                int numDigits = val == 0 ? 1 : (int)Math.ceil(Math.log10(Math.abs(val)));
                float degAdj = deg;//deg < 181 ? deg - 90 + 180 : deg - 90+180;
                g2d.translate(halfSquareSize,halfSquareSize);   // Move to center
                if( (deg % 360) <= 180 ){
                    g2d.translate(-(numDigits-1)*scale*2,0);        // On right side of dial, nudge numbers left
                }
                g2d.rotate(degAdj * Math.PI / 180 +Math.PI*.5); // Rotate to account for location on dial
                g2d.translate(-halfSquareSize*.8f,0);           // Move left, 9 o'clock, for reference
                g2d.rotate(-degAdj * Math.PI / 180 -Math.PI*.5);// Rotate back to horizontal
                g2d.translate(-4*scale, 5*scale);
                g2d.scale(scale*.8f,.8f*scale);
                g2d.drawString(""+val,0,0);
                g2d.setTransform(baseTransform);

            }   // end for: each numeral
        }   // end if: numerals not null
    }
    
    
        
    protected void paintHands( Graphics2D g2d, int squareSize ){
        
        // Draw values
        float[] hands = getValues();
        if( hands != null ){
            for( int i = 0; i < hands.length; i++ ){
                float deg = hands[i];
                
                // Special case: first hand may be an image
                if( i == 0 && this.handsImage != null ){
                    paintRotatedImage(g2d, handsImage, deg);
                } else {
                    float handScale = (.88f * halfSquareSize / HAND_MAX_Y * (1-i*.3f)) * this.handScale;
                    g2d.translate(halfSquareSize,halfSquareSize);
                    g2d.rotate( deg * Math.PI / 180 + Math.PI);
                    g2d.scale(handScale,handScale);
                    g2d.setPaint(HAND_PAINT);

                    g2d.fill( this.hand );
                    g2d.setTransform(baseTransform);
                }   // end else: just a hand
            }
        }   // end if: values not null
        
        // Draw button on hand if we're not painting an image
        if( this.handsImage == null ){
            g2d.translate(halfSquareSize,halfSquareSize);
            float f1 = squareSize*.04f;
            g2d.setPaint(this.handButtonPaint);
            g2d.fill( new Ellipse2D.Float(-f1,-f1,2*f1,2*f1));
        }
        
    }
    
    
    
    protected void paintBezel( Graphics2D g2d, int squareSize ){
        
        
        // Border
        g2d.setPaint(BORDER_PAINT);
        g2d.setStroke(new BasicStroke(2*scale));
        g2d.draw( new Ellipse2D.Float(squareSize*.01f,squareSize*.01f,squareSize*.98f,squareSize*.98f));
        
    }
    
    
    
}   // end DefaultGauge
