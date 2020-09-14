package org.kie.dmn.core.compiler.alphanetbased;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;

import static com.github.javaparser.StaticJavaParser.parseType;
import static org.kie.dmn.feel.codegen.feel11.CodegenStringUtil.replaceClassNameWith;

public class TableCells {

    private int numRows;
    private int numColumns;

    TableCell[][] cells;

    public TableCells(int numRows, int numColumns) {
        this.numRows = numRows;
        this.numColumns = numColumns;
        cells = new TableCell[numRows][numColumns];
    }

    public void add(TableCell unitTestField) {
        unitTestField.addToCells(cells);
    }

    public void addUnaryTestClass(Map<String, String> allClasses) {
        // I'm pretty sure we can abstract this iteration to avoid copying it
        for (int rowIndex = 0; rowIndex < numRows; rowIndex++) {
            for (int columnIndex = 0; columnIndex < numColumns; columnIndex++) {
                cells[rowIndex][columnIndex].addUnaryTestClass(allClasses);
            }
        }
    }

    private CompilationUnit getAlphaClassTemplate() {
        InputStream resourceAsStream = this.getClass()
                .getResourceAsStream("/org/kie/dmn/core/alphasupport/AlphaNodeCreationTemplate.java");
        return StaticJavaParser.parse(resourceAsStream);
    }

    public void addAlphaNetworkNode(BlockStmt alphaNetworkStatements, ClassOrInterfaceDeclaration dmnAlphaNetworkClass, Map<String, String> allClasses) {
        // I'm pretty sure we can abstract this iteration to avoid copying it
        for (int rowIndex = 0; rowIndex < numRows; rowIndex++) {

            CompilationUnit alphaNetworkCreationCU = getAlphaClassTemplate();
            String methodName = String.format("AlphaNodeCreation%s", rowIndex);

            ClassOrInterfaceDeclaration clazz = alphaNetworkCreationCU.findFirst(ClassOrInterfaceDeclaration.class).orElseThrow(() -> new RuntimeException());
            replaceClassNameWith(clazz, "AlphaNodeCreationTemplate", methodName);

            ConstructorDeclaration constructorDeclaration = clazz.addConstructor(Modifier.Keyword.PUBLIC);
            constructorDeclaration.addParameter(new Parameter(parseType("org.kie.dmn.core.compiler.alphanetbased.NetworkBuilderContext"), "ctx"));

            for (int columnIndex = 0; columnIndex < numColumns; columnIndex++) {
                TableCell tableCell = cells[rowIndex][columnIndex];
                tableCell.addNodeCreation(constructorDeclaration.getBody(), clazz);
            }

            String methodNameWithPackage = TableCell.PACKAGE + "." + methodName;
            allClasses.put(methodNameWithPackage, alphaNetworkCreationCU.toString());

            String newAlphaNetworkClass = String.format(
                    "new %s(ctx)", methodNameWithPackage
            );


            alphaNetworkStatements.addStatement(StaticJavaParser.parseExpression(newAlphaNetworkClass));

        }
    }
}