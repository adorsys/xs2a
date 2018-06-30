package de.adorsys.psd2.validator.common;

import org.bouncycastle.asn1.DERUTF8String;

public class NCAName extends DERUTF8String {
	public NCAName(String string) {
		super(string);
	}

	public static NCAName getInstance(Object obj){
		if(obj instanceof NCAName) return (NCAName) obj;
		return new NCAName(DERUTF8String.getInstance(obj).getString());
	}
}
