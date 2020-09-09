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

    public TableIndex previousColumn() {
        return new TableIndex(row, column - 1);
    }

    public String appendTableIndexSuffix(String sourceString) {
        // DMN DTable are 1Based
        return String.format("%sR%sC%s", sourceString, row + 1, column + 1);
    }

    public DTableModel.DColumnModel getColumn(List<DTableModel.DColumnModel> columns) {
        return columns.get(column);
    }

    public boolean isFirstColumn() {
        return column == 0;
    }

    public int columnIndex() {
        return column;
    }

    public void addToCells(TableCell[][] cells, TableCell tableCell) {
        cells[row][column] = tableCell;
    }

}
