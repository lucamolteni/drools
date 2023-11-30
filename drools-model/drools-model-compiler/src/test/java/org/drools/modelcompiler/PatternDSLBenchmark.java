/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.drools.modelcompiler;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.drools.model.Global;
import org.drools.model.Model;
import org.drools.model.Rule;
import org.drools.model.Variable;
import org.drools.model.impl.ModelImpl;
import org.drools.modelcompiler.domain.Person;
import org.drools.modelcompiler.domain.Result;
import org.kie.api.KieBase;
import org.kie.api.conf.EqualityBehaviorOption;
import org.kie.api.runtime.ClassObjectFilter;
import org.kie.api.runtime.KieSession;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.drools.model.DSL.accFunction;
import static org.drools.model.DSL.accumulate;
import static org.drools.model.DSL.declarationOf;
import static org.drools.model.DSL.globalOf;
import static org.drools.model.DSL.on;
import static org.drools.model.PatternDSL.pattern;
import static org.drools.model.PatternDSL.rule;

@State(Scope.Benchmark)
public class PatternDSLBenchmark {

    private KieBase kieBase;

    @Setup(Level.Trial)
    public void setup() {
        Variable<Person> person = declarationOf(  Person.class );
        Variable<Integer> resultSum = declarationOf(  Integer.class );
        Variable<Double> resultAvg = declarationOf(  Double.class );
        Variable<Integer> age = declarationOf(  Integer.class );
        Global<Result> resultG = globalOf(Result.class, "defaultpkg", "result");

        Rule rule = rule("defaultpkg", "accumulate")
                .build(
                        accumulate( pattern( person ).expr(p -> p.getName().startsWith("M")).bind(age, Person::getAge),
                                    accFunction(org.drools.core.base.accumulators.IntegerSumAccumulateFunction::new, age).as(resultSum),
                                    accFunction(org.drools.core.base.accumulators.AverageAccumulateFunction::new, age).as(resultAvg)),
                        on(resultSum, resultAvg, resultG)
                                .execute((sum, avg, r) -> r.setValue( "total = " + sum + "; average = " + avg ))
                );

        Model model = new ModelImpl()
                .addRule( rule )
                .addGlobal(resultG);

        kieBase = KieBaseBuilder.createKieBaseFromModel(model, EqualityBehaviorOption.EQUALITY);
    }

    @Benchmark
    @Fork(value = 4)
    @BenchmarkMode(Mode.Throughput)
    @Warmup(iterations = 10, time = 400, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 17, time = 100, timeUnit = TimeUnit.MILLISECONDS)
    public void testAccumulate() {
        KieSession ksession = kieBase.newKieSession();

        Result result = new Result();
        ksession.setGlobal("result", result);

        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Edson", 35));
        ksession.insert(new Person("Mario", 40));

        ksession.fireAllRules();
        assertThat(result.getValue()).isEqualTo("total = 77; average = 38.5");

        ksession.dispose();

    }

    public static <T> List<T> getObjectsIntoList(KieSession ksession, Class<T> clazz) {
        return (List<T>) ksession.getObjects(new ClassObjectFilter(clazz)).stream().collect(Collectors.toList());
    }
}
