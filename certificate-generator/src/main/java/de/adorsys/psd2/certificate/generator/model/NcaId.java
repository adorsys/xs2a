package de.adorsys.psd2.certificate.generator.model;

import org.bouncycastle.asn1.DERUTF8String;

public class NcaId extends DERUTF8String {

    public NcaId(String string) {
        super(string);
    }
}
