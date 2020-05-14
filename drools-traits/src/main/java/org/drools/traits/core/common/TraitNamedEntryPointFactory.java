package org.drools.traits.core.common;

import org.drools.core.common.NamedEntryPoint;
import org.drools.core.common.NamedEntryPointFactory;
import org.drools.core.impl.StatefulKnowledgeSessionImpl;
import org.drools.core.reteoo.EntryPointNode;
import org.drools.core.rule.EntryPointId;

public class TraitNamedEntryPointFactory implements NamedEntryPointFactory {

    @Override
    public NamedEntryPoint createNamedEntryPoint(EntryPointNode addedNode, EntryPointId id, StatefulKnowledgeSessionImpl wm) {
        return new TraitNamedEntryPoint(id, addedNode, wm, wm.getKnowledgeBase().getConfiguration().getComponentFactory());
    }
}
