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

package de.adorsys.aspsp.xs2a.domain;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "CashAccountType", value = "Cash Account Type")
public enum CashAccountType {
    // TODO documentation doesn't have any definition. hhttps://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/45
	// Berlin Group mentions this should be ExternalCashAccountType1Code from ISO 20022, therefore values from 27-CashAccountType in 
	// ExternalCodeSets_1Q2018_May2018_v1 or newer (see https://www.iso20022.org/external_code_list.page) looks best.
	// The enum would either be CURRENT_ACCOUNT, etc. as below or use the ISO codes ("CACC" in this case) directly.
    CURRENT_ACCOUNT
}
