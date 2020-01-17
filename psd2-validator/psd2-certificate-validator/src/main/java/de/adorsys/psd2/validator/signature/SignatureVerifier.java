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
