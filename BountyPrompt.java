package bountyprompt;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.extension.ExtensionUnloadingHandler;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.persistence.PersistedObject;
import burp.api.montoya.sitemap.SiteMapFilter;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import burp.api.montoya.ui.contextmenu.InvocationType;
import burp.api.montoya.EnhancedCapability;
import bountyprompt.ai.AiPromptExecutor;
import bountyprompt.gui.MainGui;
import bountyprompt.gui.PromptPanel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import java.io.File;
import java.io.StringReader;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static burp.api.montoya.EnhancedCapability.AI_FEATURES;
import bountyprompt.gui.FilterURLs;
import java.awt.Component;

public class BountyPrompt implements BurpExtension, ExtensionUnloadingHandler, ContextMenuItemsProvider {

    // Helpers & callbacks
    public MontoyaApi api;
    public Logging logging;

    public MainGui gui;
    PersistedObject BBAIData;
    String filename;

    private AiPromptExecutor aiExecutor;
    private ExecutorService executorService;

    public static final String EXTENSION_NAME = "Bounty Prompt";

    // Callbacks
    private final ArrayList<Runnable> unloadCallbacks = new ArrayList<>();

    @Override
    public void initialize(MontoyaApi api) {
        // Set the internals
        this.api = api;
        this.logging = api.logging();

        //Add the static host to the extension scope
        final String staticHost = "http://bountyprompt_extension.com";
        api.scope().includeInScope(staticHost);

        BBAIData = api.persistence().extensionData();
        filename = BBAIData.getString("FILENAME");

        if (filename == null) {
            // Get the current working directory of the extension
            String workingDir = System.getProperty("user.home");
            // Build the prompts directory path
            filename = workingDir + File.separator + "prompts";
            // Create the directory if it doesn't exist
            File promptsDir = new File(filename);
            if (!promptsDir.exists()) {
                promptsDir.mkdirs();
            }
        }

        // Se the extension name
        api.extension().setName(EXTENSION_NAME);

        // Register the extension as a context menu items provider
        api.userInterface().registerContextMenuItemsProvider(this);

        // Register the extension as an unload handler
        api.extension().registerUnloadingHandler(this);

        SwingUtilities.invokeLater(() -> {
            gui = new MainGui(BountyPrompt.this, BBAIData, filename);
            api.userInterface().registerSuiteTab(EXTENSION_NAME, gui);
            executorService = Executors.newFixedThreadPool(5);
            aiExecutor = new AiPromptExecutor(api, api.ai(), logging, gui, executorService);

            // Print the welcome message
            logging.logToOutput("Bounty Prompt v1.0.0");
            logging.logToOutput("A solution from Bounty Security S.L (https://bountysecurity.ai)");
            logging.logToOutput("In case you find a bug, write us to hello@bountysecurity.ai");
            logging.logToOutput("");
        });
    }

