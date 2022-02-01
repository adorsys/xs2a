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
