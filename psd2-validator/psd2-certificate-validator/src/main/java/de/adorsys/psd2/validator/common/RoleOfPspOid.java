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
