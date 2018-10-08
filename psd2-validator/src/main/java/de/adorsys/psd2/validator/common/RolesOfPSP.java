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
