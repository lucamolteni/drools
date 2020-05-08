package org.drools.traits.core.reteoo;

import org.drools.core.reteoo.KieComponentFactory;
import org.drools.core.reteoo.KieComponentFactoryFactory;

public class TraitKieComponentFactoryFactory implements KieComponentFactoryFactory {

    @Override
    public KieComponentFactory createKieComponentFactory() {
        return new TraitKieComponentFactory();
    }
}
