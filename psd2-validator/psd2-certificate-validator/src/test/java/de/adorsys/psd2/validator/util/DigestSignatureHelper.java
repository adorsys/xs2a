/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.validator.util;

import com.nimbusds.jose.util.X509CertUtils;
import de.adorsys.psd2.validator.certificate.util.CertificateUtils;
import de.adorsys.psd2.validator.signature.service.Digest;
import de.adorsys.psd2.validator.signature.service.algorithm.HashingAlgorithm;
import de.adorsys.xs2a.reader.JsonReader;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.security.cert.X509Certificate;

public class DigestSignatureHelper {

    private final KeyPairHolder keyPairHolder;
    private final PrivateKey privateKey;
    private final X509Certificate x509Certificate;

    /**
     * Uses pair of keys from certificate generator
     */
    public DigestSignatureHelper() {
        JsonReader jsonReader = new JsonReader();
        keyPairHolder = jsonReader.getObjectFromFile("helper/key-pair.json", KeyPairHolder.class);

        privateKey = loadPrivateKey();
        x509Certificate = X509CertUtils.parse(
            CertificateUtils.normalizeCertificate(keyPairHolder.getEncodedCert())
        );
    }

    /**
     * Calculates `digest` header based on request payload
     *
     * @param requestBody request payload
     * @return digest like `SHA-256=C9pfGam1eKT0VcGxGxLTrn05G8BUEJX2KA84VcrAtQo=`
     */
    public String digest(String requestBody) {
        return buildDigest(requestBody)
                   .getHeaderValue();
    }

    /**
     * Signs input string
     *
     * @param content input string
     * @return signature
     * @throws Exception
     */
    public String sign(String content) throws Exception {
        byte[] data = content.getBytes(StandardCharsets.UTF_8);

        Signature sig = Signature.getInstance("SHA256WithRSA");
        sig.initSign(privateKey);
        sig.update(data);

        return Base64.encodeBase64String(sig.sign());
    }

    /**
     * Signature verification method
     *
     * @param content   content
     * @param signature signature
     * @return TRUE if signature is correct and FALSE otherwise
     * @throws Exception
     */
    public boolean verify(String content, String signature) throws Exception {
        byte[] data = content.getBytes(StandardCharsets.UTF_8);
        byte[] signatureBytes = Base64.decodeBase64(signature);

        Signature sig = Signature.getInstance("SHA256WithRSA");
        sig.initVerify(x509Certificate.getPublicKey());
        sig.update(data);

        return sig.verify(signatureBytes);
    }

    /**
     * Calculates `signature` header:
     * <p>
     * example:
     * keyId="SN=7ceaf114,CA=OU=Information%20Technology,O=Trust%20Service%20Provider%20AG,L=Nuremberg,ST=BAVARIA,C=DE",algorithm="SHA256withRSA",headers="x-request-id digest psu-ip-address accept psu-id",signature="c7+BjFQSmcJ/f6sLV7YiFcGqGvmJ4T6M9boFCV1JyT0Iq7mJXLA0W2gKlaFEO31BGLbGqbXRfJTp3rpR+KqKl57EJuPqQNsFZAaDAqo/RTfbgCfsMh1Fbo+jmkCufpFCTtUxC/konWChoU10YWPglFQ86/KXBFqlO33G6RugxrZ+BdV6QUqzVEoY1cP7bYu89HbE92OzAiXi70vqPqGmiwB5PrFtSGf4ZUtJT6ruwgctaxGoNOGSN5a/RnnbaMRZgveH+NEdJX0b3MpedlO2gG8ek3XOeRLF0jf9IZ9zDkyl8hqpefq2C3EJHIAMMEOA/iuwwtms+oAgg8Ffd3NTdw=="
     *
     * @param signature signature
     * @param headers   headers (x-request-id digest psu-ip-address accept psu-id)
     * @return `signature` header
     */
    public String getSignatureHeader(String signature, String headers) {
        String keyId = getKeyIdFromCertificate(x509Certificate);
        return "keyId=\"" + keyId + "\"" +
                   ",algorithm=\"" + x509Certificate.getSigAlgName() + "\"" +
                   ",headers=\"" + headers + "\"" +
                   ",signature=\"" + signature + "\"";
    }

    private String getKeyIdFromCertificate(X509Certificate certificate) {
        String var10000 = certificate.getSerialNumber().toString(16);
        return "SN=" + var10000 + ",CA=" + certificate.getIssuerX500Principal().getName()
                                               .replace(" ", "%20")
                                               .replace("\n", "");
    }

    private PrivateKey loadPrivateKey() {
        String privateKey = keyPairHolder.getPrivateKey()
                                .replace("-----BEGIN RSA PRIVATE KEY-----", "-----BEGIN RSA PRIVATE KEY-----\n")
                                .replace("-----END RSA PRIVATE KEY-----", "\n-----END RSA PRIVATE KEY-----");
        BufferedReader br = new BufferedReader(new StringReader(privateKey));
        try {
            Security.addProvider(new BouncyCastleProvider());
            PEMParser pp = new PEMParser(br);
            PEMKeyPair pemKeyPair = (PEMKeyPair) pp.readObject();
            KeyPair kp = new JcaPEMKeyConverter().getKeyPair(pemKeyPair);
            pp.close();
            return kp.getPrivate();
        } catch (IOException ex) {
            throw new RuntimeException("Could not read private key from classpath", ex);
        }
    }

    private Digest buildDigest(String requestBody) {
        return Digest.builder()
                   .requestBody(requestBody)
                   .hashingAlgorithm(HashingAlgorithm.SHA256)
                   .build();
    }

    /**
     * Container for private and public keys from certificate generator
     */
    @Data
    @NoArgsConstructor
    public static class KeyPairHolder {
        private String encodedCert;
        private String privateKey;
    }
}
