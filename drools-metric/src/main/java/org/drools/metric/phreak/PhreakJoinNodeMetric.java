/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.drools.metric.phreak;

import org.drools.core.common.ReteEvaluator;
import org.drools.core.common.TupleSets;
import org.drools.core.phreak.PhreakJoinNode;
import org.drools.core.reteoo.BetaMemory;
import org.drools.core.reteoo.JoinNode;
import org.drools.core.reteoo.AbstractLeftTuple;
import org.drools.core.reteoo.LeftTupleSink;
import org.drools.metric.util.MetricLogUtils;

public class PhreakJoinNodeMetric extends PhreakJoinNode {

    @Override
    public void doNode(JoinNode joinNode,
                       LeftTupleSink sink,
                       BetaMemory bm,
                       ReteEvaluator reteEvaluator,
                       TupleSets<AbstractLeftTuple> srcLeftTuples,
                       TupleSets<AbstractLeftTuple> trgLeftTuples,
                       TupleSets<AbstractLeftTuple> stagedLeftTuples) {
        try {
            MetricLogUtils.getInstance().startMetrics(joinNode);

            super.doNode(joinNode, sink, bm, reteEvaluator, srcLeftTuples, trgLeftTuples, stagedLeftTuples);

        } finally {
            MetricLogUtils.getInstance().logAndEndMetrics();
        }
    }
}
