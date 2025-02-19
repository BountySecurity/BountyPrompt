package bountyprompt.ai;

import burp.api.montoya.ai.Ai;
import burp.api.montoya.ai.chat.Message;
import burp.api.montoya.ai.chat.PromptResponse;
import burp.api.montoya.ai.chat.PromptException;
import burp.api.montoya.logging.Logging;
import bountyprompt.gui.MainGui;
import bountyprompt.gui.PromptPanel;
import burp.api.montoya.MontoyaApi;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import static burp.api.montoya.ai.chat.Message.systemMessage;
import static burp.api.montoya.ai.chat.Message.userMessage;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.scanner.audit.issues.AuditIssueConfidence;
import burp.api.montoya.scanner.audit.issues.AuditIssueSeverity;
import bountyprompt.issue.IssueSender;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class AiPromptExecutor {

    private final Ai ai;
    private final Logging logging;
    private final MainGui mainGui;
    private final ExecutorService executorService;
    private final IssueSender issueSender;
    private final MontoyaApi api;

    /**
     * Constructor that initializes the AI, Logging, MainGui, ExecutorService,
     * and PersistedObject instances.
     *
     * @param ai the Montoya AI instance
     * @param logging the Logging instance for output and error logging
     * @param mainGui the MainGui instance containing the promptsOutput text
     * area
     * @param executorService an ExecutorService to run AI tasks asynchronously
     */
    public AiPromptExecutor(MontoyaApi api, Ai ai, Logging logging, MainGui mainGui, ExecutorService executorService) {
        this.ai = ai;
        this.logging = logging;
        this.mainGui = mainGui;
        this.executorService = executorService;
        this.api = api;
        this.issueSender = new IssueSender(this.api);

    }

    public void executePromptAsync(List<HttpRequestResponse> remaining, PromptPanel.Prompt prompt, String extraContent) {
        executorService.submit(() -> {
            // Check if AI functionality is enabled
            if (!ai.isEnabled()) {
                String notEnabledMsg = "AI is not enabled. Please enable AI in Burp Extensions.";
                logging.logToOutput(notEnabledMsg);
                SwingUtilities.invokeLater(()
                        -> JOptionPane.showMessageDialog(mainGui, notEnabledMsg, "Error", JOptionPane.ERROR_MESSAGE));
                return;
            }

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
                // Build system message
                Message systemMsg = systemMessage(prompt.systemPrompt);

                // Append extraContent (for example, obtained from the remaining request/responses) to the user prompt
                String fullUserPrompt = prompt.userPrompt;
                if (extraContent != null && !extraContent.trim().isEmpty()) {
                    fullUserPrompt += "\n\n" + extraContent;
                }

                // Remove any tags of the form [HTTP_...] from the full user prompt
                fullUserPrompt = fullUserPrompt.replaceAll("\\[HTTP_[^\\]]+\\]", "").trim();

                // Build user message
                Message userMsg = userMessage(fullUserPrompt);

                mainGui.appendToPromptsOutput("Sending prompt to AI: executing system and user messages.\n\nUser Prompt (before changes): \n\n" + prompt.userPrompt + "\n");
                PromptResponse response = ai.prompt().execute(systemMsg, userMsg);
                String aiOutput = response.content();

                if (aiOutput != null && !aiOutput.trim().isEmpty() && !aiOutput.trim().contains("NONE")) {
                    if ("Issue".equalsIgnoreCase(prompt.outputType)) {
                        String aiOutputHTML = convertMarkdownToHtml(aiOutput);
                        AuditIssueSeverity severity = AuditIssueSeverity.valueOf(prompt.severity.toUpperCase());
                        AuditIssueConfidence confidence = AuditIssueConfidence.valueOf(prompt.confidence.toUpperCase());
                        issueSender.sendIssue(prompt.title, aiOutputHTML, null, remaining, severity, confidence);
                    }
                    mainGui.appendResponse(prompt.title, aiOutput);
                } else {
                    mainGui.appendToPromptsOutput("AI response is empty or NONE. No issue created.");
                }
            } catch (PromptException e) {
                String errorMsg = e.getMessage().toLowerCase(Locale.US);
                if (errorMsg.contains("not enough credits")) {
                    String creditsMsg = "Not enough AI credits. Please increase your credit balance.";
                    logging.logToOutput(creditsMsg);
                    SwingUtilities.invokeLater(()
                            -> JOptionPane.showMessageDialog(mainGui, creditsMsg, "Error", JOptionPane.ERROR_MESSAGE));
                } else {
                    mainGui.appendToPromptsOutput("Error executing prompt: " + e.getMessage());
                    logging.logToError("Error executing prompt: " + e.getMessage(), e);
                }
            } catch (Exception e) {
                mainGui.appendToPromptsOutput("Unexpected error during AI prompt execution: " + e.getMessage());
                logging.logToError("Unexpected error during AI prompt execution: " + e.getMessage(), e);
            }
        });
    }

    public String convertMarkdownToHtml(String markdown) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }

}
