package bountyprompt.issue;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.Marker;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.scanner.audit.issues.AuditIssue;
import burp.api.montoya.scanner.audit.issues.AuditIssueConfidence;
import burp.api.montoya.scanner.audit.issues.AuditIssueSeverity;

import java.util.List;

import static burp.api.montoya.scanner.audit.issues.AuditIssue.auditIssue;

public class IssueSender {

    private final MontoyaApi api;

    /**
     * Constructor.
     *
     * @param api the Montoya API instance.
     */
    public IssueSender(MontoyaApi api) {
        this.api = api;
    }

    /**
     * Sends an issue to Burp Suite.
     * <p>
     * If only one HTTP request/response is provided, the issue will use the full URL (static host plus the endpoint);
     * if there are several, the issue will be created against the static host.
     *
     * @param issueName   the name/title of the issue.
     * @param issueDetail the detailed description of the issue (for example, the AI response).
     * @param remediation the remediation suggestion or background (can be empty).
     * @param responses   the list of HTTP request/response objects associated with the issue.
     * @param severity    the severity of the issue.
     * @param confidence  the confidence level of the issue.
     */
    public void sendIssue(String issueName, String issueDetail, String remediation,
                          List<HttpRequestResponse> responses,
                          AuditIssueSeverity severity, AuditIssueConfidence confidence) {

        // Static host to be used in the sitemap.
        final String staticHost = "http://bountyprompt_extension.com";

        // If only one request is provided, the issue will use the request path appended to the static host;
        // if there are multiple requests, only the static host will be used.
        String baseUrl = staticHost;

        // Create the AuditIssue using the static auditIssue(…) method.
        AuditIssue issue = auditIssue(
                "Bounty Prompt - " + issueName,
                issueDetail,
                null, // Remediation (can be null or empty)
                baseUrl,
                severity,
                confidence,
                null,      // Optional: references (for example, a documentation URL)
                null,      // Optional: remediation references
                severity,  // Some examples repeat the severity for an extra field
                responses.get(0).withResponseMarkers(new Marker[0]) // Adjust as needed for response markers
        );

        // Add the issue to Burp Suite's Site Map.
        api.siteMap().add(issue);
    }
}
