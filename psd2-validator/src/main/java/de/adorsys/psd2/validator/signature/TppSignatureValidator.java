package de.adorsys.psd2.validator.signature;

import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.tomitribe.auth.signatures.Signature;

import com.nimbusds.jose.util.X509CertUtils;

public class TppSignatureValidator {

	/**
	 * signature should not be null
	 * signature should be conform with psd2 addition
	 * signature should be verifiable by the entry certificate
	 * 
	 * @param signature
	 * @param tppCertificate
	 * @return true or false
	 * @throws Exception 
	 */
	public static boolean verifySignature(String signature, String tppEncodedCert) throws Exception{
		
		if(StringUtils.isEmpty(signature))
			throw new Exception("SIGNATURE_MISSING");
		
		if(StringUtils.isEmpty(tppEncodedCert))
		throw new Exception("CERTIFICAT_MISSING");
		
		X509Certificate cert = X509CertUtils.parse(tppEncodedCert);
		
		PublicKey key = cert.getPublicKey();
		
		Signature signatureData = Signature.fromString(signature);
		
		/*if(!verifySignatureHeadersContentPDS2MantadoryAttributes(signatureData.getHeaders()))
			throw new Exception("SIGNATURE_INVALID");*/
		
		new SignatureVerifier(key, signatureData);
		
		return true;
	}

	
	private static boolean verifySignatureHeadersContentPDS2MantadoryAttributes(List<String> headers){

		List<String> mandatoryHeadersPsd2 = Arrays.asList(new String[] { "Digest", "TPP-Transaction", "TPP-Request-ID", "Date" });
		
		for(String headerPsd2:mandatoryHeadersPsd2){
			if(!headers.contains(headerPsd2))
				return false;
		}
		
		return true;
	}
	
	
}
