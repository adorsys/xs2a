package de.adorsys.psd2.validator.common;

import java.util.Arrays;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.DERSequence;

public class RolesOfPSP {
	private final RoleOfPSP[] roles;
	private final DERSequence sequence;

	public RolesOfPSP(RoleOfPSP... roles) {
		this.roles = roles.clone();
		this.sequence = new DERSequence(
				Arrays.stream(roles).map(RoleOfPSP::toDERSequence).toArray(ASN1Encodable[]::new));

	}

	public static RolesOfPSP getInstance(Object obj) {
		if (obj instanceof RolesOfPSP) {
			return (RolesOfPSP) obj;
		}

		ASN1Encodable[] array = DERSequence.getInstance(obj).toArray();

		RoleOfPSP[] roles = Arrays.stream(array).map(RoleOfPSP::getInstance).toArray(RoleOfPSP[]::new);

		return new RolesOfPSP(roles);
	}

	public DERSequence toDERSequence() {
		return sequence;
	}

	public RoleOfPSP[] getRoles() {
		return roles.clone();
	}

}
