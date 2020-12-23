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

package de.adorsys.psd2.consent.api;

public class CmsConstant {

    private CmsConstant() {
    }

    public static final class PATH {
        public static final String CONSENT_ID = "consent-id";
        public static final String STATUS = "status";
        public static final String AUTHORISATION_ID = "authorisation-id";
        public static final String REDIRECT_ID = "redirect-id";

        private PATH() {}
    }

    public static final class HEADERS {
        public static final String PSU_ID = "psu-id";
        public static final String PSU_ID_TYPE = "psu-id-type";
        public static final String PSU_CORPORATE_ID = "psu-corporate-id";
        public static final String PSU_CORPORATE_ID_TYPE = "psu-corporate-id-type";
        public static final String INSTANCE_ID = "instance-id";

        private HEADERS() {}
    }

    public static final class QUERY {
        public static final String PAGE_INDEX = "pageIndex";
        public static final String ITEMS_PER_PAGE = "itemsPerPage";

        private QUERY() {}
    }
}
