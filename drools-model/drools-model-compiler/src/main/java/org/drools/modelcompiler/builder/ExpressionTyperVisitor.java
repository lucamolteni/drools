package org.drools.modelcompiler.builder;

import java.util.HashSet;
import java.util.Set;

import org.drools.compiler.lang.descr.AccumulateDescr;
import org.drools.compiler.lang.descr.AndDescr;
import org.drools.compiler.lang.descr.BaseDescr;
import org.drools.compiler.lang.descr.ConditionalBranchDescr;
import org.drools.compiler.lang.descr.DescrVisitor;
import org.drools.compiler.lang.descr.EvalDescr;
import org.drools.compiler.lang.descr.ExistsDescr;
import org.drools.compiler.lang.descr.ExprConstraintDescr;
import org.drools.compiler.lang.descr.ForallDescr;
import org.drools.compiler.lang.descr.FromDescr;
import org.drools.compiler.lang.descr.NamedConsequenceDescr;
import org.drools.compiler.lang.descr.NotDescr;
import org.drools.compiler.lang.descr.OrDescr;
import org.drools.compiler.lang.descr.PackageDescr;
import org.drools.compiler.lang.descr.PatternDescr;
import org.drools.compiler.lang.descr.RuleDescr;
import org.drools.core.addon.TypeResolver;
import org.drools.mvelcompiler.CompiledConstraint;
import org.drools.mvelcompiler.ConstraintCompiler;
import org.drools.mvelcompiler.ParsingResult;
import org.drools.mvelcompiler.context.MvelCompilerContext;

import static org.drools.modelcompiler.builder.generator.DrlxParseUtil.THIS_PLACEHOLDER;

public class ExpressionTyperVisitor implements DescrVisitor {

    private ConstraintCompiler constraintCompiler;
    private MvelCompilerContext mvelCompilerContext;

    public void typeExpression(PackageDescr packageDescr, TypeResolver typeResolver) {
        mvelCompilerContext = new MvelCompilerContext(typeResolver);
        Set<String> imports = new HashSet<>();
        imports.add("java.util.*");
        imports.add("java.lang.*");
        imports.add("java.math.*");
        imports.forEach(typeResolver::addImport);
        constraintCompiler = new ConstraintCompiler(mvelCompilerContext);
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
        for (BaseDescr bd : descr.getDescrs()) {
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
        // This could be in a different phase
        mvelCompilerContext.addDeclaration(THIS_PLACEHOLDER, descr.getObjectType());
        for (BaseDescr bd : descr.getDescrs()) {
            bd.accept(this);
        }
    }

    @Override
    public void visit(PackageDescr descr) {
        System.out.println("Package: " + descr.getName());
        for (RuleDescr rd : descr.getRules()) {
            rd.accept(this);
        }
    }

    @Override
    public void visit(RuleDescr descr) {
        System.out.println("Rule name: " + descr.getName());
        descr.getLhs().accept(this);
    }

    @Override
    public void visit(ExprConstraintDescr descr) {
        String expression = descr.getExpression();
        String withThis = THIS_PLACEHOLDER + "." + expression;
        System.out.println("expression = " + withThis);
        CompiledConstraint parsingResult = constraintCompiler.compileExpression(withThis);
        descr.setParsedExpression(parsingResult);

        System.out.println("Compiled expression: " + parsingResult);
    }
}
