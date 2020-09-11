/*
* Copyright 2019 Red Hat, Inc. and/or its affiliates.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.kie.dmn.core;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.List;

import org.drools.compiler.kie.builder.impl.DrlProject;
import org.drools.compiler.kie.builder.impl.KieBuilderImpl;
import org.drools.modelcompiler.ExecutableModelProject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.ReleaseId;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.core.alphasupport.DMNProvider;
import org.kie.dmn.core.alphasupport.DecisionTableDMNProvider;
import org.kie.dmn.core.compiler.ExecModelCompilerOption;
import org.kie.dmn.core.util.DMNRuntimeUtil;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;
import org.slf4j.Logger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class DMNDecisionTableAlphaSupportingTest2 extends BaseInterpretedVsCompiledTest {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(DMNDecisionTableAlphaSupportingTest2.class);
    private final boolean useExecModelCompiler;
    private DMNRuntime runtime;
    private DMNModel dmnModel;
    private Resource dmnResource;
    private DMNRuntime dmnRuntime;
    private DMNContext dmnContext;

    private int numberOfDecisionTableRules = 1000;

    public DMNDecisionTableAlphaSupportingTest2(final boolean useExecModelCompiler ) {
        super( useExecModelCompiler );
        this.useExecModelCompiler = useExecModelCompiler;
    }

    @Before
    public void init() throws Exception {
        final DMNProvider dmnProvider = new DecisionTableDMNProvider();

        System.setProperty(ExecModelCompilerOption.PROPERTY_NAME, Boolean.toString(useExecModelCompiler));

        dmnResource = KieServices.get().getResources()
                .newReaderResource(new StringReader(dmnProvider.getDMN(numberOfDecisionTableRules)))
                .setResourceType(ResourceType.DMN)
                .setSourcePath("dmnFile.dmn");
        dmnRuntime = getDMNRuntimeWithResources(false, dmnResource);
        dmnModel = dmnRuntime.getModel("https://github.com/kiegroup/kie-dmn", "decision-table-name");

        dmnContext = dmnRuntime.newContext();
        dmnContext.set("Age", BigDecimal.valueOf(18));
        dmnContext.set("RiskCategory", "Medium");
        dmnContext.set("isAffordable", true);
    }

    @Test
    public void evaluateDecisionTable() {
        DMNResult dmnResult = dmnRuntime.evaluateAll(dmnModel, dmnContext);
        assertFalse(dmnResult.hasErrors());
    }

    public static DMNRuntime getDMNRuntimeWithResources(final boolean useCanonicalModel,
                                                        final Resource... resources) throws IOException {
        final KieContainer kieContainer = BuildtimeUtil.createKieContainerFromResources(useCanonicalModel, resources);
        return kieContainer.newKieSession().getKieRuntime(DMNRuntime.class);
    }



}
