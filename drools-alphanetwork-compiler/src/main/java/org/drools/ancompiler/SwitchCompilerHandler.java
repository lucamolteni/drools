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

package org.drools.ancompiler;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Stream;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.reteoo.AlphaNode;
import org.drools.core.reteoo.ModifyPreviousTuples;
import org.drools.core.rule.IndexableConstraint;
import org.drools.core.spi.InternalReadAccessor;
import org.drools.core.spi.PropagationContext;
import org.drools.core.util.index.AlphaRangeIndex;

import static com.github.javaparser.StaticJavaParser.parseType;
import static com.github.javaparser.ast.NodeList.nodeList;

public abstract class SwitchCompilerHandler extends AbstractCompilerHandler {

    protected static final String MODIFY_PREVIOUS_TUPLE_TYPE = ModifyPreviousTuples.class.getCanonicalName();

    public ClassOrInterfaceType modifyPreviousTuplesType() {
        return StaticJavaParser.parseClassOrInterfaceType(MODIFY_PREVIOUS_TUPLE_TYPE);
    }

    protected static final String MODIFY_PREVIOUS_TUPLE_PARAM_NAME = "modifyPreviousTuples";

    protected final StringBuilder builder;
    private Class<?> fieldType;

    static final String LOCAL_FACT_VAR_NAME = "fact";
    protected static final String FACT_HANDLE_PARAM_TYPE = InternalFactHandle.class.getCanonicalName();

    public ClassOrInterfaceType factHandleType() {
        return StaticJavaParser.parseClassOrInterfaceType(FACT_HANDLE_PARAM_TYPE);
    }

    protected static final String PROP_CONTEXT_PARAM_TYPE = PropagationContext.class.getName();

    public ClassOrInterfaceType propagationContextType() {
        return StaticJavaParser.parseClassOrInterfaceType(PROP_CONTEXT_PARAM_TYPE);
    }

    protected static final String WORKING_MEMORY_PARAM_TYPE = InternalWorkingMemory.class.getName();

    public ClassOrInterfaceType workingMemoryType() {
        return StaticJavaParser.parseClassOrInterfaceType(WORKING_MEMORY_PARAM_TYPE);
    }

    static final String FACT_HANDLE_PARAM_NAME = "handle";
    static final String PROP_CONTEXT_PARAM_NAME = "context";
    static final String WORKING_MEMORY_PARAM_NAME = "wm";

    protected  BlockStmt statements = new BlockStmt();
    protected Deque<SwitchStmt> switchStatements = new ArrayDeque<>();
    protected Deque<SwitchEntry> switchEntries = new ArrayDeque<>();

    protected Deque<IfStmt> ifStatements = new ArrayDeque<>();

    protected SwitchCompilerHandler(StringBuilder builder) {
        this.builder = builder;
    }

    protected void generateSwitch(IndexableConstraint indexableConstraint) {
        final InternalReadAccessor fieldExtractor = indexableConstraint.getFieldExtractor();
        fieldType = fieldExtractor.getExtractToClass();

        if (canInlineValue()) {

            String switchVariableName = "switchVar";
            ExpressionStmt switchVariable = localVariableWithCastInitializer(parseType(fieldType.getCanonicalName()),
                                                                             switchVariableName,
                                                                             new MethodCallExpr(new NameExpr("readAccessor"),
                                                                                                "getValue",
                                                                                                nodeList(new NameExpr(LOCAL_FACT_VAR_NAME))));


            this.statements.addStatement(switchVariable);
            SwitchStmt switchStmt = new SwitchStmt().setSelector(new NameExpr(switchVariableName));

            Statement nullCheck;
            if (fieldType.isPrimitive()) {
                nullCheck = new BlockStmt().addStatement(switchStmt);
            } else {
                nullCheck = new IfStmt()
                        .setCondition(new BinaryExpr(new NameExpr(switchVariableName), new NullLiteralExpr(), BinaryExpr.Operator.NOT_EQUALS))
                        .setThenStmt(switchStmt);
            }

            this.statements.addStatement(nullCheck);
            this.switchStatements.push(switchStmt);

        } else { // Hashable but not inlinable

            String localVariableName = "NodeId";

            ExpressionStmt expressionStmt = localVariableWithCastInitializer(parseType("java.lang.Integer"),
                                                                             localVariableName,
                                                                             new MethodCallExpr(new NameExpr(getVariableName()), "get", nodeList(
                                                                                     new MethodCallExpr(
                                                                                             new NameExpr("readAccessor"),
                                                                                             "getValue",
                                                                                             nodeList(new NameExpr(LOCAL_FACT_VAR_NAME))
                                                                                     ))));

            this.statements.addStatement(expressionStmt);

            SwitchStmt switchStmt = new SwitchStmt().setSelector(new MethodCallExpr(new NameExpr(localVariableName), "intValue", nodeList()));

            // ensure that the value is present in the node map
            Statement nullCheck  = new IfStmt()
                        .setCondition(new BinaryExpr(new NameExpr(localVariableName), new NullLiteralExpr(), BinaryExpr.Operator.NOT_EQUALS))
                        .setThenStmt(switchStmt);

            this.statements.addStatement(nullCheck);
            this.switchStatements.push(switchStmt);
        }
    }

