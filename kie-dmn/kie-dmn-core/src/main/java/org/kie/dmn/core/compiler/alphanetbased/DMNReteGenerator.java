package org.kie.dmn.core.compiler.alphanetbased;

import org.drools.core.reteoo.ObjectTypeNode;
import org.kie.dmn.model.api.DecisionTable;

public class DMNReteGenerator {

    AlphaNetworkBuilderContext alphaNetworkBuilderContext = new AlphaNetworkBuilderContext(null);

    public ObjectTypeNode createRete(DecisionTable decisionTable, TableCells tableCells, String decisionTableName) {
        return tableCells.createRete(alphaNetworkBuilderContext);

    }
}
