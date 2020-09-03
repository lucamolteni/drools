package org.kie.dmn.core.compiler.alphanetbased;

import java.util.Map;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import org.kie.dmn.api.core.DMNType;
import org.kie.dmn.core.compiler.DMNCompilerContext;
import org.kie.dmn.core.compiler.DMNFEELHelper;
import org.kie.dmn.core.compiler.execmodelbased.DTableModel;
import org.kie.dmn.feel.lang.Type;

public class TableCell {

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

    private String input;
    private final DMNFEELHelper feel;
    private final DMNCompilerContext ctx;
    private TableIndex tableIndex;
    private String columnName;
    private Type type;

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
    }

    public void addAlphaNetwork(BlockStmt stmt) {

        Expression targetExpr = null;
        Expression createAlphaNodeExpr = null;
        Expression newAlphaNodeExpression = new AssignExpr(targetExpr, createAlphaNodeExpr, AssignExpr.Operator.ASSIGN);
        stmt.addStatement(newAlphaNodeExpression);
    }

    public void addUnaryTestClass(Map<String, String> allClasses) {
        ClassOrInterfaceDeclaration sourceCode = feel.generateUnaryTestsSource(
                input,
                ctx,
                type,
                false);

        String className = String.format("UnaryTest%s", tableIndex.getStringIndex());
        sourceCode.setName(className);
        allClasses.put(String.format("%s", className), sourceCode.toString());
    }
}



