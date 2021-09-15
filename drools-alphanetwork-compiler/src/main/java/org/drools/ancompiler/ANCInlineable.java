package org.drools.ancompiler;

import com.github.javaparser.ast.expr.MethodCallExpr;

public interface ANCInlineable {

    // TODO DT-ANC find better name
    MethodCallExpr createJavaMethod();

}
