package org.kie.dmn.core.compiler.alphanetbased;

import org.drools.core.base.ClassObjectType;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.impl.KnowledgeBaseFactory;
import org.drools.core.reteoo.EntryPointNode;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.core.reteoo.builder.BuildContext;
import org.drools.core.rule.Declaration;
import org.drools.core.rule.Pattern;
import org.drools.model.Variable;

import static org.drools.model.DSL.declarationOf;

public class NetworkBuilderContext {

    public InternalKnowledgeBase kBase;
    public BuildContext buildContext;
    public Variable<TableContext> variable;
    public Declaration declaration;
    public ObjectTypeNode otn;

    public NetworkBuilderContext() {
        kBase = (InternalKnowledgeBase) KnowledgeBaseFactory.newKnowledgeBase();
        buildContext = new BuildContext(kBase);
        EntryPointNode entryPoint = buildContext.getKnowledgeBase().getRete().getEntryPointNodes().values().iterator().next();
        ClassObjectType objectType = new ClassObjectType(TableContext.class);
        variable = declarationOf(TableContext.class, "$ctx");

        Pattern pattern = new Pattern(1, objectType, "$ctx");
        declaration = pattern.getDeclaration();

        otn = new ObjectTypeNode(buildContext.getNextId(), entryPoint, objectType, buildContext);
        buildContext.setObjectSource(otn);
    }
}
