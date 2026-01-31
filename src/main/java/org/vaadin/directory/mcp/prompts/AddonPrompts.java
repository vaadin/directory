package org.vaadin.directory.mcp.prompts;

import org.springaicommunity.mcp.annotation.McpPrompt;
import org.springframework.stereotype.Component;

/**
 * MCP Prompts providing AI guidance for effective addon discovery and usage.
 *
 * Prompts help guide AI models on how to use the MCP tools effectively,
 * providing context, examples, and best practices for addon searches.
 */
@Component
public class AddonPrompts {

    /**
     * Prompt 1: Guide for searching addons
     *
     * Provides comprehensive instructions on using the directory_search tool
     * with various parameter combinations and search strategies.
     */
    @McpPrompt(
        name = "search-guide",
        description = "Guide for effectively searching Vaadin Directory addons"
    )
    public String searchGuide() {
        return """
                # Vaadin Directory Search Guide

                ## How to Search for Addons

                Use the `directory_search` tool with these parameters:

                **query** (required): Search terms
                - Component names (e.g., "grid", "chart", "upload")
                - Keywords (e.g., "data visualization", "file handling")
                - Tags (e.g., "form", "layout", "mobile")

                **vaadinVersion** (optional): Target Vaadin version
                - Major version: "24", "23", "14"
                - Filters for compatibility

                **type** (optional): Addon category
                - "component" - UI components
                - "integration" - Third-party integrations
                - "theme" - Visual themes
                - "tool" - Development tools

                **limit** (optional): Max results (1-50, default: 10)

                ## Example Searches

                1. Find grid components for Vaadin 24:
                   - query: "grid"
                   - vaadinVersion: "24"
                   - type: "component"

                2. Find chart integrations:
                   - query: "chart"
                   - type: "integration"

                3. Find mobile-friendly components:
                   - query: "mobile responsive"

                ## Tips
                - Start with broad searches, then refine
                - Use vaadinVersion to ensure compatibility
                - Check rating and compatibility in results
                """;
    }

    /**
     * Prompt 2: Guide for getting addon details
     *
     * Explains how to retrieve comprehensive addon information including
     * installation instructions, compatibility, and usage examples.
     */
    @McpPrompt(
        name = "addon-details-guide",
        description = "Guide for retrieving detailed addon information"
    )
    public String addonDetailsGuide() {
        return """
                # Vaadin Addon Details Guide

                ## How to Get Addon Information

                Use the `directory_getAddon` tool to retrieve:
                - Installation instructions (Maven/Gradle)
                - Version compatibility
                - Usage examples
                - Documentation links
                - License information

                ## Parameters

                **addonId** (required): The addon's URL identifier
                - Found in search results as "urlIdentifier"
                - Example: "avatar", "vaadin-grid-pro", "app-layout"

                **vaadinVersion** (required): Target Vaadin version
                - Determines which addon version to recommend
                - Example: "24", "23", "14"

                ## Response Includes

                1. **Installation Info**
                   - Maven coordinates
                   - Gradle dependency syntax
                   - Version compatibility details

                2. **Code Snippets**
                   - Usage examples in Java
                   - Integration patterns

                3. **Metadata**
                   - Author information
                   - License type
                   - Documentation URLs
                   - GitHub repository

                ## Workflow Example

                1. Search for addons: `directory_search`
                2. Note the `urlIdentifier` from results
                3. Get full details: `directory_getAddon`
                4. Use installation info and examples provided

                ## Tips
                - Always specify vaadinVersion for accurate compatibility info
                - Check the compatibility confidence level in response
                - Review code snippets for integration guidance
                """;
    }
}
