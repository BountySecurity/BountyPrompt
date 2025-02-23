package bountyprompt.ai;

import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.scanner.audit.issues.AuditIssueConfidence;
import burp.api.montoya.scanner.audit.issues.AuditIssueSeverity;
import bountyprompt.gui.MainGui;
import bountyprompt.gui.PromptPanel;
import bountyprompt.issue.IssueSender;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.logging.Logging;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import java.util.concurrent.ExecutorService;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import java.io.StringReader;

/**
 * GroqPromptExecutor is a class that facilitates executing prompts on the Groq
 * platform. It is used in a similar way as AiPromptExecutor.
 */
public class GroqPromptExecutor {

    private final IGroqApiClient groqApiClient;
    private final String model;
    private final Logging logging;
    private final MainGui mainGui;
    private final ExecutorService executorService;
    private final IssueSender issueSender;

    /**
     * Constructor that initializes the Groq API client, Logging, MainGui,
     * ExecutorService, and IssueSender.
     *
     * @param groqApiClient an implementation of IGroqApiClient (e.g.,
     * GroqApiClientImpl)
     * @param model the identifier of the model to be used (e.g., "groq-model")
     * @param logging the Logging instance for output and error logging
     * @param mainGui the MainGui instance containing the prompts output area
     * @param executorService an ExecutorService to run Groq tasks
     * asynchronously
     */
    public GroqPromptExecutor(IGroqApiClient groqApiClient, String model, Logging logging, MainGui mainGui, ExecutorService executorService, MontoyaApi api) {
        this.groqApiClient = groqApiClient;
        this.model = model;
        this.logging = logging;
        this.mainGui = mainGui;
        this.executorService = executorService;
        this.issueSender = new IssueSender(api);
    }

    /**
     * Executes the prompt asynchronously using Groq. Combines the system
     * prompt, user prompt, and extra content into a single message, sends the
     * request via the Groq API, and updates the GUI based on the response.
     *
     * @param remaining a list of HttpRequestResponse objects selected by the
     * user
     * @param prompt the prompt configuration (contains userPrompt,
     * systemPrompt, outputType, etc.)
     * @param extraContent additional content generated from the selected
     * messages
     */
    public void executePromptAsync(List<HttpRequestResponse> remaining, PromptPanel.Prompt prompt, String extraContent) {
        executorService.submit(() -> {
            // Validate that the prompt fields are not null or empty
            if (prompt.systemPrompt == null || prompt.systemPrompt.trim().isEmpty()) {
                logging.logToError("Error: System prompt is missing or empty.");
                return;
            }
            if (prompt.userPrompt == null || prompt.userPrompt.trim().isEmpty()) {
                logging.logToError("Error: User prompt is missing or empty.");
                return;
            }
            try {
                // Build the full prompt combining system and user prompts
                String fullPrompt = "System: " + prompt.systemPrompt + "\n\nUser: " + prompt.userPrompt;
                if (extraContent != null && !extraContent.trim().isEmpty()) {
                    fullPrompt += "\n\n" + extraContent;
                }
                // Remove any tags of the form [HTTP_...]
                fullPrompt = fullPrompt.replaceAll("\\[HTTP_[^\\]]+\\]", "").trim();

                mainGui.appendToPromptsOutput("Sending prompt to Groq: \n\n Prompt (before changes): \n\nSystem: " + prompt.systemPrompt + "\n\nUser: " + prompt.userPrompt + "\n");
                //logging.logToOutput("Sending prompt to Groq: " + fullPrompt);

                // Execute the prompt via Groq API and subscribe to the response
                executePrompt(fullPrompt).subscribe(response -> {
                    String cleanedResponse = response.replaceAll("(?s)<think>.*?</think>", "").trim();
                    SwingUtilities.invokeLater(() -> {
                        try {
                            if (cleanedResponse != null && !cleanedResponse.trim().isEmpty() && !cleanedResponse.contains("NONE")) {
                                if ("Issue".equalsIgnoreCase(prompt.outputType)) {
                                    String responseHTML = convertMarkdownToHtml(cleanedResponse);
                                    AuditIssueSeverity severity = AuditIssueSeverity.valueOf(prompt.severity.toUpperCase());
                                    AuditIssueConfidence confidence = AuditIssueConfidence.valueOf(prompt.confidence.toUpperCase());
                                    issueSender.sendIssue(prompt.title, responseHTML, null, remaining, severity, confidence);
                                    mainGui.appendResponse(prompt.title, responseHTML);
                                } else {
                                    mainGui.appendResponse(prompt.title, cleanedResponse);
                                }
                            } else {
                                mainGui.appendToPromptsOutput("Groq response is empty or NONE. No issue created.");
                            }
                        } catch (Exception ex) {
                            logging.logToError("Error in SwingUtilities.invokeLater: " + ex.getMessage(), ex);
                        }
                    });
                }, error -> {
                    logging.logToError("Error executing Groq prompt: " + error.getMessage(), error);
                    SwingUtilities.invokeLater(() -> {
                        mainGui.appendToPromptsOutput("Error executing Groq prompt: " + error.getMessage());
                        JOptionPane.showMessageDialog(mainGui, "Error executing Groq prompt: " + error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    });
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    mainGui.appendToPromptsOutput("Unexpected error during Groq prompt execution: " + e.getMessage());
                });
                logging.logToError("Unexpected error during Groq prompt execution: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Executes the prompt asynchronously, expecting a complete response.
     *
     * @param promptText the input message for the assistant
     * @return a Single that emits the response (content) returned by the
     * assistant
     */
    public Single<String> executePrompt(String promptText) {
        JsonObject request = buildRequest(promptText);
        return groqApiClient.createChatCompletionAsync(request)
                .map(response -> {
                    String responseStr = response.toString();
                    if (responseStr.contains("error")) {
                        logging.logToError("Raw response: " + responseStr);
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(mainGui,
                                    "Error executing Groq prompt: " + responseStr,
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        });
                    }
                    return extractContent(response);
                });
    }

    /**
     * Builds the JSON request for the API from the prompt.
     *
     * @param promptText the input message
     * @return a JsonObject representing the request
     */
    private JsonObject buildRequest(String promptText) {
        JsonArrayBuilder messagesBuilder = Json.createArrayBuilder()
                .add(Json.createObjectBuilder()
                        .add("role", "user")
                        .add("content", promptText));
        JsonObjectBuilder requestBuilder = Json.createObjectBuilder()
                .add("model", model)
                .add("messages", messagesBuilder);
        return requestBuilder.build();
    }

    /**
     * Extracts the textual content from the complete response.
     *
     * @param response the JSON response object
     * @return the content of the response, or an empty string if not found
     */
    private String extractContent(JsonObject response) {
        try {
            return response.getJsonArray("choices")
                    .getJsonObject(0)
                    .getJsonObject("message")
                    .getString("content");
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Converts Markdown text to HTML using Flexmark.
     *
     * @param markdown the Markdown input text
     * @return the HTML representation of the text
     */
    public String convertMarkdownToHtml(String markdown) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }
}
