/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.iharder.gauge;

/**
 *
 * @author robert.harder
 */
public class DecorativeExampleGauge extends DefaultGauge {

    private float[] value = new float[1];
    
    /**
     * Returns a decorative gauge with all the trimmings:
     * Major ticks, Minor ticks, Major numerals, Minor numerals,
     * Colored bands.
     * @return
     */
    public DecorativeExampleGauge(){
        setHands( new float[]{225} );
        setMajorTickDegrees(new float[] {0, 90, 270} );
        setMinorTickDegrees(new float[]{ 45, 135, 225, 315 });
        setMajorNumerals(  new int[][]{
          { 225, 0 },
          { 270, 1 },
          { 315, 2 },
          {   0, 3 },
          {  45, 4 },
          {  90, 5 },
          { 135, 6 }
        });
        setMinorNumerals(new int[][]{
          { 247, 500 },
          { 292, 1500 },
          { 337, 2500 },
          {  22, 3500 },
          {  67, 4500 },
          {  112, 5500 }
        });
        setBands( new Object[][]{ 
          {225, 90, java.awt.Color.green },
          {90, 112, java.awt.Color.yellow },
          {112, 135, java.awt.Color.red }
        });
    }
    
    public void setValue( int val ){
        value[0] = val;
        setHands(value);
    }
    
    
    
}
