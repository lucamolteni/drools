package org.drools.ancompiler;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;

public interface CanInlineInANC {

    /*
        This is the expression to initialise the inline form
        This will be inlined directly inside the Compiled Alpha Network
     */
    Expression toANCInlinedForm();

}
