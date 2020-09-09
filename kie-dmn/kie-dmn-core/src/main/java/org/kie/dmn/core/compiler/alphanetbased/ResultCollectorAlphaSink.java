package org.kie.dmn.core.compiler.alphanetbased;

import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.reteoo.LeftInputAdapterNode;
import org.drools.core.reteoo.ModifyPreviousTuples;
import org.drools.core.reteoo.ObjectSource;
import org.drools.core.reteoo.builder.BuildContext;
import org.drools.core.spi.PropagationContext;

public class ResultCollectorAlphaSink extends LeftInputAdapterNode {

    private final Object result;
    private final ResultCollector resultCollector;

    public ResultCollectorAlphaSink(int id, ObjectSource source, BuildContext context, Object result, ResultCollector resultCollector) {
        super(id, source, context);
        this.result = result;
        this.resultCollector = resultCollector;
    }

    @Override
    public void assertObject(InternalFactHandle factHandle, PropagationContext propagationContext, InternalWorkingMemory workingMemory) {
        resultCollector.addResult(result);
    }

    @Override
    public void modifyObject(InternalFactHandle factHandle, ModifyPreviousTuples modifyPreviousTuples, PropagationContext context, InternalWorkingMemory workingMemory) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void byPassModifyToBetaNode(InternalFactHandle factHandle, ModifyPreviousTuples modifyPreviousTuples, PropagationContext context, InternalWorkingMemory workingMemory) {
        throw new UnsupportedOperationException();
    }
}
