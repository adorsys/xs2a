package de.adorsys.psd2.validator.common;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

public class RolesOfPSP {
	private final RoleOfPSP[] roles;
	private final DERSequence sequence;

	public RolesOfPSP(RoleOfPSP[] roles) {
		this.roles = roles;
		ASN1Encodable[] array = new ASN1Encodable[roles.length];
		for (int i = 0; i < this.roles.length; i++) {
			array[i] = this.roles[i].toDERSequence();
		}
		this.sequence = new DERSequence(array);
	}

	public static RolesOfPSP getInstance(Object obj) {
		if (obj instanceof RolesOfPSP)
			return (RolesOfPSP) obj;
		ASN1Sequence instance = DERSequence.getInstance(obj);
		ASN1Encodable[] array = instance.toArray();
		RoleOfPSP[] roles = new RoleOfPSP[array.length];
		for (int i = 0; i < array.length; i++) {
			roles[i] = RoleOfPSP.getInstance(array[i]);
		}
		return new RolesOfPSP(roles);
	}

	public DERSequence toDERSequence() {
		return sequence;
	}

	public RoleOfPSP[] getRoles() {
		return roles;
	}

}
