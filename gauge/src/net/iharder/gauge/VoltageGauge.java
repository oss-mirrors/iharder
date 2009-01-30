/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.iharder.gauge;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author robert.harder
 */
public class VoltageGauge extends DefaultGauge {
    private final static Logger LOGGER = Logger.getLogger(VoltageGauge.class.getName());

    public final static String VOLTAGE_PROP = "voltage";
    private float voltage;
    private float[] hands = new float[1];

    public final static String MIN_VOLTAGE_PROP = "min_voltage";
    private int minVoltage = 9;
    public final static String MAX_VOLTAGE_PROP = "max_voltage";
    private int maxVoltage = 15;
    private float[] majorTicks = new float[2];
    private float[] minorTicks = new float[0];
    
    public final static String REDLINE_PROP = "redline";
    private float redline = 9.5f;
        
    public final static String YELLOWLINE_PROP = "yellowline";
    private float yellowline = 10;
    
    
    private final static int RED_BAND_INDEX = 0;
    private final static int YELLOW_BAND_INDEX = 1;
    private final static int GREEN_BAND_INDEX = 2;
    private float minDegrees = 360-60;
    private float maxDegrees = 360+60;
    private Object[][] bands = new Object[][]{
        {null,null,java.awt.Color.red},
        {null,null,java.awt.Color.yellow},
        {null,null,java.awt.Color.green}
    };
    
    
    public final static String VOLTAGES_TO_DISPLAY_PROP = "voltages_to_display";
    private int[] voltagesToDisplay;
    private int[][] numerals = new int[2][2];
    
    
    /**
     * Creates a VoltageGauge centered around a 12V signal.
     */
    public VoltageGauge(){
        initComponents();
    }
    
    private void initComponents(){
        setLabel("VOLT");
        update();
        this.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                showEditor();
            }   // end mouseClicked
        }); // end mouse adapter
    }   // end initComponents
    
    
    
    
    /**
     * Sets the minimum voltage displayed on the gauge.
     * Must be an integer.
     * @param min
     */
    public void setMinVoltage( int min ){
        int oldVal = this.minVoltage;
        this.minVoltage = min;
        update();
        firePropertyChange( MIN_VOLTAGE_PROP, oldVal, min );
    }
    
    /**
     * Returns the minimum voltage displayed on the gauge.
     * @return
     */
    public int getMinVoltage(){
        return this.minVoltage;
    }
    
    
    
    /**
     * Sets the maximum voltage displayed on the gauge.
     * Must be an integer.
     * @param max
     */
    public void setMaxVoltage( int max ){
        int oldVal = this.maxVoltage;
        this.maxVoltage = max;
        update();
        firePropertyChange( MAX_VOLTAGE_PROP, oldVal, max );
    }
    
    
    /**
     * Returns the maximum voltage displayed on the gauge.
     * @return
     */
    public int getMaxVoltage(){
        return this.maxVoltage;
    }
    
    /**
     * Sets the current voltage to display.
     * @param voltage
     */
    public void setVoltage( float voltage ){
        float oldVal = this.voltage;
        this.voltage = voltage;
        if( voltage < this.minVoltage ){
            hands[0] = this.minDegrees;
        } else if( voltage > this.maxVoltage ){
            hands[0] = this.maxDegrees;
        } else {
            float degPerVolt = (maxDegrees - minDegrees) / (maxVoltage - minVoltage);
            hands[0] = this.minDegrees + (voltage - this.minVoltage) * degPerVolt;
        }
        setValues(hands);
        firePropertyChange( VOLTAGE_PROP, oldVal, voltage );
    }
    
    
    /**
     * Sets the voltage at which the redline begins.
     */
    public void setRedline( float redline ){
        float oldVal = this.redline;
        this.redline = redline;
        update();
        firePropertyChange(REDLINE_PROP,oldVal,redline);
    }
    
    /**
     * Sets the voltage at which the redline begins.
     */
    public float getRedline(){
        return this.redline;
    }
    
    
    
    /**
     * Sets the voltage at which the yellowline begins.
     */
    public void setYellowline( float yellowline ){
        float oldVal = this.yellowline;
        this.yellowline = yellowline;
        update();
        firePropertyChange(YELLOWLINE_PROP,oldVal,yellowline);
    }
    
    /**
     * Sets the voltage at which the yellowline begins.
     */
    public float getYellowline(){
        return this.yellowline;
    }
    
    /**
     * Sets the voltages (integers) to display on the gauge.
     * @param vtd
     */
    public void setVoltagesToDisplay( int[] vtd ){
        int[] oldVal = this.voltagesToDisplay;
        this.voltagesToDisplay = vtd;
        update();
        firePropertyChange( VOLTAGES_TO_DISPLAY_PROP, oldVal, vtd );
    }
    
    /**
     * Returns a list of voltages (integers) to display on the gauge.
     */
    public int[] getVoltagesToDisplay(){
        return this.voltagesToDisplay;
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
        float degPerVolt = (maxDegrees - minDegrees) / (maxVoltage - minVoltage);
        
        // Two major ticks: min and max
        this.majorTicks[0] = this.minDegrees;
        this.majorTicks[1] = this.maxDegrees;
        
        // Minor ticks: integer voltages between min and max
        int numMinTicks = this.maxVoltage - this.minVoltage - 1;
        if( numMinTicks < 0 ){
            this.minorTicks = null;
        } else {
            if( this.minorTicks.length != numMinTicks ){
                this.minorTicks = new float[ numMinTicks ];
            }   // end if: need to resize array
            for( int i = 0; i < numMinTicks; i++ ){
                this.minorTicks[i] = this.minDegrees + (i+1) * degPerVolt;
            }   // end for: each minor tick
        }
        super.setMajorTickDegrees(this.majorTicks);
        super.setMinorTickDegrees(this.minorTicks);
    }
    
    
    /**
     * Updates the min and max voltage numerals.
     */
    private void updateNumerals(){
        if( this.voltagesToDisplay == null ){
            if( numerals.length != 2 ){
                numerals = new int[2][2];
            }   // end if: resize
            numerals[0][0] = (int)minDegrees;
            numerals[0][1] = minVoltage;

            numerals[1][0] = (int)maxDegrees;
            numerals[1][1] = maxVoltage;
        } else {
            if( numerals.length != voltagesToDisplay.length ){
                numerals = new int[voltagesToDisplay.length][2];
            }   // end if: resize
            float degPerVolt = (maxDegrees - minDegrees) / (maxVoltage - minVoltage);
            for( int i = 0; i < voltagesToDisplay.length; i++ ){
                numerals[i][1] = voltagesToDisplay[i];
                numerals[i][0] = (int)(minDegrees + (voltagesToDisplay[i] - minVoltage) * degPerVolt);
            }   // end for: each voltage to display
            
        }   // end else: not null
        
    
        setMajorNumerals(numerals);
    }
    
    
    
    /**
     * Update the bands on the gauge.
     */
    private void updateBands(){
        float degPerVolt = (maxDegrees - minDegrees) / (maxVoltage - minVoltage);
        
        float redStopDeg = minDegrees + (redline - minVoltage) * degPerVolt;
        float yellowStopDeg = redStopDeg + (yellowline - redline) * degPerVolt;
        
        bands[RED_BAND_INDEX][0] = minDegrees;
        bands[RED_BAND_INDEX][1] = redStopDeg;
        
        bands[YELLOW_BAND_INDEX][0] = redStopDeg;
        bands[YELLOW_BAND_INDEX][1] = yellowStopDeg;
        
        bands[GREEN_BAND_INDEX][0] = yellowStopDeg;
        bands[GREEN_BAND_INDEX][1] = maxDegrees;
        
        setBands(bands);
    }
    
    
