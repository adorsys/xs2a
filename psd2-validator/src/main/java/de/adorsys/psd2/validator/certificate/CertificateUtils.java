package de.adorsys.psd2.validator.certificate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

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

			byte[] bytesArray = new byte[(int) file.length()];

			FileInputStream fis;
			try {
				fis = new FileInputStream(file);
				fis.read(bytesArray); // read file into bytes[]
				fis.close();

				X509Certificate cert = X509CertUtils.parse(bytesArray);
				if (cert != null) {
					listCert.add(cert);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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

			byte[] bytesArray = new byte[(int) file.length()];
			FileInputStream fis;
			try {
				fis = new FileInputStream(file);
				fis.read(bytesArray); // read file into bytes[]
				fis.close();

				X509Certificate cert = X509CertUtils.parse(bytesArray);
				String encodeCert = X509CertUtils.toPEMString(cert);
				return encodeCert;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return null;
	}
}
