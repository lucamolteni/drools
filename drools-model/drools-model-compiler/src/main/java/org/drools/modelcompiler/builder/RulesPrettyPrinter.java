package org.drools.modelcompiler.builder;

import org.drools.compiler.lang.descr.AccumulateDescr;
import org.drools.compiler.lang.descr.AndDescr;
import org.drools.compiler.lang.descr.BaseDescr;
import org.drools.compiler.lang.descr.ConditionalBranchDescr;
import org.drools.compiler.lang.descr.DescrVisitor;
import org.drools.compiler.lang.descr.EvalDescr;
import org.drools.compiler.lang.descr.ExistsDescr;
import org.drools.compiler.lang.descr.ForallDescr;
import org.drools.compiler.lang.descr.FromDescr;
import org.drools.compiler.lang.descr.NamedConsequenceDescr;
import org.drools.compiler.lang.descr.NotDescr;
import org.drools.compiler.lang.descr.OrDescr;
import org.drools.compiler.lang.descr.PackageDescr;
import org.drools.compiler.lang.descr.PatternDescr;
import org.drools.compiler.lang.descr.RuleDescr;

public class RulesPrettyPrinter implements DescrVisitor {

    public void prettyPrintRule(PackageDescr packageDescr) {
        this.visit(packageDescr);
    }

    @Override
    public void visit(BaseDescr descr) {

    }

    @Override
    public void visit(AccumulateDescr descr) {

    }

    @Override
    public void visit(AndDescr descr) {
        System.out.println("AndDescr: " + descr.getText());
        for(BaseDescr bd : descr.getDescrs()) {
            bd.accept(this);
        }
    }

    @Override
    public void visit(NotDescr descr) {

    }

    @Override
    public void visit(ExistsDescr descr) {

    }

    @Override
    public void visit(ForallDescr descr) {

    }

    @Override
    public void visit(OrDescr descr) {

    }

    @Override
    public void visit(EvalDescr descr) {

    }

    @Override
    public void visit(FromDescr descr) {

    }

    @Override
    public void visit(NamedConsequenceDescr descr) {

    }

    @Override
    public void visit(ConditionalBranchDescr descr) {

    }

    @Override
    public void visit(PatternDescr descr) {
        System.out.println("PatternDescr: ");
        System.out.println("Identifier: " + descr.getIdentifier());
        System.out.println("Object Type: " + descr.getObjectType());
        for(BaseDescr bd : descr.getDescrs()) {
            bd.accept(this);
        }
    }

    @Override
    public void visit(PackageDescr descr) {
        System.out.println("Package: " + descr.getName());
        for(RuleDescr rd : descr.getRules()) {
            rd.accept(this);
        }
    }

    @Override
    public void visit(RuleDescr descr) {
        System.out.println("Rule name: " + descr.getName());
       descr.getLhs().accept(this);
    }
}
