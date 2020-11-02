/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.dmn.core.compiler.alphanetbased;

import java.util.Map;
import java.util.Optional;

import javax.swing.text.html.Option;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import org.kie.dmn.core.compiler.DMNCompilerContext;
import org.kie.dmn.core.compiler.DMNFEELHelper;
import org.kie.dmn.core.compiler.execmodelbased.DTableModel;
import org.kie.dmn.feel.codegen.feel11.CodegenStringUtil;
import org.kie.dmn.feel.lang.Type;

import static com.github.javaparser.StaticJavaParser.parseExpression;
import static com.github.javaparser.StaticJavaParser.parseType;
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

    private static final String CREATE_ALPHA_NODE_METHOD = "createAlphaNode";
    private static final String CREATE_INDEX_NODE_METHOD = "createIndex";
    public static final String PACKAGE = "org.kie.dmn.core.alphasupport";

    private Optional<String> output = Optional.empty();

    public void setOutput(String s) {
        output = Optional.of(s);
    }

    public static class TableCellFactory {

        final DMNFEELHelper feel;
        final DMNCompilerContext ctx;

        public TableCellFactory(DMNFEELHelper feel, DMNCompilerContext ctx) {
            this.feel = feel;
            this.ctx = ctx;
        }

        public TableCell createInputCell(TableIndex tableIndex, DTableModel.DColumnModel columnModel, String input) {
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

    private String addIndex(BlockStmt stmt) {
        com.github.javaparser.ast.type.Type indexType = StaticJavaParser.parseType("org.drools.model.Index");
        String indexName = tableIndex.appendTableIndexSuffix("index");

        VariableDeclarationExpr variable = new VariableDeclarationExpr(indexType, indexName);

        // TODO LUCA this is wrong
        Expression indexValueExpr;
        if(input.contains("\"")) {
            indexValueExpr = new NameExpr(input);
        } else {
            indexValueExpr = new StringLiteralExpr(input);
        }
        Expression alphaNodeCreation = new MethodCallExpr(null, CREATE_INDEX_NODE_METHOD, NodeList.nodeList(
                parseExpression("String.class"),
                parseExpression(String.format("x -> (String)x.getValue(%s)",
                                              tableIndex.columnIndex())),
                indexValueExpr

        ));
        final Expression expr = new AssignExpr(variable, alphaNodeCreation, AssignExpr.Operator.ASSIGN);
        stmt.addStatement(expr);

        return indexName;
    }

    public void addNodeCreation(BlockStmt stmt, ClassOrInterfaceDeclaration alphaNetworkClass) {
        com.github.javaparser.ast.type.Type alphaNodeType = StaticJavaParser.parseType("AlphaNode");
        String alphaNodeName = tableIndex.appendTableIndexSuffix("alphaNode");
        VariableDeclarationExpr variable = new VariableDeclarationExpr(alphaNodeType, alphaNodeName);

        // This is used for Alpha Sharing. It needs to have the column name to avoid collisions with same test in other cells
        String constraintIdentifier = CodegenStringUtil.escapeIdentifier(columnName + input);

        String lambdaMethodName = tableIndex.appendTableIndexSuffix("test");
        Expression methodReference = new MethodReferenceExpr(new ThisExpr(), NodeList.nodeList(), lambdaMethodName);

        MethodDeclaration unaryTestMethod = alphaNetworkClass.addMethod(lambdaMethodName);
        unaryTestMethod.setParameters(NodeList.nodeList(new Parameter(parseType("TableContext"), "x")));
        unaryTestMethod.setType(parseType("boolean"));

        MethodCallExpr testExpression = parseExpression(String.format("%s.getTestInstance().apply(x.getEvalCtx(), x.getValue(%s))",
                                                                      unaryTestClassNameWithPackage,
                                                                      tableIndex.columnIndex()));
        unaryTestMethod.setBody(new BlockStmt(NodeList.nodeList(new ReturnStmt(testExpression))));

        Expression alphaNodeCreation;
        if (tableIndex.isFirstColumn()) {
            String indexName = addIndex(stmt);
            alphaNodeCreation = new MethodCallExpr(null, CREATE_ALPHA_NODE_METHOD, NodeList.nodeList(
                    new NameExpr("ctx"),
                    parseExpression("ctx.otn"),
                    new StringLiteralExpr(constraintIdentifier),
                    methodReference,
                    new NameExpr(indexName)
            ));
        } else {
            alphaNodeCreation = new MethodCallExpr(null, CREATE_ALPHA_NODE_METHOD, NodeList.nodeList(
                    new NameExpr("ctx"),
                    new NameExpr(tableIndex.previousColumn().appendTableIndexSuffix("alphaNode")),
                    new StringLiteralExpr(constraintIdentifier),
                    methodReference
            ));
        }

        final Expression expr = new AssignExpr(variable, alphaNodeCreation, AssignExpr.Operator.ASSIGN);
        stmt.addStatement(expr);

        output.ifPresent(o -> {
            //         addResultSink(ctx, this, alphac2r1, "HIGH");
            Expression resultSinkMethodCallExpr = new MethodCallExpr(null,
                                                                     "addResultSink",
                                                                     NodeList.nodeList(
                                                                             new NameExpr("ctx"),
                                                                             new NameExpr(alphaNodeName),
                                                                             new NameExpr(o))); // why is this already quoted?
            stmt.addStatement(resultSinkMethodCallExpr);
        });
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

    public void addToCells(TableCell[][] cells) {
        tableIndex.addToCells(cells, this);
    }
}



