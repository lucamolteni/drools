package org.drools.ancompiler;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
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
import org.drools.modelcompiler.constraints.ConstraintEvaluator;
import org.drools.modelcompiler.constraints.LambdaConstraint;

public class InlineableAlphaNode extends AlphaNode implements Inlineable {

    public InlineableAlphaNode() {
    }

    public static Builder createBuilder() {
        return new Builder();
    }

    public static class Builder {
        LambdaConstraint constraint;
        MethodCallExpr methodCallExpr;

        public <T extends Object> Builder withConstraint(
                String id,
                                      Predicate1<T> predicate,
                                      Index index,
                                      Variable<T> variable,
                                      Declaration declaration) {
            constraint = createConstraint(id, predicate, index, variable, declaration);
            methodCallExpr = createMethodCallExpr(id);
            return this;
        }

        private MethodCallExpr createMethodCallExpr(String id) {
            // i.e. InlineableAlphaNode.Builder
            // .createConstraint("Age_62_6118", p -> evaluateAllTests(p, UnaryTestR1C1.getInstance(), 0, "trace"), null).getLambdaConstraint();

            MethodCallExpr methodCallExpr = new MethodCallExpr(new NameExpr(Builder.class.getCanonicalName()), "createConstraint");
            methodCallExpr.addArgument(new StringLiteralExpr(id));
//            methodCallExpr.addArgument(new LambdaExpr(Parameter.))

            return methodCallExpr;

        }

        public static <T extends Object> LambdaConstraint createConstraint(String id,
                                                                    Predicate1<T> predicate,
                                                                    Index index,
                                                                    Variable<T> variable,
                                                                    Declaration declaration) {
            SingleConstraint1<T> constraint = new SingleConstraint1<T>(id, variable, predicate);
            constraint.setIndex(index);
            LambdaConstraint lambda = new LambdaConstraint(new ConstraintEvaluator(new Declaration[]{declaration}, constraint));
            lambda.setType(Constraint.ConstraintType.ALPHA);
            return lambda;
        }

        public InlineableAlphaNode createAlphaNode(int id, ObjectSource objectSource, BuildContext context) {
            return new InlineableAlphaNode(id, constraint, objectSource, context);
        }
    }

    private InlineableAlphaNode(int id, AlphaNodeFieldConstraint constraint, ObjectSource objectSource, BuildContext context) {
        super(id, constraint, objectSource, context);
    }
    @Override
    public MethodCallExpr createJavaMethod() {
        return null;
    }
}
