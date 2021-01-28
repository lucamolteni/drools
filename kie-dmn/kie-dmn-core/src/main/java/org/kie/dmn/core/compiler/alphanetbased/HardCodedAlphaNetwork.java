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

package org.kie.dmn.core.compiler.alphanetbased;

import java.util.Arrays;

import org.drools.ancompiler.CompiledNetwork;
import org.drools.core.common.DefaultFactHandle;
import org.drools.core.reteoo.AlphaNode;
import org.drools.model.Index;
import org.kie.dmn.feel.lang.EvaluationContext;

import static org.kie.dmn.core.compiler.alphanetbased.AlphaNetworkCreation.createIndex;
import static org.kie.dmn.feel.codegen.feel11.CompiledFEELSemanticMappings.gracefulEq;
import static org.kie.dmn.feel.codegen.feel11.CompiledFEELSemanticMappings.gt;
import static org.kie.dmn.feel.codegen.feel11.CompiledFEELSemanticMappings.includes;
import static org.kie.dmn.feel.codegen.feel11.CompiledFEELSemanticMappings.lt;
import static org.kie.dmn.feel.codegen.feel11.CompiledFEELSemanticMappings.range;

public class HardCodedAlphaNetwork implements DMNCompiledAlphaNetwork {

    protected final ResultCollector resultCollector = new ResultCollector();
    protected CompiledNetwork compiledNetwork;

    private final NetworkBuilderContext ctx;
    private final AlphaNetworkCreation alphaNetworkCreation;

    public HardCodedAlphaNetwork() {
        ctx = new NetworkBuilderContext(resultCollector);
        alphaNetworkCreation = new AlphaNetworkCreation(ctx);
    }

    @Override
    public void initRete() {


        Index index1 = createIndex(String.class, x -> (String) x.getValue(0), "false");
        AlphaNode alphac1r1 = alphaNetworkCreation.createAlphaNode(ctx.otn, "\"false\"", x -> UT1.apply(x.getEvalCtx(),
                                                                                        x.getValue(0)),
                                              index1);

        AlphaNode alphac2r1 = alphaNetworkCreation.createAlphaNode( alphac1r1, "<100", x -> UT2.apply(x.getEvalCtx(), x.getValue(1)));
        alphaNetworkCreation.addResultSink( alphac2r1, "HIGH");

        // == alphac1r1 (verifica alpha node sharing)
        AlphaNode alphac1r2 = alphaNetworkCreation.createAlphaNode( ctx.otn, "\"false\"", x -> UT1.apply(x.getEvalCtx(), x.getValue(0)), index1);
        AlphaNode alphac2r2 = alphaNetworkCreation.createAlphaNode( alphac1r2, "[100..120)", x -> UT3.apply(x.getEvalCtx(), x.getValue(1)));

        alphaNetworkCreation.addResultSink( alphac2r2, "MEDIUM");

        AlphaNode alphac2r3 = alphaNetworkCreation.createAlphaNode( alphac1r1, "[120..130]", x -> UT4.apply(x.getEvalCtx(), x.getValue(1)));
        alphaNetworkCreation.addResultSink( alphac2r3, "LOW");

        AlphaNode alphac2r4 = alphaNetworkCreation.createAlphaNode( alphac1r1, ">130", x -> UT5.apply(x.getEvalCtx(), x.getValue(1)));
        alphaNetworkCreation.addResultSink( alphac2r4, "VERY LOW");

        Index index2 = createIndex(String.class, x -> (String) x.getValue(0), "true");
        AlphaNode alphac1r5 = alphaNetworkCreation.createAlphaNode( ctx.otn, "\"true\"", x -> UT6.apply(x.getEvalCtx(), x.getValue(0)), index2);

        AlphaNode alphac2r5 = alphaNetworkCreation.createAlphaNode( alphac1r5, "<80", x -> UT7.apply(x.getEvalCtx(), x.getValue(1)));
        alphaNetworkCreation.addResultSink( alphac2r5, "DECLINE");
        AlphaNode alphac2r6 = alphaNetworkCreation.createAlphaNode( alphac1r5, "[80..90)", x -> UT8.apply(x.getEvalCtx(), x.getValue(1)));
        alphaNetworkCreation.addResultSink( alphac2r6, "HIGH");
        AlphaNode alphac2r7 = alphaNetworkCreation.createAlphaNode( alphac1r5, "[90..110]", x -> UT9.apply(x.getEvalCtx(), x.getValue(1)));
        alphaNetworkCreation.addResultSink( alphac2r7, "MEDIUM");
        AlphaNode alphac2r8 = alphaNetworkCreation.createAlphaNode( alphac1r5, ">110", x -> UT10.apply(x.getEvalCtx(), x.getValue(1)));
        alphaNetworkCreation.addResultSink( alphac2r8, "LOW");

        char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
        System.out.println(System.getProperty("alphalength"));
        int alphalength = Integer.valueOf(System.getProperty("alphalength", "52"));
        alphabet = Arrays.copyOf(alphabet, alphalength);
        for (char c : alphabet) {
            alphabet(ctx, String.valueOf(c));
        }

        Index index3 = createIndex(String.class, x -> (String) x.getValue(0), "dummy");
        AlphaNode alphaDummy = alphaNetworkCreation.createAlphaNode( ctx.otn, x -> false, index3);
        alphaNetworkCreation.addResultSink( alphaDummy, "DUMMY");
    }

