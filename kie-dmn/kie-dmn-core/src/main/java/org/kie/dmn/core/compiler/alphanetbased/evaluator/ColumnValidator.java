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
package org.kie.dmn.core.compiler.alphanetbased.evaluator;

import java.util.Collection;
import java.util.List;

import org.kie.dmn.api.core.DMNType;
import org.kie.dmn.api.feel.runtime.events.FEELEvent;
import org.kie.dmn.feel.lang.EvaluationContext;
import org.kie.dmn.feel.runtime.UnaryTest;
import org.kie.dmn.feel.runtime.events.InvalidInputEvent;

public abstract class ColumnValidator {

    protected abstract List<UnaryTest> inputTests();

    protected abstract DMNType dmnType();

    protected abstract String validValues();

    protected abstract String columnName();

    protected abstract String decisionTableName();

    public FEELEvent validate(EvaluationContext ctx, Object actualValue) {
        if (inputTests() != null) {
            boolean satisfies = true;
            if (dmnType() != null && dmnType().isCollection() && actualValue instanceof Collection) {
                for (Object parameterItem : (Collection<?>) actualValue) {
                    satisfies &= inputTests().stream().map(ut -> ut.apply(ctx, parameterItem)).filter(x -> x != null && x).findAny().orElse(false);
                }
            } else {
                satisfies = inputTests().stream().map(ut -> ut.apply(ctx, actualValue)).filter(x -> x != null && x).findAny().orElse(false);
            }
            if (!satisfies) {
                return new InvalidInputEvent(FEELEvent.Severity.ERROR,
                                             String.format("%s='%s' does not match any of the valid values %s for decision table '%s'.", columnName(), actualValue, validValues(), decisionTableName()),
                                             decisionTableName(),
                                             null,
                                             validValues());
            }
        }
        return null;
    }
}
