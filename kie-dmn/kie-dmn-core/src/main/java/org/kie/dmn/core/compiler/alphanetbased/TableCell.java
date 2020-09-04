package org.kie.dmn.core.compiler.alphanetbased;

import java.util.Map;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import org.kie.dmn.core.compiler.DMNCompilerContext;
import org.kie.dmn.core.compiler.DMNFEELHelper;
import org.kie.dmn.core.compiler.execmodelbased.DTableModel;
import org.kie.dmn.feel.codegen.feel11.CodegenStringUtil;
import org.kie.dmn.feel.lang.Type;

import static com.github.javaparser.StaticJavaParser.parseExpression;
import static org.kie.dmn.feel.codegen.feel11.CodegenStringUtil.replaceClassNameWith;

public class TableCell {
    private final String input;
    private final DMNFEELHelper feel;
    private final DMNCompilerContext ctx;
    private final TableIndex tableIndex;
    private final String columnName;
    private final Type type;

    private final String unaryTestClassName;
    private final String unaryTestClassNameWithPackage;

    private static final String CREATE_ALPHA_NODE_METHOD = "org.kie.dmn.core.compiler.alphanetbased.AlphaNetworkCompilerUtils.createAlphaNode";
    private static final String PACKAGE = "org.kie.dmn.core.alphasupport";

    public static class TableCellFactory {

        final DMNFEELHelper feel;
        final DMNCompilerContext ctx;

        public TableCellFactory(DMNFEELHelper feel, DMNCompilerContext ctx) {
            this.feel = feel;
            this.ctx = ctx;
        }

        public TableCell createUnitTestField(TableIndex tableIndex, DTableModel.DColumnModel columnModel, String input) {
            return new TableCell(feel, ctx, tableIndex, columnModel, input);
        }
    }


    private TableCell(DMNFEELHelper feel,
                      DMNCompilerContext ctx,
                      TableIndex tableIndex,
                      DTableModel.DColumnModel columnModel,
                      String input) {
        this.feel = feel;
        this.ctx = ctx;
        this.tableIndex = tableIndex;
        this.columnName = columnModel.getName();
        this.input = input;
        this.type = columnModel.getType();
        this.unaryTestClassName = tableIndex.appendTableIndexSuffix("UnaryTest");
        this.unaryTestClassNameWithPackage = PACKAGE + "." + unaryTestClassName;
    }

    public void addAlphaNetworkNode(BlockStmt stmt) {
        com.github.javaparser.ast.type.Type alphaNodeType = StaticJavaParser.parseType("org.drools.core.reteoo.AlphaNode");
        VariableDeclarationExpr variable = new VariableDeclarationExpr(alphaNodeType, tableIndex.appendTableIndexSuffix("alphaNode"));

        // This is used for Alpha Sharing. It needs to have the column name to avoid collisions with same test in other cells
        String constraintIdentifier = CodegenStringUtil.escapeIdentifier(columnName + input);

        Expression objectSource;
        if(tableIndex.isFirstColumn()) {
            objectSource = parseExpression("ctx.otn");
        } else {
            objectSource = new NameExpr(tableIndex.previousColumn().appendTableIndexSuffix("alphaNode"));
        }

        Expression alphaNodeCreation = new MethodCallExpr(null, CREATE_ALPHA_NODE_METHOD, NodeList.nodeList(
                new NameExpr("ctx"),
                objectSource,
                new StringLiteralExpr(constraintIdentifier),
                parseExpression(String.format("x -> %s.getTestInstance().apply(x.getEvalCtx(), x.getValue(%s))",
                                              unaryTestClassNameWithPackage,
                                              tableIndex.columnIndex())),
                new NameExpr("index1")

        ));
        final Expression expr = new AssignExpr(variable, alphaNodeCreation, AssignExpr.Operator.ASSIGN);
        stmt.addStatement(expr);
    }

    public void addUnaryTestClass(Map<String, String> allClasses) {
        ClassOrInterfaceDeclaration sourceCode = feel.generateUnaryTestsSource(
                input,
                ctx,
                type,
                false);

        replaceClassNameWith(sourceCode, "TemplateCompiledFEELUnaryTests", unaryTestClassName);

        sourceCode.setName(unaryTestClassName);

        CompilationUnit cu = new CompilationUnit(PACKAGE);
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = cu.addClass(unaryTestClassName);
        classOrInterfaceDeclaration.replace(sourceCode);

        allClasses.put(unaryTestClassNameWithPackage, cu.toString());
    }
}



