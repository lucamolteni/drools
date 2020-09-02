package org.kie.dmn.core.alphasupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.lang.Override;

import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.core.reteoo.AlphaNode;
import org.drools.model.Index;
import org.kie.dmn.core.compiler.alphanetbased.DMNCompiledAlphaNetwork;
import org.kie.dmn.core.compiler.alphanetbased.NetworkBuilderContext;
import org.kie.dmn.core.compiler.alphanetbased.ResultCollector;
import org.kie.dmn.feel.lang.EvaluationContext;
import org.kie.dmn.core.compiler.alphanetbased.AlphaNetworkCompilerUtils;

import static org.drools.compiler.reteoo.compiled.ObjectTypeNodeCompiler.compile;
import static org.kie.dmn.core.compiler.alphanetbased.AlphaNetworkCompilerUtils.addResultSink;
import static org.kie.dmn.core.compiler.alphanetbased.AlphaNetworkCompilerUtils.createAlphaNode;
import static org.kie.dmn.core.compiler.alphanetbased.AlphaNetworkCompilerUtils.createIndex;

// All implementations are used only for templating purposes and should never be called
public class DMNAlphaNetworkTemplate implements DMNCompiledAlphaNetwork {

    protected final ResultCollector resultCollector = new ResultCollector();

    @Override
    public Object evaluate(EvaluationContext evalCtx) {
        return "LOW";
    }

    @Override
    public ResultCollector getResultCollector() {
        return resultCollector;
    }
}
