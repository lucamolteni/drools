package org.drools.traits.core.factmodel;

import org.drools.core.factmodel.ClassBuilder;
import org.drools.core.factmodel.DefaultClassBuilderFactory;
import org.drools.core.factmodel.traits.TraitCoreService;
import org.drools.traits.core.factmodel.traits.TraitMapPropertyWrapperClassBuilderImpl;
import org.drools.traits.core.factmodel.traits.TraitMapProxyClassBuilderImpl;

import static org.drools.core.reteoo.ServiceRegistryUtils.fromTraitRegistry;

public class TraitClassBuilderFactory extends DefaultClassBuilderFactory {

    // Trait interfaces
    private ClassBuilder traitBuilder;

    @Override
    public ClassBuilder getTraitBuilder() {
        return traitBuilder;
    }

    @Override
    public void setTraitBuilder(ClassBuilder tcb) {
        traitBuilder = tcb;
    }

    // Trait property wrappers
    private ClassBuilder propertyWrapperBuilder;

    @Override
    public ClassBuilder getPropertyWrapperBuilder() {
        if (propertyWrapperBuilder == null) {
            propertyWrapperBuilder = new TraitMapPropertyWrapperClassBuilderImpl();
        }
        return propertyWrapperBuilder;
    }

    @Override
    public void setPropertyWrapperBuilder(ClassBuilder pcb) {
        propertyWrapperBuilder = pcb;
    }

    // Trait proxy wrappers

    private ClassBuilder traitProxyBuilder;

    @Override
    public ClassBuilder getTraitProxyBuilder() {
        if (traitProxyBuilder == null) {
            traitProxyBuilder = new TraitMapProxyClassBuilderImpl();
        }
        return traitProxyBuilder;
    }

    @Override
    public void setTraitProxyBuilder(ClassBuilder tpcb) {
        traitProxyBuilder = tpcb;
    }
}
