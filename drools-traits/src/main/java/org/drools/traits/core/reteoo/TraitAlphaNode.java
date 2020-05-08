package org.drools.traits.core.reteoo;

import java.util.List;

import org.drools.traits.core.base.evaluators.IsAEvaluatorDefinition;
import org.drools.core.reteoo.AlphaNode;
import org.drools.core.reteoo.ObjectSource;
import org.drools.core.reteoo.PropertySpecificUtil;
import org.drools.core.reteoo.builder.BuildContext;
import org.drools.core.rule.constraint.EvaluatorConstraint;
import org.drools.core.spi.AlphaNodeFieldConstraint;
import org.drools.core.util.bitmask.BitMask;
import org.kie.api.runtime.rule.Operator;

public class TraitAlphaNode extends AlphaNode {

    public TraitAlphaNode() {
    }

    public TraitAlphaNode(int id, AlphaNodeFieldConstraint constraint, ObjectSource objectSource, BuildContext context) {
        super(id, constraint, objectSource, context);
    }

    @Override
    public BitMask calculateDeclaredMask(Class modifiedClass, List<String> settableProperties) {
        BitMask mask = constraint.getListenedPropertyMask(modifiedClass, settableProperties);
        if (isTraitEvaluator()) {
            return mask.set(PropertySpecificUtil.TRAITABLE_BIT);
        }
        return mask;
    }

    private boolean isTraitEvaluator() {
        if (constraint instanceof EvaluatorConstraint && ((EvaluatorConstraint) constraint).isSelf()) {
            Operator op = ((EvaluatorConstraint) constraint).getEvaluator().getOperator();
            return op == IsAEvaluatorDefinition.ISA || op == IsAEvaluatorDefinition.NOT_ISA;
        }
        return false;
    }
}
