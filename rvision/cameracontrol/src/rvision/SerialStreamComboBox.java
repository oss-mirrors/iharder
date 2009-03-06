package rvision;

import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.SwingWorker;

/**
 * Handy class for listing serial ports.
 * @author robert.harder
 */
public class SerialStreamComboBox extends JComboBox {
    
    private final static Logger LOGGER = Logger.getLogger( SerialStreamComboBox.class.getName() );
    
    private String serialPort;
    private DefaultComboBoxModel model;
    
    public SerialStreamComboBox(){
        super();
        this.model = new DefaultComboBoxModel();
        setModel( this.model );
        initComponents();
    }

    private void initComponents() {
        populateSerialPortCombo();
    }
    
    
    /**
     * Returns the selected serial port name.
     * @return
     */
    public String getSerialPort(){
        DefaultComboBoxModel model = this.model;
        return model == null
          ? null
          : model.getSize() == 0
            ? null
            : model.getSelectedItem().toString();
    }
    
    
    /**
     * Sets the selected serial port, if that port is known.
     * If the port is unknown, the instruction is ignored.
     * @param port
     * @return
     */
    public void setSerialPort( String port ){
        if( this.model.getIndexOf(port) >= 0 ){
            this.model.setSelectedItem(port);
        } else {
            LOGGER.warning("Cannot set serial port to unknown port " + port );
        }
    }
    
    
    
    
    
    
    /**
     * Populates the serial port combo box and tries hard to
     * accommodate the user if this is called while the box is
     * being selected. In fact the plan is to call this every time
     * the box first gets clicked to ensure the list is always
     * up to date.
     */
    private void populateSerialPortCombo(){
        SwingWorker<String[],Object> sw = new SwingWorker<String[],Object>(){
            @Override
            protected String[] doInBackground() throws Exception {
                return SerialStream.getPortNames();
            }   // end doInBackground
            
            @Override
            protected void done(){
            String prevPort = serialPort;   // Previous value
                String[] names = null;
                try{ names = get(); }
                catch( Exception exc ){
                    LOGGER.warning("Error retrieving serial port names: " + exc.getMessage() );
                }   // end catch
                if( names != null ){
                    //Object sel = baudCombo.getSelectedItem();
                    
                    // Remove ports from list that are not available
                    for( int i = 0; i < model.getSize(); i++ ){
                        Object item = model.getElementAt(i);
                        boolean valid = false;
                        for( String name : names ){
                            if( name.equals( item ) ){
                                valid = true;
                                break;  // Out of loop: each real port
                            }   // end if: found match
                        }   // end for: each real port
                        if( !valid ){
                            model.removeElementAt(i);
                            i--; // Since we've removed one.
                        }   // end if: port no longer valid
                    }   // end for: each item in combo
                    
                    // Make sure all ports are represented
                    for( String name : names ){
                        boolean present = false;
                        for( int i = 0; i < model.getSize(); i++ ){
                            if( name.equals( model.getElementAt(i) ) ){
                                present = true;
                                break;  // Out of loop: each item in combo
                            }   // end if: found match
                        }   // end for: each item in combo
                        if( !present ){
                            model.addElement(name);
                        }   // end if: add port
                    }   // end for: each real port
                    
                    // Select previously-selected port
                    if( prevPort == null && names.length > 0 ){ // Nothing previously selected
                        model.setSelectedItem(names[0]);        // First available serial port
                    } else {                                    // Select previous in the list
                        for( int i = 0; i < model.getSize(); i++ ){  // Try to set what was already selected
                            if( prevPort.equals( model.getElementAt(i) ) ){
                                model.setSelectedItem(model.getElementAt(i));
                                break;  // Out of loop: each item in combo
                            }   // end if: found match
                        }   // end for: each item in combo
                        if( model.getSize() > 0 ){
                            //setSerialPort( serialComboModel.getSelectedItem().toString() );
                        }
                    }   // end else: 
                }   // end if: names != null
            }   // end done
        };  // end swingworker
        sw.execute();
    }
    
    
    

}
