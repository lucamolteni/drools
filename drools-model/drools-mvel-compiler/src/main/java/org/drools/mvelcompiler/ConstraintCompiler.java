package org.drools.mvelcompiler;

import com.github.javaparser.ast.expr.Expression;
import org.drools.mvel.parser.MvelParser;
import org.drools.mvelcompiler.ast.TypedExpression;
import org.drools.mvelcompiler.context.MvelCompilerContext;

public class ConstraintCompiler {

    private final MvelCompilerContext mvelCompilerContext;

    public ConstraintCompiler(MvelCompilerContext mvelCompilerContext) {
        this.mvelCompilerContext = mvelCompilerContext;
    }

    public CompiledConstraint compileExpression(String mvelBlock) {
        Expression mvelExpression = MvelParser.parseExpression(mvelBlock);
        TypedExpression rhs = new RHSPhase(mvelCompilerContext).invoke(mvelExpression);
        return new CompiledConstraint(rhs);
    }
}
