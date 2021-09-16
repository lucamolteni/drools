/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.kie.dmn.core.alphasupport;

import java.lang.Override;

import org.drools.core.common.DefaultFactHandle;
import org.drools.core.reteoo.AlphaNode;
import org.drools.ancompiler.CompiledNetwork;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.model.Index;
import org.kie.dmn.api.feel.runtime.events.FEELEvent;
import org.kie.dmn.core.compiler.alphanetbased.AlphaNetworkCreation;
import org.kie.dmn.core.compiler.alphanetbased.DMNCompiledAlphaNetworkEvaluator;
import org.kie.dmn.core.compiler.alphanetbased.AlphaNetworkBuilderContext;
import org.kie.dmn.core.compiler.alphanetbased.ResultCollector;
import org.kie.dmn.feel.lang.EvaluationContext;
import org.kie.dmn.core.compiler.alphanetbased.PropertyEvaluator;
import org.kie.dmn.feel.runtime.decisiontables.DecisionTable;
import org.kie.dmn.feel.runtime.decisiontables.HitPolicy;

import static org.kie.dmn.core.compiler.alphanetbased.AlphaNetworkCreation.createIndex;

// All implementations are used only for templating purposes and should never be called
public class DMNAlphaNetworkTemplate implements DMNCompiledAlphaNetworkEvaluator {

    protected final ResultCollector resultCollector = new ResultCollector();
    protected CompiledNetwork compiledNetwork;

    protected final AlphaNetworkBuilderContext builderContext = new AlphaNetworkBuilderContext(resultCollector);
    protected final AlphaNetworkCreation alphaNetworkCreation = new AlphaNetworkCreation(builderContext);

    private final HitPolicy hitPolicy = HitPolicy.fromString("HIT_POLICY_NAME");

    protected PropertyEvaluator propertyEvaluator;

    @Override
    public void setCompiledNetwork(CompiledNetwork compiledAlphaNetwork) {
        this.compiledNetwork = compiledAlphaNetwork;
    }

    @Override
    public ObjectTypeNode getObjectTypeNode() {
        return builderContext.otn;
    }

    public PropertyEvaluator getOrCreatePropertyEvaluator(EvaluationContext evaluationContext) {
        if(propertyEvaluator == null) {
            propertyEvaluator = new PropertyEvaluator(evaluationContext, "PROPERTY_NAMES");
        }
        return propertyEvaluator;
    }

    @Override
    public FEELEvent validate(EvaluationContext evaluationContext) {
        PropertyEvaluator propertyEvaluator = getOrCreatePropertyEvaluator(evaluationContext);

        // Validation Column
        {
            FEELEvent resultValidation0 =
                    ValidatorC0.getInstance().validate(evaluationContext,
                                                       propertyEvaluator.getValue(777));
            if (resultValidation0 != null) {
                return resultValidation0;
            }
        }

        return null;
    }


    @Override
    public Object evaluate(EvaluationContext evaluationContext, DecisionTable decisionTable) {


        // Clean previous results
        resultCollector.clearResults();

        // init CompiledNetwork with object needed for results,
        compiledNetwork.init(builderContext);

        // create lambda constraints and results
        compiledNetwork.initConstraintsResults();

        // Fire rete network
        compiledNetwork.propagateAssertObject(new DefaultFactHandle(getOrCreatePropertyEvaluator(evaluationContext)), null, null);

        // Find result with Hit Policy applied
        Object result = resultCollector.applyHitPolicy(evaluationContext, hitPolicy, decisionTable);

        return result;
    }

    @Override
    public ResultCollector getResultCollector() {
        return resultCollector;
    }
}
