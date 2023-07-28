package org.drools.compiler.integrationtests.incrementalcompilation;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

@RunWith(Parameterized.class)
@Ignore("secondary type cache fix breaks eval")
public class AddRemoveGenerated2RulesStringIntegerTest extends AbstractAddRemoveGenerated2RulesTest {

    public AddRemoveGenerated2RulesStringIntegerTest(final ConstraintsPair constraintsPair) {
        super(constraintsPair);
    }

    @Parameterized.Parameters
    public static Collection<ConstraintsPair[]> getRulesConstraints() {
        return generateRulesConstraintsCombinations(
                " String() \n",
                " exists(Integer() and Integer()) \n",
                " exists(Integer() and exists(Integer() and Integer())) \n");
    }
}
