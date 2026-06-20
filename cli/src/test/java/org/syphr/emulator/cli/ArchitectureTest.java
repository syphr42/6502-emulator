/*
 * Copyright © 2025-2026 Gregory P. Moyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
