package org.drools.compiler.common;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.transform.Result;

import org.drools.compiler.Person;
import org.drools.compiler.integrationtests.SerializationHelper;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.ClassObjectFilter;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;

import static org.junit.Assert.assertEquals;

public class MarshallerTest {

    @Test
    public void testAgendaReconciliation() throws Exception {
        String str =
                "import java.util.Collection\n" +
                        "rule R1 when\n" +
                        "    String() from [ \"x\", \"y\", \"z\" ]\n" +
                        "then\n" +
                        "end\n";

        KieBase kbase = new KieHelper().addContent(str, ResourceType.DRL ).build();
        KieSession ksession = kbase.newKieSession();
        assertEquals( 3, ksession.fireAllRules() );

        ksession = SerializationHelper.getSerialisedStatefulKnowledgeSession(ksession, true );

        assertEquals( 0, ksession.fireAllRules() );
    }

    @Test
    public void testAgendaReconciliation3() throws Exception {
        String str =
                "import java.util.Collection\n" +
                        "rule R1 when\n" +
                        "    String() from [ \"x\", \"y\", \"z\" ]\n" +
                        "then\n" +
                        "end\n";

        KieBase kbase = new KieHelper().addContent(str, ResourceType.DRL ).build();
        KieSession ksession = kbase.newKieSession();

        ksession = SerializationHelper.getSerialisedStatefulKnowledgeSession(ksession, true );

        assertEquals( 3, ksession.fireAllRules() );
    }

    @Test
    public void testAgendaReconciliationAccumulate() throws Exception {

        String str =
                "import " + Person.class.getCanonicalName() + ";" +
                        "rule X when\n" +
                        "  accumulate ( $p: Person ( getName().startsWith(\"M\")); \n" +
                        "                $sum : sum($p.getAge())  \n" +
                        "              )                          \n" +
                        "then\n" +
                        "  insert($sum);\n" +
                        "end";

        KieBase kbase = new KieHelper().addContent(str, ResourceType.DRL).build();
        KieSession ksession = kbase.newKieSession();

        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Edson", 35));
        ksession.insert(new Person("Mario", 40));

        assertEquals(1, ksession.fireAllRules());

        ksession = SerializationHelper.getSerialisedStatefulKnowledgeSession(ksession, true);

        assertEquals(0, ksession.fireAllRules());
    }

}