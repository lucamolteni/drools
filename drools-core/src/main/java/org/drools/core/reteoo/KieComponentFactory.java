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

import java.util.Optional;

import org.drools.core.base.FieldDataFactory;
import org.drools.core.base.TraitHelper;
import org.drools.core.common.AgendaFactory;
import org.drools.core.common.AgendaGroupFactory;
import org.drools.core.common.BeliefSystemFactory;
import org.drools.core.common.InternalWorkingMemoryActions;
import org.drools.core.common.InternalWorkingMemoryEntryPoint;
import org.drools.core.common.NamedEntryPointFactory;
import org.drools.core.common.PropagationContextFactory;
import org.drools.core.common.WorkingMemoryFactory;
import org.drools.core.definitions.InternalKnowledgePackage;
import org.drools.core.factmodel.ClassBuilderFactory;
import org.drools.core.factmodel.traits.TraitFactory;
import org.drools.core.factmodel.traits.TraitRegistry;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.reteoo.builder.NodeFactory;
import org.drools.core.rule.LogicTransformerFactory;
import org.drools.core.spi.FactHandleFactory;
import org.drools.core.util.TripleFactory;
import org.drools.core.util.TripleStore;

public interface KieComponentFactory {

    static KieComponentFactory createKieComponentFactory() {
        Optional<KieComponentFactoryFactory> kieComponentFactory = ServiceRegistryUtils.optionalService(KieComponentFactoryFactory.class);
        return kieComponentFactory.map(KieComponentFactoryFactory::createKieComponentFactory).orElseGet(DefaultKieComponentFactory::new);
    }

    FactHandleFactory getFactHandleFactoryService();

    NamedEntryPointFactory getNamedEntryPointFactory();

    WorkingMemoryFactory getWorkingMemoryFactory();

    NodeFactory getNodeFactoryService();

    void setNodeFactoryProvider(NodeFactory provider);

    PropagationContextFactory getPropagationContextFactory();

    BeliefSystemFactory getBeliefSystemFactory();

    RuleBuilderFactory getRuleBuilderFactory();

    AgendaFactory getAgendaFactory();

    AgendaGroupFactory getAgendaGroupFactory();

    FieldDataFactory getFieldFactory();

    TripleFactory getTripleFactory();

    LogicTransformerFactory getLogicTransformerFactory();

    TraitFactory initTraitFactory(InternalKnowledgeBase knowledgeBase);

    TraitFactory getTraitFactory();

    TraitRegistry getTraitRegistry();

    TripleStore getTripleStore();

    TraitHelper createTraitHelper(InternalWorkingMemoryActions workingMemory, InternalWorkingMemoryEntryPoint nep);

    ClassBuilderFactory getClassBuilderFactory();

    Class<?> getBaseTraitProxyClass();

    InternalKnowledgePackage createKnowledgePackage(String name);
}
