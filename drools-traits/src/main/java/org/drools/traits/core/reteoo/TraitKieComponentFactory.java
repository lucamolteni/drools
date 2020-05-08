package org.drools.traits.core.reteoo;

import org.drools.core.factmodel.ClassBuilderFactory;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.drools.traits.core.factmodel.TraitClassBuilderFactory;
import org.drools.core.factmodel.traits.TraitFactory;
import org.drools.core.factmodel.traits.TraitRegistry;
import org.drools.core.reteoo.DefaultKieComponentFactory;
import org.drools.core.reteoo.builder.NodeFactory;
import org.drools.traits.core.factmodel.traits.TraitFactoryImpl;
import org.drools.traits.core.factmodel.traits.TraitProxyImpl;
import org.drools.traits.core.factmodel.traits.TraitRegistryImpl;

public class TraitKieComponentFactory extends DefaultKieComponentFactory {

    private NodeFactory nodeFactory = TraitPhreakNodeFactory.getInstance();

    @Override
    public NodeFactory getNodeFactoryService() {
        return nodeFactory;
    }

    public void setNodeFactoryProvider(NodeFactory provider) {
        nodeFactory = provider;
    }

    private Class<?> baseTraitProxyClass = TraitProxyImpl.class;

    @Override
    public Class<?> getBaseTraitProxyClass() {
        return baseTraitProxyClass;
    }

    private TraitFactory traitFactory;

    @Override
    public TraitFactory getTraitFactory() {
        if(traitFactory == null) {
            traitFactory = new TraitFactoryImpl<>();
        }
        return traitFactory;
    }

    public void setTraitFactory(TraitFactory tf) {
        traitFactory = tf;
    }

    private TraitRegistry traitRegistry;

    @Override
    public TraitRegistry getTraitRegistry() {
        if(traitRegistry == null) {
            traitRegistry = new TraitRegistryImpl();
        }
        return traitRegistry;
    }

    private TraitClassBuilderFactory traitClassBuilderFactory = new TraitClassBuilderFactory();

    @Override
    public ClassBuilderFactory getClassBuilderFactory() {
        return traitClassBuilderFactory;
    }
}
