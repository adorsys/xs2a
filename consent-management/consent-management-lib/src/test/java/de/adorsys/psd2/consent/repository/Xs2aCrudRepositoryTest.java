/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.consent.repository;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.tngtech.archunit.lang.syntax.elements.ClassesShouldConjunction;
import org.junit.jupiter.api.Test;
import org.springframework.data.repository.CrudRepository;

class Xs2aCrudRepositoryTest {

    @Test
    void checkCrudRepositoryNotUsed() {
        JavaClasses importedClasses = new ClassFileImporter().importPackages("de.adorsys.psd2");

        ClassesShouldConjunction rule = ArchRuleDefinition.classes()
                                            .that().areAssignableTo(CrudRepository.class)
                                            .should()
                                            .beAssignableTo(CrudRepository.class);

        rule.check(importedClasses);
    }
}
