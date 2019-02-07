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

package de.adorsys.psd2.consent.repository.specification;

public class EntityAttribute {
    public static final String INSTANCE_ID_ATTRIBUTE = "instanceId";
    public static final String AUTHORISATION_EXTERNAL_ID_ATTRIBUTE = "externalId";
    public static final String CONSENT_EXTERNAL_ID_ATTRIBUTE = "externalId";
    public static final String PSU_DATA_ATTRIBUTE = "psuData";
    public static final String CONSENT_ID_ATTRIBUTE = "consentId";
    public static final String PAYMENT_ID_ATTRIBUTE = "paymentId";
    public static final String TIMESTAMP_ID_ATTRIBUTE = "timestamp";
    public static final String CREATION_TIMESTAMP_ATTRIBUTE = "creationTimestamp";

    public static final String PSU_ID_ATTRIBUTE = "psuId";
    public static final String PSU_ID_TYPE_ATTRIBUTE = "psuIdType";
    public static final String PSU_CORPORATE_ID_ATTRIBUTE = "psuCorporateId";
    public static final String PSU_CORPORATE_ID_TYPE_ATTRIBUTE = "psuCorporateIdType";

    public static final String TPP_INFO_ATTRIBUTE = "tppInfo";
    public static final String TPP_INFO_AUTHORISATION_NUMBER_ATTRIBUTE = "authorisationNumber";

    public static final String ASPSP_ACCOUNT_ACCESSES_ATTRIBUTE = "aspspAccountAccesses";
    public static final String ASPSP_ACCOUNT_ID_ATTRIBUTE = "aspspAccountId";

    private EntityAttribute() {
    }
}
