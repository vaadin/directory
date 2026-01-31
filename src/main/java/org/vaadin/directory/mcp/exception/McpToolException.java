package org.vaadin.directory.mcp.exception;

/**
 * Exception for MCP tool errors that will be properly serialized by Spring AI.
 */
public class McpToolException extends RuntimeException {

    public McpToolException(String message) {
        super(message);
    }

    public McpToolException(String message, Throwable cause) {
        super(message, cause);
    }
}
