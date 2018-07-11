package de.adorsys.psd2.validator.common;

import java.util.Arrays;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERUTF8String;

import de.adorsys.psd2.validator.certificate.util.TppRole;

public enum RoleOfPSP {
	PSP_AS(RoleOfPspOid.id_psd2_role_psp_as, RoleOfPspName.PSP_AS, TppRole.ASPSP.name()), PSP_PI(
			RoleOfPspOid.id_psd2_role_psp_pi, RoleOfPspName.PSP_PI, TppRole.PISP.name()), PSP_AI(
					RoleOfPspOid.id_psd2_role_psp_ai, RoleOfPspName.PSP_AI, TppRole.AISP.name()), PSP_IC(
							RoleOfPspOid.id_psd2_role_psp_ic, RoleOfPspName.PSP_IC, TppRole.PIISP.name());

	private ASN1ObjectIdentifier roleOfPspOid;
	private DERUTF8String roleOfPspName;
	private DERSequence sequence;
	private String normalizedRoleName;

	private RoleOfPSP(ASN1ObjectIdentifier roleOfPspOid, DERUTF8String roleOfPspName, String normalizedRoleName) {
		this.roleOfPspOid = roleOfPspOid;
		this.roleOfPspName = roleOfPspName;
		this.normalizedRoleName = normalizedRoleName;
		sequence = new DERSequence(new ASN1Encodable[] { roleOfPspOid, roleOfPspName });
	}

	public static RoleOfPSP getInstance(ASN1Encodable asn1Encodable) {
		ASN1Sequence sequence = ASN1Sequence.getInstance(asn1Encodable);
		ASN1ObjectIdentifier objectIdentifier = ASN1ObjectIdentifier.getInstance(sequence.getObjectAt(0));
		DERUTF8String instance = DERUTF8String.getInstance(sequence.getObjectAt(1));

		return Arrays.stream(RoleOfPSP.values())
				.filter(role -> role.getRoleOfPspOid().getId().equals(objectIdentifier.getId())
						&& role.getRoleOfPspName().getString().equals(instance.getString()))
				.findFirst().orElseThrow(() -> new IllegalArgumentException(
						"unknown object in getInstance: " + asn1Encodable.getClass().getName()));
	}

	public ASN1ObjectIdentifier getRoleOfPspOid() {
		return roleOfPspOid;
	}

	public DERUTF8String getRoleOfPspName() {
		return roleOfPspName;
	}

	public DERSequence toDERSequence() {
		return sequence;
	}

	public String getNormalizedRoleName() {
		return normalizedRoleName;
	}

}
