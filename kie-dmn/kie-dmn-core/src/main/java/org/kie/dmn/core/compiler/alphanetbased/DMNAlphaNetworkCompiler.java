package org.kie.dmn.core.compiler.alphanetbased;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.kie.dmn.core.compiler.DMNCompilerContext;
import org.kie.dmn.core.compiler.DMNFEELHelper;
import org.kie.dmn.core.compiler.execmodelbased.DTableModel;
import org.kie.dmn.core.impl.DMNModelImpl;
import org.kie.dmn.feel.lang.Type;

public class DMNAlphaNetworkCompiler {

    private DMNCompilerContext ctx;
    private final DMNModelImpl model;
    private final DMNFEELHelper feel;

    public DMNAlphaNetworkCompiler(DMNCompilerContext ctx, DMNModelImpl model, DMNFEELHelper feel) {
        this.ctx = ctx;
        this.model = model;
        this.feel = feel;
    }

    public List<UnitTestField> generateUnaryTests(DTableModel dTableModel) {

        List<UnitTestField> unitTests = new ArrayList<>();
        List<DTableModel.DRowModel> rows = dTableModel.getRows();
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            DTableModel.DRowModel row = rows.get(rowIndex);
            for (int columnIndex = 0; columnIndex < row.getInputs().size(); columnIndex++) {
                String input = row.getInputs().get(columnIndex);
                unitTests.add(new UnitTestField(rowIndex, columnIndex, input, dTableModel.getColumns().get(columnIndex).getType()));

            }
        }
        return unitTests;
    }

    class UnitTestField {
        int rowIndex;
        int columnIndex;
        String input;
        Type type;

        public UnitTestField(int rowIndex, int columnIndex, String input, Type type) {
            this.rowIndex = rowIndex;
            this.columnIndex = columnIndex;
            this.input = input;
            this.type = type;
        }

        public String generateSourceCode(DMNCompilerContext ctx, DMNFEELHelper feel) {
            ClassOrInterfaceDeclaration sourceCode = feel.generateUnaryTestsSource(
                    input,
                    ctx,
                    type);

            return sourceCode.toString();

        }
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
