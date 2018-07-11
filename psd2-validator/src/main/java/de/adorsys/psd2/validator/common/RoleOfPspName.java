package de.adorsys.psd2.validator.common;

import org.bouncycastle.asn1.DERUTF8String;

public class RoleOfPspName extends DERUTF8String {
	public static final RoleOfPspName PSP_AS = new RoleOfPspName("PSP_AS");
	public static final RoleOfPspName PSP_PI = new RoleOfPspName("PSP_PI");
	public static final RoleOfPspName PSP_AI = new RoleOfPspName("PSP_AI");
	public static final RoleOfPspName PSP_IC = new RoleOfPspName("PSP_IC");
	
	private RoleOfPspName(String string) {
		super(string);
	}
}
