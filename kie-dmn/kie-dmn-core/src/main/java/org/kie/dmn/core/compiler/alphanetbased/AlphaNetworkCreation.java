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

import java.util.UUID;

import org.drools.ancompiler.InlineableAlphaNode;
import org.drools.core.reteoo.AlphaNode;
import org.drools.core.reteoo.ObjectSource;
import org.drools.core.reteoo.builder.BuildUtils;
import org.drools.model.Index;
import org.drools.model.functions.Function1;
import org.drools.model.functions.Predicate1;
import org.drools.model.index.AlphaIndexImpl;
import org.kie.dmn.feel.lang.EvaluationContext;

public class AlphaNetworkCreation {

    private static final BuildUtils buildUtils = new BuildUtils();

    private final AlphaNetworkBuilderContext ctx;

    public AlphaNetworkCreation(AlphaNetworkBuilderContext ctx) {
        this.ctx = ctx;
    }

    private int getNextId() {
        return ctx.buildContext.getNextId();
    }

    public AlphaNode createAlphaNode(ObjectSource source, String id, Predicate1<PropertyEvaluator> predicate) {
        return createAlphaNode(source, id, predicate, null);
    }

    @Deprecated
    public AlphaNode createAlphaNode(ObjectSource source, Predicate1<PropertyEvaluator> predicate, Index index) {
        return createAlphaNode(source, UUID.randomUUID().toString(), predicate, null);
    }

    public <T extends Class<?>> void  addResultSink(ObjectSource source, int row, String columnName, Function1<EvaluationContext, Object> outputEvaluationFunction) {
        ResultCollectorAlphaSink objectSink = new ResultCollectorAlphaSink(getNextId(),
                                                                           source,
                                                                           ctx.buildContext,
                                                                           row,
                                                                           columnName,
                                                                           ctx.resultCollector,
                                                                           outputEvaluationFunction
        );
        source.addObjectSink(objectSink);
    }


    /**
     * IMPORTANT: remember to use the FEEL expression as an Identifier for the same constraint
     * <p>
     * Prefix: column name + value
     */
    // The Alpha Node will be used to generate the ANC and the LambdaConstraint will be inlined using the Alpha Node Id as a reference
    public InlineableAlphaNode createAlphaNode(ObjectSource source, String id, Predicate1<PropertyEvaluator> predicate, Index index) {
        InlineableAlphaNode candidateAlphaNode = InlineableAlphaNode.createBuilder()
                .withConstraint(id, predicate, index, ctx.variable, ctx.declaration)
                .createAlphaNode(getNextId(), source,
                                 ctx.buildContext);

        InlineableAlphaNode sharedAlphaNode = buildUtils.attachNode(ctx.buildContext, candidateAlphaNode);
        System.out.println(String.format("Created Alpha Node id: %s - %s", sharedAlphaNode.getId(), id));
        return sharedAlphaNode;
    }

    public static <I> AlphaIndexImpl<PropertyEvaluator, I> createIndex(Class<I> indexedClass, Function1<PropertyEvaluator, I> leftExtractor, I rightValue) {
        return new AlphaIndexImpl<>(indexedClass, Index.ConstraintType.EQUAL, 1, leftExtractor, rightValue);
    }
}
