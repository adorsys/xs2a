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

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.util.Arrays;

import javax.crypto.Mac;

import org.tomitribe.auth.signatures.Algorithm;
import org.tomitribe.auth.signatures.Base64;
import org.tomitribe.auth.signatures.Signature;
import org.tomitribe.auth.signatures.UnsupportedAlgorithmException;

public class Symmetric implements Verify {

	private final Key key;
	private final Algorithm algorithm;
	private final Provider provider;
	private final Signature signature;

	Symmetric(final Key key, final Provider provider, final Algorithm algorithm, final Signature signature) {
		this.key = key;
		this.provider = provider;
		this.algorithm = algorithm;
		this.signature = signature;
	}

	@Override
	public boolean verify(final byte[] signingStringBytes) {

		try {

			final Mac mac = provider == null ? Mac.getInstance(algorithm.getJmvName())
					: Mac.getInstance(algorithm.getJmvName(), provider);
			mac.init(key);
			byte[] hash = mac.doFinal(signingStringBytes);
			byte[] encoded = Base64.encodeBase64(hash);
			return Arrays.equals(encoded, signature.getSignature().getBytes());

		} catch (NoSuchAlgorithmException e) {

			throw new UnsupportedAlgorithmException(algorithm.getJmvName());

		} catch (Exception e) {

			throw new IllegalStateException(e);

		}
	}
}
