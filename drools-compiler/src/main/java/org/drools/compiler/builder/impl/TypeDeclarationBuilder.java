package org.drools.compiler.builder.impl;

import java.util.ArrayList;
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

    public static class NoOpTypeDeclarationBuilder implements TypeDeclarationBuilder {

        @Override
        public TypeDeclaration getAndRegisterTypeDeclaration(Class<?> cls, String packageName) {
            System.out.println("getAndRegisterTypeDeclaration");
            return null;
        }

        @Override
        public TypeDeclaration getTypeDeclaration(Class<?> cls) {
            System.out.println("getTypeDeclaration");
            return null;
        }

        @Override
        public void processTypeDeclarations(PackageDescr packageDescr, PackageRegistry pkgRegistry, Collection<AbstractClassTypeDeclarationDescr> unsortedDescrs, List<TypeDefinition> unresolvedTypes, Map<String, AbstractClassTypeDeclarationDescr> unprocesseableDescrs) {
            System.out.println("processTypeDeclarations");
        }

        @Override
        public void processTypeDeclarations(Collection<CompositePackageDescr> packages, List<AbstractClassTypeDeclarationDescr> unsortedDescrs, List<TypeDefinition> unresolvedTypes, Map<String, AbstractClassTypeDeclarationDescr> unprocesseableDescrs) {
            System.out.println("processTypeDeclarations");

        }

        @Override
        public Collection<String> removeTypesGeneratedFromResource(Resource resource) {
            System.out.println("removeTypesGeneratedFromResource = " + resource);
            return new ArrayList<>();
        }
    }
}
