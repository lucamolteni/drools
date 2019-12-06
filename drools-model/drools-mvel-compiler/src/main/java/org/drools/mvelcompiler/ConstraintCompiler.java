package org.drools.mvelcompiler;

import java.util.ArrayList;

import org.drools.mvel.parser.DrlxParser;
import org.drools.mvel.parser.ast.expr.DrlxExpression;
import org.drools.mvelcompiler.ast.TypedExpression;
import org.drools.mvelcompiler.context.MvelCompilerContext;

public class ConstraintCompiler {

    private final MvelCompilerContext mvelCompilerContext;

    public ConstraintCompiler(MvelCompilerContext mvelCompilerContext) {
        this.mvelCompilerContext = mvelCompilerContext;
    }

    public CompiledConstraint compileExpression(String constraintExpression) {
        DrlxExpression mvelExpression = new DrlxParser(new ArrayList<>()).parse(constraintExpression);
        TypedExpression rhs = new RHSPhase(mvelCompilerContext).invoke(mvelExpression.getExpr());
        return new CompiledConstraint(rhs);
    }
}
