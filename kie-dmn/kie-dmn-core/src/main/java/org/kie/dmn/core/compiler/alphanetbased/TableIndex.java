package org.kie.dmn.core.compiler.alphanetbased;

import java.util.List;

import org.kie.dmn.core.compiler.execmodelbased.DTableModel;

public class TableIndex {

    private final int row;
    private final int column;

    public TableIndex(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public String getStringIndex() {
        // DMN DTable are 1Based
        return String.format("R%sC%s", row + 1, column + 1);
    }

    public DTableModel.DColumnModel getColumn(List<DTableModel.DColumnModel> columns) {
        return columns.get(column);
    }
}
