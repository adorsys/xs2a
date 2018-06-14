package de.adorsys.psd2.validator.certificate.util;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.nimbusds.jose.util.X509CertUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CertificateUtils {

	public static X509Certificate[] getCertificates(String folderName, String... fileNames) {

		List<X509Certificate> listCert = new ArrayList<>();

		for (String fileName : fileNames) {

			X509Certificate certificate = getCertificate(folderName + "/" + fileName);
			listCert.add(certificate);
		}

		return listCert.toArray(new X509Certificate[listCert.size()]);
	}

	public static X509Certificate getCertificate(String filePath) {

		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try {
			byte[] bytes = IOUtils.resourceToByteArray(filePath, loader);
			X509Certificate cert = X509CertUtils.parse(bytes);
			return cert;
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
			String encodeCert = X509CertUtils.toPEMString(cert);
			return encodeCert;
		} catch (IOException e) {
			log.debug(e.getMessage());
		}
		return null;
	}

}
