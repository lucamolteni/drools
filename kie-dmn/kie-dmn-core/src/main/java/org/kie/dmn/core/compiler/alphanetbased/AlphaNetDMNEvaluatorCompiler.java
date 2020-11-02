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

import java.util.Collections;
import java.util.Map;

import org.drools.ancompiler.CompiledNetwork;
import org.drools.ancompiler.CompiledNetworkSource;
import org.drools.ancompiler.ObjectTypeNodeCompiler;
import org.drools.core.reteoo.ObjectTypeNode;
import org.kie.dmn.core.api.DMNExpressionEvaluator;
import org.kie.dmn.core.ast.DMNBaseNode;
import org.kie.dmn.core.compiler.DMNCompilerContext;
import org.kie.dmn.core.compiler.DMNCompilerImpl;
import org.kie.dmn.core.compiler.DMNEvaluatorCompiler;
import org.kie.dmn.core.compiler.execmodelbased.DTableModel;
import org.kie.dmn.core.impl.DMNModelImpl;
import org.kie.dmn.model.api.DecisionTable;
import org.kie.memorycompiler.KieMemoryCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.dmn.core.compiler.generators.GeneratorsUtil.getDecisionTableName;

public class AlphaNetDMNEvaluatorCompiler extends DMNEvaluatorCompiler {

    static final Logger logger = LoggerFactory.getLogger(AlphaNetDMNEvaluatorCompiler.class);

    public AlphaNetDMNEvaluatorCompiler(DMNCompilerImpl compiler) {
        super(compiler);
    }

    @Override
    protected DMNExpressionEvaluator compileDecisionTable(DMNCompilerContext ctx, DMNModelImpl model, DMNBaseNode node, String dtName, DecisionTable dt) {
        String decisionName = getDecisionTableName(dtName, dt);
        DTableModel dTableModel = new DTableModel(ctx.getFeelHelper(), model, dtName, decisionName, dt);

//        DMNCompiledAlphaNetwork hardCodedAlphaNetwork = new HardCodedAlphaNetwork();

        TableCell.TableCellFactory tableCellFactory = new TableCell.TableCellFactory(ctx.getFeelHelper(), ctx);
        DMNAlphaNetworkCompiler dmnAlphaNetworkCompiler = new DMNAlphaNetworkCompiler(ctx, model, tableCellFactory);
        Map<String, String> allTypesSourceCode = dmnAlphaNetworkCompiler.generateSourceCode(dtName, dt);

        ClassLoader thisDMNClassLoader = this.getClass().getClassLoader();
        Map<String, Class<?>> compiledClasses = KieMemoryCompiler.compile(allTypesSourceCode, thisDMNClassLoader);
        DMNCompiledAlphaNetwork dmnCompiledAlphaNetwork = createAlphaNetworkInstance(compiledClasses);

        dmnCompiledAlphaNetwork.initRete();
        CompiledNetwork compiledAlphaNetwork = dmnCompiledAlphaNetwork.createCompiledAlphaNetwork(this);
        dmnCompiledAlphaNetwork.setCompiledAlphaNetwork(compiledAlphaNetwork);

        return new AlphaNetDMNExpressionEvaluator(dmnCompiledAlphaNetwork)
                .initParameters(ctx.getFeelHelper(), ctx, dTableModel, node);
    }

    public CompiledNetwork createCompiledAlphaNetwork(ObjectTypeNode otn) {
        // Move this outside so that we can debug it

        ObjectTypeNodeCompiler objectTypeNodeCompiler = new ObjectTypeNodeCompiler(otn);
        CompiledNetworkSource compiledNetworkSource = objectTypeNodeCompiler.generateSource();

        Map<String, Class<?>> compiledClasses = KieMemoryCompiler.compile(Collections.singletonMap(
                        compiledNetworkSource.getName(), compiledNetworkSource.getSource()), this.getRootClassLoader());

        Class<?> aClass = compiledClasses.get(compiledNetworkSource.getName());
        return compiledNetworkSource.createInstanceAndSet(aClass);
    }

    // TODO LUCA fix this
    protected DMNCompiledAlphaNetwork createAlphaNetworkInstance(Map<String, Class<?>> compiled) {
        Class<?> inputSetClass = compiled.get("org.kie.dmn.core.alphasupport.DMNAlphaNetwork");
        Object inputSetInstance = null;
        try {
            inputSetInstance = inputSetClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return (DMNCompiledAlphaNetwork) inputSetInstance;
    }
}
