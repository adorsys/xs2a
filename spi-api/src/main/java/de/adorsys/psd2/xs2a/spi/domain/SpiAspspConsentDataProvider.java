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
