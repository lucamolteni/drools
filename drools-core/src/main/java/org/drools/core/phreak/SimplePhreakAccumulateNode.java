/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.drools.core.phreak;

import org.drools.base.rule.Accumulate;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.PropagationContext;
import org.drools.core.common.ReteEvaluator;
import org.drools.core.common.TupleSets;
import org.drools.core.reteoo.AccumulateNode;
import org.drools.core.reteoo.AccumulateNode.AccumulateMemory;
import org.drools.core.reteoo.AccumulateNode.BaseAccumulation;
import org.drools.core.reteoo.LeftTuple;
import org.drools.core.reteoo.LeftTupleSink;

public class SimplePhreakAccumulateNode {

    public void doNode(AccumulateNode accNode,
                       LeftTupleSink sink,
                       AccumulateMemory am,
                       ReteEvaluator reteEvaluator,
                       TupleSets<LeftTuple> srcLeftTuples,
                       TupleSets<LeftTuple> trgLeftTuples,
                       TupleSets<LeftTuple> stagedLeftTuples) {

        Accumulate accumulate = accNode.getAccumulate();

        for (LeftTuple leftTuple = srcLeftTuples.getInsertFirst(); leftTuple != null; ) {
            LeftTuple next = leftTuple.getStagedNext();
            final PropagationContext context = leftTuple.getPropagationContext();
            final BaseAccumulation baseAccumulation = (BaseAccumulation) leftTuple.getContextObject();

            Object result = accumulate.getResult(am.workingMemoryContext, baseAccumulation, leftTuple, reteEvaluator);

            InternalFactHandle resultFactHandle = accNode.createResultFactHandle(context, reteEvaluator, leftTuple, result);

            LeftTuple resultTuple = sink.createLeftTuple(resultFactHandle, leftTuple, sink);
            trgLeftTuples.addInsert(resultTuple);

            leftTuple = next;
        }
    }
}
