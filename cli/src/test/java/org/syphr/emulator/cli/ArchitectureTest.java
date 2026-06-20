package org.syphr.emulator.cli;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "org.syphr.emulator.cli")
public class ArchitectureTest
{
    @ArchTest
    static final ArchRule noPackagesDependOnGuiPackage = noClasses().that()
                                                                    .resideOutsideOfPackages("..cli", "..gui..")
                                                                    .should()
                                                                    .dependOnClassesThat()
                                                                    .resideInAPackage("..gui..");

    @ArchTest
    static final ArchRule noPackagesDependOnShellPackage = noClasses().that()
                                                                      .resideOutsideOfPackages("..cli", "..shell..")
                                                                      .should()
                                                                      .dependOnClassesThat()
                                                                      .resideInAPackage("..shell..");

    @ArchTest
    static final ArchRule noPackagesDependOnSimplePackage = noClasses().that()
                                                                       .resideOutsideOfPackages("..cli", "..simple..")
                                                                       .should()
                                                                       .dependOnClassesThat()
                                                                       .resideInAPackage("..simple..");
}
