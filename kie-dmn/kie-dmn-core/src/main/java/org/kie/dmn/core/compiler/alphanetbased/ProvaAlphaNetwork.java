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

import java.util.ArrayList;
import java.util.List;

import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.core.base.ClassObjectType;
import org.drools.core.common.DefaultFactHandle;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.impl.KnowledgeBaseFactory;
import org.drools.core.reteoo.AlphaNode;
import org.drools.core.reteoo.EntryPointNode;
import org.drools.core.reteoo.LeftInputAdapterNode;
import org.drools.core.reteoo.ModifyPreviousTuples;
import org.drools.core.reteoo.ObjectSource;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.core.reteoo.builder.BuildContext;
import org.drools.core.reteoo.compiled.CompiledNetwork;
import org.drools.core.rule.Declaration;
import org.drools.core.rule.Pattern;
import org.drools.core.spi.Constraint;
import org.drools.core.spi.PropagationContext;
import org.drools.model.Variable;
import org.drools.model.constraints.SingleConstraint1;
import org.drools.model.functions.Predicate1;
import org.drools.modelcompiler.constraints.ConstraintEvaluator;
import org.drools.modelcompiler.constraints.LambdaConstraint;

import static org.drools.compiler.reteoo.compiled.ObjectTypeNodeCompiler.compile;
import static org.drools.core.reteoo.builder.BuildUtils.attachNode;
import static org.drools.model.DSL.declarationOf;

public class ProvaAlphaNetwork {

    private final ResultCollector resultCollector = new ResultCollector();

    private CompiledNetwork compiledNetwork;

    public static void main(String[] args) {
        ProvaAlphaNetwork provaAlphaNetwork = generateCompiledNetwork();

        Object result = provaAlphaNetwork.evaluate("Ciao");
        System.out.println(result);
    }

    public Object evaluate( Object evalCtx ) {
        resultCollector.results.clear();
        compiledNetwork.assertObject( new DefaultFactHandle( evalCtx ), null, null );
        return applyHitPolicy( resultCollector.results );
    }

    private Object applyHitPolicy( List<Object> results ) {
        return results.get(0);
    }

    public static ProvaAlphaNetwork generateCompiledNetwork() {
        ProvaAlphaNetwork network = new ProvaAlphaNetwork();

        NetworkBuilderContext ctx = new NetworkBuilderContext();

        Predicate1<String> stringPredicate1 = x -> true;
        AlphaNode alphac1r1 = createAlphaNode(ctx, ctx.otn, "\"true\"", stringPredicate1);

        addResultSink(ctx, network, alphac1r1, "arrivederci roma");

        network.compiledNetwork = compile(new KnowledgeBuilderImpl(ctx.kBase), ctx.otn);
        network.compiledNetwork.setObjectTypeNode(ctx.otn);
        return network;
    }

    private static void addResultSink(NetworkBuilderContext ctx, ProvaAlphaNetwork network, ObjectSource source, Object result ) {
        source.addObjectSink( new ResultCollectorAlphaSink( ctx.buildContext.getNextId(), source, ctx.buildContext, result, network.resultCollector ) );
    }

    private static AlphaNode createAlphaNode(NetworkBuilderContext ctx, ObjectSource source, String id, Predicate1<String> predicate) {
        SingleConstraint1<String> constraint = new SingleConstraint1<>(id, ctx.variable, predicate);
        LambdaConstraint lambda = new LambdaConstraint(new ConstraintEvaluator(new Declaration[]{ctx.declaration}, constraint));
        lambda.setType(Constraint.ConstraintType.ALPHA);
        return attachNode(ctx.buildContext, new AlphaNode(ctx.buildContext.getNextId(), lambda, source, ctx.buildContext));
    }

    static class ResultCollector {
        final List<Object> results = new ArrayList<>();
    }

    public static class ResultCollectorAlphaSink extends LeftInputAdapterNode {

        private final Object result;
        private final ResultCollector resultCollector;

        public ResultCollectorAlphaSink( int id, ObjectSource source, BuildContext context, Object result, ResultCollector resultCollector ) {
            super( id, source, context );
            this.result = result;
            this.resultCollector = resultCollector;
        }

        @Override
        public void assertObject( InternalFactHandle factHandle, PropagationContext propagationContext, InternalWorkingMemory workingMemory ) {
            resultCollector.results.add(result);
        }

        @Override
        public void modifyObject( InternalFactHandle factHandle, ModifyPreviousTuples modifyPreviousTuples, PropagationContext context, InternalWorkingMemory workingMemory ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void byPassModifyToBetaNode( InternalFactHandle factHandle, ModifyPreviousTuples modifyPreviousTuples, PropagationContext context, InternalWorkingMemory workingMemory ) {
            throw new UnsupportedOperationException();
        }
    }

    private static class NetworkBuilderContext {
        public InternalKnowledgeBase kBase;
        public BuildContext buildContext;
        public Variable<String> variable;
        public Declaration declaration;
        public ObjectTypeNode otn;

        public NetworkBuilderContext() {
            kBase = KnowledgeBaseFactory.newKnowledgeBase();
            buildContext = new BuildContext( kBase );
            EntryPointNode entryPoint = buildContext.getKnowledgeBase().getRete().getEntryPointNodes().values().iterator().next();
            variable = declarationOf( String.class, "$ctx" );

            ClassObjectType objectType = new ClassObjectType( String.class );
            Pattern pattern = new Pattern( 1, objectType, "$ctx" );
            declaration = pattern.getDeclaration();

            otn = new ObjectTypeNode( buildContext.getNextId(), entryPoint, objectType, buildContext );
            buildContext.setObjectSource( otn );
        }
    }
}
