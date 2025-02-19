package bountyprompt.gui;

import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.editor.EditorOptions;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;
import bountyprompt.BountyPrompt;

import javax.swing.*;

public class UrlsRequestResponse extends JPanel {
    public HttpRequestEditor requestViewer;
    public HttpResponseEditor responseViewer;
    public HttpRequestResponse currentlyDisplayedItem;

    public UrlsRequestResponse(BountyPrompt ext) {
        initComponents();
        requestViewer = ext.api.userInterface().createHttpRequestEditor(EditorOptions.READ_ONLY);
        responseViewer = ext.api.userInterface().createHttpResponseEditor(EditorOptions.READ_ONLY);

        request.add("Request", requestViewer.uiComponent());
        response.add("Response", responseViewer.uiComponent());
    }

    public void showRequestResponse(HttpRequestResponse requestResponse) {
        currentlyDisplayedItem = requestResponse;
        requestViewer.setRequest(requestResponse.request());
        responseViewer.setResponse(requestResponse.response());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        response = new javax.swing.JTabbedPane();
        request = new javax.swing.JTabbedPane();

        jSplitPane1.setResizeWeight(0.5);

        response.setSize(new java.awt.Dimension(0, 1));
        jSplitPane1.setRightComponent(response);

        request.setSize(new java.awt.Dimension(0, 1));
        jSplitPane1.setLeftComponent(request);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSplitPane jSplitPane1;
    public javax.swing.JTabbedPane request;
    public javax.swing.JTabbedPane response;
    // End of variables declaration//GEN-END:variables

}
