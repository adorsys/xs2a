package de.adorsys.psd2.validator.common;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;

public class RoleOfPspOid extends ASN1ObjectIdentifier {
    public static final ASN1ObjectIdentifier    etsi_psd2_roles      = new ASN1ObjectIdentifier("0.4.0.19495.1");
    public static final RoleOfPspOid    id_psd2_role_psp_as  = new RoleOfPspOid(etsi_psd2_roles.branch("1"));
    public static final RoleOfPspOid    id_psd2_role_psp_pi =  new RoleOfPspOid(etsi_psd2_roles.branch("2"));
    public static final RoleOfPspOid    id_psd2_role_psp_ai =  new RoleOfPspOid(etsi_psd2_roles.branch("3"));
    public static final RoleOfPspOid    id_psd2_role_psp_ic =  new RoleOfPspOid(etsi_psd2_roles.branch("4"));
	
	public RoleOfPspOid(ASN1ObjectIdentifier identifier) {
		super(identifier.getId());
	}

}
