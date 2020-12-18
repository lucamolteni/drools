/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.ancompiler;

import java.util.ArrayList;
import java.util.List;

import org.drools.core.reteoo.AlphaNode;
import org.drools.core.reteoo.BetaNode;
import org.drools.core.reteoo.LeftInputAdapterNode;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.core.reteoo.WindowNode;
import org.drools.core.rule.IndexableConstraint;
import org.drools.core.util.index.AlphaRangeIndex;

public class AssertHandler extends SwitchCompilerHandler {

    /**
     * This flag is used to instruct the AssertHandler to tell it to generate a local varible
     * in the {@link org.drools.ancompiler.CompiledNetwork#propagateAssertObject} for holding the value returned
     * from the {@link org.drools.core.common.InternalFactHandle#getFactHandle()}.
     *
     * This is only needed if there is at least 1 set of hashed alpha nodes in the network
     */
    private final boolean alphaNetContainsHashedField;

    private final String factClassName;
    private static final String ASSERT_OBJECT_CALL = ".assertObject(";

    private final List<String> assertObjectMethods = new ArrayList<>();

    private StringBuilder currentAssertObjectMethod;

    private int switchCaseCounter = 0;

    AssertHandler(StringBuilder builder, String factClassName) {
        this(builder, factClassName, false);
    }

    public AssertHandler(StringBuilder builder, String factClassName, boolean alphaNetContainsHashedField) {
        super(builder);
        this.factClassName = factClassName;
        this.alphaNetContainsHashedField = alphaNetContainsHashedField;
    }

    @Override
    public void startObjectTypeNode(ObjectTypeNode objectTypeNode) {
        builder.append(ASSERT_METHOD_SIGNATURE).append(NEWLINE);

        builder.append("if(logger.isDebugEnabled()) {\n" +
                       "            logger.debug(\"Propagate assert on compiled alpha network {} {} {}\", handle, context, wm);\n" +
                       "        }\n").append(NEWLINE);

        // we only need to create a reference to the object, not handle, if there is a hashed alpha in the network
        if (alphaNetContainsHashedField) {
            // example of what this will look like
            // ExampleFact fact = (ExampleFact) handle.getObject();
            builder.append(factClassName).append(" ").append(LOCAL_FACT_VAR_NAME).
                    append(" = (").append(factClassName).append(")").
                    append(FACT_HANDLE_PARAM_NAME).append(".getObject();").
                    append(NEWLINE);
        }
    }

    @Override
    public void startBetaNode(BetaNode betaNode) {
        builder.append(getVariableName(betaNode)).append(ASSERT_OBJECT_CALL).
                append(FACT_HANDLE_PARAM_NAME).append(",").
                append(PROP_CONTEXT_PARAM_NAME).append(",").
                append(WORKING_MEMORY_PARAM_NAME).append(");").append(NEWLINE);
    }


    @Override
    public void startWindowNode(WindowNode windowNode) {
        builder.append(getVariableName(windowNode)).append(ASSERT_OBJECT_CALL).
                append(FACT_HANDLE_PARAM_NAME).append(",").
                append(PROP_CONTEXT_PARAM_NAME).append(",").
                append(WORKING_MEMORY_PARAM_NAME).append(");").append(NEWLINE);
    }

    @Override
    public void startLeftInputAdapterNode(LeftInputAdapterNode leftInputAdapterNode) {
        assertObject(leftInputAdapterNode, currentAssertObjectMethod);
    }

    @Override
    public void startNonHashedAlphaNode(AlphaNode alphaNode) {
        if(currentAssertObjectMethod != null) {
            ifIsAllowed(alphaNode, currentAssertObjectMethod);
        }
    }

    @Override
    public void endNonHashedAlphaNode(AlphaNode alphaNode) {
        if(currentAssertObjectMethod != null) {
            currentAssertObjectMethod.append("}").append(NEWLINE);
        }
    }

