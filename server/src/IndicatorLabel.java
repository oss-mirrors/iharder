

import java.awt.event.*;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * A handy subclass of JLabel that goes from disabled to enabled
 * when the {@link #indicate} method is called. Thread safe.
 * Useful for indicating things like "received packet."
 * @author rharder@users.sourceforge.net
 */
public class IndicatorLabel extends javax.swing.JLabel{

    public final static String  DELAY_PROP = "delay";
    private final static int    DELAY_DEFAULT = 200;   // milliseconds
    
    private javax.swing.Timer   timer;
    private int                 delay;
    
    public IndicatorLabel(){
        super();
        initComponents();
    }
    
    public IndicatorLabel(String label){
        super(label);
        initComponents();
    }
    
    private void initComponents(){
        
        setFont(getFont().deriveFont(getFont().getStyle() | java.awt.Font.BOLD, getFont().getSize()-1));
        setForeground(new java.awt.Color(0, 102, 0));
        setEnabled(false);
        
        this.delay = DELAY_DEFAULT;
        
        this.timer = new javax.swing.Timer(0,null);
        this.timer.setInitialDelay(delay); 
        this.timer.setRepeats(false);
        this.timer.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                setEnabled(false); // Clear indicator
            }   // end actionPerformed
        }); // end action listener
        
        this.addMouseListener( new MouseAdapter(){
            public void mouseClicked(MouseEvent e) {
                String result = JOptionPane.showInputDialog("Set delay (ms) for indicator " + getText(),delay);
                try{ setDelay( Integer.parseInt(result) ); }
                catch( Exception exc ){
                    System.err.println("Could not set delay to " + result + ": " + exc.getMessage() );
                }   // end catch
            }   // end mouseClicked
        });
    }
    
    public synchronized void indicate(){
        if( SwingUtilities.isEventDispatchThread() ){
            setEnabled(true);
            this.timer.restart();
        } else {
            SwingUtilities.invokeLater( new Runnable(){
                public void run(){
                    setEnabled(true);
                    timer.restart();
                }   // end run
            }); // end invoke later
        }   // end else: not event thread
    }   // end indicate
    
    
    public void setDelay( int delay ){
        if( delay < 0 ){
            throw new IllegalArgumentException("Delay must not be negative: " + delay );
        }
        int old = this.delay;
        this.delay = delay;
        firePropertyChange(DELAY_PROP,old,delay);
    }
    
    public int getDelay(){
        return this.delay;
    }
    
}
