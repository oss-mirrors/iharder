package net.iharder.script;

import javax.script.*;
import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 *
 * @author  rob
 */
public class EnginePane extends javax.swing.JPanel {

    private ScriptEngineFactory factory;
    private ScriptEngine engine;

    /** Creates new form EnginePane */
    public EnginePane() {
        initComponents();
    }
    
    
    public void setFactory( ScriptEngineFactory factory ){
        this.factory = factory;
        String engineName = factory.getEngineName();
        String engineVer = factory.getEngineVersion();
        String langName = factory.getLanguageName();
        String langVer = factory.getLanguageVersion();
        List<String> names = factory.getNames();
        List<String> mimes = factory.getMimeTypes();
        List<String> exts = factory.getExtensions();

        this.setName( engineName ); // Name the panel (appears in tab also)
        this.engVerLabel.setText( String.format("%s version %s", engineName, engineVer ) );
        this.engVerLabel.setToolTipText("factory.getEngineName()\nfactory.getEngineVersion()");

        this.langVerLabel.setText( String.format("%s version %s", langName, langVer ) );
        this.engVerLabel.setToolTipText("factory.getLanguageName()\nfactory.getLanguageVersion()");

        {   // Names
            StringBuilder sb = new StringBuilder("<html>");
            for( String n : names ){
                sb.append( n + ", " );
            }   // end for: each name
            sb.delete( sb.length()-2, sb.length()-1 ).append("</html>");
            this.namesLabel.setText( sb.toString() );
            this.namesLabel.setToolTipText( "factory.getNames()" );
        }   // end block: Names

        {   // Mime types
            StringBuilder sb = new StringBuilder("<html>");
            for( String s : mimes ){
                sb.append( s + ", " );
            }   // end for: each name
            sb.delete( sb.length()-2, sb.length()-1 ).append("</html>");
            this.mimesLabel.setText( sb.toString() );
            this.mimesLabel.setToolTipText( "factory.getMimeTypes()" );
        }   // end block: Mime types

        {   // Extensions
            StringBuilder sb = new StringBuilder("<html>");
            for( String s : exts ){
                sb.append( s + ", " );
            }   // end for: each name
            sb.delete( sb.length()-2, sb.length()-1 ).append("</html>");
            this.extsLabel.setText( sb.toString() );
            this.extsLabel.setToolTipText( "factory.getExtensions()" );
        }   // end block: Mime types

        this.scriptInArea.setText( factory.getOutputStatement("hello, world") );
        
    }


    private void alert( String message ){
        JOptionPane.showMessageDialog(this,message);
    }