    private void assertObject(LeftInputAdapterNode leftInputAdapterNode, StringBuilder builder) {
        if(currentAssertObjectMethod != null) {
            builder.append(getVariableName(leftInputAdapterNode)).append(".assertObject(").
                    append(FACT_HANDLE_PARAM_NAME).append(",").
                    append(PROP_CONTEXT_PARAM_NAME).append(",").
                    append(WORKING_MEMORY_PARAM_NAME).append(");").append(NEWLINE);
        }
    }


    private void ifIsAllowed(AlphaNode alphaNode, StringBuilder builder) {
        builder.append("if ( ").append(getVariableName(alphaNode)).
                append(".isAllowed(").append(FACT_HANDLE_PARAM_NAME).append(",").
                append(WORKING_MEMORY_PARAM_NAME).
                append(") ) {").append(NEWLINE);
    }

    @Override
    public void startHashedAlphaNodes(IndexableConstraint indexableConstraint) {
        generateSwitch(indexableConstraint);
    }

    @Override
    public void startHashedAlphaNode(AlphaNode hashedAlpha, Object hashedValue) {
        generateSwitchCase(hashedAlpha, hashedValue);
        currentAssertObjectMethod = new StringBuilder();
        currentAssertObjectMethod.append(
                String.format("private void assertObject%s(org.drools.core.common.InternalFactHandle handle, " +
                                      "org.drools.core.spi.PropagationContext context, " +
                                      "org.drools.core.common.InternalWorkingMemory wm) {", switchCaseCounter)
        );

        builder.append(String.format("assertObject%s(handle, context, wm);", switchCaseCounter));
    }

    @Override
    public void endHashedAlphaNode(AlphaNode hashedAlpha, Object hashedValue) {
        builder.append("break;").append(NEWLINE);

        closeStatement(currentAssertObjectMethod);
        assertObjectMethods.add(currentAssertObjectMethod.toString());
        currentAssertObjectMethod = null;
        switchCaseCounter++;
    }

    @Override
    public void endHashedAlphaNodes(IndexableConstraint indexableConstraint) {
        // close switch statement
        closeStatement(builder);
        // and if statement for ensuring non-null
        closeStatement(builder);
    }

    @Override
    public void endObjectTypeNode(ObjectTypeNode objectTypeNode) {
        // close the assertObject method
        closeStatement(builder);
    }

    @Override
    public void startRangeIndex(AlphaRangeIndex alphaRangeIndex) {
        String rangeIndexVariableName = getRangeIndexVariableName(alphaRangeIndex, getMinIdFromRangeIndex(alphaRangeIndex));
        String matchingResultVariableName = rangeIndexVariableName + "_result";
        String matchingNodeVariableName = matchingResultVariableName + "_node";
        builder.append("java.util.Collection<org.drools.core.reteoo.AlphaNode> " + matchingResultVariableName + " = " + rangeIndexVariableName + ".getMatchingAlphaNodes(" + FACT_HANDLE_PARAM_NAME + ".getObject());").append(NEWLINE);
        builder.append("for (org.drools.core.reteoo.AlphaNode " + matchingNodeVariableName + " : " + matchingResultVariableName + ") {").append(NEWLINE);
        builder.append("switch (" + matchingNodeVariableName + ".getId()) {").append(NEWLINE);
    }

    @Override
    public void startRangeIndexedAlphaNode(AlphaNode alphaNode) {
        builder.append("case " + alphaNode.getId() + ":").append(NEWLINE);
    }

    @Override
    public void endRangeIndexedAlphaNode(AlphaNode alphaNode) {
        builder.append("break;").append(NEWLINE);
    }

    @Override
    public void endRangeIndex(AlphaRangeIndex alphaRangeIndex) {
        builder.append("}").append(NEWLINE);
        builder.append("}").append(NEWLINE);
    }

    @Override
    public void nullCaseAlphaNodeStart(AlphaNode hashedAlpha) {
        super.nullCaseAlphaNodeStart(hashedAlpha);
    }

    public void addAssertObjectMethods() {
        for(String assertObjectMethod : assertObjectMethods) {
            builder.append(assertObjectMethod);
        }
    }
}
