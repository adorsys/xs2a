package de.adorsys.psd2.validator.signature;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Map;

import javax.crypto.Mac;

import org.tomitribe.auth.signatures.Algorithm;
import org.tomitribe.auth.signatures.Base64;
import org.tomitribe.auth.signatures.Signature;
import org.tomitribe.auth.signatures.Signatures;
import org.tomitribe.auth.signatures.UnsupportedAlgorithmException;

/**
 * A new instance of the Verifier class needs to be created for each signature.
 */
public class SignatureVerifier {

	private final Verify verify;
	private final Signature signature;
	private final Algorithm algorithm;
	private final Provider provider;

	public SignatureVerifier(final Key key, final Signature signature) {
		this(key, signature, null);
	}

	public SignatureVerifier(final Key key, final Signature signature, final Provider provider) {
		requireNonNull(key, "Key cannot be null");
		this.signature = requireNonNull(signature, "Signature cannot be null");
		this.algorithm = signature.getAlgorithm();
		this.provider = provider;

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
