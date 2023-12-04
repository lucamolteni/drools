/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.drools.core.reteoo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.drools.base.base.ObjectType;
import org.drools.base.reteoo.AccumulateContextEntry;
import org.drools.base.reteoo.NodeTypeEnums;
import org.drools.base.rule.Accumulate;
import org.drools.base.rule.Pattern;
import org.drools.base.rule.consequence.Consequence;
import org.drools.base.rule.constraint.AlphaNodeFieldConstraint;
import org.drools.core.RuleBaseConfiguration;
import org.drools.core.common.BetaConstraints;
import org.drools.core.common.InternalAgenda;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.Memory;
import org.drools.core.common.PropagationContext;
import org.drools.core.common.ReteEvaluator;
import org.drools.core.event.RuleEventListenerSupport;
import org.drools.core.phreak.PhreakAccumulateNode;
import org.drools.core.reteoo.builder.BuildContext;
import org.drools.core.rule.consequence.InternalMatch;
import org.drools.core.rule.consequence.KnowledgeHelper;
import org.drools.core.util.index.TupleList;
import org.kie.internal.runtime.StatefulKnowledgeSession;

import static org.drools.core.phreak.TupleEvaluationUtil.flushLeftTupleIfNecessary;

/**
 * AccumulateNode
 * A beta node capable of doing accumulate logic.
 *
 * Created: 04/06/2006
 *
 * @version $Id$
 */
public class SimpleGroupByNode extends AccumulateNode {

    private static final long          serialVersionUID = 510L;

    protected Accumulate                 accumulate;
    protected AlphaNodeFieldConstraint[] resultConstraints;
    protected BetaConstraints            resultBinder;

    public SimpleGroupByNode() {
    }

    public SimpleGroupByNode(int id, LeftTupleSource leftInput, ObjectSource rightInput, AlphaNodeFieldConstraint[] resultConstraints, BetaConstraints sourceBinder, BetaConstraints resultBinder, Accumulate accumulate, BuildContext context) {
        super(id, leftInput, rightInput, sourceBinder, context);
        this.setObjectCount(leftInput.getObjectCount() + 1); // 'accumulate' node increases the object count
        this.resultBinder = resultBinder;
        this.resultBinder.init( context, getType() );
        this.resultConstraints = resultConstraints;
        this.accumulate = accumulate;
        this.tupleMemoryEnabled = context.isTupleMemoryEnabled();

        hashcode = this.leftInput.hashCode() ^
                this.rightInput.hashCode() ^
                this.accumulate.hashCode() ^
                this.resultBinder.hashCode() ^
                Arrays.hashCode( this.resultConstraints );
    }


    @Override
    protected ObjectType getObjectTypeForPropertyReactivity( LeftInputAdapterNode leftInput, Pattern pattern ) {
        return pattern != null && isRightInputIsRiaNode() ?
               pattern.getObjectType() :
               leftInput.getParentObjectSource().getObjectTypeNode().getObjectType();
    }

    public short getType() {
        return NodeTypeEnums.AccumulateNode;
    }

    public Accumulate getAccumulate() {
        return this.accumulate;
    }       

    public AlphaNodeFieldConstraint[] getResultConstraints() {
        return resultConstraints;
    }

    public BetaConstraints getResultBinder() {
        return resultBinder;
    }

