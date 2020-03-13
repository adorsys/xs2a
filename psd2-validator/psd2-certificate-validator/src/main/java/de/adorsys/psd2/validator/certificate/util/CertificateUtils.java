/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.validator.certificate.util;

import com.nimbusds.jose.util.X509CertUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

@Slf4j
public class CertificateUtils {
    private static final int CERTIFICATE_PART_DATA_SIZE = 64;
    private static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----\n";
    private static final String END_CERTIFICATE = "-----END CERTIFICATE-----";

    private CertificateUtils() {
    }

    public static X509Certificate[] getCertificates(String folderName, String... fileNames) {
		return Arrays.stream(fileNames)
                .map(fileName -> getCertificate(folderName + "/" + fileName))
                .toArray(X509Certificate[]::new);
	}

	private static X509Certificate getCertificate(String filePath) {

		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try {
			byte[] bytes = IOUtils.resourceToByteArray(filePath, loader);
            return X509CertUtils.parse(bytes);
		} catch (IOException e) {
			log.debug(e.getMessage());
		}
		return null;
	}

	public static String getCertificateByName(String filename) {

		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try {
			byte[] bytes = IOUtils.resourceToByteArray("certificates/" + filename, loader);
			X509Certificate cert = X509CertUtils.parse(bytes);
            return X509CertUtils.toPEMString(cert);
		} catch (IOException e) {
			log.debug(e.getMessage());
		}
		return null;
	}

    /**
     * Normalizes certificate: removes excess blanks and wraps by beginning and end tags.
     *
     * @param certificate certificate text
     * @return normalized certificate
     * <p>
     * -----BEGIN CERTIFICATE-----
     * (certificate)
     * -----END CERTIFICATE-----
     */
    public static String normalizeCertificate(String certificate) {
        if (certificate == null) {
            return null;
        }
        String certificateData = getCertificateData(certificate);
        return BEGIN_CERTIFICATE +
                   certificateData.replaceAll(".{" + CERTIFICATE_PART_DATA_SIZE + "}", "$0" + StringUtils.LF) +
                   END_CERTIFICATE;
    }

    private static String getCertificateData(String certificate) {
        return certificate.replace(" ", "")
                   .replace("\n", "")
                   .replace("-----BEGINCERTIFICATE-----", "")
                   .replace("-----ENDCERTIFICATE-----", "");
    }
}
