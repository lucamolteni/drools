package org.drools.traits.core.common;

import java.util.concurrent.locks.ReentrantLock;

import org.drools.core.base.TraitHelper;
import org.drools.core.common.ClassAwareObjectStore;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.NamedEntryPoint;
import org.drools.core.definitions.rule.impl.RuleImpl;
import org.drools.core.factmodel.traits.TraitProxy;
import org.drools.core.factmodel.traits.TraitableBean;
import org.drools.core.impl.StatefulKnowledgeSessionImpl;
import org.drools.core.reteoo.EntryPointNode;
import org.drools.core.reteoo.KieComponentFactory;
import org.drools.core.reteoo.TerminalNode;
import org.drools.core.rule.EntryPointId;
import org.drools.core.spi.Activation;
import org.drools.core.spi.PropagationContext;

public class TraitNamedEntryPoint extends NamedEntryPoint {

    protected TraitHelper traitHelper;

    public TraitNamedEntryPoint(EntryPointId entryPoint,
                                EntryPointNode entryPointNode,
                                StatefulKnowledgeSessionImpl wm,
                                KieComponentFactory componentFactory) {
        this(entryPoint,
             entryPointNode,
             wm,
             new ReentrantLock(), componentFactory);
    }

    public TraitNamedEntryPoint(EntryPointId entryPoint,
                                EntryPointNode entryPointNode,
                                StatefulKnowledgeSessionImpl wm,
                                ReentrantLock lock,
                                KieComponentFactory componentFactory) {
        this.entryPoint = entryPoint;
        this.entryPointNode = entryPointNode;
        this.wm = wm;
        this.kBase = this.wm.getKnowledgeBase();
        this.lock = lock;
        this.handleFactory = this.wm.getFactHandleFactory();
        this.pctxFactory = kBase.getConfiguration().getComponentFactory().getPropagationContextFactory();
        this.objectStore = new ClassAwareObjectStore(this.kBase.getConfiguration(), this.lock);
        this.traitHelper = componentFactory.createTraitHelper(wm, this);
    }

    @Override
    protected void beforeUpdate(InternalFactHandle handle, Object object, Activation activation, Object originalObject, PropagationContext propagationContext) {
        if (handle.isTraitable() && object != originalObject
                && object instanceof TraitableBean && originalObject instanceof TraitableBean) {
            this.traitHelper.replaceCore(handle, object, originalObject, propagationContext.getModificationMask(), object.getClass(), activation);
        }
    }

    @Override
    protected void afterRetract(InternalFactHandle handle, RuleImpl rule, TerminalNode terminalNode) {
        if (handle.isTraiting() && handle.getObject() instanceof TraitProxy) {
            (((TraitProxy) handle.getObject()).getObject()).removeTrait(((TraitProxy) handle.getObject())._getTypeCode());
        } else if (handle.isTraitable()) {
            traitHelper.deleteWMAssertedTraitProxies(handle, rule, terminalNode);
        }
    }

    @Override
    protected void beforeDestroy(RuleImpl rule, TerminalNode terminalNode, InternalFactHandle handle) {
        if (handle.isTraitable()) {
            traitHelper.deleteWMAssertedTraitProxies(handle, rule, terminalNode);
        }
    }

    @Override
    public TraitHelper getTraitHelper() {
        return traitHelper;
    }
}
