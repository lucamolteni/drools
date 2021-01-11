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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.drools.core.common.NamedEntryPoint;
import org.drools.core.reteoo.CompositeObjectSinkAdapter;
import org.drools.core.reteoo.ObjectSinkPropagator;
import org.drools.core.reteoo.ObjectTypeNode;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.builder.conf.AlphaNetworkCompilerOption;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LargeAlphaNetworkTest extends BaseModelTest {

    public LargeAlphaNetworkTest(RUN_TYPE testRunType) {
        super(testRunType);
    }

    @Test
    public void testVeryLargeAlphaNetwork() {
        final String str =
                "global java.util.List results;\n" +
                        "import " + Person.class.getCanonicalName() + ";\n" +
                        "rule minus18 when\n" +
                        "    $p : Person( name == \"a\", age < 18 )\n" +
                        "then\n" +
                        " results.add($p);\n" +
                        "end\n" +
                        "rule between1825 when\n" +
                        "    $p : Person( name == \"a\", age >= 18, age <= 25 )\n" +
                        "then\n" +
                        " results.add($p);\n" +
                        "end\n" +
                        "rule between2530 when\n" +
                        "    $p : Person( name == \"a\", age >= 25, age <= 30 )\n" +
                        "then\n" +
                        " results.add($p);\n" +
                        "end\n" +
                        "rule greaterThan30 when\n" +
                        "    $p : Person( name == \"a\", age > 30 )\n" +
                        "then\n" +
                        " results.add($p);\n" +
                        "end\n";

        KieSession ksession = getKieSession(str);
        ArrayList<Object> results = new ArrayList<>();
        ksession.setGlobal("results", results);
        Person a = new Person("a", 15);
        Person b = new Person("a", 26);
        ksession.insert(a);
        ksession.insert(b);

        try {
            int rulesFired = ksession.fireAllRules();
            Assertions.assertThat(results).contains(a);
            Assertions.assertThat(results).contains(b);
        } finally {
            ksession.dispose();
        }
    }
}
