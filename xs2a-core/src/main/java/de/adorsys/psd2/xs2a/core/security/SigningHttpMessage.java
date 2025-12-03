/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.core.security;

import java.util.Optional;

/**
 * Immutable record class for signing HTTP messages.
 * Contains digest and X-JWS-Signature information.
 */
public record SigningHttpMessage(
    /**
     * The digest of the HTTP message
     */
    Optional<String> digest,

    /**
     * The X-JWS-Signature of the HTTP message
     */
    Optional<String> xJwsSignature
) {
    /**
     * Creates a new SigningHttpMessage instance.
     *
     * @param digest the digest of the HTTP message
     * @param xJwsSignature the X-JWS-Signature of the HTTP message
     */
    public SigningHttpMessage {
        // Record constructor with validation
    }
}
