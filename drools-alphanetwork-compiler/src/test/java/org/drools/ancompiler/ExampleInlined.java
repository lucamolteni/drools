package org.drools.ancompiler;

import java.util.ArrayList;

public class ExampleInlined extends CompiledNetwork {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ExampleInlined.class);
    org.drools.core.spi.InternalReadAccessor readAccessor;
    private org.drools.modelcompiler.constraints.LambdaConstraint lambdaConstraint4; // [AlphaNode(4) constraint=Constraint for 'GENERATED_4C446D3FA136F2604FFB740C15D17579' (index: null)]
    private org.drools.core.reteoo.AlphaTerminalNode alphaTerminalNode5; // [AlphaTerminalNode(5)]
    private org.drools.modelcompiler.constraints.LambdaConstraint lambdaConstraint7; // [AlphaNode(7) constraint=Constraint for 'GENERATED_1DF411A71B7C80D4D462E604BFEC5805' (index: null)]
    private org.drools.core.reteoo.AlphaTerminalNode alphaTerminalNode8; // [AlphaTerminalNode(8)]

    public ExampleInlined(org.drools.core.spi.InternalReadAccessor readAccessor, java.util.Map<String, org.drools.core.util.index.AlphaRangeIndex> rangeIndexDeclarationMap) {
        this.readAccessor = readAccessor;
    }

    protected void setNetworkNodeReference(org.drools.core.common.NetworkNode node) {
        boolean setNetworkResult0 = setNetworkNode0(node);
        if (setNetworkResult0) {
            return;
        }
    }

    private boolean setNetworkNode0(org.drools.core.common.NetworkNode node) {
        switch (node.getId()) {
            case 4:
                lambdaConstraint4 = (org.drools.modelcompiler.constraints.LambdaConstraint) ((org.drools.core.reteoo.AlphaNode) node).getConstraint();
                return true;
            case 5:
                alphaTerminalNode5 = (org.drools.core.reteoo.AlphaTerminalNode) node;
                return true;
            case 7:
                lambdaConstraint7 = (org.drools.modelcompiler.constraints.LambdaConstraint) ((org.drools.core.reteoo.AlphaNode) node).getConstraint();
                return true;
            case 8:
                alphaTerminalNode8 = (org.drools.core.reteoo.AlphaTerminalNode) node;
                return true;
        }
        return false;
    }

    public final void propagateAssertObject(org.drools.core.common.InternalFactHandle handle, org.drools.core.spi.PropagationContext context, org.drools.core.common.InternalWorkingMemory wm) {
        if (logger.isDebugEnabled()) {
            logger.debug("propagateAssertObject on compiled alpha network {} {} {}", handle, context, wm);
        }
        try {
            Person p = (Person) handle.getObject();
            if (LambdaPredicate7E9FC115F334D72E2F645F5337518814.INSTANCE.test(p)) {
                LambdaConsequenceB295AE36B3F2340F3F1109859CD89EFA.INSTANCE.execute(new ArrayList(), p);
            }
            if (lambdaConstraint7.isAllowed(handle, wm)) {
                alphaTerminalNode8.assertObject(handle, context, wm);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public final void propagateModifyObject(org.drools.core.common.InternalFactHandle handle, org.drools.core.reteoo.ModifyPreviousTuples modifyPreviousTuples, org.drools.core.spi.PropagationContext context, org.drools.core.common.InternalWorkingMemory wm) {
        if (logger.isDebugEnabled()) {
            logger.debug("propagateModifyObject on compiled alpha network {} {} {}", handle, context, wm);
        }

        try {
            Person p = (Person) handle.getObject();
            if (LambdaPredicate7E9FC115F334D72E2F645F5337518814.INSTANCE.test(p)) {
                LambdaConsequenceB295AE36B3F2340F3F1109859CD89EFA.INSTANCE.execute(new ArrayList(), p);
            }
            if (lambdaConstraint7.isAllowed(handle, wm)) {
                alphaTerminalNode8.modifyObject(handle, modifyPreviousTuples, context, wm);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getAssociationsSize() {
        return objectTypeNode.getAssociationsSize();
    }

    public short getType() {
        return objectTypeNode.getType();
    }

    public int getAssociatedRuleSize() {
        return objectTypeNode.getAssociatedRuleSize();
    }

    public int getAssociationsSize(org.kie.api.definition.rule.Rule rule) {
        return objectTypeNode.getAssociationsSize(rule);
    }

    public boolean isAssociatedWith(org.kie.api.definition.rule.Rule rule) {
        return objectTypeNode.isAssociatedWith(rule);
    }

    public void byPassModifyToBetaNode(org.drools.core.common.InternalFactHandle factHandle,
                                       org.drools.core.reteoo.ModifyPreviousTuples modifyPreviousTuples,
                                       org.drools.core.spi.PropagationContext context,
                                       org.drools.core.common.InternalWorkingMemory workingMemory) {
        throw new UnsupportedOperationException();
    }

    @org.drools.compiler.kie.builder.MaterializedLambda()
    public enum LambdaPredicate7E9FC115F334D72E2F645F5337518814 implements org.drools.model.functions.Predicate1<Person>, org.drools.model.functions.HashedExpression {

        INSTANCE;

        public static final String EXPRESSION_HASH = "E9C8AA867521DD83B43DB2726C677DA9";

        public String getExpressionHash() {
            return EXPRESSION_HASH;
        }

        @Override()
        public boolean test(Person _this) throws Exception {
            return _this.getName().startsWith("M");
        }

        @Override()
        public org.drools.model.functions.PredicateInformation predicateInformation() {
            return new org.drools.model.functions.PredicateInformation("name.startsWith(\"M\")", "M", "r0.drl");
        }
    }

    @org.drools.compiler.kie.builder.MaterializedLambda()
    public enum LambdaConsequenceB295AE36B3F2340F3F1109859CD89EFA implements org.drools.model.functions.Block2<java.util.List, Person>, org.drools.model.functions.HashedExpression {

        INSTANCE;

        public static final String EXPRESSION_HASH = "0C8D6203B4D44D076C16DBE02BDB515F";

        public String getExpressionHash() {
            return EXPRESSION_HASH;
        }

        @Override()
        public void execute(java.util.List resultsM, Person $p) throws Exception {
            resultsM.add($p);
        }
    }
}
