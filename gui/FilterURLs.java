package bountyprompt.gui;

import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.params.HttpParameterType;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.http.message.requests.HttpRequest;
import bountyprompt.BountyPrompt;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FilterURLs extends JPanel {

    private final DefaultTableModel tableModel;
    private final List<HttpRequestResponse> messages;

    // Internal elements
    public List<HttpRequestResponse> filteredMessages;
    private final List<HttpRequestResponse> originalMessages;
    private final DefaultTableModel replaceTableModel;
    private final BountyPrompt ext;

    /**
     * Constructor.
     *
     * @param requestResponses the list of HttpRequestResponse objects to
     * display.
     */
    public FilterURLs(BountyPrompt ext, List<HttpRequestResponse> requestResponses) {
        this.messages = new ArrayList<>(requestResponses);
        this.ext = ext;
        this.filteredMessages = new ArrayList<>(requestResponses);
        this.originalMessages = new ArrayList<>(requestResponses);
        this.tableModel = initTableModel();
        this.replaceTableModel = initReplaceTableModel();
        initComponents();
        showURLs(this.messages);
        showFilterURLsNumbers();
    }

    public static List<HttpRequestResponse> showURLFilterPopup(Component parent, List<HttpRequestResponse> requestResponses) {
        // Create an instance of FilterURLs. We pass 'null' for ext if not needed in the popup.
        FilterURLs filterPanel = new FilterURLs(null, requestResponses);
        int option = JOptionPane.showConfirmDialog(parent, filterPanel,
                "Select URLs to include", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            // When OK is pressed, return the filtered (remaining) request/response objects.
            return filterPanel.getRemainingRequestResponses();
        }
        // If the user cancels, return an empty list.
        return new ArrayList<>();
    }

    public List<HttpRequestResponse> getRemainingRequestResponses() {
        return new ArrayList<>(filteredMessages);
    }

    /**
     * Returns a list of all URLs currently in the table.
     *
     * @return a List<String> containing all the URLs remaining in the table.
     */
    public List<String> getSelectedURLs() {
        List<String> urls = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String url = (String) tableModel.getValueAt(i, 0);
            urls.add(url);
        }
        return urls;
    }

    private DefaultTableModel initTableModel() {
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class getColumnClass(int column) {
                switch (column) {
                    case 3:
                        return Integer.class;
                    case 4:
                        return Integer.class;
                    case 6:
                        return Integer.class;
                    default:
                        return String.class;
                }
            }

            @Override
            public Object getValueAt(int row, int column) {
                Object value = super.getValueAt(row, column);
                if (column == 5 && value instanceof Integer && (Integer) value == 0) {
                    return null;
                }
                return value;
            }
        };

        // Define table columns
        {
            tableModel.setNumRows(0);
            tableModel.setColumnCount(0);
            tableModel.addColumn("Host");
            tableModel.addColumn("Method");
            tableModel.addColumn("Path and Query");
            tableModel.addColumn("Params");
            tableModel.addColumn("Cookies");
            tableModel.addColumn("Content Type");
            tableModel.addColumn("HTTP Code");
        }

        return tableModel;
    }

    private DefaultTableModel initReplaceTableModel() {
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }

            @Override
            public Class getColumnClass(int column) {
                return String.class;
            }

            @Override
            public Object getValueAt(int row, int column) {
                Object value = super.getValueAt(row, column);
                if (column == 5 && value instanceof Integer && (Integer) value == 0) {
                    return null;
                }
                return value;
            }
        };

        // Define table columns
        {
            tableModel.setNumRows(0);
            tableModel.setColumnCount(0);
            tableModel.addColumn("Match");
            tableModel.addColumn("Replace");
            tableModel.addColumn("Type");
        }

        return tableModel;
    }

    private JTable initTable() {
        JTable table = new JTable(initTableModel());
        table.setModel(this.tableModel);

        // Set columns widths
        {
            table.getColumnModel().getColumn(0).setPreferredWidth(200);
            table.getColumnModel().getColumn(1).setPreferredWidth(70);
            table.getColumnModel().getColumn(2).setPreferredWidth(270);
            table.getColumnModel().getColumn(3).setPreferredWidth(70);
            table.getColumnModel().getColumn(4).setPreferredWidth(75);
            table.getColumnModel().getColumn(5).setPreferredWidth(100);
            table.getColumnModel().getColumn(6).setPreferredWidth(90);
        }

        // Set columns alignment
        {
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(JLabel.CENTER);
            table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
            table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
            table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
            table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
            table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        }

        // Set table sorter
        {
            TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
            table.setRowSorter(sorter);
            List<RowSorter.SortKey> sortKeys = new ArrayList<>();
            sorter.setSortKeys(sortKeys);
            sorter.sort();
            table.getTableHeader().setReorderingAllowed(false);
        }

        // Set table double-click listener
        {
            table.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent mouseEvent) {
                    // If it's no double-click, or no row selected, we discard it
                    JTable table = (JTable) mouseEvent.getSource();
                    int selectedRow = table.getSelectedRow();
                    if (mouseEvent.getClickCount() != 2 || selectedRow == -1) {
                        return;
                    }

                    // Otherwise, we display the request / response viewer
                    UrlsRequestResponse urlsRequestResponse = new UrlsRequestResponse(ext);
                    JOptionPane optionPane = new JOptionPane(urlsRequestResponse, JOptionPane.PLAIN_MESSAGE, JOptionPane.CLOSED_OPTION);

                    // With the corresponding request and response
                    int modelRowIndex = table.convertRowIndexToModel(selectedRow);
                    HttpRequest request = filteredMessages.get(modelRowIndex).request();
                    JDialog dialog = optionPane.createDialog("Request to " + request.url());
                    dialog.setSize(new Dimension(800, 650));
                    dialog.setResizable(true);
                    dialog.setLocationRelativeTo(null);
                    urlsRequestResponse.showRequestResponse(filteredMessages.get(modelRowIndex));
                    dialog.setVisible(true);
                    dialog.toFront();
                }
            });
        }

        return table;
    }

    private void initExtensionsInput() {
        extensionsTextInput.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update();
            }

            private void update() {
                // Reset original messages
                filteredMessages = new ArrayList<>(originalMessages);

                // And process again existing filters
                tableModel.setNumRows(0);
                checkURLsCheckbox();
                showURLs(filteredMessages);
                showFilterURLsNumbers();
            }
        });
    }

    private void initRegexInput() {
        regexTextInput.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update();
            }

            private void update() {
                // Reset original messages
                filteredMessages = new ArrayList<>(originalMessages);

                // And process again existing filters
                tableModel.setNumRows(0);
                checkURLsCheckbox();
                showURLs(filteredMessages);
                showFilterURLsNumbers();
            }
        });
    }

    private void showURLs(List<HttpRequestResponse> requestResponses) {
        short http_status = 0;
        int cookie_number = 0;

        for (HttpRequestResponse requestResponse : requestResponses) {
            HttpRequest request = requestResponse.request();
            try {
                if (requestResponse.response() != null) {
                    http_status = requestResponse.response().statusCode();
                }
            } catch (Exception ignored) {
                // Invalid response, do nothing
            }
            cookie_number = 0;
            for (ParsedHttpParameter param : request.parameters()) {
                if (param.type() == HttpParameterType.COOKIE) {
                    cookie_number++;
                }
            }

            String contentType = "";
            if (getContentType(requestResponse) != null) {
                contentType = getContentType(requestResponse);
            }

            String protocol = "http";
            if (requestResponse.httpService() != null && requestResponse.httpService().secure()) {
                protocol = "https";
            }

            String host;
            if (requestResponse.httpService() != null) {
                host = String.format("%s://%s", protocol, requestResponse.httpService().host());
            } else if (requestResponse.request().httpService() != null) {
                host = String.format("%s://%s", protocol, requestResponse.request().httpService().host());
            } else {
                HttpHeader hostHeader = requestResponse.request()
                        .headers()
                        .stream().filter(h -> h.name().equalsIgnoreCase("Host"))
                        .findFirst().orElse(null);

                if (hostHeader != null) {
                    host = hostHeader.value();
                } else {
                    host = "<Error: Unknown Host>";
                }
            }

            this.tableModel.addRow(new Object[]{host, request.method(), request.path(), request.parameters().size(), cookie_number, contentType, http_status});

        }
        table.setModel(this.tableModel);
        urlsNumberUpdate(filteredMessages);
    }

    private void showFilterURLsNumbers() {
        showFilterURLsSameUrlNumbers();
        showFilterURLsOutOfScopeNumbers();
        showFilterURLsNoParamsNumbers();
        showFilterURLsExtensionNumbers();
        showFilterURLsRegexNumbers();
    }

    private void showFilterURLsSameUrlNumbers() {
        Set<HttpRequestParams> set = new HashSet<>();
        int duplicatesCount = 0;
        for (HttpRequestResponse requestResponse : filteredMessages) {
            HttpRequest request = requestResponse.request();
            List<String> parameterNames = request.parameters().stream()
                    .map(ParsedHttpParameter::name)
                    .sorted()
                    .collect(Collectors.toList());
            HttpRequestParams reqParams = new HttpRequestParams(request.url(), parameterNames);
            if (!set.add(reqParams)) {
                duplicatesCount++;
            }
        }

        duplicateURLNumbers.setText(String.valueOf(duplicatesCount));
    }

    private void showFilterURLsOutOfScopeNumbers() {
        int count = 0;

        for (HttpRequestResponse requestResponse : filteredMessages) {
            try {
                if (!ext.api.scope().isInScope(requestResponse.request().url())) {
                    count++;
                }
            } catch (Exception ignored) {
                // Invalid url, do nothing
            }
        }

        duplicateURLOut.setText(String.valueOf(count));
    }

    private void showFilterURLsNoParamsNumbers() {
        int count = 0;
        for (HttpRequestResponse requestResponse : filteredMessages) {
            HttpRequest request = requestResponse.request();
            if (request.parameters().isEmpty()) {
                count++;
            }
        }

        duplicateURLNoParams.setText(String.valueOf(count));
    }

    private void showFilterURLsExtensionNumbers() {
        String extensions = extensionsTextInput.getText();
        if (extensions.isEmpty()) {
            duplicateURLExtension.setText(String.valueOf(0));
            return;
        }

        int count = 0;
        String[] extensionList = extensions.split(",");

        for (HttpRequestResponse requestResponse : filteredMessages) {
            HttpRequest request = requestResponse.request();
            String path = request.path();
            for (String extension : extensionList) {
                if (path.endsWith("." + extension.trim())) {
                    count++;
                    break;
                }
            }
        }

        duplicateURLExtension.setText(String.valueOf(count));
    }

    private void showFilterURLsRegexNumbers() {
        String regex = regexTextInput.getText();
        if (regex.isEmpty()) {
            duplicateURLRegex.setText(String.valueOf(0));
            return;
        }

        int count = 0;
        Pattern pattern = Pattern.compile(regex.trim());

        for (HttpRequestResponse requestResponse : filteredMessages) {
            Matcher matcher = pattern.matcher(requestResponse.request().url());
            if (matcher.find()) {
                count++;
            }
        }

        duplicateURLRegex.setText(String.valueOf(count));
    }

    private void filterURLs(List<HttpRequestResponse> requestResponses, int row) {
        requestResponses.remove(row);
        table.setModel(this.tableModel);
        urlsNumberUpdate(filteredMessages);
    }

    private void urlsNumberUpdate(List<HttpRequestResponse> requestResponses) {
        totalUrlsNumber.setText(String.valueOf(requestResponses.size()));
    }

    private void removeURLsSameUrlAndParams() {
        Set<HttpRequestParams> set = new HashSet<>();
        Iterator<HttpRequestResponse> iterator = filteredMessages.iterator();

        while (iterator.hasNext()) {
            HttpRequestResponse requestResponse = iterator.next();
            HttpRequest request = requestResponse.request();
            List<String> parameterNames = request.parameters().stream()
                    .map(ParsedHttpParameter::name)
                    .sorted()
                    .collect(Collectors.toList());
            HttpRequestParams reqParams = new HttpRequestParams(requestResponse.request().url(), parameterNames);
            if (!set.add(reqParams)) {
                iterator.remove();
            }
        }
    }

    private void removeOutOFScopeURLs() {
        filteredMessages.removeIf(requestResponse -> !ext.api.scope().isInScope(requestResponse.request().url()));
    }

    private void removeURLsNoParameters() {
        filteredMessages.removeIf(requestResponse -> requestResponse.request().parameters().isEmpty());
    }

    private void removeURLsExtensions() {
        String extensions = extensionsTextInput.getText();
        if (extensions.isEmpty()) {
            return;
        }

        Iterator<HttpRequestResponse> iterator = filteredMessages.iterator();
        while (iterator.hasNext()) {
            HttpRequest request = iterator.next().request();
            for (String extension : extensions.split(",")) {
                if (request.path().endsWith("." + extension.trim())) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    private void removeURLsRegex() {
        String regex = regexTextInput.getText();
        if (regex.isEmpty()) {
            return;
        }

        Pattern pattern = Pattern.compile(regex.trim());
        Iterator<HttpRequestResponse> iterator = filteredMessages.iterator();

        while (iterator.hasNext()) {
            Matcher matcher = pattern.matcher(iterator.next().request().url());
            if (matcher.find()) {
                iterator.remove();
            }
        }
    }

    private void checkURLsCheckbox() {
        if (jCheckBox1.isSelected()) {
            removeURLsSameUrlAndParams();
        }
        if (jCheckBox2.isSelected()) {
            removeOutOFScopeURLs();
        }
        if (jCheckBox3.isSelected()) {
            removeURLsNoParameters();
        }
        if (jCheckBox4.isSelected()) {
            removeURLsExtensions();
        }
        if (jCheckBox5.isSelected()) {
            removeURLsRegex();
        }

    }

    private String getContentType(HttpRequestResponse requestResponse) {
        if (requestResponse.response() == null) {
            return null;
        }

        String contentType = "";
        for (HttpHeader header : requestResponse.response().headers()) {
            if (header.toString().toUpperCase().startsWith("CONTENT-TYPE:")) {
                contentType = header.toString().split("\\s+", 2)[1].split(";")[0];
                break;
            }
        }
        return contentType;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        removeBtn = new javax.swing.JButton();
        resetUrlsBtn = new javax.swing.JButton();
        filterUrlsPanel = new javax.swing.JPanel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        extensionsTextInput = new javax.swing.JTextField();
        jCheckBox4 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        duplicateURLNumbers = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        duplicateURLOut = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        duplicateURLNoParams = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        duplicateURLExtension = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jCheckBox5 = new javax.swing.JCheckBox();
        regexTextInput = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        duplicateURLRegex = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        scrollPane = new javax.swing.JScrollPane();
        table = this.initTable();
        totalUrlsLabel = new javax.swing.JLabel();
        totalUrlsNumber = new javax.swing.JLabel();

        removeBtn.setText("Remove");
        removeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeUrls(evt);
            }
        });

        resetUrlsBtn.setText("Reset URLs");
        resetUrlsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetUrls(evt);
            }
        });

        filterUrlsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Filter URLs"));

        jCheckBox1.setText("Remove duplicate URLs (same URL and Parameters)");
        jCheckBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                filterURLEquals(evt);
            }
        });

        jCheckBox2.setText("Remove out of scope URLs");
        jCheckBox2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                removeOutScopeUrls(evt);
            }
        });

        extensionsTextInput.setText("jpg,jpeg,gif,css,tif,tiff,png,ttf,woff,woff2,ico,pdf,svg,txt,js");
        extensionsTextInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                extensionsTextInputActionPerformed(evt);
            }
        });

        jCheckBox4.setText("Remove URLs with the following extensions");
        jCheckBox4.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                removeURLsExtensions(evt);
            }
        });

        jCheckBox3.setText("Remove URLs with no parameters");
        jCheckBox3.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                filterURLsNoParams(evt);
            }
        });

        jLabel3.setText("[");

        duplicateURLNumbers.setText("0");

        jLabel4.setText("urls]");

        jLabel5.setText("[");

        duplicateURLOut.setText("0");

        jLabel6.setText("urls]");

        jLabel7.setText("[");

        duplicateURLNoParams.setText("0");

        jLabel8.setText("urls]");

        jLabel9.setText("[");

        duplicateURLExtension.setText("0");

        jLabel10.setText("urls]");

        jCheckBox5.setText("Remove URLs with the regex");
        jCheckBox5.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox5removeURLsRegex(evt);
            }
        });

        jLabel11.setText("[");

        duplicateURLRegex.setText("0");

        jLabel12.setText("urls]");

        javax.swing.GroupLayout filterUrlsPanelLayout = new javax.swing.GroupLayout(filterUrlsPanel);
        filterUrlsPanel.setLayout(filterUrlsPanelLayout);
        filterUrlsPanelLayout.setHorizontalGroup(
            filterUrlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filterUrlsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(filterUrlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(filterUrlsPanelLayout.createSequentialGroup()
                        .addComponent(jCheckBox2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5)
                        .addGap(0, 0, 0)
                        .addComponent(duplicateURLOut)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel6))
                    .addGroup(filterUrlsPanelLayout.createSequentialGroup()
                        .addComponent(jCheckBox3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel7)
                        .addGap(0, 0, 0)
                        .addComponent(duplicateURLNoParams)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel8))
                    .addGroup(filterUrlsPanelLayout.createSequentialGroup()
                        .addComponent(jCheckBox1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addGap(0, 0, 0)
                        .addComponent(duplicateURLNumbers)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 49, Short.MAX_VALUE)
                .addGroup(filterUrlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(filterUrlsPanelLayout.createSequentialGroup()
                        .addComponent(jCheckBox4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel9)
                        .addGap(0, 0, 0)
                        .addComponent(duplicateURLExtension)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel10))
                    .addGroup(filterUrlsPanelLayout.createSequentialGroup()
                        .addComponent(jCheckBox5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel11)
                        .addGap(0, 0, 0)
                        .addComponent(duplicateURLRegex)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel12))
                    .addGroup(filterUrlsPanelLayout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(filterUrlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(extensionsTextInput, javax.swing.GroupLayout.PREFERRED_SIZE, 421, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(regexTextInput, javax.swing.GroupLayout.PREFERRED_SIZE, 421, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        filterUrlsPanelLayout.setVerticalGroup(
            filterUrlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filterUrlsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(filterUrlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(filterUrlsPanelLayout.createSequentialGroup()
                        .addGroup(filterUrlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jCheckBox4)
                            .addComponent(jLabel9)
                            .addComponent(duplicateURLExtension)
                            .addComponent(jLabel10))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(extensionsTextInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(filterUrlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jCheckBox5)
                            .addComponent(jLabel11)
                            .addComponent(duplicateURLRegex)
                            .addComponent(jLabel12))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(regexTextInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(filterUrlsPanelLayout.createSequentialGroup()
                        .addGroup(filterUrlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jCheckBox1)
                            .addComponent(jLabel3)
                            .addComponent(duplicateURLNumbers)
                            .addComponent(jLabel4))
                        .addGap(18, 18, 18)
                        .addGroup(filterUrlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jCheckBox2)
                            .addComponent(jLabel5)
                            .addComponent(duplicateURLOut)
                            .addComponent(jLabel6))
                        .addGap(18, 18, 18)
                        .addGroup(filterUrlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jCheckBox3)
                            .addComponent(jLabel7)
                            .addComponent(duplicateURLNoParams)
                            .addComponent(jLabel8))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        scrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        scrollPane.setViewportView(table);

        totalUrlsLabel.setText("Total URLs:");

        totalUrlsNumber.setText(String.valueOf(this.filteredMessages.size()));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(scrollPane, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(totalUrlsLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(totalUrlsNumber)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(removeBtn)
                        .addGap(18, 18, 18)
                        .addComponent(resetUrlsBtn))
                    .addComponent(filterUrlsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {removeBtn, resetUrlsBtn});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(totalUrlsLabel)
                        .addComponent(totalUrlsNumber))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(removeBtn)
                        .addComponent(resetUrlsBtn)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(filterUrlsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {removeBtn, resetUrlsBtn});

    }// </editor-fold>//GEN-END:initComponents

    private void removeUrls(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeUrls
        int[] rows = table.getSelectedRows();
        Arrays.sort(rows);
        for (int i = rows.length - 1; i >= 0; i--) {
            int row = rows[i];
            int modelRow = table.convertRowIndexToModel(row);
            filterURLs(filteredMessages, modelRow);
            this.tableModel.removeRow(modelRow);
            urlsNumberUpdate(filteredMessages);
        }
        showFilterURLsNumbers();

    }//GEN-LAST:event_removeUrls

    private void resetUrls(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetUrls
  // Clear the table model to remove previous rows
    tableModel.setRowCount(0);
    
    // Reset the filteredMessages to the original list
    filteredMessages = new ArrayList<>(originalMessages);
    
    // Repopulate the table with the filtered messages
    showURLs(filteredMessages);
    
    // Update the URL count displayed
    urlsNumberUpdate(filteredMessages);
    
    // Reset the checkboxes
    jCheckBox1.setSelected(false);
    jCheckBox2.setSelected(false);
    jCheckBox3.setSelected(false);
    jCheckBox4.setSelected(false);
    jCheckBox5.setSelected(false);
    
    // Update any additional filter numbers if needed
    showFilterURLsNumbers();

    }//GEN-LAST:event_resetUrls

    private void jCheckBox5removeURLsRegex(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox5removeURLsRegex
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            removeURLsRegex();
        } else if (evt.getStateChange() == java.awt.event.ItemEvent.DESELECTED) {
            filteredMessages = new ArrayList<>(originalMessages);
        }

        tableModel.setNumRows(0);
        checkURLsCheckbox();
        showURLs(filteredMessages);
        urlsNumberUpdate(filteredMessages);
        showFilterURLsNumbers();
    }//GEN-LAST:event_jCheckBox5removeURLsRegex

    private void filterURLsNoParams(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_filterURLsNoParams
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            removeURLsNoParameters();

        } else if (evt.getStateChange() == java.awt.event.ItemEvent.DESELECTED) {
            filteredMessages = new ArrayList<>(originalMessages);
        }

        tableModel.setNumRows(0);
        checkURLsCheckbox();
        showURLs(filteredMessages);
        urlsNumberUpdate(filteredMessages);
        showFilterURLsNumbers();
    }//GEN-LAST:event_filterURLsNoParams

    private void removeURLsExtensions(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_removeURLsExtensions
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            removeURLsExtensions();
        } else if (evt.getStateChange() == java.awt.event.ItemEvent.DESELECTED) {
            filteredMessages = new ArrayList<>(originalMessages);
        }

        tableModel.setNumRows(0);
        checkURLsCheckbox();
        showURLs(filteredMessages);
        urlsNumberUpdate(filteredMessages);
        showFilterURLsNumbers();
    }//GEN-LAST:event_removeURLsExtensions

    private void extensionsTextInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_extensionsTextInputActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_extensionsTextInputActionPerformed

    private void removeOutScopeUrls(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_removeOutScopeUrls
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            removeOutOFScopeURLs();
        } else if (evt.getStateChange() == java.awt.event.ItemEvent.DESELECTED) {
            filteredMessages = new ArrayList<>(originalMessages);
        }

        tableModel.setNumRows(0);
        checkURLsCheckbox();
        showURLs(filteredMessages);
        urlsNumberUpdate(filteredMessages);
        showFilterURLsNumbers();
    }//GEN-LAST:event_removeOutScopeUrls

    private void filterURLEquals(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_filterURLEquals
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            removeURLsSameUrlAndParams();
        } else if (evt.getStateChange() == java.awt.event.ItemEvent.DESELECTED) {
            filteredMessages = new ArrayList<>(originalMessages);
        }

        tableModel.setNumRows(0);
        checkURLsCheckbox();
        showURLs(filteredMessages);
        urlsNumberUpdate(filteredMessages);
        showFilterURLsNumbers();
    }//GEN-LAST:event_filterURLEquals


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JLabel duplicateURLExtension;
    private javax.swing.JLabel duplicateURLNoParams;
    private javax.swing.JLabel duplicateURLNumbers;
    private javax.swing.JLabel duplicateURLOut;
    private javax.swing.JLabel duplicateURLRegex;
    private javax.swing.JTextField extensionsTextInput;
    private javax.swing.JPanel filterUrlsPanel;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JCheckBox jCheckBox5;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JTextField regexTextInput;
    private javax.swing.JButton removeBtn;
    private javax.swing.JButton resetUrlsBtn;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTable table;
    private javax.swing.JLabel totalUrlsLabel;
    private javax.swing.JLabel totalUrlsNumber;
    // End of variables declaration//GEN-END:variables

    private static class HttpRequestParams {

        private final String url;
        private final List<String> parameterNames;

        public HttpRequestParams(String url, List<String> parameterNames) {
            this.url = url;
            this.parameterNames = parameterNames;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            HttpRequestParams that = (HttpRequestParams) o;
            return Objects.equals(url, that.url) && Objects.equals(parameterNames, that.parameterNames);
        }

        @Override
        public int hashCode() {
            return Objects.hash(url, parameterNames);
        }
    }

    private static class MatchAndReplace {

        private final String type;
        private final String match;
        private final String replace;

        public MatchAndReplace(String type, String match, String replace) {
            this.type = type;
            this.match = match;
            this.replace = replace;
        }
    }
}
