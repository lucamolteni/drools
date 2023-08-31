/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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
package org.drools.ruleunits.dsl;

import java.util.ArrayList;
import java.util.List;

import org.drools.ruleunits.api.DataSource;
import org.drools.ruleunits.api.DataStore;
import org.drools.ruleunits.impl.NamedRuleUnitData;

import static org.drools.model.Index.ConstraintType.EQUAL;

public class DynamicHelloWorldUnit implements RuleUnitDefinition {

    private final DataStore<String> strings;

    private final List<String> results = new ArrayList<>();

    private final String expectedMessage; // this is the dynamic part

    public DynamicHelloWorldUnit(String expectedMessage) {
        this.strings = DataSource.createStore();
        this.expectedMessage = expectedMessage;
    }

    public DataStore<String> getStrings() {
        return strings;
    }


    public List<String> getResults() {
        return results;
    }

    @Override
    public void defineRules(RulesFactory rulesFactory) {
        // /strings[ this == expectedMessage ]
        rulesFactory.rule()
                    .on(strings)
                    .filter(EQUAL, expectedMessage) // when no extractor is provided "this" is implicit
                    .execute(results, r -> r.add("it worked!")); // the consequence can ignore the matched facts
    }
}