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

package de.adorsys.psd2.certificate.generator.service;

import com.nimbusds.jose.util.X509CertUtils;
import de.adorsys.psd2.certificate.generator.exception.CertificateGeneratorException;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Optional;

@Component
public class KeysProvider {

    @Value("${xs2a.certificate-generator.template.public.key:certificates/MyRootCA.key}")
    private String issuerPrivateKey;

    @Value("${xs2a.certificate-generator.template.private.key:certificates/MyRootCA.pem}")
    private String issuerCertificate;

    /**
     * Load private key from classpath.
     *
     * @return PrivateKey
     */
    public PrivateKey loadPrivateKey() {
        InputStream stream = getResourceAsStream(issuerPrivateKey);
        if (stream == null) {
            throw new CertificateGeneratorException("Could not read private key from classpath:" + "certificates/" + issuerPrivateKey);
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
        InputStream is = getResourceAsStream(issuerCertificate);

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

    private InputStream getResourceAsStream(String path) {
        InputStream resourceAsStream = getResourceFromPath(path);
        if (resourceAsStream == null) {
            resourceAsStream = getFromGlobalPath(path);
        }
        return resourceAsStream;
    }

    private InputStream getResourceFromPath(String path) {
        return Optional.ofNullable(getClass().getClassLoader().getResourceAsStream(path)).orElse(getFromInnerPath(path));
    }

    private InputStream getFromInnerPath(String path) {
        return Optional.ofNullable(getClass().getResourceAsStream(path)).orElse(null);
    }

    private InputStream getFromGlobalPath(String path) {
        File initialFile = new File(path);
        try {
            return new FileInputStream(initialFile);
        } catch (FileNotFoundException e) {
            return null;
        }
    }
}