    protected SwitchEntry generateSwitchCase(AlphaNode hashedAlpha, Object hashedValue) {
        SwitchEntry switchEntry = new SwitchEntry();

        if (canInlineValue()) {
            final Expression quotedHashedValue;
            if (hashedValue instanceof String) {
                quotedHashedValue = new StringLiteralExpr((String) hashedValue);
            } else if (hashedValue instanceof Long) {
                quotedHashedValue = new LongLiteralExpr((Long) hashedValue);
            } else {
                quotedHashedValue = new IntegerLiteralExpr((Integer) hashedValue);
            }

            switchEntry.setLabels(nodeList(quotedHashedValue));
        } else {
            switchEntry.setLabels(nodeList(new IntegerLiteralExpr(hashedAlpha.getId())));
        }
        switchStatements.getFirst().getEntries().add(switchEntry);
        return switchEntry;
    }

    protected boolean canInlineValue() {
        return Stream.of(String.class, Integer.class, int.class).anyMatch(c -> c.isAssignableFrom(fieldType));
    }

    @Override
    public void nullCaseAlphaNodeStart(AlphaNode hashedAlpha) {
        if (canInlineValue()) {
            builder.append("else { ");
        }
    }

    @Override
    public void nullCaseAlphaNodeEnd(AlphaNode hashedAlpha) {
        if (canInlineValue()) {
            builder.append("}").append(NEWLINE);
        }
    }

    //  type variableName = (type) sourceObject.methodName();
    protected ExpressionStmt localVariableWithCastInitializer(Type type, String variableName, MethodCallExpr source) {
        return new ExpressionStmt(
                new VariableDeclarationExpr(
                        new VariableDeclarator(type, variableName,
                                               new CastExpr(type, source))));
    }

    //  type variableName = (type) sourceObject.methodName();
    protected ExpressionStmt localVariable(Type type, String variableName, MethodCallExpr source) {
        return new ExpressionStmt(
                new VariableDeclarationExpr(
                        new VariableDeclarator(type, variableName,
                                               source)));
    }

    protected void generateRangeIndexForSwitch(AlphaRangeIndex alphaRangeIndex) {
        String rangeIndexVariableName = getRangeIndexVariableName(alphaRangeIndex, getMinIdFromRangeIndex(alphaRangeIndex));
        String matchingResultVariableName = rangeIndexVariableName + "_result";
        String matchingNodeVariableName = matchingResultVariableName + "_node";

        ExpressionStmt matchingResultVariable = localVariable(parseType("java.util.Collection<org.drools.core.reteoo.AlphaNode>"),
                                                              matchingResultVariableName,
                                                              new MethodCallExpr(new NameExpr(rangeIndexVariableName),
                                                                                 "getMatchingAlphaNodes",
                                                                                 nodeList(new MethodCallExpr(new NameExpr(FACT_HANDLE_PARAM_NAME), "getObject"))));

        final BlockStmt currentStatement = getStatementToAdd();

        currentStatement.addStatement(matchingResultVariable);

        BlockStmt body = new BlockStmt();
        ForEachStmt forEachStmt = new ForEachStmt(new VariableDeclarationExpr(parseType("org.drools.core.reteoo.AlphaNode"), matchingNodeVariableName),
                                                  new NameExpr(matchingResultVariableName), body);

        currentStatement.addStatement(forEachStmt);

        SwitchStmt switchStatement = new SwitchStmt().setSelector(new MethodCallExpr(new NameExpr(matchingNodeVariableName), "getId"));
        this.switchStatements.push(switchStatement);
        body.addStatement(switchStatement);
    }


    @Override
    public void startRangeIndex(AlphaRangeIndex alphaRangeIndex) {
        generateRangeIndexForSwitch(alphaRangeIndex);
    }

    @Override
    public void startRangeIndexedAlphaNode(AlphaNode alphaNode) {
        SwitchEntry switchEntry = new SwitchEntry().setLabels(nodeList(new IntegerLiteralExpr(alphaNode.getId())));
        switchEntries.push(switchEntry);
        switchStatements.peek().getEntries().add(switchEntry);
    }

    @Override
    public void endRangeIndexedAlphaNode(AlphaNode alphaNode) {
        addBreakStatement(switchEntries.peek());
    }

    @Override
    public void endRangeIndex(AlphaRangeIndex alphaRangeIndex) {
        SwitchStmt lastSwitch = this.switchStatements.pop();
        // Pop all entries from lastSwitch.
        // If there were only one stack, we could do this in one single operation.
        // TODO LUCA change this
        int lastSwitchEntries = lastSwitch.getEntries().size();
        for(int i = 0; i < lastSwitchEntries; i++) {
            switchEntries.pop();
        }
    }

    @Override
    public void endHashedAlphaNode(AlphaNode hashedAlpha, Object hashedValue) {
        SwitchEntry switchEntry = switchEntries.pop();
        addBreakStatement(switchEntry);
        currentMethod = null;
        switchCaseCounter++;
    }

    private void addBreakStatement(SwitchEntry switchEntry) {
        switchEntry.getStatements().add(new BreakStmt().setValue(null));
    }

    protected MethodDeclaration currentMethod;
    protected final List<MethodDeclaration> extractedMethods = new ArrayList<>();

    protected int switchCaseCounter = 0;


    public BlockStmt getStatementToAdd() {
        IfStmt peek = ifStatements.peek();
        if(peek != null) {
            return peek.getThenStmt().asBlockStmt();
        } else if (switchEntries.peek() != null) {
            return new BlockStmt (switchEntries.peek().getStatements());
        } else {
            return statements;
        }
    }
}
