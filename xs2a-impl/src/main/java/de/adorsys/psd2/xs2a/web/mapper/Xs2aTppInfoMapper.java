/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.web.mapper;

import de.adorsys.psd2.validator.certificate.util.TppCertificateData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface Xs2aTppInfoMapper {

    @Mapping(source = "name", target = "tppName")
    @Mapping(source = "pspAuthorisationNumber", target = "authorisationNumber")
    @Mapping(source = "pspAuthorityId", target = "authorityId")
    @Mapping(source = "pspAuthorityName", target = "authorityName")
    TppInfo mapToTppInfo(TppCertificateData tppInfoEntity);
}