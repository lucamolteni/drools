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

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.VoidType;
import org.drools.core.common.NetworkNode;
import org.drools.core.reteoo.AlphaNode;
import org.drools.core.reteoo.Sink;

import static com.github.javaparser.StaticJavaParser.parseExpression;
import static com.github.javaparser.StaticJavaParser.parseStatement;
import static com.github.javaparser.StaticJavaParser.parseType;
import static com.github.javaparser.ast.NodeList.nodeList;
import static org.drools.ancompiler.AbstractCompilerHandler.getVariableName;
import static org.drools.ancompiler.AbstractCompilerHandler.getVariableType;

/**
 * This handler is used to inline the creation of a constraint and a ResultCollectorSink
 * So that it doesn't depend on the RETE at runtime
 */
public class InlineFieldReferenceInitHandler {

    private static final String PARAM_TYPE = NetworkNode.class.getName();
    private static final String METHOD_NAME = "setNetworkNodeReference";
    private static final String PARAM_NAME = "node";

    private static final String statementCall = "        " +
            " {" +
            "   setNetworkNodeN(node);\n" +
            "}";

    private final List<NetworkNode> nodes;

    public InlineFieldReferenceInitHandler(List<NetworkNode> nodes) {
        this.nodes = nodes;
    }

    public void emitCode(StringBuilder builder) {

        List<MethodDeclaration> allMethods = new ArrayList<>();

        MethodDeclaration methodDeclaration = new MethodDeclaration(
                nodeList(Modifier.protectedModifier()),
                METHOD_NAME,
                new VoidType(),
                nodeParameter()
        );

        allMethods.add(methodDeclaration);
        BlockStmt setNetworkNodeReference = methodDeclaration.getBody().orElseThrow(() -> new RuntimeException("No block statement"));

        List<List<NetworkNode>> partitionedNodes = ListUtils.partition(nodes, 20);

        for (int i = 0; i < partitionedNodes.size(); i++) {
            List<NetworkNode> subNodes = partitionedNodes.get(i);
            MethodDeclaration m = generateSwitchForSubNodes(i, subNodes, setNetworkNodeReference);
            allMethods.add(m);
        }

        for (MethodDeclaration md : allMethods) {
            builder.append(md.toString());
            builder.append("\n");
        }
    }

    private NodeList<Parameter> nodeParameter() {
        return nodeList(new Parameter(parseType(PARAM_TYPE), "node"));
    }

    private MethodDeclaration generateSwitchForSubNodes(int partitionIndex,
                                                        List<NetworkNode> subNodes,
                                                        BlockStmt setNetworkNodeReferenceBody) {
        String setFieldNode = "setNetworkNode" + partitionIndex;

        BlockStmt setFieldStatementCall = StaticJavaParser.parseBlock(statementCall);
        setFieldStatementCall.findAll(MethodCallExpr.class, mc -> mc.getNameAsString().equals("setNetworkNodeN"))
                .forEach(n -> n.setName(new SimpleName("setNetworkNode" + partitionIndex)));

        setFieldStatementCall.getStatements().forEach(setNetworkNodeReferenceBody::addStatement);

        MethodDeclaration switchMethod = new MethodDeclaration(nodeList(Modifier.privateModifier()),
                                                               setFieldNode,
                                                               PrimitiveType.booleanType(),
                                                               nodeParameter()
        );

        BlockStmt statements = switchMethod.getBody().orElseThrow(() -> new RuntimeException("No"));
        generateSetterBody(statements, subNodes);

        return switchMethod;
    }

    private void generateSetterBody(BlockStmt statements, List<NetworkNode> subNodes) {
        for (NetworkNode n : subNodes) {

            String assignStatementString;
            if(n instanceof ANCInlineable) {
                // TODO DT-ANC avoid toString() and reparse it
                MethodCallExpr javaMethod = ((ANCInlineable) n).createJavaMethod();

                String variableName;
                if(n instanceof AlphaNode) {
                    variableName = getVariableName((AlphaNode) n);
                } else {
                    variableName = getVariableName((Sink)n);
                }

                assignStatementString = String.format("%s = %s;", variableName, javaMethod);
            } else if (n instanceof AlphaNode) {
                assignStatementString = getVariableAssignmentStatementAlphaNode((AlphaNode) n);
            } else {
                assignStatementString = getVariableAssignmentStatement(n);
            }
            Statement assignStmt = parseStatement(assignStatementString);
            statements.addStatement(assignStmt);
        }
    }

    private static String getVariableAssignmentStatement(NetworkNode sink) {
        Class<?> variableType = getVariableType((Sink) sink);
        String assignmentStatement;

        // for non alphas, we just need to cast to the right variable type
        assignmentStatement = getVariableName((Sink) sink) + " = (" + variableType.getCanonicalName() + ")" + InlineFieldReferenceInitHandler.PARAM_NAME + ";";

        return assignmentStatement;
    }

    private static String getVariableAssignmentStatementAlphaNode(AlphaNode alphaNode) {
        Class<?> variableType = getVariableType(alphaNode);
        String assignmentStatement;

        // we need the constraint for an alpha node assignment, so generate a cast, plus the method call to get
        // the constraint
        assignmentStatement = String.format("%s = (%s) ((%s)%s).getConstraint();", getVariableName(alphaNode), variableType.getName(), AlphaNode.class.getName(), InlineFieldReferenceInitHandler.PARAM_NAME);

        return assignmentStatement;
    }
}
