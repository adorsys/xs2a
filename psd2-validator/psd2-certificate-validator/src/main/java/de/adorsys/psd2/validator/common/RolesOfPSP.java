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

import java.util.Arrays;

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

        ASN1Encodable[] array = ASN1Sequence.getInstance(obj).toArray();

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
