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

            this.verify = new Asymmetric(PublicKey.class.cast(key));

        } else if (Mac.class.equals(algorithm.getType())) {

            this.verify = new Symmetric(key);

        } else {

            throw new UnsupportedAlgorithmException(String.format("Unknown Algorithm type %s %s", algorithm.getPortableName(), algorithm.getType().getName()));
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

    public boolean verify(final String method, final String uri, final Map<String, String> headers) throws IOException, NoSuchAlgorithmException, SignatureException {

        final String signingString = createSigningString(method, uri, headers);

        return verify.verify(signingString.getBytes());
    }

    public String createSigningString(final String method, final String uri, final Map<String, String> headers) throws IOException {
        return Signatures.createSigningString(signature.getHeaders(), method, uri, headers);
    }

    private interface Verify {
        boolean verify(byte[] signingStringBytes);
    }

    private class Asymmetric implements Verify {

        private final PublicKey key;

        private Asymmetric(final PublicKey key) {
            this.key = key;
        }

        @Override
        public boolean verify(final byte[] signingStringBytes) {
            try {

                final java.security.Signature instance = provider == null ?
                        java.security.Signature.getInstance(algorithm.getJmvName()) :
                        java.security.Signature.getInstance(algorithm.getJmvName(), provider);

                instance.initVerify(key);
                instance.update(signingStringBytes);
                return instance.verify(Base64.decodeBase64(signature.getSignature().getBytes()));

            } catch (NoSuchAlgorithmException e) {

                throw new UnsupportedAlgorithmException(algorithm.getJmvName());

            } catch (Exception e) {

                throw new IllegalStateException(e);
            }
        }
    }

    private class Symmetric implements Verify {

        private final Key key;

        private Symmetric(final Key key) {
            this.key = key;
        }

        @Override
        public boolean verify(final byte[] signingStringBytes) {

            try {

                final Mac mac = provider == null ? Mac.getInstance(algorithm.getJmvName()) : Mac.getInstance(algorithm.getJmvName(), provider);
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
}
