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

package de.adorsys.psd2.consent.web.xs2a.config;

import de.adorsys.psd2.consent.api.config.InternalCmsXs2aApiTagName;
import springfox.documentation.service.Tag;

public class InternalCmsXs2aApiTagHolder {
    public static final Tag AIS_CONSENTS = new Tag(InternalCmsXs2aApiTagName.AIS_CONSENTS, "Provides access to consent management system for AIS");
    public static final Tag AIS_PSU_DATA = new Tag(InternalCmsXs2aApiTagName.AIS_PSU_DATA, "Provides access to consent management system for PSU Data");
    public static final Tag ASPSP_CONSENT_DATA = new Tag(InternalCmsXs2aApiTagName.ASPSP_CONSENT_DATA, "Provides access to consent management system for AspspDataConsent");
    public static final Tag AUTHORISATIONS = new Tag(InternalCmsXs2aApiTagName.AUTHORISATIONS, "Provides access to consent management system for authorisation endpoints");
    public static final Tag CONSENTS = new Tag(InternalCmsXs2aApiTagName.CONSENTS, "Provides access to consent management system for common consent endpoints");
    public static final Tag EVENTS = new Tag(InternalCmsXs2aApiTagName.EVENTS, "Provides access to the consent management system for Events");
    public static final Tag PIIS_CONSENTS = new Tag(InternalCmsXs2aApiTagName.PIIS_CONSENTS, "Provides access to consent management system for PIIS");
    public static final Tag PIS_COMMON_PAYMENT = new Tag(InternalCmsXs2aApiTagName.PIS_COMMON_PAYMENT, "Provides access to common payment system for PIS");
    public static final Tag PIS_PAYMENTS = new Tag(InternalCmsXs2aApiTagName.PIS_PAYMENTS, "Provides access to consent management system for PIS");
    public static final Tag PIS_PSU_DATA = new Tag(InternalCmsXs2aApiTagName.PIS_PSU_DATA, "Provides access to consent management system for PSU Data");
    public static final Tag TPP = new Tag(InternalCmsXs2aApiTagName.TPP, "Provides access to the TPP");

    private InternalCmsXs2aApiTagHolder() {
    }
}
