package org.kie.dmn.core.alphasupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.lang.Override;

import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.core.common.DefaultFactHandle;
import org.drools.core.reteoo.AlphaNode;
import org.drools.core.reteoo.compiled.CompiledNetwork;
import org.drools.model.Index;
import org.kie.dmn.core.compiler.alphanetbased.DMNCompiledAlphaNetwork;
import org.kie.dmn.core.compiler.alphanetbased.NetworkBuilderContext;
import org.kie.dmn.core.compiler.alphanetbased.ResultCollector;
import org.kie.dmn.feel.lang.EvaluationContext;
import org.kie.dmn.core.compiler.alphanetbased.AlphaNetworkCompilerUtils;
import org.drools.core.reteoo.AlphaNode;
import org.kie.dmn.core.compiler.alphanetbased.TableContext;

import static org.drools.compiler.reteoo.compiled.ObjectTypeNodeCompiler.compile;
import static org.kie.dmn.core.compiler.alphanetbased.AlphaNetworkCompilerUtils.addResultSink;
import static org.kie.dmn.core.compiler.alphanetbased.AlphaNetworkCompilerUtils.createAlphaNode;
import static org.kie.dmn.core.compiler.alphanetbased.AlphaNetworkCompilerUtils.createIndex;
import static org.kie.dmn.core.compiler.alphanetbased.AlphaNetworkCompilerUtils.applyHitPolicy;
import static org.kie.dmn.feel.codegen.feel11.CompiledFEELSemanticMappings.gracefulEq;

// All implementations are used only for templating purposes and should never be called
public class DMNAlphaNetworkTemplate implements DMNCompiledAlphaNetwork {

    protected final ResultCollector resultCollector = new ResultCollector();
    protected CompiledNetwork compiledNetwork;

    public static final org.kie.dmn.feel.runtime.UnaryTest UT1 = (feelExprCtx, left) -> gracefulEq(feelExprCtx, "false", left);

    protected final NetworkBuilderContext ctx = new NetworkBuilderContext();

    public DMNAlphaNetworkTemplate() {

        // Alpha network creation statements
        {

        }

        Index index3 = createIndex(String.class, x -> (String)x.getValue(0), "dummy");
        AlphaNode alphaDummy = createAlphaNode(ctx, ctx.otn, x -> false, index3);
        addResultSink(ctx, this, alphaDummy, "DUMMY");

        this.compiledNetwork = compile(new KnowledgeBuilderImpl(ctx.kBase), ctx.otn);
        this.compiledNetwork.setObjectTypeNode(ctx.otn);
    }

    @Override
    public Object evaluate( EvaluationContext evalCtx ) {
        resultCollector.results.clear();
        TableContext ctx = new TableContext( evalCtx, "Existing Customer", "Application Risk Score" );
        compiledNetwork.assertObject(new DefaultFactHandle(ctx ), null, null );
//        return applyHitPolicy( resultCollector.results );
        return "LOW";
    }

    @Override
    public ResultCollector getResultCollector() {
        return resultCollector;
    }
}
