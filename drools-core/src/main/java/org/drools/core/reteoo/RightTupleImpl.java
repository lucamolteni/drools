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

import org.drools.base.reteoo.NodeTypeEnums;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.ReteEvaluator;
import org.drools.core.common.PropagationContext;
import org.drools.core.util.index.TupleList;

public class RightTupleImpl extends AbstractTuple implements RightTuple {

    private TupleList            memory;

    private AbstractLeftTuple            firstChild;
    private AbstractLeftTuple            lastChild;

    private AbstractLeftTuple            blocked;

    private RightTuple           tempNextRightTuple;
    private AbstractLeftTuple            tempBlocked;

    private boolean              retracted;

    public RightTupleImpl() { }
    
    public RightTupleImpl(InternalFactHandle handle) {
        // This constructor is here for DSL testing
        setFactHandle( handle );
    }

    public RightTupleImpl(InternalFactHandle handle,
                      RightTupleSink sink) {
        this( handle );
        this.sink = sink;

        // add to end of RightTuples on handle
        handle.addLastRightTuple( this );
    }

    public RightTupleSink getTupleSink() {
        return (BetaNode) sink;
    }
    
    public void reAdd() {
        getFactHandle().addLastRightTuple( this );
    }

    public void unlinkFromRightParent() {
        getFactHandle().removeRightTuple( this );
        setFactHandle( null );
        this.handlePrevious = null;
        this.handleNext = null;
        this.blocked = null;
        setPrevious( null );
        setNext( null );
        this.memory = null;
        this.firstChild = null;
        this.lastChild = null;
        this.sink = null;
    }

    public void unlinkFromLeftParent() {

    }

    public AbstractLeftTuple getBlocked() {
        return this.blocked;
    }
    
    public void setBlocked(AbstractLeftTuple leftTuple) {
        this.blocked = leftTuple;
    }
    
    public void addBlocked(AbstractLeftTuple leftTuple) {
        if ( this.blocked != null && leftTuple != null ) {
            leftTuple.setBlockedNext( this.blocked );
            this.blocked.setBlockedPrevious( leftTuple );
        }
        this.blocked = leftTuple;
    }

    public void removeBlocked(AbstractLeftTuple leftTuple) {
        AbstractLeftTuple previous =  leftTuple.getBlockedPrevious();
        AbstractLeftTuple next =  leftTuple.getBlockedNext();
        if ( previous != null && next != null ) {
            //remove  from middle
            previous.setBlockedNext( next );
            next.setBlockedPrevious( previous );
        } else if ( next != null ) {
            //remove from first
            this.blocked = next;
            next.setBlockedPrevious( null );
        } else if ( previous != null ) {
            //remove from end
            previous.setBlockedNext( null );
        } else {
            this.blocked = null;
        }
        leftTuple.clearBlocker();
    }

    public TupleList getMemory() {
        return memory;
    }

    public void setMemory(TupleList memory) {
        this.memory = memory;
    }

    public RightTuple getHandlePrevious() {
        return (RightTuple) handlePrevious;
    }

    public RightTuple getHandleNext() {
        return (RightTuple) handleNext;
    }

    public AbstractLeftTuple getFirstChild() {
        return firstChild;
    }

    public void setFirstChild(AbstractLeftTuple firstChild) {
        this.firstChild = firstChild;
    }

    public AbstractLeftTuple getLastChild() {
        return lastChild;
    }

    public void setLastChild(AbstractLeftTuple lastChild) {
        this.lastChild = lastChild;
    }
    
    public RightTuple getStagedNext() {
        return (RightTuple) stagedNext;
    }

    public RightTuple getStagedPrevious() {
        return (RightTuple) stagedPrevious;
    }

    public AbstractLeftTuple getTempBlocked() {
        return tempBlocked;
    }

    public void setTempBlocked(AbstractLeftTuple tempBlocked) {
        this.tempBlocked = tempBlocked;
    }

    public RightTuple getTempNextRightTuple() {
        return tempNextRightTuple;
    }

    public void setTempNextRightTuple(RightTuple tempNextRightTuple) {
        this.tempNextRightTuple = tempNextRightTuple;
    }

    public int hashCode() {
        return getFactHandle().hashCode();
    }

    public String toString() {
        return getFactHandle().toString() + "\n";
    }

    public boolean equals(Object object) {
        if (!(object instanceof RightTupleImpl)) {
            return false;
        }

        if ( object == this ) {
            return true;
        }

        RightTupleImpl other = (RightTupleImpl) object;
        // A ReteTuple is  only the same if it has the same hashCode, factId and parent
        if (hashCode() != other.hashCode()) {
            return false;
        }

        return getFactHandle() == other.getFactHandle();
    }

    public void clear() {
        super.clear();
        this.memory = null;
    }

    public void clearStaged() {
        super.clearStaged();
        this.tempNextRightTuple = null;
        this.tempBlocked = null;
    }

    @Override
    public Object getObject( int pattern ) {
        return pattern == 0 ? getFactHandle().getObject() : null;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public int getIndex() {
        return 0;
    }

    @Override
    public Object[] toObjects(boolean reverse) {
        return new Object[] { getFactHandle().getObject() };
    }

    @Override
    public InternalFactHandle get( int pattern ) {
        return pattern == 0 ? getFactHandle() : null;
    }

    @Override
    public InternalFactHandle[] toFactHandles() {
        return new InternalFactHandle[] { getFactHandle() };
    }

    @Override
    public Tuple getParent() {
        return null;
    }

    @Override
    public Tuple getSubTuple( int elements ) {
        return elements == 1 ? this : null;
    }

    @Override
    public ObjectTypeNode.Id getInputOtnId() {
        return sink != null ? getTupleSink().getRightInputOtnId() : null;
    }

    @Override
    public LeftTupleSource getTupleSource() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void retractTuple( PropagationContext context, ReteEvaluator reteEvaluator ) {
        if (!retracted) {
            getTupleSink().retractRightTuple( this, context, reteEvaluator );
            retracted = true;
        }
    }

    @Override
    public void setExpired( ReteEvaluator reteEvaluator, PropagationContext pctx ) {
        super.setExpired();
        // events expired at firing time should have a chance to produce a join (DROOLS-1329)
        // but shouldn't participate to an accumulate (DROOLS-4393)
        if (getTupleSink().getType() == NodeTypeEnums.AccumulateNode) {
            retractTuple( pctx, reteEvaluator );
        }
    }

    public InternalFactHandle getFactHandleForEvaluation() {
        return getFactHandle();
    }
}
