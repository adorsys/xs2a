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

package de.adorsys.psd2.xs2a.web.validator.constants;

public class Xs2aHeaderConstant {
    // HTTP headers that are used in validation:
    public static final String X_REQUEST_ID = "x-request-id";
    public static final String PSU_IP_ADDRESS = "psu-ip-address";
    public static final String CONSENT_ID = "consent-id";

    public static final String PSU_ID = "psu-id";
    public static final String PSU_ID_TYPE = "psu-id-type";
    public static final String PSU_CORPORATE_ID = "psu-corporate-id";
    public static final String PSU_CORPORATE_ID_TYPE = "psu-corporate-id-type";
    public static final String PSU_DEVICE_ID = "psu-device-id";

    public static final String TPP_REDIRECT_PREFERRED = "tpp-redirect-preferred";
    public static final String TPP_REDIRECT_URI = "tpp-redirect-uri";

    public static final String TPP_NOTIFICATION_CONTENT_PREFERRED = "tpp-notification-content-preferred";

    public static final String TPP_NOK_REDIRECT_URI = "tpp-nok-redirect-uri";
    public static final String TPP_EXPLICIT_AUTHORISATION_PREFERRED = "tpp-explicit-authorisation-preferred";

    public static final String TPP_REJECTION_NO_FUNDS_PREFERRED = "tpp-rejection-no funds-preferred";
    public static final String TPP_BRAND_LOGGING_INFORMATION = "tpp-brand-logging-information";

    private Xs2aHeaderConstant() {
    }
}
