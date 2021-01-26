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

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ArrayType;
import org.kie.dmn.core.compiler.DMNCompilerContext;
import org.kie.dmn.core.compiler.execmodelbased.DTableModel;
import org.kie.dmn.core.impl.DMNModelImpl;
import org.kie.dmn.feel.codegen.feel11.CodegenStringUtil;
import org.kie.dmn.model.api.DecisionTable;
import org.kie.dmn.model.api.InputClause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.javaparser.StaticJavaParser.parseType;
import static org.kie.dmn.core.compiler.generators.GeneratorsUtil.getDecisionTableName;
import static org.kie.dmn.feel.codegen.feel11.CodegenStringUtil.replaceSimpleNameWith;

public class DMNAlphaNetworkCompiler {

    private static final Logger logger = LoggerFactory.getLogger(DMNAlphaNetworkCompiler.class);

    private final DMNCompilerContext ctx;
    private final DMNModelImpl model;
    private final TableCell.TableCellFactory tableCellFactory;

    private CompilationUnit template;
    private ClassOrInterfaceDeclaration dmnAlphaNetworkClass;


    public DMNAlphaNetworkCompiler(DMNCompilerContext ctx,
                                   DMNModelImpl model,
                                   TableCell.TableCellFactory tableCellFactory) {
        this.ctx = ctx;
        this.model = model;
        this.tableCellFactory = tableCellFactory;
    }

    public GeneratedSources generateSourceCode(String dtName, DecisionTable decisionTable) {

        GeneratedSources generatedSources = new GeneratedSources();

        String decisionTableName = getDecisionTableName(dtName, decisionTable);
        String escapedDecisionTableName = String.format("DMNAlphaNetwork_%s", CodegenStringUtil.escapeIdentifier(decisionTableName));

        initTemplate();
        setDMNAlphaNetworkClassName(escapedDecisionTableName);
        initPropertyNames(decisionTable.getInput());

        DTableModel dTableModel = new DTableModel(ctx.getFeelHelper(), model, dtName, decisionTableName, decisionTable);

        TableCells tableCells = parseCells(dTableModel);

        BlockStmt alphaNetworkStatements = new BlockStmt();

        generatedSources.addTableCells(tableCells);

        tableCells.addAlphaNetworkNode(alphaNetworkStatements, generatedSources);

        BlockStmt alphaNetworkBlock = dmnAlphaNetworkClass
                .findFirst(BlockStmt.class, DMNAlphaNetworkCompiler::blockHasComment)
                .orElseThrow(RuntimeException::new);

        alphaNetworkBlock.replace(alphaNetworkStatements);

        String alphaNetworkClassWithPackage = String.format("org.kie.dmn.core.alphasupport.%s", escapedDecisionTableName);
        generatedSources.addNewAlphaNetworkClass(alphaNetworkClassWithPackage, template.toString());

        generatedSources.logGeneratedClasses();

        return generatedSources;
    }

    private static boolean blockHasComment(BlockStmt block) {
        return block.getComment().filter(c -> " Alpha network creation statements".equals(c.getContent()))
                .isPresent();
    }

    private void initTemplate() {
        template = getMethodTemplate();
        dmnAlphaNetworkClass = template.getClassByName("DMNAlphaNetworkTemplate")
                .orElseThrow(() -> new RuntimeException("Cannot find class"));
        dmnAlphaNetworkClass.removeComment();
    }

    private void setDMNAlphaNetworkClassName(String escapedDecisionTableName) {
        replaceSimpleNameWith(dmnAlphaNetworkClass, "DMNAlphaNetworkTemplate", escapedDecisionTableName);
    }

    private void initPropertyNames(List<InputClause> input) {

        NodeList<Expression> propertyNamesArray = input.stream()
                .map(inputClause -> inputClause.getInputExpression().getText())
                .map(StringLiteralExpr::new)
                .collect(Collectors.toCollection(NodeList::new));

        ArrayCreationExpr array = new ArrayCreationExpr()
                .setElementType(new ArrayType(parseType(String.class.getCanonicalName())))
                .setInitializer(new ArrayInitializerExpr(propertyNamesArray));

        template.findAll(StringLiteralExpr.class, n -> n.asString().equals("PROPERTY_NAMES"))
                .forEach(r -> r.replace(array));
    }

    public TableCells parseCells(DTableModel dTableModel) {

        List<DTableModel.DRowModel> rows = dTableModel.getRows();
        List<DTableModel.DColumnModel> columns = dTableModel.getColumns();
        TableCells tableCells = new TableCells(rows.size(), columns.size());

        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            DTableModel.DRowModel row = rows.get(rowIndex);

            for (int inputColumnIndex = 0; inputColumnIndex < row.getInputs().size(); inputColumnIndex++) {
                String input = row.getInputs().get(inputColumnIndex);
                TableIndex tableIndex = new TableIndex(rowIndex, inputColumnIndex);
                DTableModel.DColumnModel column = tableIndex.getColumn(columns);
                TableCell cell = tableCellFactory.createInputCell(tableIndex,
                                                            column,
                                                            input);

                if(inputColumnIndex == row.getInputs().size() - 1) { // last column
                    cell.setOutput(row.getOutputs().get(0)); // assume only one output
                }

                tableCells.add(cell);
            }


        }
        return tableCells;
    }

    private CompilationUnit getMethodTemplate() {
        InputStream resourceAsStream = this.getClass()
                .getResourceAsStream("/org/kie/dmn/core/alphasupport/DMNAlphaNetworkTemplate.java");
        return StaticJavaParser.parse(resourceAsStream);
    }
}
