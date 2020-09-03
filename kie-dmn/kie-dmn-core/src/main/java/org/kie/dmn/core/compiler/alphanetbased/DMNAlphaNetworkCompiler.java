package org.kie.dmn.core.compiler.alphanetbased;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.kie.dmn.core.compiler.DMNCompilerContext;
import org.kie.dmn.core.compiler.DMNFEELHelper;
import org.kie.dmn.core.compiler.execmodelbased.DTableModel;
import org.kie.dmn.core.impl.DMNModelImpl;
import org.kie.dmn.feel.lang.Type;
import org.kie.dmn.model.api.DecisionTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.dmn.core.compiler.generators.GeneratorsUtil.getDecisionTableName;

public class DMNAlphaNetworkCompiler {

    private static final Logger logger = LoggerFactory.getLogger(DMNAlphaNetworkCompiler.class);

    private DMNCompilerContext ctx;
    private final DMNModelImpl model;
    private final DMNFEELHelper feel;

    private CompilationUnit template;
    private ClassOrInterfaceDeclaration dmnAlphaNetworkClass;

    Map<String, String> allClasses = new HashMap<>();

    public DMNAlphaNetworkCompiler(DMNCompilerContext ctx, DMNModelImpl model, DMNFEELHelper feel) {
        this.ctx = ctx;
        this.model = model;
        this.feel = feel;
    }

    public Map<String, String> generateSourceCode(String dtName, DecisionTable dt) {

        initTemplate();

        String decisionName = getDecisionTableName(dtName, dt);
        DTableModel dTableModel = new DTableModel(ctx.getFeelHelper(), model, dtName, decisionName, dt);

        List<UnitTestField> unitTestFields = generateUnaryTests(dTableModel);

        for (UnitTestField ut : unitTestFields) {
            ut.addUnaryTestClass(allClasses);
        }

        allClasses.put("org.kie.dmn.core.alphasupport.DMNAlphaNetwork", template.toString());

        logGeneratedClasses();

        return allClasses;
    }

    private void logGeneratedClasses() {
        for (Map.Entry<String, String> kv : allClasses.entrySet()) {
            logger.debug("Generated class {}", kv.getKey());
            logger.debug(kv.getValue());
        }
    }

    private void initTemplate() {
        template = getMethodTemplate();
        dmnAlphaNetworkClass = template.getClassByName("DMNAlphaNetworkTemplate")
                .orElseThrow(() -> new RuntimeException("Cannot find class"));
        dmnAlphaNetworkClass.setName("DMNAlphaNetwork");
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
        String columnName;
        Type type;

        public UnitTestField(int rowIndex, int columnIndex, String columnName, String input, Type type) {
            // DMN DTable are 1Based
            this.rowIndex = rowIndex + 1;
            this.columnIndex = columnIndex + 1;
            this.input = input;
            this.type = type;
        }

        public void addAlphaNetwork(BodyDeclaration constructorBody) {

        }

        public void addUnaryTestClass(Map<String, String> allClasses) {
            ClassOrInterfaceDeclaration sourceCode = feel.generateUnaryTestsSource(
                    input,
                    ctx,
                    type,
                    false);

            String className = String.format("UnaryTestr%sc%s", rowIndex, columnIndex);
            sourceCode.setName(className);
            allClasses.put(String.format("%s", className), sourceCode.toString());
        }
    }

    private CompilationUnit getMethodTemplate() {
        InputStream resourceAsStream = this.getClass()
                .getResourceAsStream("/org/kie/dmn/core/alphasupport/DMNAlphaNetworkTemplate.java");
        return StaticJavaParser.parse(resourceAsStream);
    }
}
