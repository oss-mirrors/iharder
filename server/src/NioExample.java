
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.*;
import java.util.List;
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
 * @see TcpServer.Adapter
 * @see TcpServer.Event
 */
public class NioExample extends javax.swing.JFrame implements NioServer.Listener {

    private int tcpCtr = 1;
    private int udpCtr = 1;
    private final static long serialVersionUID = 1;
    private Map<SelectionKey,TcpWorker> tcpWorkers = new HashMap<SelectionKey,TcpWorker>();
    private JTextArea bindingsArea;
    
    /** Creates new form TcpExample */
    public NioExample() {
        initComponents();
        myInitComponents();
    }
    
    private void myInitComponents(){


        JPanel panel = new JPanel( new BorderLayout() );        // Add special place to write the text
        JScrollPane pane = new JScrollPane();
        final JTextArea area = new JTextArea();
        pane.setViewportView(area);
        panel.add( pane, BorderLayout.CENTER );
        this.tabbedPane.add("Bindings", panel);
        this.bindingsArea = area;

        
        NioServer.setLoggingLevel(Level.ALL);
        
        this.nioServer.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {

                final NioServer ns = (NioServer)evt.getSource();
                final String prop = evt.getPropertyName();
                final Object oldVal = evt.getOldValue();
                final Object newVal = evt.getNewValue();
                System.out.println("Property: " + prop + ", Old: " + oldVal + ", New: " + newVal );

                if( NioServer.STATE_PROP.equals( evt.getPropertyName() ) ){
                    final NioServer.State state = (NioServer.State)evt.getNewValue();
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
                }   // end if: state

                else if( NioServer.UDP_BINDINGS_PROP.equals( prop ) || NioServer.TCP_BINDINGS_PROP.equals( prop ) ){
                    bindingsArea.setText( "Current Bindings. Click button above to make changes.\n" + getBindingsAsString() );
                }   // end if: udp binding
            }   // end prop change
        });
        this.nioServer.fireProperties();
        
        this.nioServer.addNioServerListener(this);

        nioServer.addTcpBinding(new InetSocketAddress(1234));
        nioServer.addUdpBinding(new InetSocketAddress(1234), "239.0.0.1");
    }


    private String getBindingsAsString(){

        StringBuilder sb = new StringBuilder("TCP:\n");
        for( SocketAddress addr : this.nioServer.getTcpBindings() ){
            sb.append(" " + ((InetSocketAddress)addr).getPort() + "\n");
        }
        sb.append("UDP:\n");
        for( Map.Entry<SocketAddress,String> e : this.nioServer.getUdpBindings().entrySet() ){
            sb.append(" " + ((InetSocketAddress)e.getKey()).getPort()  );
            if( e.getValue() != null ){
                sb.append( " multicast " + e.getValue() + "" );
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Example input text
     * TCP:
     * 0.0.0.0/0.0.0.0:1234
     * UDP:
     * 0.0.0.0/0.0.0.0:1234 multicast 239.0.0.1
     *
     * @param s
     */
    private void setBindingsFromString( String s ){
        try {
            Set<SocketAddress> tcpBindings = new HashSet<SocketAddress>();
            Map<SocketAddress,String> udpBindings = new HashMap<SocketAddress,String>();
            
            BufferedReader in = new BufferedReader(new StringReader(s));
            String line = null;
            boolean tcpMode = false;
            boolean udpMode = false;
            while ((line = in.readLine()) != null) {
                
                if( line.startsWith("TCP") ){
                    tcpMode = true;
                    udpMode = false;

                } else if( line.startsWith("UDP") ){
                    tcpMode = false;
                    udpMode = true;

                } else if( tcpMode ){
                    SocketAddress addr = null;
                    try{
                        addr = new InetSocketAddress( Integer.parseInt(line.trim()));
                    } catch( Exception exc ){
                        System.err.println("Cannot make InetSocketAddress from " + line );
                    }
                    if( addr != null ){
                        tcpBindings.add( addr );
                    }
                } else if( udpMode ){
                    SocketAddress addr = null;
                    String group = null;
                    try{
                        String[] portGroup = line.split(" multicast ");
                        if( portGroup.length == 2 ){
                            addr = new InetSocketAddress( Integer.parseInt(portGroup[0].trim()));
                            group = portGroup[1].trim();
                        } else {
                            addr = new InetSocketAddress( Integer.parseInt(portGroup[0].trim()));
                        }
                    } catch( Exception exc ){
                        System.err.println("Cannot make InetSocketAddress from " + line );
                    }
                    if( addr != null ){
                        udpBindings.put( addr, group );
                    }
                }
            }   // end while: each line

            this.nioServer.setTcpBindings( tcpBindings );
            this.nioServer.setUdpBindings( udpBindings );

        } catch (IOException ex) {
            Logger.getLogger(NioExample.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tcpServer = new TcpServer();
        nioServer = new NioServer();
        jPanel1 = new javax.swing.JPanel();
        tabbedPane = new javax.swing.JTabbedPane();
        jLabel3 = new javax.swing.JLabel();
        stateLabel = new javax.swing.JLabel();
        startStopButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        tcpIndicator = new IndicatorLabel();
        jButton2 = new javax.swing.JButton();
        udpIndicator = new IndicatorLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Nio Server Example");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Incoming"));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 423, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE)
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

        jButton2.setText("Edit Bindings...");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

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
                        .addComponent(jButton2)
                        .addGap(45, 45, 45)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(stateLabel))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(startStopButton)
                                .addGap(18, 18, 18)
                                .addComponent(jButton1)
                                .addGap(18, 18, 18)
                                .addComponent(tcpIndicator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(udpIndicator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(stateLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton1)
                            .addComponent(startStopButton)
                            .addComponent(jButton2)))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(tcpIndicator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(udpIndicator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


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

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

        JTextArea area = new JTextArea( getBindingsAsString() );
        area.setColumns(30);
        area.setRows(8);
        int result = JOptionPane.showConfirmDialog(this, new JScrollPane(area), "Edit Bindings", JOptionPane.OK_CANCEL_OPTION);
        if( result == JOptionPane.OK_OPTION ){
            setBindingsFromString( area.getText() );
            System.out.println(area.getText());
        }   // end if: OK
        
    }//GEN-LAST:event_jButton2ActionPerformed
    
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
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private NioServer nioServer;
    private javax.swing.JButton startStopButton;
    private javax.swing.JLabel stateLabel;
    private javax.swing.JTabbedPane tabbedPane;
    private IndicatorLabel tcpIndicator;
    private TcpServer tcpServer;
    private IndicatorLabel udpIndicator;
    // End of variables declaration//GEN-END:variables

    

    public void nioServerNewConnectionReceived(NioServer.Event evt) {
        this.tcpIndicator.indicate();                     // New incoming connection: flash indicator at user

        JPanel panel = new JPanel( new BorderLayout() );        // Add special place to write the text
        JScrollPane pane = new JScrollPane();
        final JTextArea area = new JTextArea();
        pane.setViewportView(area);
        panel.add( pane, BorderLayout.CENTER );
        this.tabbedPane.add("TCP "+(tcpCtr++), panel);
        TcpWorker tw = new TcpWorker(area);
        this.tcpWorkers.put(evt.getKey(), tw);

    }


    public void nioServerDataReceived(NioServer.Event evt) {
        if( evt.isTcp() ){
            this.tcpIndicator.indicate();                     // New incoming connection: flash indicator at user

            String s = Charset.forName("US-ASCII").decode(evt.getBuffer()).toString();
            this.tcpWorkers.get(evt.getKey()).textReceived(s);

        } else if( evt.isUdp() ){
            this.udpIndicator.indicate();                     // New incoming connection: flash indicator at user

            String s = Charset.forName("US-ASCII").decode(evt.getBuffer()).toString();
            JPanel panel = new JPanel( new BorderLayout() );        // Add special place to write the text
            JScrollPane pane = new JScrollPane();
            final JTextArea area = new JTextArea(s);
            pane.setViewportView(area);
            panel.add( pane, BorderLayout.CENTER );
            this.tabbedPane.add("UDP "+(udpCtr++), panel);

        }
    }

    public void nioServerConnectionClosed(NioServer.Event evt) {
        if( evt.isTcp() ){
            this.tcpWorkers.get(evt.getKey()).execute();
        }
    }

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
