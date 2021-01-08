/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.VoidType;
import org.drools.core.reteoo.AlphaNode;
import org.drools.core.reteoo.BetaNode;
import org.drools.core.reteoo.LeftInputAdapterNode;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.core.reteoo.Sink;
import org.drools.core.reteoo.WindowNode;
import org.drools.core.rule.IndexableConstraint;

import static com.github.javaparser.StaticJavaParser.parseStatement;
import static com.github.javaparser.ast.NodeList.nodeList;

public class ModifyHandler extends SwitchCompilerHandler {

    private static final String ASSERT_METHOD_SIGNATURE = "public final void propagateModifyObject("
            + FACT_HANDLE_PARAM_TYPE + " " + FACT_HANDLE_PARAM_NAME + ","
            + MODIFY_PREVIOUS_TUPLE_TYPE + " " + MODIFY_PREVIOUS_TUPLE_PARAM_NAME + ","
            + PROP_CONTEXT_PARAM_TYPE + " " + PROP_CONTEXT_PARAM_NAME + ","
            + WORKING_MEMORY_PARAM_TYPE + " " + WORKING_MEMORY_PARAM_NAME + "){";

    /**
     * This flag is used to instruct the AssertHandler to tell it to generate a local varible
     * in the {@link org.drools.ancompiler.CompiledNetwork#assertObject} for holding the value returned
     * from the {@link org.drools.ancompiler.InternalFactHandle#getFactHandle()}.
     *
     * This is only needed if there is at least 1 set of hashed alpha nodes in the network
     */
    private final boolean alphaNetContainsHashedField;

    private final String factClassName;

    public ModifyHandler(StringBuilder builder, String factClassName, boolean alphaNetContainsHashedField) {
        super(builder);
        this.factClassName = factClassName;
        this.alphaNetContainsHashedField = alphaNetContainsHashedField;
    }

    @Override
    public void startObjectTypeNode(ObjectTypeNode objectTypeNode) {
        // we only need to create a reference to the object, not handle, if there is a hashed alpha in the network
        if (alphaNetContainsHashedField) {
            // example of what this will look like
            // ExampleFact fact = (ExampleFact) handle.getObject();

            ClassOrInterfaceType type = StaticJavaParser.parseClassOrInterfaceType(factClassName);
            ExpressionStmt factVariable = localVariableWithCastInitializer(type, LOCAL_FACT_VAR_NAME, new MethodCallExpr(new NameExpr(FACT_HANDLE_PARAM_NAME), "getObject"));

            statements.addStatement(factVariable);
        }
    }

    private Statement modifyMethod(Sink sink) {
        Statement modifyStatement = StaticJavaParser.parseStatement("ALPHATERMINALNODE.modifyObject(handle, modifyPreviousTuples, context, wm);");
        replaceNameExpr(modifyStatement, "ALPHATERMINALNODE", getVariableName(sink));
        return modifyStatement;
    }


    @Override
    public void startBetaNode(BetaNode betaNode) {
        getStatementToAdd().addStatement((modifyMethod(betaNode)));
    }

    @Override
    public void startWindowNode(WindowNode windowNode) {
        getStatementToAdd().addStatement((modifyMethod(windowNode)));
    }


    @Override
    public void startLeftInputAdapterNode(Object parent, LeftInputAdapterNode leftInputAdapterNode) {
        getStatementToAdd().addStatement(modifyMethod(leftInputAdapterNode));
    }

    @Override
    public void startNonHashedAlphaNode(AlphaNode alphaNode) {

        IfStmt ifStatement = parseStatement("if (CONSTRAINT.isAllowed(handle, wm)) { }").asIfStmt();

        replaceNameExpr(ifStatement, "CONSTRAINT", getVariableName(alphaNode));

        getStatementToAdd().addStatement(ifStatement);

        ifStatements.push(ifStatement);
    }


    @Override
    public void endNonHashedAlphaNode(AlphaNode alphaNode) {
        ifStatements.pop();
    }


    @Override
    public void startHashedAlphaNodes(IndexableConstraint indexableConstraint) {
        generateSwitch(indexableConstraint);
    }

    @Override
    public void startHashedAlphaNode(AlphaNode hashedAlpha, Object hashedValue) {
        SwitchEntry switchEntry = generateSwitchCase(hashedAlpha, hashedValue);
        this.switchEntries.push(switchEntry);
    }


    public void emitCode() {

        MethodDeclaration propagateAssertObject =
                new MethodDeclaration()
                        .setModifiers(Modifier.Keyword.PUBLIC, Modifier.Keyword.FINAL)
                        .setType(new VoidType())
                        .setName("propagateModifyObject")
                        .setParameters(modifyObjectParameters());

        BlockStmt body = new BlockStmt();
        propagateAssertObject.setBody(body);

        body.addStatement(StaticJavaParser.parseStatement("if(logger.isDebugEnabled()) {\n" +
                                                                  "            logger.debug(\"Propagate modify object on compiled alpha network {} {} {}\", handle, context, wm);\n" +
                                                                  "        }\n"));

        for (Statement s : statements.getStatements()) {
            body.addStatement(s);
        }

        String methodBody = propagateAssertObject.toString();
        builder.append(methodBody);

        builder.append(NEWLINE);
        for (MethodDeclaration s : extractedMethods) {
            builder.append(s.toString());
        }
    }

    private NodeList<Parameter> modifyObjectParameters() {
        return nodeList(new Parameter(factHandleType(), FACT_HANDLE_PARAM_NAME),
                        new Parameter(modifyPreviousTuplesType(), MODIFY_PREVIOUS_TUPLE_PARAM_NAME),
                        new Parameter(propagationContextType(), PROP_CONTEXT_PARAM_NAME),
                        new Parameter(workingMemoryType(), WORKING_MEMORY_PARAM_NAME));
    }
}
