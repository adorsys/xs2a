package de.adorsys.psd2.certificate.generator.service;

import com.nimbusds.jose.util.X509CertUtils;
import de.adorsys.psd2.certificate.generator.exception.CertificateGeneratorException;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;

@Component
public class KeysProvider {
    private static final String ISSUER_PRIVATE_KEY = "MyRootCA.key";
    private static final String ISSUER_CERTIFICATE = "MyRootCA.pem";

    /**
     * Load private key from classpath.
     *
     * @return PrivateKey
     */
    public PrivateKey loadPrivateKey() {
        InputStream stream = getResourceAsStream(ISSUER_PRIVATE_KEY);
        if (stream == null) {
            throw new CertificateGeneratorException("Could not read private key from classpath:" + "certificates/" + ISSUER_PRIVATE_KEY);
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        try {
            Security.addProvider(new BouncyCastleProvider());
            PEMParser pp = new PEMParser(br);
            PEMKeyPair pemKeyPair = (PEMKeyPair) pp.readObject();
            KeyPair kp = new JcaPEMKeyConverter().getKeyPair(pemKeyPair);
            pp.close();
            return kp.getPrivate();
        } catch (IOException ex) {
            throw new CertificateGeneratorException("Could not read private key from classpath", ex);
        }
    }

    /**
     * Load X509Certificate from classpath.
     *
     * @return X509Certificate
     */
    public X509Certificate loadCertificate() {
        InputStream is = getResourceAsStream(ISSUER_CERTIFICATE);

        if (is == null) {
            throw new CertificateGeneratorException("Could not find certificate in classpath");
        }

        try {
            byte[] bytes = IOUtils.toByteArray(is);
            return X509CertUtils.parse(bytes);
        } catch (IOException ex) {
            throw new CertificateGeneratorException("Could not read certificate from classpath", ex);
        }
    }

    private InputStream getResourceAsStream(String filename) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return loader.getResourceAsStream("certificates/" + filename);
    }
}
