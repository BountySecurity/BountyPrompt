package bountyprompt.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.Normalizer;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class PromptPanel {

    private String promptsDirectory;
    private JTable prompts_table;
    private JPanel buttonsContainer;
    private JButton addButton, editButton, removeButton;
    private Gson gson;
    private PromptTableModel tableModel;

    public PromptPanel(String baseDirectory, JTable prompts_table, JPanel buttonsContainer) {
        if (!baseDirectory.endsWith(File.separator)) {
            baseDirectory += File.separator;
        }
        this.promptsDirectory = baseDirectory;
        this.prompts_table = prompts_table;
        this.buttonsContainer = buttonsContainer;
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
        initComponents();
        loadPrompts();
    }

    private void initComponents() {
        // Configure the buttons container layout
        buttonsContainer.setLayout(new BoxLayout(buttonsContainer, BoxLayout.Y_AXIS));

        // Create and configure buttons
        addButton = new JButton("Add");
        editButton = new JButton("Edit");
        removeButton = new JButton("Remove");

        // Center align the buttons
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        editButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        removeButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Set uniform size for buttons
        Dimension d1 = addButton.getPreferredSize();
        Dimension d2 = editButton.getPreferredSize();
        Dimension d3 = removeButton.getPreferredSize();
        int maxWidth = Math.max(d1.width, Math.max(d2.width, d3.width));
        int height = d1.height;
        Dimension uniformSize = new Dimension(maxWidth, height);
        addButton.setMaximumSize(uniformSize);
        editButton.setMaximumSize(uniformSize);
        removeButton.setMaximumSize(uniformSize);
        addButton.setPreferredSize(uniformSize);
        editButton.setPreferredSize(uniformSize);
        removeButton.setPreferredSize(uniformSize);

        // Assign action listeners
        addButton.addActionListener(e -> addNewPrompt());
        editButton.addActionListener(e -> editSelectedPrompt());
        removeButton.addActionListener(e -> removeSelectedPrompts());

        // Add buttons with vertical spacing
        buttonsContainer.add(addButton);
        buttonsContainer.add(Box.createVerticalStrut(10));
        buttonsContainer.add(editButton);
        buttonsContainer.add(Box.createVerticalStrut(10));
        buttonsContainer.add(removeButton);

        buttonsContainer.revalidate();
        buttonsContainer.repaint();

        // Initialize the table model and configure the table
        tableModel = new PromptTableModel(new ArrayList<>());
        prompts_table.setModel(tableModel);
        prompts_table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        prompts_table.setFillsViewportHeight(true);
        prompts_table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        TableColumnModel columnModel = prompts_table.getColumnModel();
        // Set fixed widths for the first three columns; the last two expand naturally
        columnModel.getColumn(0).setPreferredWidth(20); // Title column
        columnModel.getColumn(1).setPreferredWidth(20); // Author column
        columnModel.getColumn(2).setPreferredWidth(20); // Output Type column

        // Set up a row sorter for the table
        TableRowSorter<PromptTableModel> sorter = new TableRowSorter<>(tableModel);
        prompts_table.setRowSorter(sorter);
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);
        sorter.sort();

        // Add double-click listener for editing a prompt
        prompts_table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = prompts_table.getSelectedRow();
                    if (row != -1) {
                        editSelectedPrompt();
                    }
                }
            }
        });
    }

    private void loadPrompts() {
        File dir = new File(promptsDirectory);
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                try (Reader reader = new FileReader(file)) {
                    Prompt p = gson.fromJson(reader, Prompt.class);
                    p.fileName = file.getName();
                    tableModel.addPrompt(p);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    // New method to update the directory and reload the table data.
    public void reloadPromptsFromDirectory(String newDirectory) {
        if (!newDirectory.endsWith(File.separator)) {
            newDirectory += File.separator;
        }
        this.promptsDirectory = newDirectory;
        // Clear the current prompts in the table model.
        tableModel.prompts.clear();
        tableModel.refresh();
        // Load prompts from the new directory.
        loadPrompts();
    }

    // Optionally, provide a getter if needed.
    public String getPromptsDirectory() {
        return promptsDirectory;
    }

    /**
     * Normalizes the title for use as a filename.
     */
    private String sanitizeTitle(String title) {
        String normalized = Normalizer.normalize(title, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("[^\\p{ASCII}]");
        normalized = pattern.matcher(normalized).replaceAll("");
        normalized = normalized.replaceAll("[^a-zA-Z0-9]", "_");
        if (normalized.isEmpty()) {
            normalized = "prompt";
        }
        return normalized;
    }

    /**
     * Saves the prompt to a JSON file.
     */
    private boolean savePromptToFile(Prompt p) {
        File file = new File(promptsDirectory + p.fileName);
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(p, writer);
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes the JSON file corresponding to the prompt.
     */
    private boolean deletePromptFile(Prompt p) {
        File file = new File(promptsDirectory + p.fileName);
        return file.delete();
    }

    private void addNewPrompt() {
        PromptDialog dialog = new PromptDialog((Frame) SwingUtilities.getWindowAncestor(prompts_table), "Add Prompt", null);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            Prompt newPrompt = dialog.getPrompt();
            String fileName = sanitizeTitle(newPrompt.title) + ".json";
            File file = new File(promptsDirectory + fileName);
            if (file.exists()) {
                int option = JOptionPane.showConfirmDialog(prompts_table,
                        "A prompt with that title already exists. Do you want to overwrite it?",
                        "Confirm", JOptionPane.YES_NO_OPTION);
                if (option != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            newPrompt.fileName = fileName;
            if (savePromptToFile(newPrompt)) {
                tableModel.addPrompt(newPrompt);
            } else {
                JOptionPane.showMessageDialog(prompts_table, "Error saving prompt", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editSelectedPrompt() {
        int selectedRow = prompts_table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(prompts_table, "Please select a row to edit", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Prompt p = tableModel.getPromptAt(selectedRow);
        String oldFileName = p.fileName;
        PromptDialog dialog = new PromptDialog((Frame) SwingUtilities.getWindowAncestor(prompts_table), "Edit Prompt", p);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            dialog.updatePrompt(p);
            String newFileName = sanitizeTitle(p.title) + ".json";
            if (!oldFileName.equals(newFileName)) {
                File oldFile = new File(promptsDirectory + oldFileName);
                if (oldFile.exists() && !oldFile.delete()) {
                    JOptionPane.showMessageDialog(prompts_table, "Error deleting old file", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                p.fileName = newFileName;
            }
            if (savePromptToFile(p)) {
                tableModel.refresh();
            } else {
                JOptionPane.showMessageDialog(prompts_table, "Error saving prompt", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void removeSelectedPrompts() {
        int[] selectedRows = prompts_table.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(prompts_table, "Please select one or more rows to delete", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(prompts_table, "Are you sure you want to delete the selected prompts?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        Arrays.sort(selectedRows);
        for (int i = selectedRows.length - 1; i >= 0; i--) {
            Prompt p = tableModel.getPromptAt(selectedRows[i]);
            if (deletePromptFile(p)) {
                tableModel.removePrompt(selectedRows[i]);
            } else {
                JOptionPane.showMessageDialog(prompts_table, "Error deleting file: " + p.fileName, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Internal helper classes
    public static class Prompt {

        @Expose
        public String title;
        @Expose
        public String author;
        @Expose
        public String outputType;
        @Expose
        public String systemPrompt;
        @Expose
        public String userPrompt;

        // New fields for severity and confidence
        @Expose
        public String severity;
        @Expose
        public String confidence;

        public String fileName;  // Used to identify the file

        public Prompt() {
        }
    }

    public static class PromptTableModel extends AbstractTableModel {

        // Updated table header with separate System and User Prompt columns
        private final String[] columnNames = {"Title", "Author", "Output Type", "System Prompt", "User Prompt"};
        private final List<Prompt> prompts;

        public PromptTableModel(List<Prompt> prompts) {
            this.prompts = prompts;
        }

        @Override
        public int getRowCount() {
            return prompts.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Prompt p = prompts.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return p.title;
                case 1:
                    return p.author;
                case 2:
                    return p.outputType;
                case 3:
                    return p.systemPrompt;
                case 4:
                    return p.userPrompt;
                default:
                    return "";
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        public Prompt getPromptAt(int rowIndex) {
            return prompts.get(rowIndex);
        }

        public void addPrompt(Prompt p) {
            prompts.add(p);
            fireTableRowsInserted(prompts.size() - 1, prompts.size() - 1);
        }

        public void removePrompt(int index) {
            prompts.remove(index);
            fireTableRowsDeleted(index, index);
        }

        public void refresh() {
            fireTableDataChanged();
        }
    }

    public class PromptDialog extends JDialog {

        private JTextField titleField;
        private JTextField authorField;
        private JComboBox<String> outputTypeComboBox;
        // New combo boxes for severity and confidence
        private JComboBox<String> severityComboBox;
        private JComboBox<String> confidenceComboBox;
        private JTextArea systemPromptArea;
        private JTextArea userPromptArea;
        private boolean confirmed = false;

        public PromptDialog(Frame owner, String title, Prompt prompt) {
            super(owner, title, true);
            initComponents(prompt);
            pack();
            setSize(new Dimension(800, 600));
            setLocationRelativeTo(owner);
        }

        private void initComponents(Prompt prompt) {
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            JPanel fieldsPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(5, 5, 5, 5);

            // Title label and field
            gbc.weightx = 0.0;
            fieldsPanel.add(new JLabel("Title:"), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1.0;
            titleField = new JTextField(20);
            fieldsPanel.add(titleField, gbc);

            // Author label and field
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.weightx = 0.0;
            fieldsPanel.add(new JLabel("Author:"), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1.0;
            authorField = new JTextField(20);
            fieldsPanel.add(authorField, gbc);

            // Output Type label and combo box
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.weightx = 0.0;
            fieldsPanel.add(new JLabel("Output Type:"), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1.0;
            outputTypeComboBox = new JComboBox<>(new String[]{"Issue", "Prompt Output"});
            fieldsPanel.add(outputTypeComboBox, gbc);

            // New: Severity label and combo box
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.weightx = 0.0;
            fieldsPanel.add(new JLabel("Severity:"), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1.0;
            severityComboBox = new JComboBox<>(new String[]{"Information", "Low", "Medium", "High"});
            fieldsPanel.add(severityComboBox, gbc);

            // New: Confidence label and combo box
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.weightx = 0.0;
            fieldsPanel.add(new JLabel("Confidence:"), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1.0;
            confidenceComboBox = new JComboBox<>(new String[]{"Certain", "Firm", "Tentative"});
            fieldsPanel.add(confidenceComboBox, gbc);

            // System Prompt label and text area
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.gridwidth = 2;
            gbc.weightx = 0.0;
            gbc.insets = new Insets(5, 5, 5, 5);
            fieldsPanel.add(new JLabel("System Prompt:"), gbc);
            gbc.gridy++;
            gbc.insets = new Insets(0, 5, 5, 5);
            gbc.weightx = 1.0;
            gbc.weighty = 0.5;
            gbc.fill = GridBagConstraints.BOTH;
            systemPromptArea = new JTextArea(5, 40);
            systemPromptArea.setLineWrap(true);
            systemPromptArea.setWrapStyleWord(true);
            JScrollPane systemScrollPane = new JScrollPane(systemPromptArea);
            systemScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            systemScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            fieldsPanel.add(systemScrollPane, gbc);

            // User Prompt label and text area
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.gridwidth = 2;
            gbc.insets = new Insets(5, 5, 0, 5);
            fieldsPanel.add(new JLabel("User Prompt:"), gbc);
            gbc.gridy++;
            gbc.insets = new Insets(0, 5, 5, 5);
            gbc.weightx = 1.0;
            gbc.weighty = 0.5;
            gbc.fill = GridBagConstraints.BOTH;
            userPromptArea = new JTextArea(5, 40);
            userPromptArea.setLineWrap(true);
            userPromptArea.setWrapStyleWord(true);
            JScrollPane userScrollPane = new JScrollPane(userPromptArea);
            userScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            userScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            fieldsPanel.add(userScrollPane, gbc);

            panel.add(fieldsPanel, BorderLayout.CENTER);

            // OK and Cancel buttons
            JPanel buttonPanel = new JPanel();
            JButton okButton = new JButton("OK");
            JButton cancelButton = new JButton("Cancel");

            okButton.addActionListener(e -> {
                confirmed = true;
                setVisible(false);
            });
            cancelButton.addActionListener(e -> {
                confirmed = false;
                setVisible(false);
            });
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            setContentPane(panel);

            // If a prompt is passed, populate the fields with its data.
            if (prompt != null) {
                titleField.setText(prompt.title);
                authorField.setText(prompt.author);
                outputTypeComboBox.setSelectedItem(prompt.outputType);
                // Set the new combo boxes if values exist
                if (prompt.severity != null) {
                    severityComboBox.setSelectedItem(prompt.severity);
                }
                if (prompt.confidence != null) {
                    confidenceComboBox.setSelectedItem(prompt.confidence);
                }
                systemPromptArea.setText(prompt.systemPrompt);
                userPromptArea.setText(prompt.userPrompt);
            }

            // Enable/Disable severity and confidence combo boxes based on output type
            outputTypeComboBox.addActionListener(e -> {
                boolean isIssue = "Issue".equals(outputTypeComboBox.getSelectedItem());
                severityComboBox.setEnabled(isIssue);
                confidenceComboBox.setEnabled(isIssue);
            });
        }

        public boolean isConfirmed() {
            return confirmed;
        }

        public void updatePrompt(Prompt prompt) {
            prompt.title = titleField.getText();
            prompt.author = authorField.getText();
            prompt.outputType = outputTypeComboBox.getSelectedItem().toString();
            // Update the new fields
            prompt.severity = severityComboBox.getSelectedItem().toString();
            prompt.confidence = confidenceComboBox.getSelectedItem().toString();
            prompt.systemPrompt = systemPromptArea.getText();
            prompt.userPrompt = userPromptArea.getText();
        }

        public Prompt getPrompt() {
            Prompt p = new Prompt();
            updatePrompt(p);
            return p;
        }
    }
}
