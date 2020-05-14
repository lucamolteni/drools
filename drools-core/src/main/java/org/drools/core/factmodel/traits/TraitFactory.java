package org.drools.core.factmodel.traits;

import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.marshalling.impl.MarshallerWriteContext;
import org.drools.core.marshalling.impl.ProtobufMessages;

public interface TraitFactory {

    void setRuleBase( InternalKnowledgeBase kBase );

    void writeRuntimeDefinedClasses(MarshallerWriteContext context, ProtobufMessages.Header.Builder header);
}
