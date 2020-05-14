package org.drools.traits.core.factmodel;

import org.drools.core.factmodel.ClassBuilder;
import org.drools.core.factmodel.DefaultClassBuilderFactory;
import org.drools.core.rule.TypeDeclaration;
import org.drools.traits.core.factmodel.traits.TraitClassBuilderImpl;
import org.drools.traits.core.factmodel.traits.TraitMapPropertyWrapperClassBuilderImpl;

public class TraitClassBuilderFactory extends DefaultClassBuilderFactory {

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

    public ClassBuilder getTraitProxyBuilder() {
        if (traitProxyBuilder == null) {
            traitProxyBuilder = new TraitClassBuilderImpl();
        }
        return traitProxyBuilder;
    }

    public void setTraitProxyBuilder(ClassBuilder tpcb) {
        traitProxyBuilder = tpcb;
    }

    @Override
    public ClassBuilder getClassBuilder(TypeDeclaration type) {
        switch (type.getKind()) {
            case TRAIT: return getTraitProxyBuilder();
            case ENUM: return getEnumClassBuilder();
            case CLASS: default: return getBeanClassBuilder();
        }
    }
}
