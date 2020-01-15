package org.drools.compiler.builder.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.drools.compiler.compiler.PackageRegistry;
import org.drools.compiler.lang.descr.AbstractClassTypeDeclarationDescr;
import org.drools.compiler.lang.descr.CompositePackageDescr;
import org.drools.compiler.lang.descr.PackageDescr;
import org.drools.core.rule.TypeDeclaration;
import org.kie.api.io.Resource;

public interface TypeDeclarationBuilder {

    TypeDeclaration getAndRegisterTypeDeclaration(Class<?> cls, String packageName);

    TypeDeclaration getTypeDeclaration(Class<?> cls);

    void processTypeDeclarations(PackageDescr packageDescr,
                                 PackageRegistry pkgRegistry,
                                 Collection<AbstractClassTypeDeclarationDescr> unsortedDescrs,
                                 List<TypeDefinition> unresolvedTypes,
                                 Map<String, AbstractClassTypeDeclarationDescr> unprocesseableDescrs);

    void processTypeDeclarations(Collection<CompositePackageDescr> packages,
                                 List<AbstractClassTypeDeclarationDescr> unsortedDescrs,
                                 List<TypeDefinition> unresolvedTypes,
                                 Map<String, AbstractClassTypeDeclarationDescr> unprocesseableDescrs);

    Collection<String> removeTypesGeneratedFromResource(Resource resource);
}
