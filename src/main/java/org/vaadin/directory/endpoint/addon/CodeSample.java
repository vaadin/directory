package org.vaadin.directory.endpoint.addon;

import com.vaadin.directory.entity.directory.CodeHighlight;

public class CodeSample {

    private String description;
    private String code;
    private String type;
    private int displayOrder;

    public CodeSample(CodeHighlight highlight) {
        this.description = highlight.getDescription();
        this.code = highlight.getCode();
        this.type = highlight.getType().name();
        this.displayOrder = highlight.getDisplayOrder();
    }

    public String getDescription() { return description; }

    public String getCode() { return code; }

    public String getType() { return type; }

    public int getDisplayOrder() { return displayOrder; }
}
