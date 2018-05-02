package de.adorsys.psd2.validator.signature;

import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.PublicKey;

import org.tomitribe.auth.signatures.Algorithm;
import org.tomitribe.auth.signatures.Base64;
import org.tomitribe.auth.signatures.Signature;
import org.tomitribe.auth.signatures.UnsupportedAlgorithmException;

public class Asymmetric implements Verify {

    private final PublicKey key;
    private final Algorithm algorithm;
    private final Provider provider;
    private final Signature signature;

    Asymmetric(final PublicKey key, final Provider provider, final Algorithm algorithm, final Signature signature) {
        this.key = key;
        this.provider = provider;
        this.algorithm = algorithm;
        this.signature = signature;
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
