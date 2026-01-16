import './mcp-configuration.css';
import { html } from 'lit';
import { customElement } from 'lit/decorators.js';
import { View, PageJsonLd } from '../view';
import { appStore } from 'Frontend/stores/app-store';
import { RouterLocation } from '@vaadin/router';

@customElement('mcp-configuration-view')
export class McpConfigurationView extends View {

  constructor() {
    super();
    appStore.setCurrentViewTitle('MCP Configuration - Vaadin Directory');
  }

  async onBeforeEnter(location: RouterLocation) {
    // Initialize view
  }

  updatePageMetadata(): void {
    const metadata = new PageJsonLd(
      'MCP Configuration - Vaadin Directory',
      'Learn how to configure Vaadin Directory as an MCP server for AI assistants',
      appStore.appUrl + 'mcp-configuration'
    );
    metadata.appendOrReplaceToHead();

    const canonical = document.head.querySelector('link[rel="canonical"]') as HTMLElement;
    if (canonical) canonical.setAttribute("href", appStore.appUrl + 'mcp-configuration');
  }

  render() {
    const baseUrl = window.location.origin;
    const mcpUrl = `${baseUrl}/mcp/directory`;

    return html`
      <div class="mcp-configuration-container">
        <section class="mcp-section">
          <h2>What is MCP?</h2>
          <p>
            The <strong>Model Context Protocol (MCP)</strong> allows AI assistants like Claude Desktop 
            and Cline to directly access Vaadin Directory's addon database. This enables:
          </p>
          <ul>
            <li>Searching for addons by name, keywords, or tags</li>
            <li>Getting detailed installation instructions (Maven/Gradle)</li>
            <li>Checking Vaadin version compatibility</li>
            <li>Accessing documentation and source code links</li>
            <li>Viewing ratings and usage statistics</li>
          </ul>
        </section>

        <section class="mcp-section">
          <h2>Available Tools</h2>
          <div class="tools-grid">
            <div class="tool-card">
              <h3>search</h3>
              <p>Search for addons with filters for Vaadin version, type, and maintenance status.</p>
              <code class="inline">POST ${mcpUrl}/search</code>
            </div>
            <div class="tool-card">
              <h3>getAddon</h3>
              <p>Get complete addon details including installation snippets and compatibility info.</p>
              <code class="inline">POST ${mcpUrl}/addon</code>
            </div>
          </div>
        </section>

        <section class="mcp-section">
          <h2>Quick Setup</h2>
          
          <h3>For Claude Desktop</h3>
          <p>Add this configuration to your <code>claude_desktop_config.json</code>:</p>
          <pre><code>{
  "mcpServers": {
    "vaadin-directory": {
      "url": "${mcpUrl}"
    }
  }
}</code></pre>
          <p class="config-location">
            Config file location:
            <br>• macOS: <code>~/Library/Application Support/Claude/claude_desktop_config.json</code>
            <br>• Windows: <code>%APPDATA%\\Claude\\claude_desktop_config.json</code>
          </p>

          <h3>For Cline / Other MCP Clients</h3>
          <p>Use the base URL:</p>
          <pre><code>${mcpUrl}</code></pre>
        </section>

        <section class="mcp-section">
          <h2>Example Usage</h2>
          
          <h3>Search for addons:</h3>
          <pre><code>curl -X POST ${mcpUrl}/search \\
  -H "Content-Type: application/json" \\
  -d '{
    "query": "grid",
    "vaadinVersion": "24",
    "limit": 5
  }'</code></pre>

          <h3>Get addon details:</h3>
          <pre><code>curl -X POST ${mcpUrl}/addon \\
  -H "Content-Type: application/json" \\
  -d '{
    "addonId": "avatar",
    "vaadinVersion": "24"
  }'</code></pre>
        </section>

        <section class="mcp-section">
          <h2>API Endpoints</h2>
          <table class="endpoints-table">
            <thead>
              <tr>
                <th>Endpoint</th>
                <th>Method</th>
                <th>Description</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td><code>/mcp/directory</code></td>
                <td>GET</td>
                <td>Server metadata and tool schemas</td>
              </tr>
              <tr>
                <td><code>/mcp/directory/search</code></td>
                <td>POST</td>
                <td>Search for addons</td>
              </tr>
              <tr>
                <td><code>/mcp/directory/addon</code></td>
                <td>POST</td>
                <td>Get detailed addon information</td>
              </tr>
              <tr>
                <td><code>/mcp/directory/health</code></td>
                <td>GET</td>
                <td>Health check</td>
              </tr>
            </tbody>
          </table>
        </section>

        <section class="mcp-section">
          <h2>Compatibility Confidence Levels</h2>
          <table class="confidence-table">
            <thead>
              <tr>
                <th>Level</th>
                <th>Description</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td><span class="badge high">high</span></td>
                <td>Exact Vaadin version match found</td>
              </tr>
              <tr>
                <td><span class="badge medium">medium</span></td>
                <td>Major version match (e.g., 24.x matches 24)</td>
              </tr>
              <tr>
                <td><span class="badge low">low</span></td>
                <td>No version match, returning latest version</td>
              </tr>
              <tr>
                <td><span class="badge unknown">unknown</span></td>
                <td>No version information available</td>
              </tr>
            </tbody>
          </table>
        </section>

        <section class="mcp-footer">
          <p>
            <strong>Schema Version:</strong> 1 
            <span class="separator">|</span>
            <strong>Status:</strong> <span class="status-badge">✓ Active</span>
            <span class="separator">|</span>
            <strong>CORS:</strong> Enabled
          </p>
        </section>
      </div>
    `;
  }
}

