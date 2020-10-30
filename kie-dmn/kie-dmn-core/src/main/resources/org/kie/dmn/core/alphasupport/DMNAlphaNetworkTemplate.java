package org.kie.dmn.core.alphasupport;

import java.lang.Override;
import java.util.Map;

import org.drools.ancompiler.CompiledNetworkSource;
import org.drools.ancompiler.ObjectTypeNodeCompiler;
import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.core.common.DefaultFactHandle;
import org.drools.core.reteoo.AlphaNode;
import org.drools.ancompiler.CompiledNetwork;
import org.drools.core.reteoo.Rete;
import org.drools.core.reteoo.ReteDumper;
import org.drools.model.Index;
import org.kie.dmn.core.compiler.alphanetbased.DMNCompiledAlphaNetwork;
import org.kie.dmn.core.compiler.alphanetbased.NetworkBuilderContext;
import org.kie.dmn.core.compiler.alphanetbased.ResultCollector;
import org.kie.dmn.feel.lang.EvaluationContext;
import org.kie.dmn.core.compiler.alphanetbased.TableContext;
import org.kie.memorycompiler.KieMemoryCompiler;

import static org.drools.core.util.MapUtils.mapValues;
import static org.kie.dmn.core.compiler.alphanetbased.AlphaNetworkCompilerUtils.addResultSink;
import static org.kie.dmn.core.compiler.alphanetbased.AlphaNetworkCompilerUtils.createAlphaNode;
import static org.kie.dmn.core.compiler.alphanetbased.AlphaNetworkCompilerUtils.createIndex;

// All implementations are used only for templating purposes and should never be called
public class DMNAlphaNetworkTemplate implements DMNCompiledAlphaNetwork {

    protected final ResultCollector resultCollector = new ResultCollector();
    protected CompiledNetwork compiledNetwork;

    protected final NetworkBuilderContext ctx = new NetworkBuilderContext(resultCollector);

    public DMNAlphaNetworkTemplate() {

        // Alpha network creation statements
        {

        }

        Index index3 = createIndex(String.class, x -> (String)x.getValue(0), "dummy");
        AlphaNode alphaDummy = createAlphaNode(ctx, ctx.otn, x -> false, index3);
        addResultSink(ctx, alphaDummy, "DUMMY");

        Rete rete = ctx.kBase.getRete();

        ReteDumper.dumpRete(rete);

//        Map<String, CompiledNetworkSource> compiledNetworkSourcesMap = ObjectTypeNodeCompiler.compiledNetworkSourceMap(rete);
//
//        Map<String, Class<?>> compiledClasses = KieMemoryCompiler.compile(mapValues(compiledNetworkSourcesMap, CompiledNetworkSource::getSource),
//                                                                          this.getClass().getClassLoader());
//        compiledNetworkSourcesMap.values().forEach(c -> {
//            Class<?> aClass = compiledClasses.get(c.getName());
//            c.setCompiledNetwork(aClass);
//        });
    }

    @Override
    public Object evaluate( EvaluationContext evalCtx ) {
        resultCollector.clearResults();
        TableContext ctx = new TableContext( evalCtx, "Existing Customer", "Application Risk Score" );
        compiledNetwork.propagateAssertObject(new DefaultFactHandle(ctx ), null, null );
        return resultCollector.getWithHitPolicy();
    }

    @Override
    public ResultCollector getResultCollector() {
        return resultCollector;
    }
}
