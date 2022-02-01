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

package de.adorsys.psd2.validator.signature;

import lombok.NonNull;

import java.util.Map;

public interface SignatureVerifier {
    /**
     * Verifies signature compliance with incoming headers map and TPP-SIGNATURE-CERTIFICATE
     *
     * @param signature  generated value according Signing HTTP Messages
     * @see <a href="https://datatracker.ietf.org/doc/draft-cavage-http-signatures"> HTTP Signature </a>
     *
     * @param tppEncodedCert The certificate used for signing the request, in base64 encoding.
     * @param headers Map with all request headers with their values
     * @param method Name of HTTP method according to rfc1945 spec. (HTTP/1.0)
     * @param url URL form request
     * @return <code>true</code> if signature is compliance with other parameters. <code>false</code> otherwise.
     */
    boolean verify(@NonNull String signature, @NonNull String tppEncodedCert, @NonNull Map<String, String> headers, @NonNull String method, @NonNull String url);
}