    @Override
    public void assertObject( InternalFactHandle factHandle, PropagationContext pctx, ReteEvaluator reteEvaluator ) {
        final BetaMemory memory = getBetaMemoryFromRightInput(this, reteEvaluator);

        RightTuple rightTuple = createRightTuple( factHandle, this, pctx );

        boolean stagedInsertWasEmpty = memory.getStagedRightTuples().addInsert(rightTuple);
        if ( isLogTraceEnabled ) {
            log.trace("BetaNode stagedInsertWasEmpty={}", stagedInsertWasEmpty );
        }


        boolean shouldFlush = isStreamMode();
        if ( memory.getAndIncCounter() == 0 ) {
            if ( stagedInsertWasEmpty ) {
                memory.setNodeDirtyWithoutNotify();
            }
            shouldFlush = memory.linkNode( this, reteEvaluator, !rightInputIsPassive ) | shouldFlush;
        } else if ( stagedInsertWasEmpty ) {
            shouldFlush = memory.setNodeDirty( this, reteEvaluator, !rightInputIsPassive ) | shouldFlush;
        }

        if (shouldFlush) {
            flushLeftTupleIfNecessary( reteEvaluator, memory.getOrCreateSegmentMemory( this, reteEvaluator ), isStreamMode() );
        }


        LeftInputAdapterNode lian = (LeftInputAdapterNode) this.leftInput;
        LeftInputAdapterNode.LiaNodeMemory lm = reteEvaluator.getNodeMemory(lian);
        LeftTuple leftTuple = lm.getSegmentMemory().getStagedLeftTuples().getInsertFirst();

        AccumulateMemory am = (AccumulateMemory) reteEvaluator.getNodeMemory(this);

        AccumulateNode.BaseAccumulation accumulationContext = am.getAccumulationContext();
        Object value = accumulate.accumulate(am.workingMemoryContext,
                                             accumulationContext,
                                             leftTuple,
                                             factHandle,
                                             reteEvaluator);

        LeftTuple match = createLeftTuple(leftTuple, rightTuple,
                                                  null, null,
                                                  this,true);

        // postAccumulate(accNode, accctx, match); this is used by GroupBy

        Object result = accumulate.getResult(am.workingMemoryContext, accumulationContext, leftTuple, reteEvaluator);

        InternalFactHandle resultFactHandle = createResultFactHandle(pctx, reteEvaluator, leftTuple, result);

        LeftTupleSink firstLeftTupleSink = sink.getFirstLeftTupleSink();
        LeftTuple resultTuple = firstLeftTupleSink.createLeftTuple(resultFactHandle, leftTuple, firstLeftTupleSink);

        // Si può usare il context object del match per passare la resultTuple già calcolata senza passare dal RuleNetworkEvaluator/Executor?
        // Oppure meglio lanciare la consequence qui

        // Guarda PhreakGroupByNode e implementa questo su GroupBy

        InternalMatch resultTupleInternalMatch = (InternalMatch) resultTuple;

        Consequence consequence = resultTupleInternalMatch.getConsequence();
        KnowledgeHelper knowledgeHelper = ((InternalAgenda)((StatefulKnowledgeSession)reteEvaluator).getAgenda()).getKnowledgeHelper();
        try {
            knowledgeHelper.setActivation(resultTupleInternalMatch);

            if ( log.isTraceEnabled() ) {
                log.trace("Fire event {} for rule \"{}\" \n{}", consequence.getName(), resultTupleInternalMatch.getRule().getName(), resultTupleInternalMatch.getTuple());
            }

            RuleEventListenerSupport ruleEventSupport = reteEvaluator.getRuleEventSupport();
            ruleEventSupport.onBeforeMatchFire(resultTupleInternalMatch);
            consequence.evaluate(knowledgeHelper, reteEvaluator);
            ruleEventSupport.onAfterMatchFire(resultTupleInternalMatch);

            resultTupleInternalMatch.setActive(false);
            knowledgeHelper.reset();
        } catch ( final Exception e ) {
            e.printStackTrace();
            knowledgeHelper.restoreActivationOnConsequenceFailure(resultTupleInternalMatch);
        } finally {
            if (resultTupleInternalMatch.getActivationFactHandle() != null ) {
                // update the Activation in the WM
                InternalFactHandle activationFactHandle = resultTupleInternalMatch.getActivationFactHandle();
                reteEvaluator.getDefaultEntryPoint().getEntryPointNode().modifyActivation(activationFactHandle, resultTupleInternalMatch.getPropagationContext(), reteEvaluator);
            }
        }
    }

    AccumulateNode.BaseAccumulation initAccumulationContext(AccumulateMemory am, ReteEvaluator reteEvaluator, Accumulate accumulate, LeftTuple leftTuple) {
        AccumulateNode.GroupByContext accContext = new AccumulateNode.GroupByContext();
        leftTuple.setContextObject( accContext );
        // A lot less is done here, compared to super, as it needs to be done on demand during the Group creation.
        return accContext;
    }



    public InternalFactHandle createResultFactHandle(final PropagationContext context,
                                                     final ReteEvaluator reteEvaluator,
                                                     final LeftTuple leftTuple,
                                                     final Object result) {
        InternalFactHandle handle = null;
        if ( context.getReaderContext() != null ) {
            handle = context.getReaderContext().createAccumulateHandle( context.getEntryPoint(), reteEvaluator, leftTuple, result, getId() );
        }
        if (handle == null) {
            handle = reteEvaluator.createFactHandle( result,
                                                     null, // no need to retrieve the ObjectTypeConf, acc result is never an event or a trait
                                                     null );
        }
        return handle;
    }

    @Override
    public void doAttach( BuildContext context ) {
        super.doAttach( context );
    }

    protected int calculateHashCode() {
        return 0;
    }

