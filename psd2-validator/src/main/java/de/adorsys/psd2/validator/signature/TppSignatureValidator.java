package de.adorsys.psd2.validator.signature;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.tomitribe.auth.signatures.Signature;

import com.nimbusds.jose.util.X509CertUtils;

public class TppSignatureValidator {

	/**
	 * mandatory header fields for http signature in case of psd2
	 */
	private static final List<String> MANDATORY_HEADERS_PSD2 = Arrays
			.asList(new String[] { "digest", "tpp-transaction-id", "tpp-request-id", "date" });

	/**
	 * signature should not be null signature should be conform with psd2
	 * addition signature should be verifiable by the entry certificate
	 *
	 * @param signature
	 * @return true or false
	 * @throws IOException
	 * @throws SignatureException
	 * @throws NoSuchAlgorithmException
	 */
	public boolean verifySignature(String signature, String tppEncodedCert, Map<String, String> headers)
			throws NoSuchAlgorithmException, SignatureException, IOException {

		if (StringUtils.isBlank(signature)) {
			throw new IllegalArgumentException("SIGNATURE_MISSING");
		}

		if (StringUtils.isBlank(tppEncodedCert)) {
			throw new IllegalArgumentException("CERTIFICAT_MISSING");
		}

		X509Certificate cert = X509CertUtils.parse(tppEncodedCert);

		PublicKey key = cert.getPublicKey();

		Signature signatureData = Signature.fromString(signature);

		if (!signatureData.getHeaders().containsAll(MANDATORY_HEADERS_PSD2)) {
			throw new IllegalArgumentException("SIGNATURE_INVALID");
		}

		SignatureVerifier verifier = new SignatureVerifier(key, signatureData);
		return verifier.verify("method", "uri", headers);
	}

}
