package de.adorsys.psd2.validator.common;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERUTF8String;

import de.adorsys.psd2.validator.certificate.util.TppRole;

public class RoleOfPSP {
	public static final RoleOfPSP PSP_AS = new RoleOfPSP(RoleOfPspOid.id_psd2_role_psp_as, RoleOfPspName.PSP_AS, TppRole.ASPSP.name());
	public static final RoleOfPSP PSP_PI = new RoleOfPSP(RoleOfPspOid.id_psd2_role_psp_pi, RoleOfPspName.PSP_PI, TppRole.PISP.name());
	public static final RoleOfPSP PSP_AI = new RoleOfPSP(RoleOfPspOid.id_psd2_role_psp_ai, RoleOfPspName.PSP_AI, TppRole.AISP.name());
	public static final RoleOfPSP PSP_IC = new RoleOfPSP(RoleOfPspOid.id_psd2_role_psp_ic, RoleOfPspName.PSP_IC, TppRole.PIISP.name());
	
	private final ASN1ObjectIdentifier roleOfPspOid;
	private final DERUTF8String roleOfPspName;
	private final DERSequence sequence;
	private final String normalizedRoleName;

	private RoleOfPSP(ASN1ObjectIdentifier roleOfPspOid, DERUTF8String roleOfPspName, String normalizedRoleName) {
		this.roleOfPspOid = roleOfPspOid;
		this.roleOfPspName = roleOfPspName;
		this.normalizedRoleName = normalizedRoleName;
		sequence = new DERSequence(new ASN1Encodable[]{roleOfPspOid, roleOfPspName});
	}

	public static RoleOfPSP getInstance(ASN1Encodable asn1Encodable) {
		ASN1Sequence sequence = ASN1Sequence.getInstance(asn1Encodable);
		ASN1ObjectIdentifier objectIdentifier = ASN1ObjectIdentifier.getInstance(sequence.getObjectAt(0));
		DERUTF8String instance = DERUTF8String.getInstance(sequence.getObjectAt(1));
		if(RoleOfPspOid.id_psd2_role_psp_as.getId().equals(objectIdentifier.getId()) && RoleOfPspName.PSP_AS.getString().equals(instance.getString())){
			return PSP_AS;
		}
		if(RoleOfPspOid.id_psd2_role_psp_pi.getId().equals(objectIdentifier.getId()) && RoleOfPspName.PSP_PI.getString().equals(instance.getString())){
			return PSP_AS;
		}
		if(RoleOfPspOid.id_psd2_role_psp_ai.getId().equals(objectIdentifier.getId()) && RoleOfPspName.PSP_AI.getString().equals(instance.getString())){
			return PSP_AS;
		}
		if(RoleOfPspOid.id_psd2_role_psp_ic.getId().equals(objectIdentifier.getId()) && RoleOfPspName.PSP_IC.getString().equals(instance.getString())){
			return PSP_AS;
		}
		throw new IllegalArgumentException("unknown object in getInstance: " + asn1Encodable.getClass().getName());		
	}

	public ASN1ObjectIdentifier getRoleOfPspOid() {
		return roleOfPspOid;
	}

	public DERUTF8String getRoleOfPspName() {
		return roleOfPspName;
	}
	
	public DERSequence toDERSequence(){
		return sequence;
	}

	public String getNormalizedRoleName() {
		return normalizedRoleName;
	}
	
}
	