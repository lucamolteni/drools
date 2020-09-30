package org.drools.compiler.kie.builder.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class UpdaterContainerContainerImpl implements Consumer<KieBaseUpdaterFactory>,
                                                      KieBaseUpdaters {

    List<KieBaseUpdaterFactory> children = new ArrayList<>();

    public UpdaterContainerContainerImpl() {
    }

    @Override
    public List<KieBaseUpdaterFactory> getChildren() {
        return children;
    }

    @Override
    public void accept(KieBaseUpdaterFactory o) {
        children.add(o);
    }
}