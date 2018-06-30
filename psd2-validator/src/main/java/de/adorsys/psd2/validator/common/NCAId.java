package de.adorsys.psd2.validator.common;

import org.bouncycastle.asn1.DERUTF8String;

public class NCAId extends DERUTF8String {

	public NCAId(String string) {
		super(string);
	}

	public static NCAId getInstance(Object obj){
		if(obj instanceof NCAId) return (NCAId) obj;
		return new NCAId(DERUTF8String.getInstance(obj).getString());
	}
}
