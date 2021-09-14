package org.kie.dmn.core.compiler.alphanetbased;

import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import org.drools.ancompiler.Inlineable;
import org.drools.core.reteoo.AlphaNode;
import org.drools.core.reteoo.ObjectSource;
import org.drools.core.reteoo.builder.BuildContext;
import org.drools.core.rule.Declaration;
import org.drools.core.spi.AlphaNodeFieldConstraint;
import org.drools.core.spi.Constraint;
import org.drools.model.Index;
import org.drools.model.Variable;
import org.drools.model.constraints.SingleConstraint1;
import org.drools.model.functions.Predicate1;
import org.drools.model.functions.PredicateInformation;
import org.drools.modelcompiler.constraints.ConstraintEvaluator;
import org.drools.modelcompiler.constraints.LambdaConstraint;
import org.kie.dmn.core.compiler.alphanetbased.evaluator.TestEvaluator;

public class InlineableAlphaNode extends AlphaNode implements Inlineable {

    private MethodCallExpr methodCallExpr;

    public InlineableAlphaNode() {
    }

    public static Builder createBuilder() {
        return new Builder();
    }

    public static class Builder {
        LambdaConstraint constraint;
        MethodCallExpr methodCallExpr;
        String id;

        public <T> Builder withConstraint(
                                        String id,
                                      Predicate1<T> predicate, // TODO DT-ANC this is bound to be removed
                                      Index index,
                                      Variable<T> variable,
                                      Declaration declaration) {
            constraint = createConstraint(id, predicate, index, variable, declaration);
            this.id = id;
            return this;
        }
        public static <T extends Object> LambdaConstraint createConstraint(String id,
                                                                    Predicate1<T> predicate,
                                                                    Index index,
                                                                    Variable<T> variable,
                                                                    Declaration declaration) {
            // TODO DT-ANC need these twos to keep the two code paths otherwise only the second
            SingleConstraint1<T> constraint;
            if(predicate != null) {
                constraint = new SingleConstraint1<T>(id, variable, predicate);
            } else {
                PredicateInformation predicateInformation = PredicateInformation.EMPTY_PREDICATE_INFORMATION;
                constraint = new SingleConstraint1<T>(id, predicateInformation);
            }
            constraint.setIndex(index);
            LambdaConstraint lambda = new LambdaConstraint(new ConstraintEvaluator(new Declaration[]{declaration}, constraint));
            lambda.setType(Constraint.ConstraintType.ALPHA);
            return lambda;
        }

        public Builder withFeelConstraint(String feelConstraintTest, int index, String traceString) {
            // i.e. InlineableAlphaNode.Builder
            // .createConstraint("Age_62_6118", p -> evaluateAllTests(p, UnaryTestR1C1.getInstance(), 0, "trace"), null)

            methodCallExpr = new MethodCallExpr(new NameExpr(Builder.class.getCanonicalName()), "createConstraint");

            methodCallExpr.addArgument(new StringLiteralExpr(id));

            Parameter parameter = new Parameter();
            parameter.setName("p");
            MethodCallExpr evaluateAllTests = new MethodCallExpr(new NameExpr(TestEvaluator.class.getCanonicalName()), "evaluateAllTests");
            evaluateAllTests.addArgument(new MethodCallExpr(new NameExpr(feelConstraintTest), "getInstance"));
            evaluateAllTests.addArgument(new IntegerLiteralExpr(index));
            evaluateAllTests.addArgument(new StringLiteralExpr(traceString));
            methodCallExpr.addArgument(new LambdaExpr(parameter, evaluateAllTests));

            return this;
        }


        public InlineableAlphaNode createAlphaNode(int id, ObjectSource objectSource, BuildContext context) {
            return new InlineableAlphaNode(id, constraint, objectSource, context, methodCallExpr);
        }

    }

    private InlineableAlphaNode(int id, AlphaNodeFieldConstraint constraint, ObjectSource objectSource, BuildContext context, MethodCallExpr methodCallExpr) {
        super(id, constraint, objectSource, context);
        this.methodCallExpr = methodCallExpr;
    }
    @Override
    public MethodCallExpr createJavaMethod() {
        return methodCallExpr;
    }
}
