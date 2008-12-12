package mil2525b;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.ImageIcon;
import javax.swing.JFormattedTextField;
import javax.swing.SwingUtilities;

/**
 * Serves up the mil2525b images via a built-in web server.
 * This is meant to serve as an example of using the
 * {@link HttpServer} embeddable class, but it may work
 * well for you standalone as well.
 * 
 * @see HttpServer
 * @author  robert.harder
 */
public class HttpServerGui extends javax.swing.JFrame {
    private static HttpServerGui httpServerGui;
    
    private Preferences prefs = Prefs.get(this);
    private boolean preview = prefs.getBoolean("preview",true);
    private TrayIcon trayIcon;
    
    
    /** Creates new form HttpServerGui */
    public HttpServerGui() {
        initComponents();
        
        this.previewCheckbox.setSelected(preview);
        this.previewLabel.setEnabled(preview);
        
        this.httpServer.setPort( prefs.getInt("port",8000) );
        
        this.httpServer.fireProperties();
        this.httpServer.addTcpServerListener(new TcpServer.Listener() {

            public void tcpServerStateChanged(TcpServer.Event evt) {
                final TcpServer.State state = evt.getState();
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        switch( state ){
                            case STARTING:
                                statusLabel.setText("Starting");
                                break;
                                
                            case STARTED:
                                startStopBtn.setText("Stop");
                                startStopBtn.setEnabled(true);
                                statusLabel.setText("Listening");
                                prefs.putBoolean("running", true);
                                break;

                            case STOPPING:
                                statusLabel.setText("Stopping");
                                break;
                                
                            case STOPPED:
                                startStopBtn.setText("Start");
                                startStopBtn.setEnabled(true);
                                statusLabel.setText("Not listening");
                                prefs.putBoolean("running", false);
                                break;
                                
                        }   // end switch
                    }   //end run
                }); // end invoke later
            }

