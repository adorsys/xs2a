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

package de.adorsys.psd2.xs2a.spi.domain;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides access to encrypted ASPSP's Consent Data object.
 * This object is stored as byte-array in Consent/Payment and encrypted,
 * so that it can be read only in scope of a TPP request.
 * May be used to store some session/workflow relevant data, that is needed to
 * service TPP request without PSU in session.
 *
 * The user is responsible for correct serialisation/deserialisation of consent data object as byte array.
 */
public interface SpiAspspConsentDataProvider {
    /**
     * Loads AspspConsentData array from database
     * @return consent data byte array. Empty byte array if no data was set before.
     */
    @NotNull
    byte[] loadAspspConsentData();

    /**
     * Updates AspspConsentData object in the database. If no changes were made, no database update will be called.
     * Setting null or empty array removes AspspConsentData object from the database.
     * @param aspspConsentData byte array to be stored in consent/payment encrypted
     */
    void updateAspspConsentData(@Nullable byte[] aspspConsentData);

    /**
     * Deletes AspspConsentData object from the database.
     */
    void clearAspspConsentData();
}
