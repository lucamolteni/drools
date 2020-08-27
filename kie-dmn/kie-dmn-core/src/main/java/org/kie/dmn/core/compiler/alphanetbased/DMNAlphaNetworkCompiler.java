package org.kie.dmn.core.compiler.alphanetbased;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.kie.dmn.core.compiler.execmodelbased.DTableModel;
import org.kie.dmn.core.impl.DMNModelImpl;

public class DMNAlphaNetworkCompiler {

    public DMNAlphaNetworkCompiler(DMNModelImpl model) {

    }

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

    private CompilationUnit getMethodTemplate() {
        InputStream resourceAsStream = this.getClass()
                .getResourceAsStream("/org/kie/dmn/core/alphasupport/DMNAlphaNetworkTemplate.java");
        return StaticJavaParser.parse(resourceAsStream);
    }

    public Map<String, String> generateSourceCode() {

        CompilationUnit template = getMethodTemplate();

        template.getClassByName("DMNAlphaNetworkTemplate").ifPresent(c -> c.setName("DMNAlphaNetwork"));

        return Collections.singletonMap("org.kie.dmn.core.alphasupport.DMNAlphaNetwork", template.toString());
    }
}
