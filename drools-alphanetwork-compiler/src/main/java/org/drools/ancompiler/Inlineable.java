package org.drools.ancompiler;

import com.github.javaparser.ast.expr.MethodCallExpr;

public interface Inlineable {

    // TODO DT-ANC find better name
    MethodCallExpr createJavaMethod();

}
