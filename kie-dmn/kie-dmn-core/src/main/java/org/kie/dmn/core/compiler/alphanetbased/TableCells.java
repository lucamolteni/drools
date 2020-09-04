package org.kie.dmn.core.compiler.alphanetbased;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;

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

    public void addAlphaNetworkNode(BlockStmt alphaNetworkStatements, ClassOrInterfaceDeclaration dmnAlphaNetworkClass) {
        BlockStmt allStatements = new BlockStmt();

        // I'm pretty sure we can abstract this iteration to avoid copying it
        for (int rowIndex = 0; rowIndex < numRows; rowIndex++) {

            String methodName = String.format("alphaNodeCreation%s", rowIndex);

            BlockStmt creationStatements = new BlockStmt();
            MethodDeclaration methodDeclaration = newMethod(dmnAlphaNetworkClass, allStatements, methodName);
            methodDeclaration.setBody(creationStatements);

            for (int columnIndex = 0; columnIndex < numColumns; columnIndex++) {
                cells[rowIndex][columnIndex].addNodeCreation(creationStatements, dmnAlphaNetworkClass);
            }

        }

        partitionStatementsAndAddToMethod(allStatements, dmnAlphaNetworkClass, alphaNetworkStatements);
    }

    private MethodDeclaration newMethod(ClassOrInterfaceDeclaration dmnAlphaNetworkClass, BlockStmt allStatements, String methodName) {
        MethodDeclaration methodDeclaration = dmnAlphaNetworkClass.addMethod(methodName);
        allStatements.addStatement(new MethodCallExpr(null, methodName));
        return methodDeclaration;
    }

    private void partitionStatementsAndAddToMethod(BlockStmt allStatements, ClassOrInterfaceDeclaration dmnAlphaNetworkClass,
                                                   BlockStmt alphaNetworkStatements) {
        int size = allStatements.getStatements().size();

        NodeList<Statement> statements = allStatements.getStatements();

        List<BlockStmt> allBlocks = new ArrayList<>();
        BlockStmt partitioned = null;
        for (int i = 0; i < statements.size(); i++) {
            if(i % 10 == 0) {
                partitioned = new BlockStmt();
                allBlocks.add(partitioned);
            }
            partitioned.addStatement(statements.get(i));
        }

        for (int i = 0; i < allBlocks.size(); i++) {
            BlockStmt b = allBlocks.get(i);
            String methodName = "block" + i;

            MethodDeclaration methodDeclaration = newMethod(dmnAlphaNetworkClass, alphaNetworkStatements, methodName);
            methodDeclaration.setBody(b);
        }

    }
}