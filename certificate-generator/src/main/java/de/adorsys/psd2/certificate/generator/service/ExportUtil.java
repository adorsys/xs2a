package de.adorsys.psd2.certificate.generator.service;

import de.adorsys.psd2.certificate.generator.exception.CertificateGeneratorException;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import java.io.IOException;
import java.io.StringWriter;

public class ExportUtil {

    private ExportUtil() {
    }

    public static String exportToString(Object obj) {
        try (StringWriter writer = new StringWriter(); JcaPEMWriter pemWriter = new JcaPEMWriter(writer)) {
            pemWriter.writeObject(obj);
            pemWriter.flush();
            return writer.toString().replaceAll("\n", "");
        } catch (IOException ex) {
            throw new CertificateGeneratorException("Could not export certificate", ex);
        }
    }

}
