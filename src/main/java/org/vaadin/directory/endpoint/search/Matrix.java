package org.vaadin.directory.endpoint.search;

import com.vaadin.fusion.Nonnull;

import java.util.List;
import java.util.stream.Collectors;

public class Matrix {

    @Nonnull
    private final List<String> rows;
    @Nonnull
    private final List<String> cols;
    @Nonnull
    private final List<List<String>> data;

    public Matrix(List<String> rows, List<String> cols, List<List<String>> data) {
        this.rows = rows;
        this.cols = cols;
        this.data = data;
    }

    @Nonnull
    public List<String> getRows() { return rows; }

    @Nonnull
    public List<String> getCols() { return cols; }

    @Nonnull
    public List<List<String>> getData() { return data; }

    @Override
    public String toString() {
        return "Matrix{" +
                "rows=" + rows +
                ", cols=" + cols +
                ", data=" + data +
                '}';
    }

    public String toTabString() {
        StringBuilder output = new StringBuilder(" \t");
        output.append(cols.stream().collect(Collectors.joining("\t"))).append("\n");
        for (int i = 0; i < rows.size(); i++) {
            output.append(rows.get(i))
                    .append("\t")
                    .append(data.get(i).stream().collect(Collectors.joining("\t")))
                    .append("\n");
        }
        return output.toString();
    }

}
