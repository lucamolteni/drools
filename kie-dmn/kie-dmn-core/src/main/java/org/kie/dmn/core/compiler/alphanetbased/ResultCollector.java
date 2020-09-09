package org.kie.dmn.core.compiler.alphanetbased;

import java.util.ArrayList;
import java.util.List;

public class ResultCollector {

    private final List<Object> results = new ArrayList<>();

    public void addResult(Object o) {
        results.add(o);
    }

    public void clearResults() {
        results.clear();
    }

    public Object getWithHitPolicy() {
        return results.get(0);
    }
}
