


import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;



/**
 * <p>This is a sample app to show some ways to interact with the {@link TcpServer}.</p>
 * 
 * <p>This code is released into the Public Domain.
 * Since this is Public Domain, you don't need to worry about
 * licensing, and you can simply copy this TcpServer.java file
 * to your own package and use it as you like. Enjoy.
 * Please consider leaving the following statement here in this code:</p>
 * 
 * <p><em>This <tt>TcpServer</tt> class was copied to this project from its source as 
 * found at <a href="http://iharder.net" target="_blank">iHarder.net</a>.</em></p>
 *
 * @author Robert Harder
 * @author rharder@users.sourceforge.net
 * @version 0.1
 * @see TcpServer
 * @see TcpServer.Listener
 * @see TcpServer.Event
 */
public class TcpExample extends javax.swing.JFrame implements TcpServer.Listener {
    
    private int sockCtr = 1;
    private final static long serialVersionUID = 1;
    
    /** Creates new form TcpExample */
    public TcpExample() {
        initComponents();
        myInitComponents();
    }
    
    private void myInitComponents(){
        
        //TcpServer.setLoggingLevel(Level.OFF);
        
        this.tcpServer.setExecutor( Executors.newCachedThreadPool() );
        
        this.tcpServer.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {

                final String prop = evt.getPropertyName();
                final Object oldVal = evt.getOldValue();
                final Object newVal = evt.getNewValue();
                System.out.println("Property: " + prop + ", Old: " + oldVal + ", New: " + newVal );

                if( TcpServer.STATE_PROP.equals( prop ) ){
                    final TcpServer.State state = (TcpServer.State)newVal;
                    SwingUtilities.invokeLater( new Runnable() {
                        public void run() {
                            switch( state ){
                                case STARTING:
                                    stateLabel.setText( "Starting" );
                                    startStopButton.setEnabled( false );
                                    break;
                                case STARTED:
                                    stateLabel.setText( "Started" );
                                    startStopButton.setText( "Stop" );
                                    startStopButton.setEnabled( true );
                                    break;
                                case STOPPING:
                                    stateLabel.setText( "Stopping" );
                                    startStopButton.setEnabled( false );
                                    break;
                                case STOPPED:
                                    stateLabel.setText( "Stopped" );
                                    startStopButton.setText( "Start" );
                                    startStopButton.setEnabled( true );
                                    break;
                                default:
                                    assert false : state;
                                    break;
                            }   // end switch
                        }   // end run
                    });
                }
                
                if( TcpServer.PORT_PROP.equals( evt.getPropertyName() ) ){
                    SwingUtilities.invokeLater( new Runnable() {
                        public void run() {
                            portField.setValue( newVal );
                        }   // end run
                    }); // end swing utilities
                }   // end if: port
            }   // end prop change
        });
        this.tcpServer.addTcpServerListener(this);
        this.tcpServer.fireProperties();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tcpServer = new TcpServer();
        jLabel1 = new javax.swing.JLabel();
        portField = new javax.swing.JFormattedTextField(0);
        jPanel1 = new javax.swing.JPanel();
        tabbedPane = new javax.swing.JTabbedPane();
        jLabel3 = new javax.swing.JLabel();
        stateLabel = new javax.swing.JLabel();
        startStopButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        newSocketIndicator = new IndicatorLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Tcp Server Example");

        jLabel1.setText("Port:");

        portField.setColumns(12);
        portField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                portFieldActionPerformed(evt);
            }
        });
        portField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                portFieldFocusLost(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Incoming"));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel3.setText("State:");

        stateLabel.setText("Unknown");

        startStopButton.setText("Start/Stop");
        startStopButton.setEnabled(false);
        startStopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startStopButtonActionPerformed(evt);
            }
        });

        jButton1.setText("Stress");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        newSocketIndicator.setText("NEW");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(58, 58, 58)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(portField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(stateLabel))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(120, 120, 120)
                                .addComponent(startStopButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1)
                                .addGap(18, 18, 18)
                                .addComponent(newSocketIndicator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(4, 4, 4)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(portField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)
                            .addComponent(stateLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(startStopButton)
                            .addComponent(jButton1)))
                    .addComponent(newSocketIndicator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void portFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_portFieldActionPerformed
        try {
            JFormattedTextField field = (JFormattedTextField) evt.getSource();
            field.commitEdit();//GEN-LAST:event_portFieldActionPerformed
            this.tcpServer.setPort((Integer)field.getValue());
        } catch (ParseException ex) {
            Logger.getLogger(TcpExample.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void startStopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startStopButtonActionPerformed
        TcpServer.State state = this.tcpServer.getState();
        switch( state ){
            case STOPPED:
                this.tcpServer.start();
                break;
            case STARTED:
                this.tcpServer.stop();
                break;
            default:
                System.err.println("Shouldn't see this. State: " + state );
                break;
        }
    }//GEN-LAST:event_startStopButtonActionPerformed

    private void portFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_portFieldFocusLost
        try {
            JFormattedTextField field = (JFormattedTextField) evt.getSource();
            field.commitEdit();
            this.tcpServer.setPort((Integer)field.getValue());
        } catch (ParseException ex) {
            Logger.getLogger(TcpExample.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_portFieldFocusLost

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        //this.incomingArea.setText( this.incomingArea.getText() + 
        //  "(Stressing the server by starting and stopping it randomly " +
        //  "with 100 threads, 100 times over ~10 seconds. Look for thrown " +
        //  "Exceptions in console.)\n" );
        for( int i = 0; i < 100; i++ ){
            Thread t = new Thread( new Runnable() {
                public void run() {
                    for( int i = 0; i < 100; i++ ){
                        double d = Math.random();
                        if( d < .5 ){
                            tcpServer.start();
                        } else {
                            tcpServer.stop();
                        }
                        try{
                            Thread.sleep( (int)(Math.random()*100) );
                        } catch( InterruptedException exc ){
                            exc.printStackTrace();
                        }   // end catch
                    }   // end for
                }   // end runnable
            }); // end thread
            t.start();
        }
    }//GEN-LAST:event_jButton1ActionPerformed
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TcpExample().setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private IndicatorLabel newSocketIndicator;
    private javax.swing.JFormattedTextField portField;
    private javax.swing.JButton startStopButton;
    private javax.swing.JLabel stateLabel;
    private javax.swing.JTabbedPane tabbedPane;
    private TcpServer tcpServer;
    // End of variables declaration//GEN-END:variables

    public void socketReceived(TcpServer.Event evt) {
        this.newSocketIndicator.indicate();                     // New incoming connection: flash indicator at user
        final Socket socket = evt.getSocket();                  // Get the TCP socket
        
        JPanel panel = new JPanel( new BorderLayout() );        // Add special place to write the text
        JScrollPane pane = new JScrollPane();
        final JTextArea area = new JTextArea();
        pane.setViewportView(area);
        panel.add( pane, BorderLayout.CENTER );
        this.tabbedPane.add(""+(sockCtr++), panel);
        
        // Process the long-lived connection on another thread
        // so that we can immediately return control to the server
        // and wait for another connection while this one persists.
        // Ordinary servers would not user a SwingWorker since they
        // would not be tying themselves to the Swing GUI.
        SwingWorker<Socket,String> sw = new SwingWorker<Socket,String>() {
            @Override
            protected Socket doInBackground() throws Exception {
                InputStream in = socket.getInputStream();       // Data in from remote user
                OutputStream out = socket.getOutputStream();    // Response to remote user
                
                byte[] buff = new byte[1024];                   // Arbitrary buffer size
                int num = -1;                                   // Number of bytes read
                while( (num = in.read(buff)) >= 0 ){            // Read bytes into buffer
                    publish( new String( buff, 0, num ) );      // Published bytes will be made available
                }   // end while                                // on the event thread for GUI display.
                
                return socket;                                  // The socket is closed.
            }
            
            @Override
            protected void process( List<String> chunks ){      // Queued up published byte arrays
                StringBuilder sb = new StringBuilder();         // Put together into one string
                for( String s: chunks ){
                    sb.append(s);
                }   // end for: each string
                area.setText( area.getText() + sb );            // Add string to JTextArea
            }   // end process
            
            @Override
            protected void done(){
                try{
                    Socket socket = get();                      // As provided by doInBackground()
                    try{ socket.close(); }                      // Try to close socket if needed
                    catch( Exception e1 ){}
                    area.setText( area.getText() + "\nSocket Closed." );
                } catch( Exception exc ){
                    exc.printStackTrace();
                }
            }   // end done
        };
        sw.execute();                                           // Don't forget to start the thread!
        
    }
    
}
