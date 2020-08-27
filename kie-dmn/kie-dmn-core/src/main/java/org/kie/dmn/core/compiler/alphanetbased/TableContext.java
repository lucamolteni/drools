package org.kie.dmn.core.compiler.alphanetbased;

import org.kie.dmn.feel.lang.EvaluationContext;

public class TableContext {

    private final EvaluationContext evalCtx;
    private final Object[] values;

    public TableContext(EvaluationContext evalCtx, String... propNames) {
        this.evalCtx = evalCtx;
        this.values = new Object[propNames.length];
        for (int i = 0; i < propNames.length; i++) {
            values[i] = evalCtx.getValue( propNames[i] );
        }
    }

    public Object getValue(int i) {
        return values[i];
    }

    public EvaluationContext getEvalCtx() {
        return evalCtx;
    }
}
