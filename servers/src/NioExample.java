



import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
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
public class NioExample extends javax.swing.JFrame implements NioServer.Listener {
private final static long serialVersionUID = 1;

    private int tcpCtr = 1;
    private int udpCtr = 1;
    private Map<SelectionKey,TcpWorker> tcpWorkers = new HashMap<SelectionKey,TcpWorker>();
    private Charset charset = Charset.forName("US-ASCII");
    private CharsetDecoder decoder = charset.newDecoder();

    /** Creates new form TcpExample */
    public NioExample() {
        initComponents();
        myInitComponents();
    }
    
    private void myInitComponents(){
        
        NioServer.setLoggingLevel(Level.ALL);
        
        //this.tcpServer.setExecutor( Executors.newCachedThreadPool() );
        
        this.nioServer.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {

                final String prop = evt.getPropertyName();
                final Object oldVal = evt.getOldValue();
                final Object newVal = evt.getNewValue();
                System.out.println("Property: " + prop + ", Old: " + oldVal + ", New: " + newVal );

                if( NioServer.STATE_PROP.equals( prop ) ){
                    final NioServer.State state = (NioServer.State)newVal;
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
                
                if( NioServer.SINGLE_TCP_PORT_PROP.equals( prop ) ||
                    NioServer.SINGLE_UDP_PORT_PROP.equals( prop )){
                    SwingUtilities.invokeLater( new Runnable() {
                        public void run() {
                            portField.setValue( newVal );
                        }   // end run
                    }); // end swing utilities
                }   // end if: port


                if( NioServer.LAST_EXCEPTION_PROP.equals( prop ) ){
                    Throwable t = (Throwable)newVal;
                    JOptionPane.showConfirmDialog(null, t.getMessage(), "Server Error", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE );
                }



            }   // end prop change
        });
        this.nioServer.addNioServerListener(this);
        this.nioServer.fireProperties();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nioServer = new NioServer();
        jLabel1 = new javax.swing.JLabel();
        portField = new javax.swing.JFormattedTextField(0);
        jPanel1 = new javax.swing.JPanel();
        tabbedPane = new javax.swing.JTabbedPane();
        jLabel3 = new javax.swing.JLabel();
        stateLabel = new javax.swing.JLabel();
        startStopButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        tcpIndicator = new IndicatorLabel();
        udpIndicator = new IndicatorLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Nio Server Example");

        jLabel1.setText("Port (TCP and UDP):");

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
                .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 510, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
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

        tcpIndicator.setText("TCP");

