/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.traits.compiler;

import java.util.List;

import org.drools.compiler.KieTraitsCompiler;
import org.drools.compiler.UpdateTypeDeclarationDescr;
import org.drools.core.rule.constraint.EvaluatorConstraint;
import org.drools.core.spi.Constraint;
import org.drools.traits.core.base.evaluators.IsAEvaluatorDefinition;

public class KieTraitsCompilerImpl implements KieTraitsCompiler {


    @Override
    public UpdateTypeDeclarationDescr updateTypeDescr() {
        return new UpdateTraitInformation();
    }

    @Override
    public boolean isAEvaluatorPresent(List<Constraint> constraints) {
        for (Constraint constr : constraints) {
            if (constr instanceof EvaluatorConstraint && ((EvaluatorConstraint) constr).isSelf()) {
                EvaluatorConstraint ec = ((EvaluatorConstraint) constr);
                if (ec.getEvaluator().getOperator() == IsAEvaluatorDefinition.ISA || ec.getEvaluator().getOperator() == IsAEvaluatorDefinition.NOT_ISA) {
                    return true;
                }
            }
        }
        return false;
    }
}
