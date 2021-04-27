/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.drools.core.util.ObjectHashMap;
import org.drools.model.Drools;
import org.drools.model.functions.Predicate1;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 4)
@Warmup(iterations = 5)
@Measurement(iterations = 3)
public class ANCBenchmark {

    // todo use splittable random per generare stringhe più diverse

    // TODO
    // -prof perfnorm
    // prof gc
    // branch-misses

    @Param({"10000"})
    private int N;

    private List<String> DATA_FOR_TESTING;

    interface DroolsNode {
        void assertObject(Blackhole bh, String s);
    }

    class DroolsAlphaNode implements DroolsNode {

        private Predicate<String> predicate;
        DroolsNode terminalNode;

        public DroolsAlphaNode(Predicate<String> predicate, DroolsNode terminalNode) {
            this.predicate = predicate;
            this.terminalNode = terminalNode;
        }

        @Override
        public void assertObject(Blackhole bh, String s) {
            if(predicate.test(s)) {
                terminalNode.assertObject(bh, s);
            }
        }
    }

    class TerminalNode implements DroolsNode {

        @Override
        public void assertObject(Blackhole bh, String s) {
            bh.consume(s);
        }
    }

    private List<DroolsNode> constraints = new ArrayList<>();

    private ObjectHashMap hashedSinkMap = new ObjectHashMap();


    TerminalNode terminalNode = new TerminalNode();

    @Setup
    public void setup() {
        DATA_FOR_TESTING = createData();

        constraints.add(createConstraintEquals("10"));
        constraints.add(createConstraintEquals("20"));
        constraints.add(createConstraintEquals("30"));

        hashedSinkMap.put("10", terminalNode);
        hashedSinkMap.put("20", terminalNode);
        hashedSinkMap.put("30", terminalNode);
    }

    private DroolsNode createConstraintEquals(String stringEquals) {
        return new DroolsAlphaNode(s -> s.equals(stringEquals), terminalNode);
    }

    @Benchmark
    public void testBruteForce(Blackhole bh) throws Exception {
        for (int i = 0; i < DATA_FOR_TESTING.size(); i++) {
            String s = DATA_FOR_TESTING.get(i);
            for (DroolsNode p : constraints) {
                p.assertObject(bh, s);
            }
        }
    }

    @Benchmark
    public void testHashing(Blackhole bh) throws Exception {
        for (int i = 0; i < DATA_FOR_TESTING.size(); i++) {
            String s = DATA_FOR_TESTING.get(i);

            DroolsNode p = (DroolsNode) hashedSinkMap.get(s);
            if(p != null) {
                p.assertObject(bh, s);
            }
        }
    }

    @Benchmark
    public void testInlining(Blackhole bh) throws Exception {
        for (int i = 0; i < DATA_FOR_TESTING.size(); i++) {
            String s = DATA_FOR_TESTING.get(i);
            switch (s) {
                case "10": // there should be three different terminal nodes
                    terminalNode.assertObject(bh, s);
                    break;
                case "20":
                    terminalNode.assertObject(bh, s);
                    break;
                case "30":
                    terminalNode.assertObject(bh, s);
                    break;
                default:
            }
        }
    }

    private List<String> createData() {
        List<String> data = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            data.add(String.valueOf(i));
        }
        return data;
    }
}