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

package de.adorsys.psd2.consent.api;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CmsConstant {
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class PATH {
        public static final String CONSENT_ID = "consent-id";
        public static final String STATUS = "status";
        public static final String AUTHORISATION_ID = "authorisation-id";
        public static final String REDIRECT_ID = "redirect-id";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class HEADERS {
        public static final String PSU_ID = "psu-id";
        public static final String PSU_ID_TYPE = "psu-id-type";
        public static final String PSU_CORPORATE_ID = "psu-corporate-id";
        public static final String PSU_CORPORATE_ID_TYPE = "psu-corporate-id-type";
        public static final String INSTANCE_ID = "instance-id";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class QUERY {
        public static final String PAGE_INDEX = "pageIndex";
        public static final String ITEMS_PER_PAGE = "itemsPerPage";
        public static final String ADDITIONAL_TPP_INFO = "additionalTppInfo";
        public static final String STATUS = "status";
        public static final String ACCOUNT_NUMBER = "accountNumber";
    }
}
