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

package de.adorsys.psd2.consent.web.xs2a.config;

import springfox.documentation.service.Tag;

public class InternalCmsXs2aApiTagHolder {
    public static final Tag AIS_CONSENTS = new Tag(InternalCmsXs2aApiTagName.AIS_CONSENTS, "Provides access to consent management system for AIS");
    public static final Tag AIS_PSU_DATA = new Tag(InternalCmsXs2aApiTagName.AIS_PSU_DATA, "Provides access to consent management system for PSU Data");
    public static final Tag ASPSP_CONSENT_DATA = new Tag(InternalCmsXs2aApiTagName.ASPSP_CONSENT_DATA, "Provides access to consent management system for AspspDataConsent");
    public static final Tag EVENTS = new Tag(InternalCmsXs2aApiTagName.EVENTS, "Provides access to the consent management system for Events");
    public static final Tag PIIS_CONSENTS = new Tag(InternalCmsXs2aApiTagName.PIIS_CONSENTS, "Provides access to consent management system for PIIS");
    public static final Tag PIS_COMMON_PAYMENT = new Tag(InternalCmsXs2aApiTagName.PIS_COMMON_PAYMENT, "Provides access to common payment system for PIS");
    public static final Tag PIS_PAYMENTS = new Tag(InternalCmsXs2aApiTagName.PIS_PAYMENTS, "Provides access to consent management system for PIS");
    public static final Tag PIS_PSU_DATA = new Tag(InternalCmsXs2aApiTagName.PIS_PSU_DATA, "Provides access to consent management system for PSU Data");
    public static final Tag TPP = new Tag(InternalCmsXs2aApiTagName.TPP, "Provides access to the TPP");
}
