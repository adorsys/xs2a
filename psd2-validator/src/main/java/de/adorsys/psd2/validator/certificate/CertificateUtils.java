package de.adorsys.psd2.validator.certificate;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.nimbusds.jose.util.X509CertUtils;

public class CertificateUtils {

	public static X509Certificate[] getCertificates(String folderName) {

		List<X509Certificate> listCert = new ArrayList<>();

		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource(folderName);
		if(url == null) {
			return new X509Certificate[] {};
		}
		String path = url.getPath();
		File[] files = new File(path).listFiles();

		for (File file : files) {
			
			try {
				byte[] bytesArray = FileUtils.readFileToByteArray(file);
				X509Certificate cert = X509CertUtils.parse(bytesArray);
				if (cert != null) {
					listCert.add(cert);
				}
			} catch (IOException e1) {
				
				e1.printStackTrace();
			}

		}
		return listCert.toArray(new X509Certificate[listCert.size()]);
	}

	public static String getCertificateByName(String filename) {

		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource("certificates/" + filename);
		String path = url.getPath();
		File file = new File(path);

		if (file.exists()) {

			try {
				byte[] bytesArray = FileUtils.readFileToByteArray(file);
				X509Certificate cert = X509CertUtils.parse(bytesArray);
				String encodeCert = X509CertUtils.toPEMString(cert);
				return encodeCert;
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return null;
	}
}
