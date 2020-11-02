/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.consent.repository;

import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AuthorisationRepository extends CrudRepository<AuthorisationEntity, Long>, JpaSpecificationExecutor<AuthorisationEntity> {

    Optional<AuthorisationEntity> findByExternalId(String externalId);

    Optional<AuthorisationEntity> findByExternalIdAndType(String externalId,
                                                          AuthorisationType authorisationType);

    List<AuthorisationEntity> findAllByParentExternalIdAndType(String parentExternalId,
                                                               AuthorisationType authorisationType);

    List<AuthorisationEntity> findAllByParentExternalIdAndType(String parentExternalId,
                                                               AuthorisationType authorisationType,
                                                               Pageable pageable);

    List<AuthorisationEntity> findAllByParentExternalIdAndTypeIn(String parentExternalId,
                                                                 Set<AuthorisationType> authorisationTypes);

    List<AuthorisationEntity> findAllByParentExternalIdAndTypeIn(String parentExternalId,
                                                                 Set<AuthorisationType> authorisationTypes,
                                                                 Pageable pageable);
}
