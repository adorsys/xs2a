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

	private static final List<String> MANDATODY_HEADERS_PSD2 = Arrays
			.asList(new String[] { "Digest", "TPP-Transaction", "TPP-Request-ID", "Date" });

	/**
	 * signature should not be null signature should be conform with psd2
	 * addition signature should be verifiable by the entry certificate
	 * 
	 * @param signature
	 * @param tppCertificate
	 * @return true or false
	 * @throws IOException
	 * @throws SignatureException
	 * @throws NoSuchAlgorithmException
	 */
	public static boolean verifySignature(String signature, String tppEncodedCert, Map<String, String> headers)
			throws NoSuchAlgorithmException, SignatureException, IOException {

		if (StringUtils.isEmpty(signature)) {
			throw new IllegalArgumentException("SIGNATURE_MISSING");
		}

		if (StringUtils.isEmpty(tppEncodedCert)) {
			throw new IllegalArgumentException("CERTIFICAT_MISSING");
		}

		X509Certificate cert = X509CertUtils.parse(tppEncodedCert);

		PublicKey key = cert.getPublicKey();

		Signature signatureData = Signature.fromString(signature);

		//TODO 01 check if signature headers contain the mandory psd2 headers attribute

		SignatureVerifier verifier = new SignatureVerifier(key, signatureData);
		verifier.verify("method", "uri", headers);

		return true;
	}

}
