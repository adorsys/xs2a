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

package de.adorsys.psd2.xs2a.core.piis;

/**
 * Type of the tpp access, indicating which TPPs have an access to the consent
 *
 * @deprecated since 5.11 and will be removed, all PIIS consent have SINGLE_TPP access type
 */
@Deprecated
// TODO: Remove type, all PIIS consents are single now https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1219
public enum PiisConsentTppAccessType {
    /**
     * Only one particular TPP has an access
     */
    SINGLE_TPP,

    /**
     * All TPPs have an access
     */
    ALL_TPP
}