    /**
     * Returns the ScriptEngine, possibly reused, and plumbed
     * to intercept output to display in a text area.
     * @param reuse
     * @return
     */
    private synchronized ScriptEngine getEngine( boolean reuse ){

        // If not reusing the engine, clear it
        if( !reuse ){
            this.engine = null;
        }   // end if: not reusing

        // Create script engine if needed
        if( this.engine == null ){

            this.engine = this.factory.getScriptEngine();
            this.engine.getContext().setWriter( new PrintWriter( new Writer() {
                StringBuilder sb = new StringBuilder();
                @Override
                public void write(char[] cbuf, int off, int len) throws IOException {
                    sb.append(cbuf, off, len);
                }   // end write

                @Override
                public void flush() throws IOException {
                    final String s = sb.toString();
                    sb = new StringBuilder();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            scriptOutArea.setText( scriptOutArea.getText() + s );
                        }
                    });
                }   // end flush

                @Override
                public void close() throws IOException {
                    final String s = sb.toString();
                    sb = new StringBuilder();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            scriptOutArea.setText( scriptOutArea.getText() + s );
                        }
                    });
                }   // end close
            }));
        }   // end if: engine == null;

        return this.engine;
    }
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        evL = new javax.swing.JLabel();
        lvL = new javax.swing.JLabel();
        langVerLabel = new javax.swing.JLabel();
        engVerLabel = new javax.swing.JLabel();
        nL = new javax.swing.JLabel();
        namesLabel = new javax.swing.JLabel();
        mL = new javax.swing.JLabel();
        mimesLabel = new javax.swing.JLabel();
        eL = new javax.swing.JLabel();
        extsLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        scriptInPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        scriptInArea = new javax.swing.JTextArea();
        runButton = new javax.swing.JButton();
        reuseEngine = new javax.swing.JCheckBox();
        scriptOutPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        scriptOutArea = new javax.swing.JTextArea();

        jPanel1.setLayout(new java.awt.GridBagLayout());

        evL.setText("Engine, version:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1.add(evL, gridBagConstraints);

        lvL.setText("Language, version:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1.add(lvL, gridBagConstraints);

        langVerLabel.setFont(langVerLabel.getFont().deriveFont(langVerLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        langVerLabel.setText("TBD");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 0);
        jPanel1.add(langVerLabel, gridBagConstraints);

        engVerLabel.setFont(engVerLabel.getFont().deriveFont(engVerLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        engVerLabel.setText("TBD");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 0);
        jPanel1.add(engVerLabel, gridBagConstraints);

        nL.setText("Names:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1.add(nL, gridBagConstraints);

        namesLabel.setFont(namesLabel.getFont().deriveFont(namesLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        namesLabel.setText("TBD");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 0);
        jPanel1.add(namesLabel, gridBagConstraints);

        mL.setText("MIME types:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1.add(mL, gridBagConstraints);

        mimesLabel.setFont(mimesLabel.getFont().deriveFont(mimesLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        mimesLabel.setText("TBD");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 0);
        jPanel1.add(mimesLabel, gridBagConstraints);

        eL.setText("Extensions:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1.add(eL, gridBagConstraints);

        extsLabel.setFont(extsLabel.getFont().deriveFont(extsLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        extsLabel.setText("TBD");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 0);
        jPanel1.add(extsLabel, gridBagConstraints);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Sample Scripting"));

        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(0.9);

        scriptInArea.setFont(new java.awt.Font("Monospaced", 0, 14)); // NOI18N
        scriptInArea.setRows(2);
        jScrollPane1.setViewportView(scriptInArea);

        runButton.setText("Run");
        runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runButtonActionPerformed(evt);
            }
        });

        reuseEngine.setText("Maintain engine state");
        reuseEngine.setToolTipText("Maintains variables, function declarations, etc across multiple runs");

        javax.swing.GroupLayout scriptInPanelLayout = new javax.swing.GroupLayout(scriptInPanel);
        scriptInPanel.setLayout(scriptInPanelLayout);
        scriptInPanelLayout.setHorizontalGroup(
            scriptInPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(scriptInPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(scriptInPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, scriptInPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(reuseEngine)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 309, Short.MAX_VALUE)
                        .addComponent(runButton))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 523, Short.MAX_VALUE))
                .addContainerGap())
        );
        scriptInPanelLayout.setVerticalGroup(
            scriptInPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, scriptInPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(scriptInPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(runButton)
                    .addComponent(reuseEngine))
                .addContainerGap())
        );

        jSplitPane1.setLeftComponent(scriptInPanel);

        scriptOutArea.setEditable(false);
        scriptOutArea.setRows(2);
        jScrollPane2.setViewportView(scriptOutArea);

        javax.swing.GroupLayout scriptOutPanelLayout = new javax.swing.GroupLayout(scriptOutPanel);
        scriptOutPanel.setLayout(scriptOutPanelLayout);
        scriptOutPanelLayout.setHorizontalGroup(
            scriptOutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(scriptOutPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 523, Short.MAX_VALUE)
                .addContainerGap())
        );
        scriptOutPanelLayout.setVerticalGroup(
            scriptOutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(scriptOutPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane1.setRightComponent(scriptOutPanel);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 585, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runButtonActionPerformed
        this.runButton.setEnabled(false);
        final String script = this.scriptInArea.getText();
        final boolean reuse = this.reuseEngine.isSelected();
        this.scriptOutArea.setText("");

        SwingWorker<Object,Object> sw = new SwingWorker<Object,Object>(){
            @Override
            protected Object doInBackground() throws Exception {
                ScriptEngine e = getEngine( reuse );
                return e.eval(script);
            }   // end doInBackground

            @Override
            protected void done(){
                Object result = null;
                try {
                    result = get();
                } catch (InterruptedException ex) {
                    Logger.getLogger(EnginePane.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    alert(ex.getMessage());
                    Logger.getLogger(EnginePane.class.getName()).log(Level.SEVERE, null, ex);
                }
                runButton.setEnabled(true);
            }   // end done
        };
        sw.execute();
    }//GEN-LAST:event_runButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel eL;
    private javax.swing.JLabel engVerLabel;
    private javax.swing.JLabel evL;
    private javax.swing.JLabel extsLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JLabel langVerLabel;
    private javax.swing.JLabel lvL;
    private javax.swing.JLabel mL;
    private javax.swing.JLabel mimesLabel;
    private javax.swing.JLabel nL;
    private javax.swing.JLabel namesLabel;
    private javax.swing.JCheckBox reuseEngine;
    private javax.swing.JButton runButton;
    private javax.swing.JTextArea scriptInArea;
    private javax.swing.JPanel scriptInPanel;
    private javax.swing.JTextArea scriptOutArea;
    private javax.swing.JPanel scriptOutPanel;
    // End of variables declaration//GEN-END:variables

}
