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

package de.adorsys.psd2.consent.psu.api.config;

public class CmsPsuApiTagName {
    public static final String ASPSP_CONSENT_DATA = "Aspsp Consent Data";
    public static final String PSU_AIS_CONSENTS = "PSU AIS Consents";
    public static final String PSU_PIIS_CONSENTS = "PSU PIIS, Consents";
    public static final String PSU_PIS_PAYMENT = "PSU PIS Payment";

    private CmsPsuApiTagName() {
    }
}
