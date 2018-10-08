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

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

public class PSD2QCType {
	private final RolesOfPSP rolesOfPSP;
	private final NCAName nCAName;
	private final NCAId nCAId;

	public PSD2QCType(RolesOfPSP rolesOfPSP, NCAName nCAName, NCAId nCAId) {
		this.rolesOfPSP = rolesOfPSP;
		this.nCAName = nCAName;
		this.nCAId = nCAId;
	}

	public static PSD2QCType getInstance(ASN1Encodable asn1Encodable) {
		ASN1Sequence sequence = ASN1Sequence.getInstance(asn1Encodable);
		RolesOfPSP rolesOfPSP = RolesOfPSP.getInstance(sequence.getObjectAt(0));
		NCAName nCAName = NCAName.getInstance(sequence.getObjectAt(1));
		NCAId nCAId = NCAId.getInstance(sequence.getObjectAt(2));
		return new PSD2QCType(rolesOfPSP, nCAName, nCAId);
	}
	
	public DERSequence toDERSequence(){
		return new DERSequence(new ASN1Encodable[] { rolesOfPSP.toDERSequence(), nCAName, nCAId });
	}

	public RolesOfPSP getRolesOfPSP() {
		return rolesOfPSP;
	}

	public NCAName getnCAName() {
		return nCAName;
	}

	public NCAId getnCAId() {
		return nCAId;
	}
}