    @Override
    public CompiledNetwork createCompiledAlphaNetwork(AlphaNetDMNEvaluatorCompiler compiler) {
        return compiler.createCompiledAlphaNetwork(ctx.otn);
    }

    @Override
    public void setCompiledAlphaNetwork(CompiledNetwork compiledAlphaNetwork) {
        this.compiledNetwork = compiledAlphaNetwork;
    }

    @Override
    public Object evaluate(EvaluationContext evalCtx) {
        resultCollector.clearResults();
        TableContext ctx = new TableContext(evalCtx, "Existing Customer", "Application Risk Score");
        compiledNetwork.propagateAssertObject(new DefaultFactHandle(ctx), null, null);
        return resultCollector.getWithHitPolicy();
    }

    @Override
    public ResultCollector getResultCollector() {
        return resultCollector;
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

    private void alphabet(NetworkBuilderContext ctx, String sChar) {
        final org.kie.dmn.feel.runtime.UnaryTest UTx = (feelExprCtx, left) -> gracefulEq(feelExprCtx, sChar, left);
        Index index1 = createIndex(String.class, x -> (String) x.getValue(0), sChar);
        AlphaNode alphac1r1 = alphaNetworkCreation.createAlphaNode(ctx.otn,"\"" + sChar + "\"", x -> UTx.apply(x.getEvalCtx(), x.getValue(0)), index1);

        AlphaNode alphac2r1 = alphaNetworkCreation.createAlphaNode( alphac1r1, "<100", x -> UT2.apply(x.getEvalCtx(), x.getValue(1)));
        alphaNetworkCreation.addResultSink( alphac2r1, "HIGH");
        AlphaNode alphac2r2 = alphaNetworkCreation.createAlphaNode( alphac1r1, "[100..120)", x -> UT3.apply(x.getEvalCtx(), x.getValue(1)));
        alphaNetworkCreation.addResultSink( alphac2r2, "MEDIUM");
        AlphaNode alphac2r3 = alphaNetworkCreation.createAlphaNode( alphac1r1, "[120..130]", x -> UT4.apply(x.getEvalCtx(), x.getValue(1)));
        alphaNetworkCreation.addResultSink( alphac2r3, "LOW");
        AlphaNode alphac2r4 = alphaNetworkCreation.createAlphaNode( alphac1r1, ">130", x -> UT5.apply(x.getEvalCtx(), x.getValue(1)));
        alphaNetworkCreation.addResultSink( alphac2r4, "VERY LOW");
    }
}