        udpIndicator.setText("UDP");

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
                        .addGap(18, 18, 18)
                        .addComponent(portField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(stateLabel))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(startStopButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1)
                                .addGap(18, 18, 18)
                                .addComponent(tcpIndicator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(10, 10, 10)
                                .addComponent(udpIndicator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(portField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel3)
                                .addComponent(stateLabel))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(startStopButton)
                                .addComponent(jButton1)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tcpIndicator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(udpIndicator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(6, 6, 6)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void portFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_portFieldActionPerformed
        try {
            JFormattedTextField field = (JFormattedTextField) evt.getSource();
            field.commitEdit();//GEN-LAST:event_portFieldActionPerformed
            int port = (Integer)field.getValue();
            this.nioServer.setSingleTcpPort(port).setSingleUdpPort(port);
        } catch (ParseException ex) {
            Logger.getLogger(TcpExample.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void startStopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startStopButtonActionPerformed
        NioServer.State state = this.nioServer.getState();
        switch( state ){
            case STOPPED:
                this.nioServer.start();
                break;
            case STARTED:
                this.nioServer.stop();
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
            int port = (Integer)field.getValue();
            this.nioServer.setSingleTcpPort(port).setSingleUdpPort(port);
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
                            nioServer.start();
                        } else {
                            nioServer.stop();
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
                new NioExample().setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private NioServer nioServer;
    private javax.swing.JFormattedTextField portField;
    private javax.swing.JButton startStopButton;
    private javax.swing.JLabel stateLabel;
    private javax.swing.JTabbedPane tabbedPane;
    private IndicatorLabel tcpIndicator;
    private IndicatorLabel udpIndicator;
    // End of variables declaration//GEN-END:variables





    public void newConnectionReceived(final NioServer.Event evt) {
        this.tcpIndicator.indicate();                     // New incoming connection: flash indicator at user

        final JPanel panel = new JPanel( new BorderLayout() );        // Add special place to write the text
        final JScrollPane pane = new JScrollPane();
        final JTextArea area = new JTextArea();
        final int count = tcpCtr++;
        TcpWorker tw = new TcpWorker(area);
        this.tcpWorkers.put(evt.getKey(), tw);
        pane.setViewportView(area);

        SwingUtilities.invokeLater( new Runnable(){
            public void run(){
                panel.add( pane, BorderLayout.CENTER );
                tabbedPane.add("TCP "+count, panel);
            }   // end run
        });
        
    }


    public void tcpDataReceived(NioServer.Event evt) {
        ByteBuffer inBuff = evt.getInputBuffer();
        ByteBuffer outBuff = evt.getOutputBuffer();

        String s = null;
        inBuff.mark();
        try {
            s = this.decoder.reset().decode(inBuff).toString();
        } catch (CharacterCodingException ex) {
            Logger.getLogger(NioExample.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Uncomment these lines to echo
        //inBuff.reset();
        //outBuff.clear();
        //outBuff.put(inBuff).flip();


        this.tcpIndicator.indicate();                     // New incoming connection: flash indicator at user
        this.tcpWorkers.get(evt.getKey()).textReceived(s);

    }

    

    public void udpDataReceived(NioServer.Event evt) {
        String s = null;
        try {
            s = this.decoder.reset().decode(evt.getInputBuffer()).toString();
        } catch (CharacterCodingException ex) {
            Logger.getLogger(NioExample.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.udpIndicator.indicate();                     // New incoming connection: flash indicator at user

        final JPanel panel = new JPanel( new BorderLayout() );        // Add special place to write the text
        final JScrollPane pane = new JScrollPane();
        final JTextArea area = new JTextArea(s);
        final int count = udpCtr++;
        pane.setViewportView(area);

        SwingUtilities.invokeLater( new Runnable(){
            public void run(){
                panel.add( pane, BorderLayout.CENTER );
                tabbedPane.add("UDP "+count, panel);
            }   // end run
        });

    }

    public void connectionClosed(NioServer.Event evt) {
        if( evt.isTcp() ){
            this.tcpWorkers.remove(evt.getKey()).execute();
        }
    }

    public void tcpReadyToWrite(NioServer.Event evt) {}

    // Process the long-lived connection on another thread
    // so that we can immediately return control to the server
    // and wait for another connection while this one persists.
    // Ordinary servers would not user a SwingWorker since they
    // would not be tying themselves to the Swing GUI.
    private class TcpWorker extends SwingWorker<Void,String> {
        //sw = new SwingWorker<Object,String>() {
        private JTextArea area;

        private TcpWorker(JTextArea area){
            this.area = area;
        }

        @Override
        protected Void doInBackground() throws Exception { return null; }

        /**
         * Even if I'm not running the SwingWorker, I can still
         * use it to queue up work that is published to process
         * when convenient on the event thread. Pretty handy, eh?
         * @param s
         */
        public void textReceived( String s ){
            publish(s);
        }

        @Override
        protected void process( List<String> chunks ){      // Queued up published byte arrays
            StringBuilder sb = new StringBuilder();         // Put together into one string
            for( String s: chunks ){
                sb.append(s);
            }   // end for: each string
            area.setText( area.getText() + sb );            // Add string to JTextArea
        }   // end process

        /**
         * Call sw.execute() when done to have this method executed.
         */
        @Override
        protected void done(){
            try{
                area.setText( area.getText() + "\nConnection Closed." );
            } catch( Exception exc ){
                exc.printStackTrace();
            }
        }   // end done

    }
    
}
