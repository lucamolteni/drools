package org.drools.traits.core.common;

import org.drools.core.WorkingMemoryEntryPoint;
import org.drools.core.common.DefaultFactHandle;
import org.drools.core.factmodel.traits.TraitFactory;
import org.drools.core.factmodel.traits.TraitTypeEnum;
import org.drools.traits.core.base.TraitHelperImpl;

public class TraitDefaultFactHandle extends DefaultFactHandle {

    public TraitDefaultFactHandle(long id, Object initialFact, long recency, WorkingMemoryEntryPoint wmEntryPoint) {
        this( id, determineIdentityHashCode( initialFact ), initialFact, recency, wmEntryPoint, false );
    }

    public TraitDefaultFactHandle(final long id,
                             final Object object,
                             final long recency,
                             final WorkingMemoryEntryPoint wmEntryPoint,
                             final boolean isTraitOrTraitable ) {
        this( id, determineIdentityHashCode( object ), object, recency, wmEntryPoint, isTraitOrTraitable );
    }

    public TraitDefaultFactHandle(final long id,
                             final int identityHashCode,
                             final Object object,
                             final long recency,
                             final WorkingMemoryEntryPoint wmEntryPoint,
                             final boolean isTraitOrTraitable ) {
        this.id = id;
        this.entryPointId = wmEntryPoint == null ? null : wmEntryPoint.getEntryPoint();
        this.wmEntryPoint = wmEntryPoint;
        this.recency = recency;
        setObject(object);
        this.identityHashCode = identityHashCode;
        this.traitType = determineTraitType(object, isTraitOrTraitable);
        if (wmEntryPoint != null) {
            setLinkedTuples( wmEntryPoint.getKnowledgeBase() );
            this.wmEntryPoint = wmEntryPoint;
        } else {
            this.linkedTuples = new SingleLinkedTuples();
        }
    }

    @Override
    public <K> K as( Class<K> klass ) throws ClassCastException {
        if ( klass.isAssignableFrom( object.getClass() ) ) {
            return (K) object;
        } else if ( this.isTraitOrTraitable() ) {
            TraitHelperImpl traitHelper = new TraitHelperImpl();
            K k = traitHelper.extractTrait(this, klass);
            if (k != null) {
                return k;
            } else {
                throw new RuntimeException(String.format("Cannot trait to %s", klass));
            }
        }
        throw new ClassCastException( "The Handle's Object can't be cast to " + klass );
    }

    @Override
    protected TraitTypeEnum determineTraitType(Object object, boolean isTraitOrTraitable) {
        if (isTraitOrTraitable) {
            TraitFactory traitFactory = getWorkingMemory().getKnowledgeBase().getConfiguration().getComponentFactory().getTraitFactory();
            return traitFactory.determineTraitType(object);
        } else {
            return TraitTypeEnum.NON_TRAIT;
        }
    }
}
