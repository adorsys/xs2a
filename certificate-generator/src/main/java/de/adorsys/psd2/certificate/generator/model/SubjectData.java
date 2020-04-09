package de.adorsys.psd2.certificate.generator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bouncycastle.asn1.x500.X500Name;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

@Data
@AllArgsConstructor
public class SubjectData {
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private X500Name x500name;
    private Integer serialNumber;
    private Date startDate;
    private Date endDate;
}
