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

package org.kie.dmn.core.alphanetwork;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.ReleaseId;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieRuntimeFactory;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.core.compiler.AlphaNetworkOption;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.kie.dmn.core.classloader.DMNClassloaderTest.getPom;

@RunWith(Parameterized.class)
public class AlphaNetworkSupportInLargeDecisionTableTest {

    @Parameterized.Parameters(name = "{0}")
    public static Object[] params() {
        return new Object[]{true, false};
    }

    private final boolean useAlphaNetwork;

    public AlphaNetworkSupportInLargeDecisionTableTest(final boolean useAlphaNetwork) {
        this.useAlphaNetwork = useAlphaNetwork;
    }

    @Test
    public void evaluateDecisionTable() {
        final DecisionTableDMNProvider dmnProvider = new DecisionTableDMNProvider();

        System.setProperty(AlphaNetworkOption.PROPERTY_NAME, Boolean.toString(useAlphaNetwork));
        KieServices kieServices = KieServices.get();

        final KieServices ks = KieServices.Factory.get();

        final ReleaseId releaseId = ks.newReleaseId("org.kie.dmn.core.alphanetwork", "alphaNetworkSupportInLargeDecisionTable", UUID.randomUUID().toString());

        final KieFileSystem kfs = ks.newKieFileSystem();
        int numberOfDecisionTableRules = 625; // TODO Luca should work with 1000
        Resource dmnResource = kieServices.getResources()
                .newReaderResource(new StringReader(dmnProvider.getDMN(numberOfDecisionTableRules)))
                .setResourceType(ResourceType.DMN)
                .setSourcePath("dmnFile.dmn");

        kfs.write(dmnResource);
        kfs.writePomXML(getPom(releaseId));

        final KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
        assertTrue(kieBuilder.getResults().getMessages().toString(), kieBuilder.getResults().getMessages().isEmpty());

        final KieContainer container = ks.newKieContainer(releaseId);
        DMNRuntime dmnRuntime = KieRuntimeFactory.of(container.getKieBase()).get(DMNRuntime.class);

        DMNModel dmnModel = dmnRuntime.getModel("https://github.com/kiegroup/kie-dmn", "decision-table-name");

        DMNContext dmnContext = dmnRuntime.newContext();
        dmnContext.set("Age", BigDecimal.valueOf(18));
        dmnContext.set("RiskCategory", "Medium");
        dmnContext.set("isAffordable", true);

        DMNResult dmnResult = dmnRuntime.evaluateAll(dmnModel, dmnContext);
        assertFalse(dmnResult.hasErrors());
    }
}
