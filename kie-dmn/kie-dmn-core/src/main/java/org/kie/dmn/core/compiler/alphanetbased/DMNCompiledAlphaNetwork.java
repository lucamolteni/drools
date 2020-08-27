package org.kie.dmn.core.compiler.alphanetbased;

import org.drools.core.reteoo.compiled.CompiledNetwork;
import org.kie.dmn.feel.lang.EvaluationContext;

public interface DMNCompiledAlphaNetwork {

    Object evaluate(EvaluationContext evalCtx);

    ResultCollector getResultCollector();
}
