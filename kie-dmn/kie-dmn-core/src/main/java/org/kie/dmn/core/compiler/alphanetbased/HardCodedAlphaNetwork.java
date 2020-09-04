/*
 * Copyright 2005 JBoss Inc
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

package org.kie.dmn.core.compiler.alphanetbased;

import java.util.Arrays;
import java.util.List;

import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.core.common.DefaultFactHandle;
import org.drools.core.reteoo.AlphaNode;
import org.drools.core.reteoo.compiled.CompiledNetwork;
import org.drools.model.Index;
import org.kie.dmn.feel.lang.EvaluationContext;

import static org.drools.compiler.reteoo.compiled.ObjectTypeNodeCompiler.compile;
import static org.kie.dmn.core.compiler.alphanetbased.AlphaNetworkCompilerUtils.addResultSink;
import static org.kie.dmn.core.compiler.alphanetbased.AlphaNetworkCompilerUtils.createAlphaNode;
import static org.kie.dmn.core.compiler.alphanetbased.AlphaNetworkCompilerUtils.createIndex;
import static org.kie.dmn.feel.codegen.feel11.CompiledFEELSemanticMappings.gracefulEq;
import static org.kie.dmn.feel.codegen.feel11.CompiledFEELSemanticMappings.gt;
import static org.kie.dmn.feel.codegen.feel11.CompiledFEELSemanticMappings.includes;
import static org.kie.dmn.feel.codegen.feel11.CompiledFEELSemanticMappings.lt;
import static org.kie.dmn.feel.codegen.feel11.CompiledFEELSemanticMappings.range;

public class HardCodedAlphaNetwork implements DMNCompiledAlphaNetwork {

    protected final ResultCollector resultCollector = new ResultCollector();
    protected CompiledNetwork compiledNetwork;

    @Override
    public Object evaluate( EvaluationContext evalCtx ) {
        resultCollector.results.clear();
        TableContext ctx = new TableContext( evalCtx, "Existing Customer", "Application Risk Score" );
        compiledNetwork.assertObject( new DefaultFactHandle( ctx ), null, null );
        return applyHitPolicy( resultCollector.results );
    }

    @Override
    public ResultCollector getResultCollector() {
        return resultCollector;
    }

    private Object applyHitPolicy( List<Object> results ) {
        return results.get(0);
    }

    public static final org.kie.dmn.feel.runtime.UnaryTest UT1 = (feelExprCtx, left) -> gracefulEq(feelExprCtx, "false", left);
    public static final org.kie.dmn.feel.runtime.UnaryTest UT1x = (feelExprCtx, left) -> gracefulEq(feelExprCtx, "false", left);

    public static final java.math.BigDecimal K_80 = new java.math.BigDecimal(80, java.math.MathContext.DECIMAL128);
    public static final java.math.BigDecimal K_90 = new java.math.BigDecimal(90, java.math.MathContext.DECIMAL128);
    public static final java.math.BigDecimal K_100 = new java.math.BigDecimal(100, java.math.MathContext.DECIMAL128);
    public static final java.math.BigDecimal K_110 = new java.math.BigDecimal(110, java.math.MathContext.DECIMAL128);
    public static final java.math.BigDecimal K_120 = new java.math.BigDecimal(120, java.math.MathContext.DECIMAL128);
    public static final java.math.BigDecimal K_130 = new java.math.BigDecimal(130, java.math.MathContext.DECIMAL128);

    public static final org.kie.dmn.feel.runtime.UnaryTest UT2 = (feelExprCtx, left) -> lt(left, K_100);


    public static final org.kie.dmn.feel.runtime.UnaryTest UT3 = (feelExprCtx, left) -> includes(feelExprCtx, range(feelExprCtx,
                                                                                                                    org.kie.dmn.feel.runtime.Range.RangeBoundary.CLOSED,
                                                                                                                    K_100, K_120,
                                                                                                                    org.kie.dmn.feel.runtime.Range.RangeBoundary.OPEN), left);


    public static final org.kie.dmn.feel.runtime.UnaryTest UT4 = (feelExprCtx, left) -> includes(feelExprCtx, range(feelExprCtx,
                                                                                                                    org.kie.dmn.feel.runtime.Range.RangeBoundary.CLOSED,
                                                                                                                    K_120, K_130,
                                                                                                                    org.kie.dmn.feel.runtime.Range.RangeBoundary.CLOSED), left);

    public static final org.kie.dmn.feel.runtime.UnaryTest UT5 = (feelExprCtx, left) -> gt(left, K_130);

    public static final org.kie.dmn.feel.runtime.UnaryTest UT6 = (feelExprCtx, left) -> gracefulEq(feelExprCtx, "true", left);

    public static final org.kie.dmn.feel.runtime.UnaryTest UT7 = (feelExprCtx, left) -> lt(left, K_80);

    public static final org.kie.dmn.feel.runtime.UnaryTest UT8 = (feelExprCtx, left) -> includes(feelExprCtx, range(feelExprCtx,
                                                                                                                    org.kie.dmn.feel.runtime.Range.RangeBoundary.CLOSED,
                                                                                                                    K_80, K_90,
                                                                                                                    org.kie.dmn.feel.runtime.Range.RangeBoundary.OPEN), left);

    public static final org.kie.dmn.feel.runtime.UnaryTest UT9 = (feelExprCtx, left) -> includes(feelExprCtx, range(feelExprCtx,
                                                                                                                    org.kie.dmn.feel.runtime.Range.RangeBoundary.CLOSED,
                                                                                                                    K_90, K_110,
                                                                                                                    org.kie.dmn.feel.runtime.Range.RangeBoundary.CLOSED), left);

    public static final org.kie.dmn.feel.runtime.UnaryTest UT10 = (feelExprCtx, left) -> gt(left, K_110);

    public HardCodedAlphaNetwork() {

        NetworkBuilderContext ctx = new NetworkBuilderContext();

        Index index1 = createIndex(String.class, x -> (String)x.getValue(0), "false");
        AlphaNode alphac1r1 = createAlphaNode(ctx, ctx.otn, "\"false\"", x -> UT1.apply(x.getEvalCtx(),
                                                                                        x.getValue(0)), // numero colonna
                                              index1);

        AlphaNode alphac2r1 = createAlphaNode(ctx, alphac1r1, "<100", x -> UT2.apply(x.getEvalCtx(), x.getValue(1)));
        addResultSink(ctx, this, alphac2r1, "HIGH");

        // == alphac1r1 (verifica alpha node sharing)
        AlphaNode alphac1r2 = createAlphaNode(ctx, ctx.otn, "\"false\"", x -> UT1.apply(x.getEvalCtx(), x.getValue(0)), index1);
        AlphaNode alphac2r2 = createAlphaNode(ctx, alphac1r2, "[100..120)", x -> UT3.apply(x.getEvalCtx(), x.getValue(1)));

        addResultSink(ctx, this, alphac2r2, "MEDIUM");

        AlphaNode alphac2r3 = createAlphaNode(ctx, alphac1r1, "[120..130]", x -> UT4.apply(x.getEvalCtx(), x.getValue(1)));
        addResultSink(ctx, this, alphac2r3, "LOW");

        AlphaNode alphac2r4 = createAlphaNode(ctx, alphac1r1, ">130", x -> UT5.apply(x.getEvalCtx(), x.getValue(1)));
        addResultSink(ctx, this, alphac2r4, "VERY LOW");

        Index index2 = createIndex(String.class, x -> (String)x.getValue(0), "true");
        AlphaNode alphac1r5 = createAlphaNode(ctx, ctx.otn, "\"true\"", x -> UT6.apply(x.getEvalCtx(), x.getValue(0)), index2);

        AlphaNode alphac2r5 = createAlphaNode(ctx, alphac1r5, "<80", x -> UT7.apply(x.getEvalCtx(), x.getValue(1)));
        addResultSink(ctx, this, alphac2r5, "DECLINE");
        AlphaNode alphac2r6 = createAlphaNode(ctx, alphac1r5, "[80..90)", x -> UT8.apply(x.getEvalCtx(), x.getValue(1)));
        addResultSink(ctx, this, alphac2r6, "HIGH");
        AlphaNode alphac2r7 = createAlphaNode(ctx, alphac1r5, "[90..110]", x -> UT9.apply(x.getEvalCtx(), x.getValue(1)));
        addResultSink(ctx, this, alphac2r7, "MEDIUM");
        AlphaNode alphac2r8 = createAlphaNode(ctx, alphac1r5, ">110", x -> UT10.apply(x.getEvalCtx(), x.getValue(1)));
        addResultSink(ctx, this, alphac2r8, "LOW");

        char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
        System.out.println(System.getProperty("alphalength"));
        int alphalength = Integer.valueOf(System.getProperty("alphalength", "52"));
        alphabet = Arrays.copyOf(alphabet, alphalength);
        for (char c : alphabet) {
            alphabet(this, ctx, String.valueOf(c));
        }

        Index index3 = createIndex(String.class, x -> (String)x.getValue(0), "dummy");
        AlphaNode alphaDummy = createAlphaNode(ctx, ctx.otn, x -> false, index3);
        addResultSink(ctx, this, alphaDummy, "DUMMY");

        this.compiledNetwork = compile(new KnowledgeBuilderImpl(ctx.kBase), ctx.otn);
        this.compiledNetwork.setObjectTypeNode(ctx.otn);
    }

    private static void alphabet(HardCodedAlphaNetwork network, NetworkBuilderContext ctx, String sChar) {
        final org.kie.dmn.feel.runtime.UnaryTest UTx = (feelExprCtx, left) -> gracefulEq(feelExprCtx, sChar, left);
        Index index1 = createIndex(String.class, x -> (String) x.getValue(0), sChar);
        AlphaNode alphac1r1 = createAlphaNode(ctx, ctx.otn, "\"" + sChar + "\"", x -> UTx.apply(x.getEvalCtx(), x.getValue(0)), index1);

        AlphaNode alphac2r1 = createAlphaNode(ctx, alphac1r1, "<100", x -> UT2.apply(x.getEvalCtx(), x.getValue(1)));
        addResultSink(ctx, network, alphac2r1, "HIGH");
        AlphaNode alphac2r2 = createAlphaNode(ctx, alphac1r1, "[100..120)", x -> UT3.apply(x.getEvalCtx(), x.getValue(1)));
        addResultSink(ctx, network, alphac2r2, "MEDIUM");
        AlphaNode alphac2r3 = createAlphaNode(ctx, alphac1r1, "[120..130]", x -> UT4.apply(x.getEvalCtx(), x.getValue(1)));
        addResultSink(ctx, network, alphac2r3, "LOW");
        AlphaNode alphac2r4 = createAlphaNode(ctx, alphac1r1, ">130", x -> UT5.apply(x.getEvalCtx(), x.getValue(1)));
        addResultSink(ctx, network, alphac2r4, "VERY LOW");
    }
}
