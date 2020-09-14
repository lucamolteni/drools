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

package org.drools.core.reteoo.compiled;

import java.util.ArrayList;
import java.util.List;

import org.drools.core.reteoo.AlphaNode;
import org.drools.core.reteoo.BetaNode;
import org.drools.core.reteoo.LeftInputAdapterNode;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.core.reteoo.WindowNode;
import org.drools.core.rule.IndexableConstraint;

public class ModifyHandler extends SwitchCompilerHandler {

    private static final String ASSERT_METHOD_SIGNATURE = "public final void modifyObject("
            + FACT_HANDLE_PARAM_TYPE + " " + FACT_HANDLE_PARAM_NAME + ","
            + MODIFY_PREVIOUS_TUPLE_NAME + " " + MODIFY_PREVIOUS_TUPLE_PARAM_NAME + ","
            + PROP_CONTEXT_PARAM_TYPE + " " + PROP_CONTEXT_PARAM_NAME + ","
            + WORKING_MEMORY_PARAM_TYPE + " " + WORKING_MEMORY_PARAM_NAME + "){";

    /**
     * This flag is used to instruct the AssertHandler to tell it to generate a local varible
     * in the {@link org.kie.reteoo.compiled.CompiledNetwork#assertObject} for holding the value returned
     * from the {@link org.kie.common.InternalFactHandle#getFactHandle()}.
     *
     * This is only needed if there is at least 1 set of hashed alpha nodes in the network
     */
    private final boolean alphaNetContainsHashedField;

    private final String factClassName;

    private int switchCaseCounter = 0;

    private final List<String> modifyObjectMethods = new ArrayList<>();
    private StringBuilder currentModifyMethod;

    public List<String> getModifyObjectMethods() {
        return modifyObjectMethods;
    }

    public ModifyHandler(StringBuilder builder, String factClassName, boolean alphaNetContainsHashedField) {
        super(builder);
        this.factClassName = factClassName;
        this.alphaNetContainsHashedField = alphaNetContainsHashedField;
    }

    @Override
    public void startObjectTypeNode(ObjectTypeNode objectTypeNode) {
        builder.append(ASSERT_METHOD_SIGNATURE).append(NEWLINE);

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
        currentModifyMethod.append(getVariableName(betaNode)).append(".modifyObject(").
                append(FACT_HANDLE_PARAM_NAME).append(",").
                append(MODIFY_PREVIOUS_TUPLE_PARAM_NAME).append(",").
                append(PROP_CONTEXT_PARAM_NAME).append(",").
                append(WORKING_MEMORY_PARAM_NAME).append(");").append(NEWLINE);
    }

    @Override
    public void startWindowNode(WindowNode windowNode) {
        currentModifyMethod.append(getVariableName(windowNode)).append(".modifyObject(").
                append(FACT_HANDLE_PARAM_NAME).append(",").
                append(MODIFY_PREVIOUS_TUPLE_PARAM_NAME).append(",").
                append(PROP_CONTEXT_PARAM_NAME).append(",").
                append(WORKING_MEMORY_PARAM_NAME).append(");").append(NEWLINE);
    }

    @Override
    public void endWindowNode(WindowNode windowNode) {

    }

    @Override
    public void startLeftInputAdapterNode(LeftInputAdapterNode leftInputAdapterNode) {
        if(currentModifyMethod != null) {
            currentModifyMethod.append(getVariableName(leftInputAdapterNode)).append(".modifyObject(").
                    append(FACT_HANDLE_PARAM_NAME).append(",").
                    append(MODIFY_PREVIOUS_TUPLE_PARAM_NAME).append(",").
                    append(PROP_CONTEXT_PARAM_NAME).append(",").
                    append(WORKING_MEMORY_PARAM_NAME).append(");").append(NEWLINE);
        }
    }

    @Override
    public void startNonHashedAlphaNode(AlphaNode alphaNode) {
        if(currentModifyMethod != null) {
            ifIsAllowed(alphaNode);
        }
    }

    private void ifIsAllowed(AlphaNode alphaNode) {
        currentModifyMethod.append("if ( ").append(getVariableName(alphaNode)).
                append(".isAllowed(").append(FACT_HANDLE_PARAM_NAME).append(",").
                append(WORKING_MEMORY_PARAM_NAME).
                append(") ) {").append(NEWLINE);
    }

    @Override
    public void endNonHashedAlphaNode(AlphaNode alphaNode) {
        // close if statement
        if(currentModifyMethod != null) {
            currentModifyMethod.append("}").append(NEWLINE);
        }
    }

    @Override
    public void startHashedAlphaNodes(IndexableConstraint indexableConstraint) {
        generateSwitch(indexableConstraint);
    }

    @Override
    public void startHashedAlphaNode(AlphaNode hashedAlpha, Object hashedValue) {
        generateSwitchCase(hashedAlpha, hashedValue);

        currentModifyMethod = new StringBuilder();
        currentModifyMethod.append(
                String.format("private void modifyObject%s(org.drools.core.common.InternalFactHandle handle, " +
                                      "org.drools.core.reteoo.ModifyPreviousTuples modifyPreviousTuples, " +
                                      "org.drools.core.spi.PropagationContext context, " +
                                      "org.drools.core.common.InternalWorkingMemory wm) {", switchCaseCounter)
        );

        builder.append(String.format("modifyObject%s(handle, modifyPreviousTuples, context, wm);", switchCaseCounter));

    }

    @Override
    public void endHashedAlphaNode(AlphaNode hashedAlpha, Object hashedValue) {
        builder.append("break;").append(NEWLINE);

        closeStatement(currentModifyMethod);
        modifyObjectMethods.add(currentModifyMethod.toString());
        currentModifyMethod = null;
        switchCaseCounter++;
    }

    @Override
    public void endHashedAlphaNodes(IndexableConstraint hashedFieldReader) {
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
    public void nullCaseAlphaNodeStart(AlphaNode hashedAlpha) {
        super.nullCaseAlphaNodeStart(hashedAlpha);
    }

    public void addModifyMethods() {
        for(String m : modifyObjectMethods) {
            builder.append(m);
        }
    }

}
