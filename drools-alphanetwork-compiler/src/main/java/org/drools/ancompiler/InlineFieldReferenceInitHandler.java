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
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.VoidType;
import org.drools.core.common.NetworkNode;
import org.drools.core.reteoo.AlphaNode;
import org.drools.core.reteoo.Sink;

import static com.github.javaparser.StaticJavaParser.parseStatement;
import static com.github.javaparser.ast.NodeList.nodeList;
import static org.drools.ancompiler.AbstractCompilerHandler.getVariableName;

/**
 * This handler is used to inline the creation of a constraint and a ResultCollectorSink
 * So that it doesn't depend on the RETE at runtime
 */
public class InlineFieldReferenceInitHandler {

    private static final String METHOD_NAME = "initConstraintsResults";

    private static final String statementCall = "        " +
            " {" +
            "   initNodeN();\n" +
            "}";

    private final List<NetworkNode> nodes;

    public InlineFieldReferenceInitHandler(List<NetworkNode> nodes) {
        this.nodes = nodes;
    }

    public void emitCode(StringBuilder builder) {

        List<MethodDeclaration> allMethods = new ArrayList<>();

        MethodDeclaration methodDeclaration = new MethodDeclaration(
                nodeList(Modifier.publicModifier()),
                new VoidType(),
                METHOD_NAME
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

    private MethodDeclaration generateSwitchForSubNodes(int partitionIndex,
                                                        List<NetworkNode> subNodes,
                                                        BlockStmt setNetworkNodeReferenceBody) {
        String setFieldNode = "initNodeN" + partitionIndex;

        BlockStmt setFieldStatementCall = StaticJavaParser.parseBlock(statementCall);
        setFieldStatementCall.findAll(MethodCallExpr.class, mc -> mc.getNameAsString().equals("initNodeN"))
                .forEach(n -> n.setName(new SimpleName("initNodeN" + partitionIndex)));

        setFieldStatementCall.getStatements().forEach(setNetworkNodeReferenceBody::addStatement);

        MethodDeclaration switchMethod = new MethodDeclaration(nodeList(Modifier.publicModifier()),
                                                               new VoidType(),
                                                               setFieldNode
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
                Expression javaMethod = ((ANCInlineable) n).createJavaMethod();

                String variableName;
                if(n instanceof AlphaNode) {
                    variableName = getVariableName((AlphaNode) n);
                } else {
                    variableName = getVariableName((Sink)n);
                }

                assignStatementString = String.format("%s = %s;", variableName, javaMethod);
            } else if (n instanceof AlphaNode) {
                assignStatementString = "";
            } else {
                assignStatementString = "";
            }
            Statement assignStmt = parseStatement(assignStatementString);
            statements.addStatement(assignStmt);
        }
    }
}
