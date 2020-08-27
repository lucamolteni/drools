package org.kie.dmn.core.alphasupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.kie.dmn.core.compiler.alphanetbased.DMNCompiledAlphaNetwork;
import org.kie.dmn.feel.lang.EvaluationContext;

// All implementations are used only for templating purposes and should never be called
public class DMNAlphaNetworkTemplate implements DMNCompiledAlphaNetwork  {

    public Object evaluate(EvaluationContext evalCtx) {
        return "LOW";
    }

}
