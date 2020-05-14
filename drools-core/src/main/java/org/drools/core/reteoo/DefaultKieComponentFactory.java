/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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

package org.drools.core.reteoo;

import java.io.Serializable;

import org.drools.core.base.FieldDataFactory;
import org.drools.core.base.FieldFactory;
import org.drools.core.base.TraitDisabledHelper;
import org.drools.core.base.TraitHelper;
import org.drools.core.common.AgendaFactory;
import org.drools.core.common.AgendaGroupFactory;
import org.drools.core.common.BeliefSystemFactory;
import org.drools.core.common.DefaultAgendaFactory;
import org.drools.core.common.DefaultNamedEntryPointFactory;
import org.drools.core.common.InternalWorkingMemoryActions;
import org.drools.core.common.InternalWorkingMemoryEntryPoint;
import org.drools.core.common.NamedEntryPointFactory;
import org.drools.core.common.PhreakBeliefSystemFactory;
import org.drools.core.common.PhreakPropagationContextFactory;
import org.drools.core.common.PhreakWorkingMemoryFactory;
import org.drools.core.common.PriorityQueueAgendaGroupFactory;
import org.drools.core.common.PropagationContextFactory;
import org.drools.core.common.WorkingMemoryFactory;
import org.drools.core.factmodel.ClassBuilderFactory;
import org.drools.core.factmodel.DefaultClassBuilderFactory;
import org.drools.core.factmodel.traits.TraitFactory;
import org.drools.core.factmodel.traits.TraitRegistry;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.reteoo.builder.NodeFactory;
import org.drools.core.reteoo.builder.PhreakNodeFactory;
import org.drools.core.rule.DefaultLogicTransformerFactory;
import org.drools.core.rule.LogicTransformerFactory;
import org.drools.core.spi.FactHandleFactory;
import org.drools.core.util.TripleFactory;
import org.drools.core.util.TripleFactoryImpl;
import org.drools.core.util.TripleStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultKieComponentFactory implements Serializable,
                                                   KieComponentFactory {

    Logger logger = LoggerFactory.getLogger(DefaultKieComponentFactory.class);

    public static final KieComponentFactory DEFAULT = new DefaultKieComponentFactory();

    private FactHandleFactory handleFactory = new ReteooFactHandleFactory();

    @Override
    public FactHandleFactory getFactHandleFactoryService() {
        return handleFactory;
    }

    @Override
    public NamedEntryPointFactory getNamedEntryPointFactory() {
        return new DefaultNamedEntryPointFactory();
    }

    private WorkingMemoryFactory wmFactory = PhreakWorkingMemoryFactory.getInstance();

    @Override
    public WorkingMemoryFactory getWorkingMemoryFactory() {
        return wmFactory;
    }

    private NodeFactory nodeFactory = PhreakNodeFactory.getInstance();

    @Override
    public NodeFactory getNodeFactoryService() {
        return nodeFactory;
    }

    @Override
    public void setNodeFactoryProvider(NodeFactory provider) {
        nodeFactory = provider;
    }

    private PropagationContextFactory propagationFactory = PhreakPropagationContextFactory.getInstance();

    @Override
    public PropagationContextFactory getPropagationContextFactory() {
        return propagationFactory;
    }

    private BeliefSystemFactory bsFactory = new PhreakBeliefSystemFactory();

    @Override
    public BeliefSystemFactory getBeliefSystemFactory() {
        return bsFactory;
    }

    private RuleBuilderFactory ruleBuilderFactory = new ReteooRuleBuilderFactory();

    @Override
    public RuleBuilderFactory getRuleBuilderFactory() {
        return ruleBuilderFactory;
    }

    private AgendaFactory agendaFactory = DefaultAgendaFactory.getInstance();

    @Override
    public AgendaFactory getAgendaFactory() {
        return agendaFactory;
    }

    private AgendaGroupFactory agendaGroupFactory = PriorityQueueAgendaGroupFactory.getInstance();

    @Override
    public AgendaGroupFactory getAgendaGroupFactory() {
        return agendaGroupFactory;
    }

    private FieldDataFactory fieldFactory = FieldFactory.getInstance();

    @Override
    public FieldDataFactory getFieldFactory() {
        return fieldFactory;
    }

    private TripleFactory tripleFactory = new TripleFactoryImpl();

    @Override
    public TripleFactory getTripleFactory() {
        return tripleFactory;
    }

    private LogicTransformerFactory logicTransformerFactory = new DefaultLogicTransformerFactory();

    @Override
    public LogicTransformerFactory getLogicTransformerFactory() {
        return logicTransformerFactory;
    }

    @Override
    public TraitFactory initTraitFactory(InternalKnowledgeBase knowledgeBase) {
        return null;
    }

    @Override
    public TraitFactory getTraitFactory() {
        return null;
    }

    @Override
    public TraitRegistry getTraitRegistry() {
        return null;
    }

    private TripleStore tripleStore = new TripleStore();

    @Override
    public TripleStore getTripleStore() {
        return tripleStore;
    }

    @Override
    public TraitHelper createTraitHelper(InternalWorkingMemoryActions workingMemory, InternalWorkingMemoryEntryPoint nep) {
        return new TraitDisabledHelper();
    }

    private ClassBuilderFactory classBuilderFactory = new DefaultClassBuilderFactory();

    @Override
    public ClassBuilderFactory getClassBuilderFactory() {
        return classBuilderFactory;
    }

    @Override
    public Class<?> getBaseTraitProxyClass() {
        return null;
    }
}
