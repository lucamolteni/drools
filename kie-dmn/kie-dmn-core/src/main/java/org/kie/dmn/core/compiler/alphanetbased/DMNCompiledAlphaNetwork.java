package org.kie.dmn.core.compiler.alphanetbased;

import org.kie.dmn.feel.lang.EvaluationContext;

public interface DMNCompiledAlphaNetwork {
    Object evaluate(EvaluationContext evalCtx);
}
