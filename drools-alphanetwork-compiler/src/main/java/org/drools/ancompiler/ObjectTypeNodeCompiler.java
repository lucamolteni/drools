/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.drools.core.InitialFact;
import org.drools.core.base.ClassObjectType;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.core.reteoo.Rete;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectTypeNodeCompiler {

    private static final String NEWLINE = "\n";
    private static final String PACKAGE_NAME = "org.drools.core.reteoo.compiled";
    private static final String BINARY_PACKAGE_NAME = PACKAGE_NAME.replace('.', '/');
    /**
     * This field hold the fully qualified class name that the {@link ObjectTypeNode} is representing.
     */
    private String className;

    /**
     * This field will hold the "simple" name of the generated class
     */
    private String generatedClassSimpleName;

    /**
     * OTN we are creating a compiled network for
     */
    private ObjectTypeNode objectTypeNode;

    private StringBuilder builder = new StringBuilder();

    private static final Logger logger = LoggerFactory.getLogger(ObjectTypeNodeCompiler.class);

    public ObjectTypeNodeCompiler(ObjectTypeNode objectTypeNode) {
        this.objectTypeNode = objectTypeNode;

        ClassObjectType classObjectType = (ClassObjectType) objectTypeNode.getObjectType();
        this.className = classObjectType.getClassName().replace("$", ".");
        final String classObjectTypeName = classObjectType.getClassName().replace('.', '_');
        final String randomId = UUID.randomUUID().toString().replace("-", "");
        generatedClassSimpleName = String.format("Compiled%sNetwork%d%s"
                , classObjectTypeName
                , objectTypeNode.getId()
                , randomId);
    }

    public CompiledNetworkSource generateSource() {
        createClassDeclaration();

        ObjectTypeNodeParser parser = new ObjectTypeNodeParser(objectTypeNode);

        // create declarations
        DeclarationsHandler declarations = new DeclarationsHandler(builder);
        parser.accept(declarations);

        // we need the hashed declarations when creating the constructor
        Collection<HashedAlphasDeclaration> hashedAlphaDeclarations = declarations.getHashedAlphaDeclarations();

        createConstructor(hashedAlphaDeclarations);

        // create set node method
        SetNodeReferenceHandler setNode = new SetNodeReferenceHandler(builder);
        parser.accept(setNode);

        // create assert method
        AssertHandler assertHandler = new AssertHandler(builder, className, hashedAlphaDeclarations.size() > 0);
        parser.accept(assertHandler);

        ModifyHandler modifyHandler = new ModifyHandler(builder, className, hashedAlphaDeclarations.size() > 0);
        parser.accept(modifyHandler);

        DelegateMethodsHandler delegateMethodsHandler = new DelegateMethodsHandler(builder);
        parser.accept(delegateMethodsHandler);

        // end of class
        builder.append("}").append(NEWLINE);

        String sourceCode = builder.toString();
        if(logger.isDebugEnabled()) {
            logger.debug("Generated Compiled Alpha Network " + sourceCode);
        }

        return new CompiledNetworkSource(
                sourceCode,
                parser.getIndexableConstraint(),
                getSourceName(),
                getBinaryName(),
                getName(),
                objectTypeNode);
    }

    /**
     * This method will output the package statement, followed by the opening of the class declaration
     */
    private void createClassDeclaration() {
        builder.append("package ").append(PACKAGE_NAME).append(";").append(NEWLINE);
        builder.append("public class ").append(generatedClassSimpleName).append(" extends ").
                append(CompiledNetwork.class.getName()).append("{ ").append(NEWLINE);

        builder.append("org.drools.core.spi.InternalReadAccessor readAccessor;\n");
    }

    /**
     * Creates the constructor for the generated class. If the hashedAlphaDeclarations is empty, it will just
     * output a empty default constructor; if it is not, the constructor will contain code to fill the hash
     * alpha maps with the values and node ids.
     *
     * @param hashedAlphaDeclarations declarations used for creating statements to populate the hashed alpha
     *                                maps for the generate class
     */
    private void createConstructor(Collection<HashedAlphasDeclaration> hashedAlphaDeclarations) {
        builder.append("public ").append(generatedClassSimpleName).append("(org.drools.core.spi.InternalReadAccessor readAccessor) {").append(NEWLINE);

        builder.append("this.readAccessor = readAccessor;\n");
        // for each hashed alpha, we need to fill in the map member variable with the hashed values to node Ids
        for (HashedAlphasDeclaration declaration : hashedAlphaDeclarations) {
            String mapVariableName = declaration.getVariableName();

            for (Object hashedValue : declaration.getHashedValues()) {
                Object value = hashedValue;

                if (value == null) {
                    // generate the map.put(hashedValue, nodeId) call
                    String nodeId = declaration.getNodeId(hashedValue);

                    builder.append(mapVariableName).append(".put(null,").append(nodeId).append(");");
                    builder.append(NEWLINE);
                } else {

                    // need to quote value if it is a string
                    if (value.getClass().equals(String.class)) {
                        value = "\"" + value + "\"";
                    } else if (value instanceof BigDecimal) {
                        value = "new java.math.BigDecimal(\"" + value + "\")";
                    } else if (value instanceof BigInteger) {
                        value = "new java.math.BigInteger(\"" + value + "\")";
                    }

                    String nodeId = declaration.getNodeId(hashedValue);

                    // generate the map.put(hashedValue, nodeId) call
                    builder.append(mapVariableName).append(".put(").append(value).append(", ").append(nodeId).append(");");
                    builder.append(NEWLINE);
                }
            }
        }

        builder.append("}").append(NEWLINE);
    }

    /**
     * Returns the fully qualified name of the generated subclass of {@link CompiledNetwork}
     *
     * @return name of generated class
     */
    private String getName() {
        return getPackageName() + "." + generatedClassSimpleName;
    }

    /**
     * Returns the fully qualified binary name of the generated subclass of {@link CompiledNetwork}
     *
     * @return binary name of generated class
     */
    private String getBinaryName() {
        return BINARY_PACKAGE_NAME + "/" + generatedClassSimpleName + ".class";
    }

    /**
     * Returns the fully qualified source name of the generated subclass of {@link CompiledNetwork}
     *
     * @return binary name of generated class
     */
    private String getSourceName() {
        return BINARY_PACKAGE_NAME + "/" + generatedClassSimpleName + ".java";
    }

    private String getPackageName() {
        return PACKAGE_NAME;
    }

    public static List<CompiledNetworkSource> compiledNetworkSource(Rete rete) {
        return objectTypeNodes(rete)
                .stream()
                .map(otn -> new ObjectTypeNodeCompiler(otn).generateSource())
                .collect(Collectors.toList());
    }

    public static List<ObjectTypeNode> objectTypeNodes(Rete rete) {
        return rete.getEntryPointNodes().values().stream()
                .flatMap(ep -> ep.getObjectTypeNodes().values().stream())
                .filter(f -> !InitialFact.class.isAssignableFrom(f.getObjectType().getClassType()))
                .collect(Collectors.toList());
    }

    public static Map<String, CompiledNetworkSource> compiledNetworkSourceMap(Rete rete) {
        List<CompiledNetworkSource> compiledNetworkSources = ObjectTypeNodeCompiler.compiledNetworkSource(rete);
        return compiledNetworkSources
                .stream()
                .collect(Collectors.toMap(CompiledNetworkSource::getName, c -> c));
    }
}