            public void tcpServerSocketReceived(TcpServer.Event evt) {
                rxLabel.indicate();
            }
        });
        this.httpServer.fireState();
        
        this.httpServer.addHttpServerListener(new HttpServer.HttpListener() {
            public void httpServerImageSent(mil2525b.HttpServer.HttpEvent evt) {
                if( preview ){
                    final String mil2525b = evt.getMil2525b();
                    final byte[] data = evt.getData();
                    SwingUtilities.invokeLater( new Runnable() {
                        public void run() {
                            previewLabel.setText(mil2525b);
                            ImageIcon icon = new ImageIcon(data);
                            previewLabel.setIcon(icon);
                            if( SystemTray.isSupported() ){
                                SystemTray tray = SystemTray.getSystemTray();
                                Dimension d = tray.getTrayIconSize();
                                Image img = icon.getImage().getScaledInstance(d.width, d.height, Image.SCALE_SMOOTH);
                                trayIcon.setImage(img);
                            }
                        }
                    });
                }   // end if: preview
            }   // end image sent
        }); // end listener
        
        if( prefs.getBoolean("running",true) ){
            this.httpServer.start();
        }
        
        
        if( SystemTray.isSupported() ){
            try {
                SystemTray tray = SystemTray.getSystemTray();
                PopupMenu popup = new PopupMenu();
                MenuItem show = new MenuItem("Mil2525b Web Server Settings");
                show.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        httpServerGui.setVisible(true);
                        httpServerGui.setState(Frame.NORMAL);
                    }   // end actionPerformed
                });
                popup.add(show);
                this.trayIcon = new TrayIcon(new javax.swing.ImageIcon(
                  Mil2525b.getUrlFromCotType("a-f-A-M-F-Q"))
                  .getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH),"Mil2525b Web Server",popup);
                tray.add(this.trayIcon);
            } catch (AWTException ex) {
                Logger.getLogger(HttpServerGui.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        
    }
    
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        httpServer = new mil2525b.HttpServer();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        previewLabel = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        statusLabel = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        portField = new javax.swing.JFormattedTextField(0);
        startStopBtn = new javax.swing.JButton();
        rxLabel = new mil2525b.IndicatorLabel();
        previewCheckbox = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();

        httpServer.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                httpServerPropertyChange(evt);
            }
        });

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Mil2525b Imagery Server");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowIconified(java.awt.event.WindowEvent evt) {
                formWindowIconified(evt);
            }
        });

        jLabel1.setText("Web server for Mil2525b imagery.");

        jLabel2.setFont(jLabel2.getFont().deriveFont(jLabel2.getFont().getStyle() & ~java.awt.Font.BOLD));
        jLabel2.setText("<html>Mil2525b example: <a href=\"http://localhost:8000/mil2525b/g-fpaa--------x\">http://localhost:8000/mil2525b/g-fpaa--------x</a></html>");

        jLabel3.setFont(jLabel3.getFont().deriveFont(jLabel3.getFont().getStyle() & ~java.awt.Font.BOLD));
        jLabel3.setText("<html>Cursor on Target example: <a href=\"http://localhost:8000/cot/a-f-A-M-F-Q\">http://localhost:8000/cot/a-f-A-M-F-Q</a></html>");

        previewLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        previewLabel.setIcon(new javax.swing.ImageIcon(Mil2525b.getUrlFromCotType("a-f-A-M-F-Q")));
        previewLabel.setText("Drone");
        previewLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        previewLabel.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        jLabel5.setText("Status:");

        statusLabel.setFont(statusLabel.getFont().deriveFont(statusLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        statusLabel.setText("Stopped");

        jLabel6.setText("Port:");

        portField.setColumns(8);
        portField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                portFieldFocusLost(evt);
            }
        });

        startStopBtn.setText("Start");
        startStopBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startStopBtnActionPerformed(evt);
            }
        });

        rxLabel.setText("RX");

        previewCheckbox.setText("Preview");
        previewCheckbox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                previewCheckboxItemStateChanged(evt);
            }
        });

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(statusLabel))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(startStopBtn, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(portField, javax.swing.GroupLayout.Alignment.LEADING))))
                        .addGap(18, 18, 18)
                        .addComponent(rxLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(previewLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(51, 51, 51)
                                .addComponent(previewCheckbox)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(previewCheckbox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(previewLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(statusLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(portField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(startStopBtn)
                            .addComponent(rxLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void httpServerPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_httpServerPropertyChange
        if( HttpServer.PORT_PROP.equals(evt.getPropertyName()) ){
            final int port = (Integer)evt.getNewValue();
            prefs.putInt("port",port);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    portField.setValue(port);
                }   // end run
            }); // end invoke later
        }
    }//GEN-LAST:event_httpServerPropertyChange

    private void portFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_portFieldFocusLost
        try {
            JFormattedTextField field = (JFormattedTextField) evt.getSource();
            field.commitEdit();//GEN-LAST:event_portFieldFocusLost
            this.httpServer.setPort( (Integer)field.getValue());
        } catch (Exception ex) {
            Logger.getLogger(HttpServerGui.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void startStopBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startStopBtnActionPerformed
        
        switch( this.httpServer.getState() ){
            case STARTED:
                startStopBtn.setEnabled(false);
                httpServer.stop();
                break;
                
            case STOPPED:
                startStopBtn.setEnabled(false);
                httpServer.start();
                break;
        }   // end switch
    }//GEN-LAST:event_startStopBtnActionPerformed

    private void previewCheckboxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_previewCheckboxItemStateChanged
        switch( evt.getStateChange() ){
            case ItemEvent.SELECTED:   
                this.preview = true; 
                this.previewLabel.setEnabled(this.preview);
                this.prefs.putBoolean("preview",this.preview);
                break;
            case ItemEvent.DESELECTED: 
                this.preview = false; 
                this.previewLabel.setEnabled(this.preview);
                this.prefs.putBoolean("preview",this.preview);
                break;
        }   // end switch
    }//GEN-LAST:event_previewCheckboxItemStateChanged

    private void formWindowIconified(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowIconified
        if( SystemTray.isSupported() ){
            httpServerGui.setVisible(false);
        }
        
    }//GEN-LAST:event_formWindowIconified
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                httpServerGui = new HttpServerGui();
                httpServerGui.setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private mil2525b.HttpServer httpServer;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JFormattedTextField portField;
    private javax.swing.JCheckBox previewCheckbox;
    private javax.swing.JLabel previewLabel;
    private mil2525b.IndicatorLabel rxLabel;
    private javax.swing.JButton startStopBtn;
    private javax.swing.JLabel statusLabel;
    // End of variables declaration//GEN-END:variables
    
}
