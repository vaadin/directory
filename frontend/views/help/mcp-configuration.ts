import './mcp-configuration.css';
import { html } from 'lit';
import { customElement } from 'lit/decorators.js';
import { View, PageJsonLd } from '../view';
import { appStore } from 'Frontend/stores/app-store';
import { router } from '../../index';

@customElement('mcp-configuration-view')
export class McpConfigurationView extends View {
  constructor() {
    super();
    appStore.setCurrentViewTitle('MCP Configuration - Vaadin Directory');
  }

  updatePageMetadata(): void {
    const metadata = new PageJsonLd(
      'MCP Configuration - Vaadin Directory',
      'Learn how to configure Vaadin Directory as an MCP server for AI assistants',
      appStore.appUrl + 'mcp-configuration',
    );
    metadata.appendOrReplaceToHead();

    const canonical = document.head.querySelector('link[rel="canonical"]');
    canonical?.setAttribute('href', `${appStore.appUrl}mcp-configuration`);
  }

  render() {
    const baseUrl = router.baseUrl;
    const mcpUrl = `${baseUrl}mcp`;

    return html`
      <div class="mcp-configuration-container">
        <section class="mcp-section">
          <h2>What is MCP?</h2>
          <p>
            The <strong>Model Context Protocol (MCP)</strong> allows AI
            assistants like Claude, Github Copilot, Cline, and other MCP clients to
            directly access Vaadin Directory's addon database:
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
          <h2>⚡ Setup Instructions</h2>
          <p>Click on your AI coding tool to expand setup instructions:</p>

          <details class="app-item">
            <summary>
              <strong>Claude Code</strong>
              <span class="badge">HTTP</span>
            </summary>
            <div class="app-content">
              <p>Add the Vaadin Directory MCP server using the CLI or manual configuration.</p>

              <h3>Option 1: CLI Command (Recommended)</h3>
              <pre><code>claude mcp add vaadin-directory --transport http ${mcpUrl}</code></pre>

              <h3>Option 2: Manual Configuration</h3>
              <pre><code>{
  "mcpServers": {
    "vaadin-directory": {
      "type": "http",
      "url": "${mcpUrl}"
    }
  }
}</code></pre>
              <p><strong>File locations:</strong></p>
              <ul>
                <li>Project-scoped: <code>.mcp.json</code> (in project root)</li>
                <li>Global: <code>~/.claude.json</code></li>
              </ul>
              <p class="note"><strong>Note:</strong> Restart Claude Code after making configuration changes.</p>
            </div>
          </details>

          <details class="app-item">
            <summary>
              <strong>Cursor</strong>
              <span class="badge">HTTP</span>
            </summary>
            <div class="app-content">
              <p>Add the Vaadin Directory MCP server to your project or global configuration.</p>
              <pre><code>{
  "mcpServers": {
    "vaadin-directory": {
      "type": "http",
      "url": "${mcpUrl}"
    }
  }
}</code></pre>
              <p><strong>File locations:</strong></p>
              <ul>
                <li>Project-scoped: <code>.cursor/mcp.json</code></li>
                <li>Global: <code>~/.cursor/mcp.json</code></li>
              </ul>
              <p class="note"><strong>Note:</strong> Restart Cursor after making configuration changes.</p>
            </div>
          </details>

          <details class="app-item">
            <summary>
              <strong>Windsurf</strong>
              <span class="badge">HTTP</span>
            </summary>
            <div class="app-content">
              <p>Add the Vaadin Directory MCP server through Windsurf Settings (bottom right) or <code>Cmd+Shift+P</code> → "Open Windsurf Settings".</p>
              <pre><code>{
  "mcpServers": {
    "vaadin-directory": {
      "type": "http",
      "url": "${mcpUrl}"
    }
  }
}</code></pre>
              <p><strong>Configuration file:</strong> <code>~/.codeium/windsurf/mcp_config.json</code></p>
              <p class="note"><strong>Note:</strong> Click the Hammer icon on the Cascade toolbar to view connected MCP tools.</p>
            </div>
          </details>

          <details class="app-item">
            <summary>
              <strong>Junie (JetBrains IDEs)</strong>
              <span class="badge">via proxy</span>
            </summary>
            <div class="app-content">
              <p>Junie only supports stdio-based MCP servers. Use <a href="https://github.com/pyroprompts/mcp-stdio-to-streamable-http-adapter" target="_blank">@pyroprompts/mcp-stdio-to-streamable-http-adapter</a> to bridge stdio to the HTTP-based Vaadin Directory MCP server.</p>
              <ol>
                <li>Open IDE settings: <code>Ctrl+Alt+S</code> (Windows/Linux) or <code>Cmd+,</code> (macOS)</li>
                <li>Navigate to Tools → Junie → MCP Settings</li>
                <li>Click the Add button and add the configuration below</li>
              </ol>
              <pre><code>{
  "mcpServers": {
    "vaadin-directory": {
      "command": "npx",
      "args": ["@pyroprompts/mcp-stdio-to-streamable-http-adapter"],
      "env": {
        "URI": "${mcpUrl}",
        "MCP_NAME": "vaadin-directory"
      }
    }
  }
}</code></pre>
              <p class="note"><strong>Note:</strong> The adapter uses environment variables to configure the connection. <code>URI</code> points to the Vaadin Directory MCP server endpoint, and <code>MCP_NAME</code> is an identifier for the server.</p>
              <p><strong>File locations:</strong></p>
              <ul>
                <li>Project-scoped: <code>.junie/mcp/mcp.json</code></li>
                <li>Global: <code>~/.junie/mcp.json</code></li>
              </ul>
            </div>
          </details>

          <details class="app-item">
            <summary>
              <strong>GitHub Copilot (JetBrains IDEs)</strong>
              <span class="badge">HTTP</span>
            </summary>
            <div class="app-content">
              <p><strong>⚠️ Important:</strong> MCP servers only work when GitHub Copilot is used in <strong>Agent mode</strong>. Regular Copilot Chat does not support MCP.</p>
              <p>Agent mode with MCP support is now in public preview for JetBrains IDEs (as of May 2025).</p>
              <ol>
                <li>Create <code>.copilot/mcp-config.json</code> in your project root (JetBrains IDEs use the same format as VS Code)</li>
                <li>Add the configuration below</li>
                <li><strong>Switch to Agent mode:</strong> Click the GitHub Copilot icon → Change to Agent mode</li>
                <li>Configure MCP servers: Click Tools icon → Add More Tools → Edit mcp.json</li>
                <li>Alternative: Click GitHub Copilot icon → Edit settings → MCP Servers section</li>
              </ol>
              <pre><code>{
  "servers": {
    "vaadin-directory": {
      "url": "${mcpUrl}"
    }
  }
}</code></pre>
              <p class="note"><strong>Enterprise Note:</strong> Organizations with Copilot Business or Enterprise must enable the "MCP servers in Copilot" policy (disabled by default).</p>
            </div>
          </details>

          <details class="app-item">
            <summary>
              <strong>GitHub Copilot (VS Code)</strong>
              <span class="badge">HTTP</span>
            </summary>
            <div class="app-content">
              <p><strong>⚠️ Important:</strong> MCP servers only work when GitHub Copilot is used in <strong>Agent mode</strong>. Regular Copilot Chat does not support MCP.</p>
              <p>Requires VS Code 1.99 or later.</p>
              <ol>
                <li>Create <code>.vscode/mcp.json</code> in your project root</li>
                <li>Add the configuration below</li>
                <li>Click the Start button that appears at the top of the MCP servers list</li>
                <li><strong>Switch to Agent mode:</strong> Open Copilot Chat → Click mode selector → Select "Agent"</li>
                <li>Click the tools icon in Agent mode to view available MCP servers</li>
              </ol>
              <pre><code>{
  "servers": {
    "vaadin-directory": {
      "type": "http",
      "url": "${mcpUrl}"
    }
  }
}</code></pre>
              <p class="note"><strong>Enterprise Note:</strong> Organizations with Copilot Business or Enterprise must enable the "MCP servers in Copilot" policy (disabled by default).</p>
            </div>
          </details>

          <details class="app-item">
            <summary>
              <strong>Codex (OpenAI)</strong>
              <span class="badge">HTTP</span>
            </summary>
            <div class="app-content">
              <p>Add the Vaadin Directory MCP server to your Codex configuration. Works with both Codex CLI and IDE extension.</p>
              <ol>
                <li>Open or create <code>~/.codex/config.toml</code></li>
                <li>Add the server configuration below</li>
                <li>Restart Codex to load the new MCP server</li>
              </ol>
              <pre><code>[mcp_servers.vaadin-directory]
url = "${mcpUrl}"</code></pre>
              <p class="note"><strong>Version Requirement:</strong> HTTP-based MCP servers require Codex CLI version 0.43 or later. If you see "missing field command" errors, upgrade with: <code>npm install -g @openai/codex@latest</code></p>
            </div>
          </details>

          <details class="app-item">
            <summary>
              <strong>Gemini CLI (Google)</strong>
              <span class="badge">HTTP</span>
            </summary>
            <div class="app-content">
              <p>Add the Vaadin Directory MCP server to your Gemini CLI configuration. Gemini will automatically use Vaadin Directory tools when relevant.</p>
              <ol>
                <li>Open or create <code>~/.gemini/settings.json</code></li>
                <li>Add the server configuration below</li>
                <li>Restart Gemini CLI to load the new MCP server</li>
              </ol>
              <pre><code>{
  "mcpServers": {
    "vaadin-directory": {
      "httpUrl": "${mcpUrl}"
    }
  }
}</code></pre>
              <p class="note"><strong>Note:</strong> Gemini CLI will automatically invoke Vaadin Directory tools when you ask questions about Vaadin addons.</p>
            </div>
          </details>

          <details class="app-item">
            <summary>
              <strong>Opencode</strong>
              <span class="badge">HTTP</span>
            </summary>
            <div class="app-content">
              <p>Add the Vaadin Directory MCP server to your Opencode configuration.</p>
              <ol>
                <li>Open or create <code>opencode.json</code> in your project root</li>
                <li>Add the server configuration below</li>
                <li>Restart Opencode to load the new MCP server</li>
              </ol>
              <pre><code>{
  "$schema": "https://opencode.ai/config.json",
  "mcp": {
    "vaadin-directory": {
      "type": "remote",
      "url": "${mcpUrl}",
      "enabled": true
    }
  }
}</code></pre>
            </div>
          </details>

          <details class="app-item">
            <summary>
              <strong>Other MCP Clients</strong>
              <span class="badge">HTTP or stdio</span>
            </summary>
            <div class="app-content">
              <p>The Vaadin Directory MCP server can be used with any MCP-compatible client. Choose the appropriate configuration based on your tool's transport support:</p>

              <h3>If your tool supports HTTP/SSE natively:</h3>
              <p>Simply point it to our HTTP endpoint:</p>
              <pre><code>${mcpUrl}</code></pre>
              <p>The exact configuration format depends on your specific tool. Look for settings like "MCP Server URL", "HTTP transport", or "Streamable HTTP" in your tool's documentation.</p>

              <h3>If your tool only supports stdio:</h3>
              <p>Use an HTTP adapter to bridge stdio to HTTP. This works with any stdio-based MCP client:</p>
              <pre><code>{
  "mcpServers": {
    "vaadin-directory": {
      "command": "npx",
      "args": ["@pyroprompts/mcp-stdio-to-streamable-http-adapter"],
      "env": {
        "URI": "${mcpUrl}",
        "MCP_NAME": "vaadin-directory"
      }
    }
  }
}</code></pre>
              <p>Adapt the JSON structure to match your tool's configuration format. The adapter uses environment variables: <code>URI</code> for the server endpoint and <code>MCP_NAME</code> as an identifier.</p>
            </div>
          </details>

          <div class="note transport-note">
            <p><strong>🔧 Transport Types:</strong> The Vaadin Directory MCP server uses <strong>HTTP transport (streamable-http)</strong>. This is natively supported by Claude Code, Cursor, and Windsurf. For stdio-only tools like Junie, you can use <a href="https://github.com/pyroprompts/mcp-stdio-to-streamable-http-adapter" target="_blank">@pyroprompts/mcp-stdio-to-streamable-http-adapter</a> as a bridge between the two transport types.</p>
          </div>
        </section>

        <section class="mcp-section">
          <h2>Example Usage</h2>
          <p>
            Once configured, simply ask your AI assistant natural language
            questions like:
          </p>
          <ul>
            <li>"Search for grid components compatible with Vaadin 24"</li>
            <li>"Get details about the Avatar addon for Vaadin 24"</li>
            <li>"Find chart integration addons"</li>
            <li>"Show me all theme addons for Vaadin 25"</li>
            <li>"What integrations are available for Vaadin?"</li>
          </ul>
          <p>
            Your AI assistant will automatically use the appropriate MCP tools
            to search and retrieve addon information from Vaadin Directory.
          </p>
        </section>

        <section class="mcp-section">
          <h2>Available Tools</h2>
          <div class="tools-grid">
            <div class="tool-card">
              <h3>directory_search</h3>
              <p>
                Search Vaadin Directory for addon components. Returns a list of addon summaries
                with compatibility, popularity, and rating information.
              </p>
              <p>
                <strong>Parameters:</strong>
              </p>
              <ul>
                <li><code>query</code> (required) - Search query (addon name, keywords)</li>
                <li><code>vaadinVersion</code> (optional) - Vaadin major version (e.g., '24', '25')</li>
                <li><code>type</code> (optional) - Addon type: component, integration, theme, or tool</li>
                <li><code>limit</code> (optional) - Maximum results (default: 10, max: 50)</li>
              </ul>
              <p class="note">
                Use this tool first to find relevant addons, then use <code>directory_getAddon</code>
                for detailed information.
              </p>
            </div>
            <div class="tool-card">
              <h3>directory_getAddon</h3>
              <p>
                Get detailed information about a specific Vaadin Directory addon,
                including Maven installation instructions, latest version, compatibility,
                and usage examples.
              </p>
              <p>
                <strong>Parameters:</strong>
              </p>
              <ul>
                <li><code>addonId</code> (required) - Addon URL identifier (e.g., 'vaadin-grid-pro', 'avatar')</li>
                <li><code>vaadinVersion</code> (optional) - Target Vaadin major version (e.g., '24', '25')</li>
              </ul>
              <p class="note">
                Call this tool after finding an addon via search to get complete installation
                and usage details.
              </p>
            </div>
          </div>
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
            <strong>Version:</strong> 2.0.0
            <span class="separator">|</span>
            <strong>Protocol:</strong> Streamable HTTP
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
