/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rvision;

import java.awt.Component;
import java.awt.GridLayout;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author robert.harder
 */
public class HttpCameraSource implements CameraSource {
    
    public final static String ENDPOINT_PROP = "endpoint";
    
    private Preferences prefs = Prefs.get(this);
    private HttpCameraClient camera;
    private String endpoint = prefs.get(ENDPOINT_PROP,"http://localhost");
    
    
    public Camera getCamera() {
        return this.camera;
    }

    @Override
    public String toString(){ return getName(); }

    public String getName() {
        return "HTTP Camera";
    }

    public Component getEditor() {
        return new Editor( endpoint );
    }

    public void setEditor(Component c) {
        if( c instanceof Editor ){
            Editor e = (Editor)c;
            this.endpoint = e.getEndpoint();
            this.prefs.put(ENDPOINT_PROP,this.endpoint);
            if( this.camera != null ){
                try {
                    this.camera.setEndpoint(new URL(this.endpoint));
                } catch (MalformedURLException ex) {
                    Logger.getLogger(HttpCameraSource.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(null,ex.getMessage());
                }
            }
        }
    }

    public void open() throws Exception {
        this.camera = new HttpCameraClient(this.endpoint);
    }

    public void close() {
        this.camera.close();
        this.camera = null;
    }
    
    private static class Editor extends JPanel {
        private JTextField endpointField;
        private Editor( String endpoint  ){
            super( new GridLayout(1,2) );
            this.endpointField = new JTextField(endpoint);
            this.add( new JLabel("Endpoint:") );
            this.add( endpointField  );
        }   // end constructor
        public String getEndpoint(){
            return this.endpointField.getText();
        }   // end 
    }

}
