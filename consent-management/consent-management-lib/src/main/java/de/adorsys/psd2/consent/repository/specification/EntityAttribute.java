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

package de.adorsys.psd2.consent.repository.specification;

public class EntityAttribute {
    public static final String INSTANCE_ID_ATTRIBUTE = "instanceId";
    public static final String AUTHORISATION_EXTERNAL_ID_ATTRIBUTE = "externalId";
    public static final String CONSENT_EXTERNAL_ID_ATTRIBUTE = "externalId";
    public static final String PSU_DATA_LIST_ATTRIBUTE = "psuDataList";
    public static final String PAYMENT_ID_ATTRIBUTE = "paymentId";
    public static final String CREATION_TIMESTAMP_ATTRIBUTE = "creationTimestamp";

    public static final String CONSENT_TYPE_ATTRIBUTE = "consentType";
    public static final String CONSENT_TPP_INFORMATION_ATTRIBUTE = "tppInformation";

    public static final String PSU_ID_ATTRIBUTE = "psuId";
    public static final String PSU_ID_TYPE_ATTRIBUTE = "psuIdType";
    public static final String PSU_CORPORATE_ID_ATTRIBUTE = "psuCorporateId";
    public static final String PSU_CORPORATE_ID_TYPE_ATTRIBUTE = "psuCorporateIdType";

    public static final String TPP_INFO_ATTRIBUTE = "tppInfo";
    public static final String TPP_INFO_AUTHORISATION_NUMBER_ATTRIBUTE = "authorisationNumber";
    public static final String ADDITIONAL_TPP_INFORMATION_ATTRIBUTE = "additionalInfo";

    public static final String ASPSP_ACCOUNT_ACCESSES_ATTRIBUTE = "aspspAccountAccesses";
    public static final String ACCOUNT_ACCESS_ATTRIBUTE_ACCOUNT_IDENTIFIER = "accountIdentifier";
    public static final String CURRENCY_ATTRIBUTE = "currency";
    public static final String ASPSP_ACCOUNT_ID_ATTRIBUTE = "aspspAccountId";
    public static final String CONSENT_STATUS = "consentStatus";

    private EntityAttribute() {
    }
}