    @Override
    public Set<EnhancedCapability> enhancedCapabilities() {
        return Set.of(AI_FEATURES);
    }

    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        JMenu promptsMenu = new JMenu("Prompts");
        if (gui != null) {
            List<PromptPanel.Prompt> prompts = gui.getPrompts();
            if (prompts.isEmpty()) {
                // If there are no prompts, display the "Configure prompts directory" option
                JMenuItem configureItem = new JMenuItem("Configure prompts directory");
                configureItem.addActionListener(e -> {
                    gui.selectExtensionTab();
                    gui.selectConfigTab();
                });
                promptsMenu.add(configureItem);
            } else {
                for (PromptPanel.Prompt prompt : prompts) {
                    JMenuItem item = new JMenuItem(prompt.title);
                    item.addActionListener(e -> {
                        // Retrieve the selected HttpRequestResponse objects from the event.
                        HttpRequestResponse[] messages = getSelectedMessages(event);
                        List<HttpRequestResponse> requestResponses = Arrays.asList(messages);
                        // Display the URL filter popup (using the FilterURLs panel) and get the filtered list.
                        List<HttpRequestResponse> remaining = FilterURLs.showURLFilterPopup(gui, requestResponses);
                        // If the user pressed OK and there are filtered objects, proceed to generate the extra content.
                        if (remaining != null && !remaining.isEmpty()) {
                            String extraContent = getFilteredContentFromList(remaining, prompt.userPrompt);
                            aiExecutor.executePromptAsync(remaining, prompt, extraContent);
                        }
                    });
                    promptsMenu.add(item);
                }
            }
        }
        return new ArrayList<>(Arrays.asList(promptsMenu));
    }

    private String sanitizeURL(String url) {
        return url.replaceAll("[\\x00-\\x20<>\"{}|\\\\^`]", "");
    }

    /**
     * Obtains and formats the request/response content from the selected
     * messages. This method uses the existing
     * getSelectedMessages(ContextMenuEvent event) method.
     *
     * @param event the ContextMenuEvent containing the selected messages.
     * @return a formatted string with the request and response details.
     */
    public String getRequestResponseContent(ContextMenuEvent event) {
        HttpRequestResponse[] messages = getSelectedMessages(event);
        StringBuilder sb = new StringBuilder();

        for (HttpRequestResponse message : messages) {
            sb.append("Request:\n");
            sb.append(message.request().toString()).append("\n");

            if (message.response() != null) {
                sb.append("Response:\n");
                sb.append(message.response().toString()).append("\n");
            }

            sb.append("----------------------------------------------------------------\n\n");
        }

        if (sb.length() == 0) {
            return "No request/response items found for the selected context.";
        }

        return sb.toString();
    }

    // It returns a list of requests (that sometimes come with responses) for the given event.
    // These requests and responses may come from:
    // - The site map tree: so it contains all the registered ones for that site.
    // - A single pair of request and response that the user selected.
    // - A single pair of request and response that comes from the message editor.
    // In any case, both request (and response, if exists) are returned.
    // It may also return an empty list if the event has no requests/responses linked.
    private HttpRequestResponse[] getSelectedMessages(ContextMenuEvent event) {
        // If there's no list of request nor a request from the message editor, we return an empty array
        if (event.selectedRequestResponses().isEmpty()
                && (event.messageEditorRequestResponse().isEmpty()
                || event.messageEditorRequestResponse().get().requestResponse() == null
                || event.messageEditorRequestResponse().get().requestResponse().request() == null)) {
            return new HttpRequestResponse[0];
        }

        if (event.invocationType().containsHttpRequestResponses()) {
            // For now, if the user triggers from the context menu, we return the whole site map
            if (event.isFrom(InvocationType.SITE_MAP_TREE)) {
                HashSet<String> hosts = new HashSet<>();
                ArrayList<HttpRequestResponse> messages = new ArrayList<>();
                for (HttpRequestResponse base : event.selectedRequestResponses()) {
                    if (base.httpService() == null) {
                        continue;
                    }

                    String host = base.httpService().host();
                    if (hosts.contains(host)) {
                        continue;
                    }

                    String protocol = "http";
                    if (base.httpService().secure()) {
                        protocol = "https";
                    }

                    messages.addAll(api.siteMap().requestResponses(SiteMapFilter.prefixFilter(protocol + "://" + host)));
                    hosts.add(host);
                }

                return messages.toArray(new HttpRequestResponse[0]);
            }
            // Otherwise, we just return the selected messages
            return event.selectedRequestResponses().toArray(new HttpRequestResponse[0]);
        }

        // Finally, if the invocation does not containsHttpRequestResponses, we return
        // the request from the message editor.
        return new HttpRequestResponse[]{event.messageEditorRequestResponse().get().requestResponse()};
    }

    /**
     * Processes a list of HttpRequestResponse objects and returns a formatted
     * string based on the tags present in the userPrompt.
     *
     * @param responses the list of remaining HttpRequestResponse objects.
     * @param userPrompt the user prompt that may include tags.
     * @return a formatted string containing only the requested parts of the
     * HTTP messages.
     */
    private String getFilteredContentFromList(List<HttpRequestResponse> responses, String userPrompt) {
        StringBuilder sb = new StringBuilder();
        for (HttpRequestResponse message : responses) {
            if (userPrompt.contains("[HTTP_Requests]")) {
                sb.append("Full Request:\n")
                        .append(message.request().toString()).append("\n");
            }
            if (userPrompt.contains("[HTTP_Requests_Headers]")) {
                sb.append("Request Headers:\n")
                        .append(getRequestHeaders(message)).append("\n");
            }
            if (userPrompt.contains("[HTTP_Requests_Parameters]")) {
                // Se añade primero la URL completa
                sb.append("Request URL:\n")
                        .append(message.request().url()).append("\n");
                // A continuación se añaden los parámetros extraídos
                sb.append("Request Parameters:\n")
                        .append(getRequestParameters(message)).append("\n");
            }
            if (userPrompt.contains("[HTTP_Request_Body]")) {
                sb.append("Request Body:\n")
                        .append(getRequestBody(message)).append("\n");
            }
            if (userPrompt.contains("[HTTP_Responses]") && message.response() != null) {
                sb.append("Full Response:\n")
                        .append(message.response().toString()).append("\n");
            }
            if (userPrompt.contains("[HTTP_Response_Headers]") && message.response() != null) {
                sb.append("Response Headers:\n")
                        .append(getResponseHeaders(message)).append("\n");
            }
            if (userPrompt.contains("[HTTP_Response_Body]") && message.response() != null) {
                sb.append("Response Body:\n")
                        .append(getResponseBody(message)).append("\n");
            }
            if (userPrompt.contains("[HTTP_Status_Code]") && message.response() != null) {
                sb.append("Status Code:\n")
                        .append(getStatusCode(message)).append("\n");
            }
            if (userPrompt.contains("[HTTP_Cookies]")) {
                sb.append("Cookies:\n")
                        .append(getCookies(message)).append("\n");
            }
            sb.append("----------------------------------------------------------------\n\n");
        }
        if (sb.length() == 0) {
            return "No request/response items found for the selected context.";
        }
        return sb.toString();
    }

    /**
     * Extracts the request headers from the HTTP request. It searches for the
     * header-body separator (either "\r\n\r\n" or "\n\n") and returns the part
     * anterior.
     */
    private String getRequestHeaders(HttpRequestResponse message) {
        String request = message.request().toString();
        int index = request.indexOf("\r\n\r\n");
        if (index == -1) {
            index = request.indexOf("\n\n");
        }
        return (index != -1) ? request.substring(0, index) : request;
    }

    private String getRequestParameters(HttpRequestResponse message) {
        String method = message.request().method();
        String urlString = message.request().url();
        URI uri;
        try {
            // Sanitizing the URL to remove illegal characters.
            urlString = sanitizeURL(urlString);
            uri = URI.create(urlString);
        } catch (IllegalArgumentException e) {
            // If the URL remains invalid after sanitization, return a default message.
            return "No parameters found due to invalid URL";
        }

        if ("GET".equalsIgnoreCase(method)) {
            String query = uri.getQuery();
            return (query != null && !query.isEmpty()) ? parseQueryString(query) : "No parameters found.";
        } else {
            String contentType = null;
            for (HttpHeader header : message.request().headers()) {
                if ("content-type".equalsIgnoreCase(header.name())) {
                    contentType = header.value().trim().toLowerCase();
                    break;
                }
            }
            String body = getRequestBody(message);
            if (contentType != null) {
                if (contentType.contains("application/x-www-form-urlencoded")) {
                    return (body != null && !body.isEmpty()) ? parseQueryString(body) : "No parameters found.";
                } else if (contentType.contains("application/json")) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode root = mapper.readTree(body);
                        List<String> pairs = new ArrayList<>();
                        flattenJson(root, "", pairs);
                        return pairs.isEmpty() ? "No parameters found." : String.join("\n", pairs);
                    } catch (Exception e) {
                        return "Unable to parse JSON parameters.";
                    }
                } else if (contentType.contains("xml")) {
                    try {
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        Document doc = builder.parse(new InputSource(new StringReader(body)));
                        NodeList nodes = doc.getElementsByTagName("*");
                        Set<String> pairs = new LinkedHashSet<>();
                        for (int i = 0; i < nodes.getLength(); i++) {
                            String nodeName = nodes.item(i).getNodeName();
                            String text = nodes.item(i).getTextContent().trim();
                            if (!text.isEmpty()) {
                                pairs.add(nodeName + ": " + text);
                            }
                        }
                        return pairs.isEmpty() ? "No parameters found." : String.join("\n", pairs);
                    } catch (Exception e) {
                        return "Unable to parse XML parameters.";
                    }
                } else {
                    return (body != null && !body.isEmpty()) ? body : "No parameters found.";
                }
            }
            return (body != null && !body.isEmpty()) ? body : "No parameters found.";
        }
    }

    private String parseQueryString(String query) {
        StringBuilder sb = new StringBuilder();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            String key = kv[0];
            String value = (kv.length > 1) ? kv[1] : "";
            key = URLDecoder.decode(key, StandardCharsets.UTF_8);
            value = URLDecoder.decode(value, StandardCharsets.UTF_8);
            sb.append(key).append(": ").append(value).append("\n");
        }
        return sb.toString().trim();
    }

    /**
     * Recursively collects all keys and values from a JSON object and adds them
     * to the list. If the node is an object, iterates over each field; if it is
     * an array, iterates over its elements. For primitive values, adds the
     * accumulated key (prefix) and the value.
     */
    private void flattenJson(JsonNode node, String prefix, List<String> pairs) {
        if (node.isObject()) {
            Iterator<String> fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode child = node.get(fieldName);
                String newPrefix = prefix.isEmpty() ? fieldName : prefix + "." + fieldName;
                flattenJson(child, newPrefix, pairs);
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                flattenJson(node.get(i), prefix + "[" + i + "]", pairs);
            }
        } else {
            pairs.add(prefix + ": " + node.asText());
        }
    }

    private String getRequestBody(HttpRequestResponse message) {
        byte[] bodyBytes = message.request().body().getBytes();
        return new String(bodyBytes, StandardCharsets.UTF_8);
    }

    private void collectJsonKeys(JsonNode node, List<String> keys) {
        if (node.isObject()) {
            Iterator<String> fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                keys.add(fieldName);
                collectJsonKeys(node.get(fieldName), keys);
            }
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                collectJsonKeys(item, keys);
            }
        }
    }

    /**
     * Extracts the response headers from the HTTP response.
     */
    private String getResponseHeaders(HttpRequestResponse message) {
        if (message.response() == null) {
            return "";
        }
        String response = message.response().toString();
        int index = response.indexOf("\r\n\r\n");
        if (index == -1) {
            index = response.indexOf("\n\n");
        }
        return (index != -1) ? response.substring(0, index) : response;
    }

    /**
     * Extracts the response body from the HTTP response.
     */
    private String getResponseBody(HttpRequestResponse message) {
        if (message.response() == null) {
            return "";
        }
        String response = message.response().toString();
        int index = response.indexOf("\r\n\r\n");
        if (index == -1) {
            index = response.indexOf("\n\n");
        }
        return (index != -1 && index + 4 < response.length()) ? response.substring(index + 4) : "";
    }

    /**
     * Extracts the status code from the HTTP response. It assumes that the
     * first line of the response is similar to "HTTP/1.1 200 OK".
     */
    private String getStatusCode(HttpRequestResponse message) {
        if (message.response() == null) {
            return "";
        }
        String response = message.response().toString();
        int firstLineEnd = response.indexOf("\r\n");
        if (firstLineEnd == -1) {
            firstLineEnd = response.indexOf("\n");
        }
        if (firstLineEnd != -1) {
            String firstLine = response.substring(0, firstLineEnd);
            String[] parts = firstLine.split(" ");
            if (parts.length >= 2) {
                return parts[1];
            }
        }
        return "";
    }

    /**
     * Extracts cookies from both the request and response headers. It searches
     * for lines starting with "Cookie:" in the request and "Set-Cookie:" in the
     * response.
     */
    private String getCookies(HttpRequestResponse message) {
        StringBuilder cookies = new StringBuilder();

        // Request cookies
        String reqHeaders = getRequestHeaders(message);
        String[] reqLines = reqHeaders.split("\\r?\\n");
        for (String line : reqLines) {
            if (line.toLowerCase().startsWith("cookie:")) {
                cookies.append(line).append("\n");
            }
        }

        // Response cookies
        if (message.response() != null) {
            String resHeaders = getResponseHeaders(message);
            String[] resLines = resHeaders.split("\\r?\\n");
            for (String line : resLines) {
                if (line.toLowerCase().startsWith("set-cookie:")) {
                    cookies.append(line).append("\n");
                }
            }
        }

        return cookies.length() > 0 ? cookies.toString() : "No cookies found.";
    }

    @Override
    public void extensionUnloaded() {
        this.logging.logToOutput("");
        this.logging.logToOutput("Unloading extension...");

        for (Runnable callback : this.unloadCallbacks) {
            try {
                callback.run();
            } catch (Exception ex) {
                String errMsg = String.format("Extension unload error: %s", ex.getMessage());
                this.logging.logToError(errMsg);
            }
        }
        BBAIData.setString("FILENAME", gui.promptsDirectory.getText());

        this.logging.logToOutput("Extension unloaded!");
    }

    public void registerUnloadCallback(Runnable callback) {
        this.unloadCallbacks.add(callback);
    }
}
