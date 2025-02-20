package bountyprompt.gui;

import burp.api.montoya.persistence.PersistedObject;
import bountyprompt.BountyPrompt;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author wagiro
 */
public class MainGui extends javax.swing.JPanel {

    PersistedObject BBAIData;
    String filename;
    PromptPanel promptPanel;

    /**
     * Creates new form MainGui
     */
    public MainGui(BountyPrompt ext, PersistedObject BBAIData, String filename) {
        this.BBAIData = BBAIData;
        this.filename = filename;
        initComponents();
        promptsDirectory.setText(this.filename);

    }

    public List<PromptPanel.Prompt> getPrompts() {
        List<PromptPanel.Prompt> promptsList = new ArrayList<>();
        PromptPanel.PromptTableModel model = (PromptPanel.PromptTableModel) prompts_table.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            promptsList.add(model.getPromptAt(i));
        }
        return promptsList;
    }


    public void appendResponse(String promptTitle, String aiResponse) {
        String formattedResponse = "Prompt Title: " + promptTitle + "\n\n"
                + "AI Response: \n\n" + aiResponse + "\n"
                + "------------------------------------------------------------------------"
                + "------------------------------------------------------------------------"
                + "------------------------------------------------------------------------\n";
        // Append the formatted response to the existing text.
        promptsOutput.append(formattedResponse);
    }

    public void appendToPromptsOutput(String additionalText) {
        String formattedResponse = additionalText + "\n"
                + "------------------------------------------------------------------------"
                + "------------------------------------------------------------------------"
                + "------------------------------------------------------------------------\n";
        promptsOutput.append(formattedResponse);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jtabpane1 = new javax.swing.JTabbedPane();
        jPanel20 = new javax.swing.JPanel(new BorderLayout());
        jScrollPane7 = new javax.swing.JScrollPane(prompts_table);
        prompts_table = new javax.swing.JTable();
        jPanel19 = new javax.swing.JPanel();
        jPanel21 = new javax.swing.JPanel();
        jScrollPane9 = new javax.swing.JScrollPane();
        promptsOutput = new javax.swing.JTextArea();
        exportTo = new javax.swing.JButton();
        clear = new javax.swing.JButton();
        jPanel22 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        openai_model = new javax.swing.JComboBox<>();
        openai_key = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox<>();
        jTextField2 = new javax.swing.JTextField();
        jLabel33 = new javax.swing.JLabel();
        jLabel51 = new javax.swing.JLabel();
        jLabel500 = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();
        promptsDirectory = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        descriptionLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        jtabpane1.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N

        promptPanel = new PromptPanel(filename,prompts_table,jPanel19);

        prompts_table.setAutoCreateRowSorter(true);
        prompts_table.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        prompts_table.getTableHeader().setReorderingAllowed(false);
        jScrollPane7.setViewportView(prompts_table);

        javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
        jPanel19.setLayout(jPanel19Layout);
        jPanel19Layout.setHorizontalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 109, Short.MAX_VALUE)
        );
        jPanel19Layout.setVerticalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 158, Short.MAX_VALUE)
        );

        jPanel19.setLayout(new BoxLayout(jPanel19, BoxLayout.Y_AXIS));  // O BoxLayout si prefieres vertical

        javax.swing.GroupLayout jPanel20Layout = new javax.swing.GroupLayout(jPanel20);
        jPanel20.setLayout(jPanel20Layout);
        jPanel20Layout.setHorizontalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel20Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 1226, Short.MAX_VALUE))
        );
        jPanel20Layout.setVerticalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane7)
                    .addGroup(jPanel20Layout.createSequentialGroup()
                        .addComponent(jPanel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 758, Short.MAX_VALUE))))
        );

        jtabpane1.addTab("     Prompts     ", jPanel20);

        jScrollPane9.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        promptsOutput.setEditable(false);
        promptsOutput.setColumns(20);
        promptsOutput.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        promptsOutput.setLineWrap(true);
        promptsOutput.setRows(5);
        jScrollPane9.setViewportView(promptsOutput);

        exportTo.setText("Export to");
        exportTo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportResponses(evt);
            }
        });

        clear.setText("Clear");
        clear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearPromptOutput(evt);
            }
        });

        javax.swing.GroupLayout jPanel21Layout = new javax.swing.GroupLayout(jPanel21);
        jPanel21.setLayout(jPanel21Layout);
        jPanel21Layout.setHorizontalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel21Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(exportTo)
                    .addComponent(clear))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 1252, Short.MAX_VALUE))
        );

        jPanel21Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {clear, exportTo});

        jPanel21Layout.setVerticalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel21Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel21Layout.createSequentialGroup()
                        .addComponent(exportTo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(clear)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 916, Short.MAX_VALUE)))
        );

        jPanel21Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {clear, exportTo});

        jtabpane1.addTab("   Prompt Output   ", jPanel21);

        jLabel7.setFont(new java.awt.Font("Helvetica Neue", 0, 16)); // NOI18N
        jLabel7.setText("OpenAI ChatGPT");
        jLabel7.setEnabled(false);

        openai_model.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "GPT-4o", "o1", "o3-mini", "o3-mini-high" }));
        openai_model.setEnabled(false);

        openai_key.setEnabled(false);

        jLabel18.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        jLabel18.setText("Model");
        jLabel18.setEnabled(false);

        jLabel26.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        jLabel26.setText("Api Key");
        jLabel26.setEnabled(false);

        jLabel31.setFont(new java.awt.Font("Helvetica Neue", 0, 16)); // NOI18N
        jLabel31.setText("DeepSeek");
        jLabel31.setEnabled(false);

        jLabel32.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        jLabel32.setText("Model");
        jLabel32.setEnabled(false);

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "deepseek-r1" }));
        jComboBox2.setEnabled(false);

        jTextField2.setEnabled(false);

        jLabel33.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        jLabel33.setText("Api Key");
        jLabel33.setEnabled(false);

        jLabel51.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        jLabel51.setForeground(new java.awt.Color(0, 117, 169));
        jLabel51.setText("Bounty Prompts Directory");

        jLabel500.setText("In this section specify the base prompts directory. ");

        jButton5.setText("Directory");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadPromptsDirectory(evt);
            }
        });

        promptsDirectory.setToolTipText("");

        jButton1.setText("Reload");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reloadPromptsDirectory(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Helvetica Neue", 0, 18)); // NOI18N
        jLabel1.setText("Coming soon...");

        javax.swing.GroupLayout jPanel22Layout = new javax.swing.GroupLayout(jPanel22);
        jPanel22.setLayout(jPanel22Layout);
        jPanel22Layout.setHorizontalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel22Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addGroup(jPanel22Layout.createSequentialGroup()
                        .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel22Layout.createSequentialGroup()
                                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel7)
                                    .addComponent(jLabel31))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel22Layout.createSequentialGroup()
                                        .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel18)
                                            .addComponent(jLabel26))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(openai_model, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(openai_key, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(jPanel22Layout.createSequentialGroup()
                                        .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel32)
                                            .addComponent(jLabel33))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jComboBox2, 0, 300, Short.MAX_VALUE)
                                            .addComponent(jTextField2)))))
                            .addComponent(jLabel500, javax.swing.GroupLayout.PREFERRED_SIZE, 575, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel51)
                            .addGroup(jPanel22Layout.createSequentialGroup()
                                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(promptsDirectory, javax.swing.GroupLayout.PREFERRED_SIZE, 700, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(533, Short.MAX_VALUE))
                    .addGroup(jPanel22Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel22Layout.setVerticalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel22Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel51)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel500)
                .addGap(18, 18, 18)
                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton5)
                    .addComponent(promptsDirectory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(openai_model, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18))
                .addGap(18, 18, 18)
                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(openai_key, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel26))
                .addGap(20, 20, 20)
                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel31)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel32))
                .addGap(18, 18, 18)
                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel33))
                .addContainerGap(571, Short.MAX_VALUE))
        );

        jtabpane1.addTab("   Config   ", jPanel22);

        descriptionLabel.putClientProperty("html.disable", null);
        descriptionLabel.setText("<html> <p style=\"text-align: justify;\"> Bounty Prompt Extension is a Burp Suite extension developed by Bounty Security that leverages advanced AI technology integrated via Burp AI. It enables users to generate intelligent security testing prompts by analyzing selected HTTP requests and responses from various sources within Burp Suite. The extension supports a wide range of HTTP tags, allowing you to automatically include specific parts of HTTP traffic (such as headers, parameters, bodies, and cookies) in your prompts, streamlining both automated and manual penetration testing workflows. </p><br/>For more details and to explore our solutions, please visit: <ul><li><a href=\\\\\\\"\\\\\\\">https://bountysecurity.ai</a></li></ul></p></html>");
        descriptionLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        descriptionLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                goWeb(evt);
            }
        });

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/BountySecurity_Logo.png"))); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(descriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 823, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(530, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addComponent(descriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(554, Short.MAX_VALUE))
        );

        jtabpane1.addTab("   About   ", jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jtabpane1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jtabpane1)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void loadPromptsDirectory(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadPromptsDirectory
        JFrame parentFrame = new JFrame();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccione un directorio base");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int userSelection = fileChooser.showOpenDialog(parentFrame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile();
            String directoryPath = selectedDirectory.getAbsolutePath();

            if (!directoryPath.endsWith(File.separator)) {
                directoryPath += File.separator;
            }

            promptsDirectory.setText(directoryPath);
            BBAIData.setString("FILENAME", filename);
            filename = directoryPath;
            promptPanel.reloadPromptsFromDirectory(filename);

        }
    }//GEN-LAST:event_loadPromptsDirectory

    private void reloadPromptsDirectory(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reloadPromptsDirectory
        promptPanel.reloadPromptsFromDirectory(promptsDirectory.getText());
    }//GEN-LAST:event_reloadPromptsDirectory

    private void exportResponses(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportResponses
        exportResponses();
    }//GEN-LAST:event_exportResponses

    private void clearPromptOutput(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearPromptOutput
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to clear the output?",
                "Confirm Clear",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            promptsOutput.setText("");
        }
    }//GEN-LAST:event_clearPromptOutput

    private void goWeb(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_goWeb
        try {
            Desktop.getDesktop().browse(new URI("https://bountysecurity.ai/?utm_source=bountyprompt"));
        } catch (URISyntaxException | IOException e) {
            
        }
    }//GEN-LAST:event_goWeb

    /**
     * Exports the content of promptsOutput to a file selected by the user.
     */
    public void exportResponses() {
        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Export Responses");
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            try (java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(fileToSave))) {
                writer.write(promptsOutput.getText());
                javax.swing.JOptionPane.showMessageDialog(this, "Export successful.", "Export", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            } catch (java.io.IOException ex) {
                javax.swing.JOptionPane.showMessageDialog(this, "Error exporting file: " + ex.getMessage(), "Export Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton clear;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JButton exportTo;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton5;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel500;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTabbedPane jtabpane1;
    private javax.swing.JTextField openai_key;
    private javax.swing.JComboBox<String> openai_model;
    public javax.swing.JTextField promptsDirectory;
    private javax.swing.JTextArea promptsOutput;
    private javax.swing.JTable prompts_table;
    // End of variables declaration//GEN-END:variables
}
