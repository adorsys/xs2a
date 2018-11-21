/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.validator.certificate.util;

import lombok.Data;

import java.util.List;

@Data
public class TppCertificateData {
    private String pspAuthorisationNumber;
    private List<TppRole> pspRoles;
    private String name;
    private String pspAuthorityName;
    private String pspAuthorityId;
    private String country;
    private String organisation;
    private String organisationUnit;
    private String city;
    private String state;
}
