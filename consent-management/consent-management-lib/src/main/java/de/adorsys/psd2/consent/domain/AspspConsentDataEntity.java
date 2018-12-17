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

package de.adorsys.psd2.consent.domain;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

@Data
@Entity(name = "aspsp_consent_data")
@ApiModel(description = "Aspsp consent data", value = "AspspConsentData")
@NoArgsConstructor
public class AspspConsentDataEntity extends InstanceDependableEntity {
    @Id
    @Column(name = "consent_id")
    private String consentId;

    @Lob
    @Column(name = "data")
    private byte[] data;

    public AspspConsentDataEntity(String consentId) {
        this.consentId = consentId;
    }
}
