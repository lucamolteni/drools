/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

import org.drools.core.reteoo.AlphaNode;
import org.drools.core.reteoo.BetaNode;
import org.drools.core.reteoo.LeftInputAdapterNode;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.core.reteoo.WindowNode;
import org.drools.core.rule.IndexableConstraint;
import org.drools.core.util.index.AlphaRangeIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebugHandler extends NetworkHandlerAdaptor {

    private final Logger logger = LoggerFactory.getLogger(DebugHandler.class);

    @Override
    public void startObjectTypeNode(ObjectTypeNode objectTypeNode) {
        logger.debug("\t\t\t\tstartObjectTypeNode:\t\t\t\t"+ objectTypeNode);
    }

    @Override
    public void startNonHashedAlphaNode(AlphaNode alphaNode) {
        logger.debug("\t\t\t\tstartNonHashedAlphaNode:\t\t\t\t"+ alphaNode);
    }

    @Override
    public void endNonHashedAlphaNode(AlphaNode alphaNode) {
        logger.debug("\t\t\t\tendNonHashedAlphaNode:\t\t\t\t"+ alphaNode);
    }

    @Override
    public void startBetaNode(BetaNode betaNode) {
        logger.debug("\t\t\t\tstartBetaNode:\t\t\t\t"+ betaNode);
    }

    @Override
    public void endBetaNode(BetaNode betaNode) {
        logger.debug("\t\t\t\tendBetaNode:\t\t\t\t"+ betaNode);
    }

    @Override
    public void startWindowNode(WindowNode windowNode) {
        logger.debug("\t\t\t\tstartWindowNode:\t\t\t\t"+ windowNode);
    }

    @Override
    public void endWindowNode(WindowNode windowNode) {
        logger.debug("\t\t\t\tendWindowNode:\t\t\t\t"+ windowNode);
    }

    @Override
    public void startLeftInputAdapterNode(LeftInputAdapterNode leftInputAdapterNode) {
        logger.debug("\t\t\t\tstartLeftInputAdapterNode:\t\t\t\t"+ leftInputAdapterNode);
    }

    @Override
    public void endWindowNode(LeftInputAdapterNode leftInputAdapterNode) {
        logger.debug("\t\t\t\tendWindowNode:\t\t\t\t"+ leftInputAdapterNode);
    }

    @Override
    public void startHashedAlphaNodes(IndexableConstraint hashedFieldReader) {
        logger.debug("\t\t\t\tstartHashedAlphaNodes:\t\t\t\t"+ hashedFieldReader);
    }

    @Override
    public void endHashedAlphaNodes(IndexableConstraint hashedFieldReader) {
        logger.debug("\t\t\t\tendHashedAlphaNodes:\t\t\t\t"+ hashedFieldReader);
    }

    @Override
    public void startHashedAlphaNode(AlphaNode hashedAlpha, Object hashedValue) {
        logger.debug("\t\t\t\tstartHashedAlphaNode:\t\t\t\t"+ hashedValue);
    }

    @Override
    public void endHashedAlphaNode(AlphaNode hashedAlpha, Object hashedValue) {
        logger.debug("\t\t\t\tendHashedAlphaNode:\t\t\t\t"+ hashedValue);
    }

    @Override
    public void endObjectTypeNode(ObjectTypeNode objectTypeNode) {
        logger.debug("\t\t\t\tendObjectTypeNode:\t\t\t\t"+ objectTypeNode);
    }

    @Override
    public void nullCaseAlphaNodeStart(AlphaNode hashedAlpha) {
        logger.debug("\t\t\t\tnullCaseAlphaNodeStart:\t\t\t\t"+ hashedAlpha);
    }

    @Override
    public void nullCaseAlphaNodeEnd(AlphaNode hashedAlpha) {
        logger.debug("\t\t\t\tnullCaseAlphaNodeEnd:\t\t\t\t"+ hashedAlpha);
    }

    @Override
    public void startRangeIndex(AlphaRangeIndex alphaRangeIndex) {
        logger.debug("\t\t\t\tstartRangeIndex:\t\t\t\t"+ alphaRangeIndex);
    }

    @Override
    public void endRangeIndex(AlphaRangeIndex alphaRangeIndex) {
        logger.debug("\t\t\t\tendRangeIndex:\t\t\t\t"+ alphaRangeIndex);
    }

    @Override
    public void startRangeIndexedAlphaNode(AlphaNode alphaNode) {
        logger.debug("\t\t\t\tstartRangeIndexedAlphaNode:\t\t\t\t"+ alphaNode);
    }

    @Override
    public void endRangeIndexedAlphaNode(AlphaNode alphaNode) {
        logger.debug("\t\t\t\tendRangeIndexedAlphaNode:\t\t\t\t"+ alphaNode);
    }
}
