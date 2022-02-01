/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.validator.common;

import org.bouncycastle.asn1.*;

import java.util.Arrays;

public enum RoleOfPSP {
	PSP_AS(RoleOfPspOid.id_psd2_role_psp_as, RoleOfPspName.PSP_AS, TppRoles.ASPSP), PSP_PI(
			RoleOfPspOid.id_psd2_role_psp_pi, RoleOfPspName.PSP_PI, TppRoles.PISP), PSP_AI(
					RoleOfPspOid.id_psd2_role_psp_ai, RoleOfPspName.PSP_AI, TppRoles.AISP), PSP_IC(
							RoleOfPspOid.id_psd2_role_psp_ic, RoleOfPspName.PSP_IC, TppRoles.PIISP);

	private ASN1ObjectIdentifier roleOfPspOid;
	private DERUTF8String roleOfPspName;
	private DERSequence sequence;
	private String normalizedRoleName;

	RoleOfPSP(ASN1ObjectIdentifier roleOfPspOid, DERUTF8String roleOfPspName, String normalizedRoleName) {
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
