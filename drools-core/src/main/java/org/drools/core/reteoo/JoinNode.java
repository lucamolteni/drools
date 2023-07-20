/*
 * Copyright 2005 Red Hat, Inc. and/or its affiliates.
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
import org.drools.core.common.BetaConstraints;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.ReteEvaluator;
import org.drools.core.reteoo.builder.BuildContext;
import org.drools.core.common.PropagationContext;

public class JoinNode extends BetaNode {

    private static final long serialVersionUID = 510l;

    public JoinNode() {

    }

    public JoinNode(final int id,
                    final LeftTupleSource leftInput,
                    final ObjectSource rightInput,
                    final BetaConstraints binder,
                    final BuildContext context) {
        super( id,
               leftInput,
               rightInput,
               binder,
               context );
        this.tupleMemoryEnabled = context.isTupleMemoryEnabled();
        this.setObjectCount(leftInput.getObjectCount() + 1); // 'join' node increase the object count
    }

    public short getType() {
        return NodeTypeEnums.JoinNode;
    }

    public String toString() {
        return "[JoinNode(" + this.getId() + ") - " + getObjectTypeNode().getObjectType() + "]";
    }
    
    public AbstractLeftTuple createPeer(AbstractLeftTuple original) {
        JoinNodeLeftTuple peer = new JoinNodeLeftTuple();
        peer.initPeer((AbstractLeftTuple) original, this);
        original.setPeer( peer );
        return peer;
    }

    public AbstractLeftTuple createLeftTuple(InternalFactHandle factHandle,
                                     boolean leftTupleMemoryEnabled) {
        return new JoinNodeLeftTuple(factHandle, this, leftTupleMemoryEnabled );
    }

    public AbstractLeftTuple createLeftTuple(final InternalFactHandle factHandle,
                                     final AbstractLeftTuple leftTuple,
                                     final Sink sink) {
        return new JoinNodeLeftTuple(factHandle,leftTuple, sink );
    }

    public AbstractLeftTuple createLeftTuple(AbstractLeftTuple leftTuple,
                                     Sink sink,
                                     PropagationContext pctx,
                                     boolean leftTupleMemoryEnabled) {
        return new JoinNodeLeftTuple(leftTuple,sink, pctx, leftTupleMemoryEnabled );
    }

    public AbstractLeftTuple createLeftTuple(AbstractLeftTuple leftTuple,
                                     RightTuple rightTuple,
                                     Sink sink) {
        return new JoinNodeLeftTuple(leftTuple, rightTuple, sink );
    }   
    
    public AbstractLeftTuple createLeftTuple(AbstractLeftTuple leftTuple,
                                     RightTuple rightTuple,
                                     AbstractLeftTuple currentLeftChild,
                                     AbstractLeftTuple currentRightChild,
                                     Sink sink,
                                     boolean leftTupleMemoryEnabled) {
        return new JoinNodeLeftTuple(leftTuple, rightTuple, currentLeftChild, currentRightChild, sink, leftTupleMemoryEnabled );        
    }

    public void retractRightTuple( final RightTuple rightTuple,
                                   final PropagationContext pctx,
                                   final ReteEvaluator reteEvaluator ) {
        final BetaMemory memory = (BetaMemory) reteEvaluator.getNodeMemory( this );
        rightTuple.setPropagationContext( pctx );
        doDeleteRightTuple( rightTuple, reteEvaluator, memory );
    }

    @Override
    public void modifyRightTuple(RightTuple rightTuple, PropagationContext context, ReteEvaluator reteEvaluator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean doRemove(RuleRemovalContext context, ReteooBuilder builder) {
        if ( !isInUse() ) {
            getLeftTupleSource().removeTupleSink( this );
            getRightInput().removeObjectSink( this );
            return true;
        }
        return false;
    }
}
