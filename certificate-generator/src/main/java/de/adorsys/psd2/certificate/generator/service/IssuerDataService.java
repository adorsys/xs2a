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

import de.adorsys.psd2.certificate.generator.exception.CertificateGeneratorException;
import de.adorsys.psd2.certificate.generator.model.IssuerData;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.springframework.stereotype.Service;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

@Slf4j
@Service
public class IssuerDataService {

    private final KeysProvider keysProvider;

    @Getter
    private IssuerData issuerData;

    public IssuerDataService(KeysProvider keysProvider) {
        this.keysProvider = keysProvider;
        this.issuerData = generateIssuerData();
    }

    private IssuerData generateIssuerData() {
        IssuerData data = new IssuerData();
        X509Certificate cert = keysProvider.loadCertificate();

        try {
            data.setX500name(new JcaX509CertificateHolder(cert).getSubject());
        } catch (CertificateEncodingException ex) {
            throw new CertificateGeneratorException("Could not read issuer data from certificate", ex);
        }

        data.setPrivateKey(keysProvider.loadPrivateKey());
        return data;
    }
}
