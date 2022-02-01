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