    @Override
    public boolean equals( final Object object ) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof SimpleGroupByNode) || this.hashCode() != object.hashCode()) {
            return false;
        }

        SimpleGroupByNode other = (SimpleGroupByNode) object;
        return this.leftInput.getId() == other.leftInput.getId() && this.rightInput.getId() == other.rightInput.getId() &&
               this.constraints.equals( other.constraints ) &&
               this.accumulate.equals( other.accumulate ) &&
               resultBinder.equals( other.resultBinder ) &&
               Arrays.equals( this.resultConstraints, other.resultConstraints );
    }

    /**
     * Creates a BetaMemory for the BetaNode's memory.
     */
    public Memory createMemory(final RuleBaseConfiguration config, ReteEvaluator reteEvaluator) {
        BetaMemory betaMemory = this.constraints.createBetaMemory(config,
                                                                  NodeTypeEnums.AccumulateNode);
        AccumulateMemory memory = this.accumulate.isMultiFunction() ?
                                  new MultiAccumulateMemory(this.accumulate, betaMemory, this.accumulate.getAccumulators()) :
                                  new SingleAccumulateMemory(this.accumulate, betaMemory, this.accumulate.getAccumulators()[0]);
        memory.workingMemoryContext = this.accumulate.createWorkingMemoryContext();
        memory.resultsContext = this.resultBinder.createContext();
        return memory;
    }

    public interface BaseAccumulation {
        PropagationContext getPropagationContext();

        void setPropagationContext(PropagationContext propagationContext);
    }

    public static class GroupByContext implements BaseAccumulation {
        private PropagationContext                              propagationContext;
        private Map<Object, TupleList<AccumulateContextEntry> > groupsMap = new HashMap<>();
        private TupleList<AccumulateContextEntry>               lastTupleList;
        private TupleList<AccumulateContextEntry>               toPropagateList;

        public PropagationContext getPropagationContext() {
            return propagationContext;
        }

        public void setPropagationContext(PropagationContext propagationContext) {
            this.propagationContext = propagationContext;
        }

        public Map<Object, TupleList<AccumulateContextEntry>> getGroups() {
            return groupsMap;
        }

        public TupleList<AccumulateContextEntry> getGroup(Object workingMemoryContext, Accumulate accumulate, Tuple leftTuple,
                                                          Object key, ReteEvaluator reteEvaluator) {
            return groupsMap.computeIfAbsent(key, k -> {
                AccumulateContextEntry entry = new AccumulateContextEntry(key);
                entry.setFunctionContext( accumulate.init(workingMemoryContext, entry, accumulate.createFunctionContext(), leftTuple, reteEvaluator) );
                PhreakAccumulateNode.initContext(workingMemoryContext, reteEvaluator, accumulate, leftTuple, entry);
                return new TupleList<>(entry);
            });
        }

        public void removeGroup(Object key) {
            groupsMap.remove(key);
        }

        public void moveToPropagateTupleList(TupleList<AccumulateContextEntry> list) {
            this.lastTupleList = list;
            if ( list.getContext().isToPropagate()) {
                return;
            }

            // add list to head
            list.setNext(toPropagateList);
            toPropagateList = list;

            list.getContext().setToPropagate(true);
        }

        public TupleList<AccumulateContextEntry> takeToPropagateList() {
            TupleList<AccumulateContextEntry> list = toPropagateList;
            toPropagateList = null;
            return list;
        }

        public void addMatchOnLastTupleList(LeftTuple match) {
            lastTupleList.add(match);
            lastTupleList.getContext().setEmpty( false );
        }

        public void clear() {
            for (TupleList<AccumulateContextEntry> list : groupsMap.values()) {
                for ( Tuple tuple = list.getFirst(); list.getFirst() != null; tuple = list.getFirst()) {
                    list.remove(tuple);
                    tuple.setContextObject(null);
                }
            }
            groupsMap.clear();
            toPropagateList = null;
            lastTupleList = null;
        }
    }

    public LeftTuple createLeftTuple(InternalFactHandle factHandle,
                                     boolean leftTupleMemoryEnabled) {
        return new JoinNodeLeftTuple(factHandle, this, leftTupleMemoryEnabled);
    }

    public LeftTuple createLeftTuple(final InternalFactHandle factHandle,
                                     final LeftTuple leftTuple,
                                     final Sink sink) {
        return new JoinNodeLeftTuple(factHandle, leftTuple, sink);
    }

    public LeftTuple createLeftTuple(LeftTuple leftTuple,
                                     Sink sink,
                                     PropagationContext pctx,
                                     boolean leftTupleMemoryEnabled) {
        return new JoinNodeLeftTuple(leftTuple, sink, pctx, leftTupleMemoryEnabled);
    }

    public LeftTuple createLeftTuple(LeftTuple leftTuple,
                                     RightTuple rightTuple,
                                     Sink sink) {
        return new JoinNodeLeftTuple(leftTuple, rightTuple, sink);
    }

    public LeftTuple createLeftTuple(LeftTuple leftTuple,
                                     RightTuple rightTuple,
                                     LeftTuple currentLeftChild,
                                     LeftTuple currentRightChild,
                                     Sink sink,
                                     boolean leftTupleMemoryEnabled) {
        return new JoinNodeLeftTuple(leftTuple, rightTuple, currentLeftChild, currentRightChild, sink, leftTupleMemoryEnabled);
    }


    public LeftTuple createPeer(LeftTuple original) {
        JoinNodeLeftTuple peer = new JoinNodeLeftTuple();
        peer.initPeer(original, this);
        original.setPeer(peer);
        return peer;
    }

    /**
     *  @inheritDoc
     *
     *  If an object is retract, call modify tuple for each
     *  tuple match.
     */
    public void retractRightTuple( final RightTuple rightTuple,
                                   final PropagationContext pctx,
                                   final ReteEvaluator reteEvaluator ) {
        final AccumulateMemory memory = (AccumulateMemory) reteEvaluator.getNodeMemory( this );

        BetaMemory bm = memory.getBetaMemory();
        rightTuple.setPropagationContext( pctx );
        doDeleteRightTuple( rightTuple, reteEvaluator, bm );
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
