package org.kie.dmn.core.compiler.alphanetbased;

import java.util.UUID;

import org.drools.core.reteoo.AlphaNode;
import org.drools.core.reteoo.ObjectSource;
import org.drools.core.rule.Declaration;
import org.drools.core.spi.Constraint;
import org.drools.model.Index;
import org.drools.model.constraints.SingleConstraint1;
import org.drools.model.functions.Function1;
import org.drools.model.functions.Predicate1;
import org.drools.model.index.AlphaIndexImpl;
import org.drools.modelcompiler.constraints.ConstraintEvaluator;
import org.drools.modelcompiler.constraints.LambdaConstraint;

import static org.drools.core.reteoo.builder.BuildUtils.attachNode;

public class AlphaNetworkCompilerUtils {

    public static void addResultSink(NetworkBuilderContext ctx, DMNCompiledAlphaNetwork network, ObjectSource source, Object result) {
        source.addObjectSink(new ResultCollectorAlphaSink(ctx.buildContext.getNextId(), source, ctx.buildContext, result, network.getResultCollector()));
    }

    public static AlphaNode createAlphaNode(NetworkBuilderContext ctx, ObjectSource source, String id, Predicate1<TableContext> predicate) {
        return createAlphaNode(ctx, source, id, predicate, null);
    }

    @Deprecated
    public static AlphaNode createAlphaNode(NetworkBuilderContext ctx, ObjectSource source, Predicate1<TableContext> predicate, Index index) {
        return createAlphaNode(ctx, source, UUID.randomUUID().toString(), predicate, null);
    }

    /**
     * IMPORTANT: remember to use the FEEL expression as an Identifier for the same constraint
     *
     * Prefix: column name + value
     */
    public static AlphaNode createAlphaNode(NetworkBuilderContext ctx, ObjectSource source, String id, Predicate1<TableContext> predicate, Index index) {
        SingleConstraint1 constraint = new SingleConstraint1(id, ctx.variable, predicate);
        constraint.setIndex(index);
        LambdaConstraint lambda = new LambdaConstraint(new ConstraintEvaluator(new Declaration[]{ctx.declaration}, constraint));
        lambda.setType(Constraint.ConstraintType.ALPHA);
        return attachNode(ctx.buildContext, new AlphaNode(ctx.buildContext.getNextId(), lambda, source, ctx.buildContext));
    }

    public static <I> AlphaIndexImpl<TableContext, I> createIndex(Class<I> indexedClass, Function1<TableContext, I> leftExtractor, I rightValue) {
        return new AlphaIndexImpl<TableContext, I>(indexedClass, Index.ConstraintType.EQUAL, 1, leftExtractor, rightValue);
    }
}
