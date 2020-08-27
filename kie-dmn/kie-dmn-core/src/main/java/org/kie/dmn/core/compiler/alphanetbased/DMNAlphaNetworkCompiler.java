package org.kie.dmn.core.compiler.alphanetbased;

import java.util.List;

import org.kie.dmn.core.compiler.execmodelbased.DTableModel;

public class DMNAlphaNetworkCompiler {

    public String generateUnaryTests(DTableModel dTableModel) {

        List<DTableModel.DRowModel> rows = dTableModel.getRows();
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            DTableModel.DRowModel row = rows.get(rowIndex);
            for (int columnIndex = 0; columnIndex < row.getInputs().size(); columnIndex++) {
                String input = row.getInputs().get(columnIndex);
                generateUnaryTest(rowIndex, columnIndex, input);

            }
        }

        return "";
    }

    private void generateUnaryTest(int rowIndex, int columnIndex, String input) {

    }
}
