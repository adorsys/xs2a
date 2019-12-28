/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
