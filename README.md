Bounty Prompt Extension
======================

Bounty Prompt Extension form Bounty Security. 

This extension leverages AI to generate security testing prompts for web penetration testing. It supports various HTTP tags to include specific parts of HTTP requests/responses in the prompts. Issues created by the extension are assigned to the static host [http://bountyprompt_extension.com/](http://bountyprompt_extension.com/), as many issues are not limited to a single domain or endpoint.

Table of Contents
-----------------

*   [1\. Open Burp Suite](#open-burp-suite)
*   [2\. Load the Extension](#load-extension)
*   [3\. Launch the Extension](#launch-extension)
*   [4\. Configuration](#configuration)
*   [5\. AI Prompts](#ai-prompts)
*   [6\. Supported HTTP Tags](#supported-http-tags)
*   [7\. Usage](#usage)
*   [8\. Installation and Execution Example](#installation-and-execution)
*   [9\. Example Prompts](#example-prompts)
*   [10\. Contributing](#contributing)
*   [11\. License](#license)
*   [12\. Contact](#contact)

1\. Open Burp Suite
-------------------

Start Burp Suite Community or Professional Edition.

2\. Load the Extension
----------------------

1.  Navigate to **Extender > Extensions**.
2.  Click **Add**.
3.  Select **Extension Type: Java**.
4.  Click **Select file...** and choose the compiled JAR file from the repository (or the directory containing your compiled classes).
5.  Click **Next** and wait for the extension to load. You should see the extension named **Bounty Prompt Extension** in the list.

3\. Launch the Extension
------------------------

1.  Once loaded, a new tab (e.g., **Bounty Prompt**) will appear in Burp Suite.
2.  The extension’s output tab will display welcome messages.

4\. Configuration
-----------------

The extension allows you to configure AI prompts. Each prompt consists of the following fields:

*   **Title:** The title of the prompt.
*   **Author:** Your identifier (e.g., `@bountysecurity`).
*   **Output Type:** Choose between _Issue_ or _Prompt Output_.
*   **Severity:** For issues – options include _Information_, _Low_, _Medium_, and _High_.
*   **Confidence:** For issues – options include _Certain_, _Firm_, and _Tentative_.
*   **System Prompt:** Instructions for the AI to define its role (e.g., "You are a web security expert specialized in SQL injection analysis...").
*   **User Prompt:** The question or task for the AI. This field may include special tags (see Supported HTTP Tags).

5\. AI Prompts
--------------

Configure your prompts in the extension UI. For issue-related prompts, set the Output Type to _Issue_ and select the desired Severity and Confidence levels. For prompts that display output, set the Output Type to _Prompt Output_.

6\. Supported HTTP Tags
-----------------------

You can include the following tags in the User Prompt to automatically insert specific HTTP properties into your prompt:

*   **\[HTTP\_Requests\]:** Includes the complete HTTP request text.
*   **\[HTTP\_Requests\_Headers\]:** Includes only the HTTP request headers.
*   **\[HTTP\_Requests\_Parameters\]:** Includes the query string parameters from the HTTP request URL.
*   **\[HTTP\_Request\_Body\]:** Includes the HTTP request body (if available).
*   **\[HTTP\_Responses\]:** Includes the complete HTTP response text.
*   **\[HTTP\_Response\_Headers\]:** Includes only the HTTP response headers.
*   **\[HTTP\_Response\_Body\]:** Includes the HTTP response body.
*   **\[HTTP\_Status\_Code\]:** Includes the HTTP response status code (e.g., 200, 404).
*   **\[HTTP\_Cookies\]:** Includes cookies extracted from the HTTP request or response.

7\. Usage
---------

1.  **Configure Prompts:** In the extension UI, add or edit prompts with the required fields.
2.  **Select Requests/Responses:** In Burp Suite, select one or more HTTP requests/responses from the Site Map or via the context menu.
3.  **Trigger a Prompt:**
    *   Right-click to open the context menu and choose the **Prompts** menu.
    *   Select a prompt to send the selected HTTP data to the AI.
4.  **Issue Creation:** If the prompt’s Output Type is set to _Issue_, an issue will be created in Burp Suite against the static host [http://bountyprompt\_extension.com/](http://bountyprompt_extension.com/). For a single request, the issue will include the full endpoint; for multiple requests, only the domain (host) is used.
5.  **Review Results:**
    *   For _Prompt Output_ type, review the AI responses in the extension’s output area.
    *   For _Issue_ type, review the created issues in the Burp Suite Issues tab.

8\. Installation and Execution Example
--------------------------------------

### Compile the Extension

If using Maven, run:

    mvn clean package

This will generate a JAR file in the `target/` directory.

### Load the Extension in Burp Suite

1.  Open Burp Suite.
2.  Go to **Extender > Extensions**.
3.  Click **Add**.
4.  Choose **Java** as the extension type.
5.  Select the built JAR file.
6.  Click **Next** and wait until the extension loads.

### Launch the Extension

1.  Once loaded, a new tab (e.g., **BountyPrompt**) will appear.
2.  The extension’s output tab will display welcome messages.

9\. Example Prompts
-------------------

Below are some sample prompts you can use or modify:

### SQL Injection & Other Attack Parameters Issue

*   **Title:** SQL Injection & Other Attack Parameters Issue
*   **Author:** @bountysecurity
*   **Output Type:** Issue
*   **Severity:** High
*   **Confidence:** Firm
*   **System Prompt:**
    
        You are a web security expert specialized in vulnerability analysis. Analyze the provided HTTP requests and identify parameters that may be vulnerable to SQL injection, XSS, command injection, and other attacks. For each potential vulnerability, list the URL and the vulnerable parameter(s). If none are found, respond with "NONE".
    
*   **User Prompt:**
    
        Please analyze the following HTTP request parameters for potential vulnerabilities. Output only the URL and the vulnerable parameter names in the following format:
        
        These are the URLs and parameters potentially vulnerable:
        - URL
          - Parameter1
          - Parameter2
        [HTTP_Requests_Parameters]
    

### Sensitive Information Disclosure Issue

*   **Title:** Sensitive Information Disclosure Issue
*   **Author:** @bountysecurity
*   **Output Type:** Issue
*   **Severity:** Medium
*   **Confidence:** Tentative
*   **System Prompt:**
    
        You are a web security expert focused on identifying sensitive information disclosures. Analyze the provided HTTP responses for accidental exposure of sensitive data such as internal IPs, version numbers, and configuration details. For each finding, output the URL and the line number where the sensitive information appears. If no sensitive information is found, respond with "NONE".
    
*   **User Prompt:**
    
        Analyze the following HTTP responses for any sensitive information.
        [HTTP_Responses]
    

### Malicious Input Reflection Analysis

*   **Title:** Malicious Input Reflection Analysis
*   **Author:** @bountysecurity
*   **Output Type:** Issue
*   **Severity:** High
*   **Confidence:** Certain
*   **System Prompt:**
    
        You are a web security expert specializing in reflection vulnerabilities. Examine the provided HTTP responses to determine if any user-supplied input is being directly reflected without proper sanitization. For each reflection, output the URL and the exact line number where it occurs. If no reflections are detected, respond with "NONE".
    
*   **User Prompt:**
    
        Examine the following HTTP responses for reflected user input vulnerabilities.
        [HTTP_Responses]
    

### Information Disclosure in Headers

*   **Title:** Information Disclosure in Headers
*   **Author:** @bountysecurity
*   **Output Type:** Issue
*   **Severity:** Low
*   **Confidence:** Tentative
*   **System Prompt:**
    
        You are a web security expert tasked with detecting information disclosure in HTTP response headers. Analyze the provided headers for any unintended exposure of server details such as version numbers, internal IP addresses, or other configuration data. For each finding, output the URL and the header line that contains the sensitive information. If no such disclosure is found, respond with "NONE".
    
*   **User Prompt:**
    
        Analyze the following HTTP response headers for sensitive information.
        [HTTP_Response_Headers]
    

### Additional Prompts

*   **API Key Exposure Check:** Analyze HTTP responses to identify any exposed API keys, secrets, or tokens.
*   **Endpoint Discovery:** Extract all unique endpoints from the provided HTTP responses, including any parameters if present.
*   **Malicious Input Reflection Analysis (Detailed):** Detect if any user-supplied input is being reflected in HTTP responses without proper sanitization.
*   **Information Disclosure in Headers (Detailed):** Identify any HTTP response headers that reveal sensitive information such as server versions, internal IPs, or configuration details.

10\. Contributing
-----------------

Contributions are welcome! Please fork the repository, submit pull requests, or open issues if you have suggestions, find bugs, or want to improve the extension. Ensure that your contributions follow the existing code style and include appropriate tests and documentation.

11\. License
------------

This project is licensed under the MIT License.

12\. Contact
------------

For questions or support, please contact: [hello@bountysecurity.ai](mailto:hello@bountysecurity.ai)