/* ********  E D I T O R  ******** */    
    
    
    
    /**
     * Show an editor to set various parameters.
     */
    private void showEditor(){
        Component editor = getEditor();
        int result = JOptionPane.showConfirmDialog(this, editor, "Settings", JOptionPane.OK_CANCEL_OPTION );
        if( result == JOptionPane.OK_OPTION ){
            setEditor(editor);
        }
    }
    
    public Component getEditor(){
        return new Editor(this);
    }
    
    public void setEditor( Component c ){
        if( c instanceof Editor ){
            Editor e = (Editor)c;
            this.setVoltagesToDisplay( e.getVoltagesToDisplay() );
        }
    }
    
    
    private class Editor extends JPanel{
        private JTextField voltsToDisplay;
        
        private Editor( VoltageGauge parent ){
            initComponents( parent );
        }
        
        private void initComponents( VoltageGauge parent ){
            setLayout( new GridLayout(2,1) );
            
            StringBuilder sb = new StringBuilder();
            int[] vtd = parent.getVoltagesToDisplay();
            if( vtd == null ){
                sb.append( parent.getMinVoltage() ).append(", ").append( parent.getMaxVoltage() );
            } else {
                for( int i = 0; i < vtd.length; i++ ){
                    sb.append( vtd[i] );
                    if( i < vtd.length-1 ){
                        sb.append(", ");
                    }   // end if: more to go
                }   // end for: each volt to display
            }   // end else: build list
            this.voltsToDisplay = new JTextField(sb.toString());
            add( new JLabel("Voltages to display:") );
            add( this.voltsToDisplay );
        }
        
        public int[] getVoltagesToDisplay(){
            String v = this.voltsToDisplay.getText();
            String[] vs = v.split("[, ]");
            List<Integer> ii = new LinkedList<Integer>();
            for( String s : vs ){
                try{
                    ii.add( Integer.parseInt(s) );
                } catch( Exception exc ){
                    LOGGER.warning( "Could not convert to integer: " + s );
                }
            }
            int[] vtd = new int[ ii.size() ];
            for( int i = 0; i < vtd.length; i++ ){
                vtd[i] = ii.get(i);
            }
            return vtd;
        }
        
    }   // end class Editor
            
            
    
    
}
