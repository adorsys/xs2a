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

package de.adorsys.psd2.validator.signature;

import org.tomitribe.auth.signatures.Algorithm;
import org.tomitribe.auth.signatures.Signature;
import org.tomitribe.auth.signatures.Signatures;
import org.tomitribe.auth.signatures.UnsupportedAlgorithmException;

import javax.crypto.Mac;
import java.io.IOException;
import java.security.*;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * A new instance of the Verifier class needs to be created for each signature.
 */
public class SignatureVerifier {

	private final Verify verify;
	private final Signature signature;

    public SignatureVerifier(final Key key, final Signature signature) {
		this(key, signature, null);
	}

	public SignatureVerifier(final Key key, final Signature signature, final Provider provider) {
		requireNonNull(key, "Key cannot be null");
		this.signature = requireNonNull(signature, "Signature cannot be null");
        Algorithm algorithm = signature.getAlgorithm();

		if (java.security.Signature.class.equals(algorithm.getType())) {

			this.verify = new Asymmetric(PublicKey.class.cast(key), provider, algorithm, signature);

		} else if (Mac.class.equals(algorithm.getType())) {

			this.verify = new Symmetric(key, provider, algorithm, signature);

		} else {

			throw new UnsupportedAlgorithmException(String.format("Unknown Algorithm type %s %s",
                                                                  algorithm.getPortableName(), algorithm.getType().getName()));
		}

		// check that the JVM really knows the algorithm we are going to use
		try {

			verify.verify("validation".getBytes());

		} catch (final RuntimeException e) {

			throw (RuntimeException) e;

		} catch (final Exception e) {

			throw new IllegalStateException("Can't initialise the Signer using the provided algorithm and key", e);
		}
	}

	public boolean verify(final String method, final String uri, final Map<String, String> headers)
			throws IOException, NoSuchAlgorithmException, SignatureException {

		final String signingString = createSigningString(method, uri, headers);

		return verify.verify(signingString.getBytes());
	}

	public String createSigningString(final String method, final String uri, final Map<String, String> headers)
			throws IOException {
		return Signatures.createSigningString(signature.getHeaders(), method, uri, headers);
	}

}
