package org.kie.dmn.core.compiler.alphanetbased;

import org.drools.core.reteoo.ObjectTypeNode;
import org.kie.dmn.model.api.DecisionTable;

public class DMNReteGenerator {

    ReteBuilderContext reteBuilderContext = new ReteBuilderContext();

    public ObjectTypeNode createRete(DecisionTable decisionTable, TableCells tableCells, String decisionTableName) {
        return tableCells.createRete(reteBuilderContext);

    }
}
