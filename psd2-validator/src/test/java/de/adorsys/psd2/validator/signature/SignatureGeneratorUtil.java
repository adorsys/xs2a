package de.adorsys.psd2.validator.signature;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

import org.tomitribe.auth.signatures.Base64;
import org.tomitribe.auth.signatures.PEM;
import org.tomitribe.auth.signatures.Signature;
import org.tomitribe.auth.signatures.Signer;

/**
 * 
 * @author guymoyo
 * 
 *         an example of TPP signature
 *
 */
public class SignatureGeneratorUtil {

	/**
	 * The “Digest” Header contains a Hash of the message body. The only hash
	 * algorithms that may be used to calculate the Digest within the context of
	 * this specification are SHA-256 and SHA-512 as defined in [RFC5843].
	 */
	private String digest = generateDigest();

	/**
	 * According to psd2, the value must be Serial Number of the TPP's
	 * certificate
	 */
	private String keyId = "1.3.6.1.4.1.21528.2.2.99.11534";

	/**
	 * According to Psd2 The algorithm must identify the same algorithm for the
	 * signature as presented in the certificate (Element “TPP- Certificate”) of
	 * this Request. It must identify SHA-256 or SHA-512 as Hash algorithm
	 */
	private String algorithm = "rsa-sha256";

	/**
	 * According to Psd2, it Must include “Digest”, “TPP-Transaction-ID”,
	 * “TPP-Request-ID”, “PSU-ID” (if and only if “PSU-ID” is included as a
	 * header of the HTTP- Request). “PSU-Corporate-ID” (if and only if “PSU-
	 * Corporate-ID” is included as a header of the HTTP-Request). “Date” No
	 * other entries may be included
	 */
	private String headers = "Digest TPP-Transaction-ID TPP-Request-ID PSU-ID Date";

	// NOPMD TODO remove this and inside a file
	private String privateKeyPem = "-----BEGIN PRIVATE KEY-----\n"
			+ "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDGAzkeZgKRpqaE\n"
			+ "yT+ZuLhMuHXfDaGgimCQSToqGORHQPeDqy6g1CfT9sEYDQzSIA10oMI7c0kuxhNX\n"
			+ "4/FW7yXeT4Dxw4LuprnUUovZas2lAhiK7XVR6ppRUvHMFTqWHsURQQgXYWJFcAo4\n"
			+ "6sIOatIgNfepw08nw6RMZDhaA4g2XIPRF+w525W0cDAAD1BcKUIxW7rud0NPO150\n"
			+ "VY/M7uCS+ZiWxpfzEkBHkIs839PXICaDJJm051pFVpKy2aShkXt6SBkJy0RMrAxI\n"
			+ "L5zpH8+Se8nFc5YDwNowgsDyooz2qbcbINNBboK2X3P7QflY9JATUKdwywaj391d\n"
			+ "wYoT+5ZRAgMBAAECggEAY5KjWoH5sLyY6BU7glW9d/cabvwv1sc/H89zDFBQGvS0\n"
			+ "guXGIOIO1Sw0lZ+aXt/3ZDqi8bpbhsXcXEonxoukA1L/iJPEd7YnpHmOEdr/ZobT\n"
			+ "SKl4YaUTzOlk5jeWqn72omKNaxRxknFw8oY6530YBVKeJCQu3dQn/rI3FCzKhmrE\n"
			+ "pVT5U2EgouK+Yv0UhKwOiPzFQP/L/CU7lDCfsf7F8zVkAXyUQJHumYrkRTUFhMwb\n"
			+ "c7KbmKRCi9FLew8X8mbZB3wDM0XQn3Oje7SzMWxx4l7EDKm6jACeH/V60m0xDhLI\n"
			+ "+o6U7QswwKj4UseAQdCeJq0s18oJrVZRzGIOWA5+MQKBgQDt93IjlfO4vTRaBxah\n"
			+ "C6B4iDmEq37dx/+NoZcTgpt5L45uWKjghhaI5KDM/85mHaUEba8+MfimBQKHycXh\n"
			+ "VpEp9sDk0da0JhfOz0EaqWQoTMpyMN5iADN6RlpZYyL1AvtcGI6zo5Ei9QB1oQ/8\n"
			+ "CHAZM4X+CpwjzjlbLCO1RPaBWwKBgQDVBKtbJAbHhrJdVrAa5wKgS8yvZYOPKjIH\n"
			+ "vzq+BIzy+m6upeQgiE82aWs9OlrUdvGbk2hALJXQbsghzr1MRtDgVRlQQDZnp4ZU\n"
			+ "CRpj/+oca//HiUo7tMq5jVmB4QfQ6X2kdARg1tF9nc/GvGIgiiYRlfhvIdWDK2iO\n"
			+ "6Yf/bDSKwwKBgFiBmwMfR4mjXXBKiKEXSPTrfbEZc9MbCrJrslwATMES7f+enBj9\n"
			+ "5i2+EwyL7AFQ70opXW9deKSO4nUMl7uKzez0qKOyZA+Wx24U6Zr3+5d9kCJOiLec\n"
			+ "aYkF1569X4gPNtv3CkRIBrggta4KXH6ZyM+muSRWX+J1ViHR1eoanzBXAoGAJbAU\n"
			+ "zSQ7mtOG/SKYN7pFaazfguy38P1rKpm3v+S4N0j9iiLJkMPtF9hg481OQqbkqjzT\n"
			+ "Rf9dZiojeG/GaBdjXz1PJDaoKYCnMHkH1Udy2SJ2d4wfuR6Me3W5r1Pr+RdGZnEs\n"
			+ "USHQoKPqHeQ4jD9E6vmYobjfuLxWXGqO6C7xwNcCgYEAyq4T9WCEzbTmKhObo+y3\n"
			+ "fltb93+qvnz30YL7lDgt98pkgFZxzMqodYXrpqmJFBbsk3cKL/owxdLJo2C6TFp8\n"
			+ "Ty39XUa8UHGEy9SePknLYNUEivu1JiA0X7+9Yg5iqKAIWX12jJctVBmCtNuwebnA\n" + "VZwbfr01KRiAf2ubWdrGr1Y=\n"
			+ "-----END PRIVATE KEY-----\n";

	private PrivateKey privateKey = null;

	private void stringToPrivateKey() {
		try {
			privateKey = PEM.readPrivateKey(new ByteArrayInputStream(privateKeyPem.getBytes()));
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String generateSignature() {

		Map<String, String> headersMap = new HashMap<>();
		headersMap.put("Digest", digest);
		headersMap.put("TPP-Transaction-ID", "3dc3d5b3-7023-4848-9853-f5400a64e80f");
		headersMap.put("TPP-Request-ID", "99391c7e-ad88-49ec-a2ad-99ddcb1f7721");
		headersMap.put("PSU-ID", "PSU-1234");
		headersMap.put("Date", "Sun, 06 Aug 2017 15:02:37 GMT");

		Signature signature = new Signature(keyId, algorithm, null, headers.split(" "));

		stringToPrivateKey();
		Signer signer = new Signer(privateKey, signature);
		Signature signed;
		try {
			signed = signer.sign("method", "uri", headersMap);
			return signed.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String generateDigest() {

		String payload = "{client_name:XS2A Client,redirect_uris:[*]}";
		String digestHeader = null;
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(payload.getBytes());
			digestHeader = "SHA-256=" + new String(Base64.encodeBase64(digest));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return digestHeader;
	}

}
