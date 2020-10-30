package org.kie.dmn.core.compiler.alphanetbased;

import org.drools.ancompiler.CompiledNetwork;
import org.kie.dmn.feel.lang.EvaluationContext;

public interface DMNCompiledAlphaNetwork {

    void initRete();

    CompiledNetwork createCompiledAlphaNetwork(AlphaNetDMNEvaluatorCompiler compiler);

    void setCompiledAlphaNetwork(CompiledNetwork compiledAlphaNetwork);

    Object evaluate(EvaluationContext evalCtx);

    ResultCollector getResultCollector();
}
