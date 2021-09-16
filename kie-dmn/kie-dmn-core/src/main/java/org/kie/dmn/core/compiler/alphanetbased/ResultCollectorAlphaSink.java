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

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import org.drools.ancompiler.ANCInlineable;
import org.drools.ancompiler.ResultCollectorSink;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.reteoo.LeftInputAdapterNode;
import org.drools.core.reteoo.ModifyPreviousTuples;
import org.drools.core.reteoo.ObjectSource;
import org.drools.core.reteoo.builder.BuildContext;
import org.drools.core.spi.PropagationContext;
import org.drools.model.functions.Function1;
import org.kie.dmn.feel.lang.EvaluationContext;

public class ResultCollectorAlphaSink extends LeftInputAdapterNode implements ResultCollectorSink,
                                                                              ANCInlineable {

    private final int row;
    private final String columnName;
    private final ResultCollector resultCollector;
    private final Function1<EvaluationContext, Object> outputEvaluationFunction;
    private final String outputClass;

    public ResultCollectorAlphaSink(int id,
                                    ObjectSource source,
                                    BuildContext context,
                                    int row,
                                    String columnName,
                                    ResultCollector resultCollector,
                                    Function1<EvaluationContext, Object> outputEvaluationFunction,
                                    String outputClass) {
        super(id, source, context);
        this.row = row;
        this.columnName = columnName;
        this.resultCollector = resultCollector;
        this.outputEvaluationFunction = outputEvaluationFunction;
        this.outputClass = outputClass;
    }


    // TODO DT-ANC not sure this should be the same class
    public ResultCollectorAlphaSink(int row,
                                    String columnName,
                                    ResultCollector resultCollector,
                                    Function1<EvaluationContext, Object> outputEvaluationFunction) {
        this.row = row;
        this.columnName = columnName;
        this.resultCollector = resultCollector;
        this.outputEvaluationFunction = outputEvaluationFunction;
        this.outputClass = ""; // used only at compilation time
    }

    @Override
    public void assertObject(InternalFactHandle factHandle, PropagationContext propagationContext, InternalWorkingMemory workingMemory) {
        resultCollector.addResult(row, columnName, outputEvaluationFunction);
    }

    @Override
    public void modifyObject(InternalFactHandle factHandle, ModifyPreviousTuples modifyPreviousTuples, PropagationContext context, InternalWorkingMemory workingMemory) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void byPassModifyToBetaNode(InternalFactHandle factHandle, ModifyPreviousTuples modifyPreviousTuples, PropagationContext context, InternalWorkingMemory workingMemory) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Expression createJavaMethod() {
        ObjectCreationExpr objectCreationExpr = new ObjectCreationExpr();

        objectCreationExpr.setType(StaticJavaParser.parseClassOrInterfaceType(this.getClass().getCanonicalName()));
        objectCreationExpr.addArgument(new IntegerLiteralExpr(row));
        objectCreationExpr.addArgument(new StringLiteralExpr(columnName));
        objectCreationExpr.addArgument(StaticJavaParser.parseExpression("ctx.getResultCollector()"));

        Expression lambdaExpr = StaticJavaParser.parseExpression(String.format("(org.kie.dmn.feel.lang.EvaluationContext x) -> %s.getInstance().apply(x)", outputClass));
        objectCreationExpr.addArgument(lambdaExpr);

        return objectCreationExpr;
    }

    @Override
    public void collectObject() {
        resultCollector.addResult(row, columnName, outputEvaluationFunction);
    }
}
