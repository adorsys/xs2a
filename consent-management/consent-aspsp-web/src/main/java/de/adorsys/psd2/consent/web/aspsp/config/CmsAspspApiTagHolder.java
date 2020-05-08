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

package de.adorsys.psd2.consent.web.aspsp.config;

import de.adorsys.psd2.consent.aspsp.api.config.CmsAspspApiTagName;
import springfox.documentation.service.Tag;

public class CmsAspspApiTagHolder {
    public static final Tag ASPSP_EXPORT_AIS_CONSENTS = new Tag(CmsAspspApiTagName.ASPSP_EXPORT_AIS_CONSENTS, "Provides access to the consent management system for exporting AIS consents by ASPSP");
    public static final Tag ASPSP_EVENTS = new Tag(CmsAspspApiTagName.ASPSP_EVENTS, "Provides access to the consent management system for ASPSP Events");
    public static final Tag ASPSP_PIIS_CONSENTS = new Tag(CmsAspspApiTagName.ASPSP_PIIS_CONSENTS, "Controller for CMS-ASPSP-API providing access to PIIS consents");
    public static final Tag ASPSP_PIIS_CONSENTS_EXPORT = new Tag(CmsAspspApiTagName.ASPSP_PIIS_CONSENTS_EXPORT, "Provides access to the consent management system for exporting PIIS consents by ASPSP");
    public static final Tag ASPSP_EXPORT_PAYMENTS = new Tag(CmsAspspApiTagName.ASPSP_EXPORT_PAYMENTS, "Provides access to the consent management system for exporting PIS payments by ASPSP");
    public static final Tag ASPSP_TPP_STOP_LIST = new Tag(CmsAspspApiTagName.ASPSP_TPP_STOP_LIST, "Provides access to the consent management system TPP Stop List");
    public static final Tag ASPSP_TPP_INFO = new Tag(CmsAspspApiTagName.ASPSP_TPP_INFO, "Provides access to the consent management system TPP Info");

    private CmsAspspApiTagHolder() {
    }
}
