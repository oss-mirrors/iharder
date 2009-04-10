/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rvision;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.prefs.Preferences;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author robert.harder
 */
public class UdpCameraSource implements CameraSource {
    
    public final static String PORT_PROP = "port";
    public final static String HOST_PROP = "host";
    
    private Preferences prefs = Prefs.get(this);
    private UdpCameraClient camera;
    private String host = prefs.get(HOST_PROP,"localhost");
    private int port = prefs.getInt(PORT_PROP,8000);
    
    public Camera getCamera() {
        return this.camera;
    }



    @Override
    public String toString(){ return getName(); }

    public String getName() {
        return "UDP Camera";
    }

    public Component getEditor() {
        return new Editor( host, port );
    }

    public void setEditor(Component c) {
        if( c instanceof Editor ){
            Editor e = (Editor)c;
            this.host = e.getHost();
            this.port = e.getPort();
            this.prefs.put(HOST_PROP, host);
            this.prefs.putInt(PORT_PROP,port);
            if( this.camera != null ){
                this.camera.setHostname(this.host);
                this.camera.setPort(this.port);
            }
        }
    }

    public void open() throws Exception {
        this.camera = new UdpCameraClient( host, port );
    }

    public void close() {
        this.camera.close();
        this.camera = null;
    }
    
    private static class Editor extends JPanel {
        private JTextField hostField;
        private JFormattedTextField portField;
        private Editor( String host, int port ){
            super( new GridLayout(2,2) );
            this.hostField = new JTextField(host);
            this.portField = new JFormattedTextField( port );
            this.add( new JLabel("Host:") );
            this.add( hostField );
            this.add( new JLabel("Port:") );
            this.add( portField );
        }   // end constructor
        public String getHost(){
            return this.hostField.getText();
        }   // end getHost
        public int getPort(){
            return (Integer)this.portField.getValue();
        }   // end getPort
    }

}
