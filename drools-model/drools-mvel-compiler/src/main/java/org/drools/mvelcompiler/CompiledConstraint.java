package org.drools.mvelcompiler;

import org.drools.mvelcompiler.ast.TypedExpression;

public class CompiledConstraint {

    private TypedExpression rhs;

    public CompiledConstraint(TypedExpression rhs) {
        this.rhs = rhs;
    }

    @Override
    public String toString() {
        return "CompiledConstraint{" +
                "rhsJava=" + rhs.toJavaExpression() +
                "type=" + rhs.getType() +
                '}';
    }
}
