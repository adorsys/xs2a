package de.adorsys.psd2.validator.signature;

import org.tomitribe.auth.signatures.Base64;
import org.tomitribe.auth.signatures.PEM;
import org.tomitribe.auth.signatures.Signature;
import org.tomitribe.auth.signatures.Signer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.resourceToString;

/**
 * @author guymoyo
 * <p>
 * an example of TPP signature
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
     * “x-request-id”, “PSU-ID” (if and only if “PSU-ID” is included as a
     * header of the HTTP- Request). “PSU-Corporate-ID” (if and only if “PSU-
     * Corporate-ID” is included as a header of the HTTP-Request). “Date” No
     * other entries may be included
     */
    private String headers = "Digest TPP-Transaction-ID x-request-id PSU-ID Timestamp";

    private String privateKeyPem = readPrivateKeyPem();

    private PrivateKey privateKey = null;

    private void stringToPrivateKey() {
        try {
            privateKey = PEM.readPrivateKey(new ByteArrayInputStream(privateKeyPem.getBytes()));
        } catch (InvalidKeySpecException | IOException e) {
            e.printStackTrace();
        }
    }

    public String generateSignature() {

        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Digest", digest);
        headersMap.put("TPP-Transaction-ID", "3dc3d5b3-7023-4848-9853-f5400a64e80f");
        headersMap.put("X-Request-ID", "99391c7e-ad88-49ec-a2ad-99ddcb1f7721");
        headersMap.put("PSU-ID", "PSU-1234");
        headersMap.put("Timestamp", "Sun, 06 Aug 2017 15:02:37 GMT");

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

    private String readPrivateKeyPem() {
        try {
            return resourceToString("/signature/privateKeyPem.crt", UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
