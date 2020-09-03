package org.kie.dmn.core.compiler.alphanetbased;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import org.kie.dmn.core.compiler.DMNCompilerContext;
import org.kie.dmn.core.compiler.DMNFEELHelper;
import org.kie.dmn.core.compiler.execmodelbased.DTableModel;
import org.kie.dmn.core.impl.DMNModelImpl;
import org.kie.dmn.model.api.DecisionTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.dmn.core.compiler.generators.GeneratorsUtil.getDecisionTableName;

public class DMNAlphaNetworkCompiler {

    private static final Logger logger = LoggerFactory.getLogger(DMNAlphaNetworkCompiler.class);

    private final DMNCompilerContext ctx;
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

        List<TableCell> tableCells = parseCells(dTableModel);

        BlockStmt alphaNetworkStatements = new BlockStmt();
        for (TableCell ut : tableCells) {
            ut.addUnaryTestClass(allClasses);
            ut.addAlphaNetwork(alphaNetworkStatements);
        }

        BlockStmt alphaNetworkBlock = dmnAlphaNetworkClass
                .findFirst(BlockStmt.class, DMNAlphaNetworkCompiler::blockHasComment)
                .orElseThrow(RuntimeException::new);

        alphaNetworkBlock.replace(alphaNetworkStatements);



        allClasses.put("org.kie.dmn.core.alphasupport.DMNAlphaNetwork", template.toString());

        logGeneratedClasses();

        return allClasses;
    }


    private static boolean blockHasComment(BlockStmt block) {
        return block.getComment().filter(c -> " Alpha network creation statements".equals(c.getContent()))
                .isPresent();
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
        dmnAlphaNetworkClass
                .findAll(SimpleName.class, ne -> ne.toString().equals("DMNAlphaNetworkTemplate"))
                .forEach(r -> {
                    r.replace(new SimpleName("DMNAlphaNetwork"));
                });
    }

    public List<TableCell> parseCells(DTableModel dTableModel) {

        List<TableCell> unitTests = new ArrayList<>();
        List<DTableModel.DRowModel> rows = dTableModel.getRows();
        List<DTableModel.DColumnModel> columns = dTableModel.getColumns();

        TableCell.TableCellFactory factory = new TableCell.TableCellFactory(feel, ctx);

        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            DTableModel.DRowModel row = rows.get(rowIndex);
            for (int columnIndex = 0; columnIndex < row.getInputs().size(); columnIndex++) {
                String input = row.getInputs().get(columnIndex);
                TableIndex tableIndex = new TableIndex(rowIndex, columnIndex);
                DTableModel.DColumnModel column = tableIndex.getColumn(columns);
                unitTests.add(factory.createUnitTestField(tableIndex,
                                                          column,
                                                          input));
            }
        }
        return unitTests;
    }

    private CompilationUnit getMethodTemplate() {
        InputStream resourceAsStream = this.getClass()
                .getResourceAsStream("/org/kie/dmn/core/alphasupport/DMNAlphaNetworkTemplate.java");
        return StaticJavaParser.parse(resourceAsStream);
    }
}
