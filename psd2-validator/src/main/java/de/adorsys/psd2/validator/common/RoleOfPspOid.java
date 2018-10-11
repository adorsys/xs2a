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
