package bountyprompt.gui;

import burp.api.montoya.persistence.PersistedObject;
import bountyprompt.BountyPrompt;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import java.io.*;
import java.util.zip.*;

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
        this.groq_key.setText(BBAIData.getString("GROQ_APIKEY"));
        Integer index = BBAIData.getInteger("GROQ_MODEL");
        if (index != null) {
            this.groq_model.setSelectedIndex(index);
        } else {
            this.groq_model.setSelectedIndex(0);
        }
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

    public void selectExtensionTab() {
        Component current = this;
        do {
            current = current.getParent();
        } while (!(current instanceof JTabbedPane));

        JTabbedPane tabPane = (JTabbedPane) current;
        for (int i = 0; i < tabPane.getTabCount(); i++) {
            if (tabPane.getTitleAt(i).equals(BountyPrompt.EXTENSION_NAME)) {
                tabPane.setSelectedIndex(i);
            }
        }
    }

    public void selectConfigTab() {
        jtabpane1.setSelectedIndex(2);
    }

    public void unzip(String zipFilePath, String destDir) throws IOException {
        File dir = new File(destDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String filePath = destDir + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    // Ensure that the parent directory exists
                    File parent = new File(filePath).getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = zis.read(buffer)) != -1) {
                            bos.write(buffer, 0, len);
                        }
                    }
                } else {
                    // If the entry is a directory, create it
                    new File(filePath).mkdirs();
                }
                zis.closeEntry();
            }
        }
    }

    public void copyDirectory(File sourceDir, File destDir) throws IOException {
        if (sourceDir.isDirectory()) {
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            String[] children = sourceDir.list();
            if (children != null) {
                for (String child : children) {
                    copyDirectory(new File(sourceDir, child), new File(destDir, child));
                }
            }
        } else {
            // Copy file content
            try (InputStream in = new FileInputStream(sourceDir); OutputStream out = new FileOutputStream(destDir)) {
                byte[] buffer = new byte[4096];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }
        }
    }

    public void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] children = directory.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteDirectory(child);
                }
            }
        }
        directory.delete();
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
        downloadPrompts = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        groq_model = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        groq_key = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
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
        jLabel7.setText("OpenAI");
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

        downloadPrompts.setText("Download");
        downloadPrompts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downloadPrompts(evt);
            }
        });

        jLabel3.setText("* Download prompts from GitHub");

        groq_model.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "distil-whisper-large-v3-en", "gemma2-9b-it", "llama-3.3-70b-versatile", "llama-3.1-8b-instant", "llama-guard-3-8b", "llama3-70b-8192", "llama3-8b-8192", "mixtral-8x7b-32768", "whisper-large-v3", "whisper-large-v3-turbo", "qwen-2.5-coder-32b", "qwen-2.5-32b", "deepseek-r1-distill-qwen-32b", "deepseek-r1-distill-llama-70b-specdec", "deepseek-r1-distill-llama-70b", "llama-3.3-70b-specdec", "llama-3.2-1b-preview", "llama-3.2-3b-preview", "llama-3.2-11b-vision-preview", "llama-3.2-90b-vision-preview" }));

        jLabel8.setFont(new java.awt.Font("Helvetica Neue", 0, 16)); // NOI18N
        jLabel8.setText("Groq Cloud");

        jLabel27.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        jLabel27.setText("Api Key");

        jLabel19.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        jLabel19.setText("Model");

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
                        .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(jPanel22Layout.createSequentialGroup()
                                .addComponent(downloadPrompts)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel3))
                            .addGroup(jPanel22Layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addGap(18, 18, 18)
                                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel19)
                                    .addComponent(jLabel27))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(groq_model, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(groq_key, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );

        jPanel22Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {downloadPrompts, jButton1, jButton5});

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(downloadPrompts, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(groq_model, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19))
                .addGap(18, 18, 18)
                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(groq_key, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel27))
                .addGap(41, 41, 41)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
                .addContainerGap(455, Short.MAX_VALUE))
        );

        jPanel22Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {downloadPrompts, jButton1, jButton5});

        jtabpane1.addTab("   Config   ", jPanel22);

        descriptionLabel.putClientProperty("html.disable", null);
        descriptionLabel.setText("<html> <p style=\"text-align: justify;\"><b>Bounty Prompt</b> is an Open-Source Burp Suite extension developed by <b>Bounty Security</b> that leverages advanced AI technology through both <b>Burp AI</b> and <b>Groq Cloud</b>. It allows you to save pre-configured AI prompts and automatically attach selected HTTP requests and responses from Burp Suite. This combined data is sent to the AI engine, which analyzes your HTTP traffic and provides tailored security testing insights—helping to streamline vulnerability assessments and penetration testing workflows.<br/><br/><b>Bounty Prompt</b> not only delivers tailored security testing insights, but also actively responds to your specific queries. For instance, it can detect sensitive information within responses, scrutinize parameters to pinpoint potential vulnerabilities, and more. Moreover, the extension supports a comprehensive set of HTTP tags, allowing you to seamlessly incorporate key elements of HTTP traffic—such as headers, parameters, bodies, and cookies—directly into your prompts.<br/></p><br/>For more details and to explore our solutions, please visit: <ul><li><a href=\\\\\\\"\\\\\\\">https://bountysecurity.ai</a></li></ul></p></html>");
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
                .addComponent(descriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(515, Short.MAX_VALUE))
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

    private void downloadPrompts(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downloadPrompts
        // URL of the GitHub project zip (downloading the main branch in this example)
        String zipUrl = "https://github.com/BountySecurity/BountyPrompt/archive/refs/heads/main.zip";
        // Get the user's home directory
        String userHome = System.getProperty("user.home");
        // Temporary file path for the downloaded zip file
        String zipFilePath = userHome + File.separator + "BountyPrompt.zip";
        // Destination directory for the prompts (home/prompts)
        String destPromptsDirPath = userHome + File.separator + "prompts";
        File destPromptsDirFolder = new File(destPromptsDirPath);

        // If the prompts directory exists, warn the user that it will be overwritten.
        if (destPromptsDirFolder.exists()) {
            int response = JOptionPane.showConfirmDialog(null,
                    "The prompts directory already exists and will be overwritten. Do you want to continue?",
                    "Confirm Overwrite",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (response != JOptionPane.YES_OPTION) {
                return; // Cancel the download if the user does not agree.
            }
        }

        try {
            // Download the zip file
            URL url = new URL(zipUrl);
            try (InputStream in = url.openStream(); FileOutputStream fos = new FileOutputStream(zipFilePath)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            // Unzip the downloaded file into the user's home directory
            // This will create a folder named "BountyPrompt-main" in the user's home directory.
            unzip(zipFilePath, userHome);

            // Define the extracted folder and the source prompts directory
            File extractedFolder = new File(userHome, "BountyPrompt-main");
            File sourcePromptsDir = new File(extractedFolder, "prompts");
            File destPromptsDir = new File(destPromptsDirPath);

            // Copy the prompts directory from the extracted folder to the destination
            copyDirectory(sourcePromptsDir, destPromptsDir);

            // Delete the downloaded zip file
            new File(zipFilePath).delete();

            // Delete the extracted "BountyPrompt-main" directory
            deleteDirectory(extractedFolder);

            // Update the text field promptsDirectory with the destination directory path
            promptsDirectory.setText(destPromptsDirPath);
            promptPanel.reloadPromptsFromDirectory(promptsDirectory.getText());

            // Show a popup message indicating that the process has completed successfully
            JOptionPane.showMessageDialog(null, "Download and extraction process completed successfully!");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error downloading or processing the project: " + ex.getMessage());
        }
    }//GEN-LAST:event_downloadPrompts

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
    private javax.swing.JButton downloadPrompts;
    private javax.swing.JButton exportTo;
    public javax.swing.JTextField groq_key;
    public javax.swing.JComboBox<String> groq_model;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton5;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel500;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField jTextField2;
    public javax.swing.JTabbedPane jtabpane1;
    private javax.swing.JTextField openai_key;
    private javax.swing.JComboBox<String> openai_model;
    public javax.swing.JTextField promptsDirectory;
    private javax.swing.JTextArea promptsOutput;
    private javax.swing.JTable prompts_table;
    // End of variables declaration//GEN-END:variables
}
