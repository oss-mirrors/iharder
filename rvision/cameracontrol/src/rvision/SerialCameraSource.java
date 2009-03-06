
package rvision;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

/**
 * I'm not sure why I made this. Maybe it's deprecated.
 * 
 * @author robert.harder
 */
public class SerialCameraSource implements CameraSource{

    public final static String SERIAL_PORT_PROP = "serial_port";
    public final static String OPEN_PROP = "open";
    
    private Preferences prefs = Prefs.get(this);
    private Camera camera;
    private String serialPort = prefs.get(SERIAL_PORT_PROP,"");
    private boolean open = false;
    
    public SerialCameraSource() {
    }

    public Camera getCamera() {
        return camera;
    }
    
    public String getSerialPort(){
        return this.serialPort;
    }
    
    public void setSerialPort( String port ) throws Exception{
        if( port == null)
            throw new NullPointerException( "Serial port must not be null." );
        
        boolean wasOpen = this.open;
        close();
        this.serialPort = port;
        if( wasOpen ){
            open();
        }
    }

    public String getName() {
        return "Serial";
    }

    public Component getEditor() {
        return new Editor(this);
    }

    public void setEditor(Component c) {
        if( c instanceof Editor ){
            String port = ((Editor)c).getSerialPort();
            try{
                setSerialPort( port );
            } catch( Exception exc ){
                JOptionPane.showMessageDialog(null,"Could not set serial port to " + port + ": " + exc.getMessage() );
            }
        }
    }

    public void open() throws Exception{
        if( this.camera != null ){
            close();
        }   // end if: need closure first
        
        this.camera = new Camera( serialPort );
        this.open = true;
    }

    public void close() {
        if( this.camera != null ){
            this.camera.close();
            this.camera = null;
        }
        this.open = false;
    }
    
    private class Editor extends JPanel{
        
        JComboBox ports;
        
        private Editor( SerialCameraSource parent ){
            super( new BorderLayout() );
            initComponents(parent);
        }
        
        private void initComponents( SerialCameraSource parent ){
            ports = new JComboBox();
            ports.setEnabled(false);
            add( new JLabel("Serial port:"), BorderLayout.WEST);
            add( ports,BorderLayout.CENTER);
            
            final String previousPort = parent.getSerialPort();
            SwingWorker<String[],Object> worker = new SwingWorker<String[],Object>(){
                @Override
                protected String[] doInBackground() throws Exception {
                    return SerialStream.getPortNames();
                }   // end doInBackground
                
                @Override
                protected void done(){
                    String[] names = new String[0];
                    try{ names = get(); }
                    catch( Exception exc ){
                        Logger.getLogger(this.getClass().getName()).log( Level.WARNING, "Could not get serial port names.",exc );
                    }
                    for( String s : names ){
                        ports.addItem(s);
                    }
                    ports.setSelectedItem(previousPort);
                    ports.setEnabled(true);
                }
            };
            worker.execute();
        }   // end initComponents
        
        private String getSerialPort(){
            Object sel = ports.getSelectedItem();
            return sel == null ? null : sel.toString();
        }
    }
    
    

}
