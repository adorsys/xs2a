/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.report.entity;

import lombok.Data;

import java.time.OffsetDateTime;


@Data
public class EventEntityForReport {
    private Long id;
    private OffsetDateTime timestamp;
    private String consentId;
    private String paymentId;
    private byte[] payload;
    private String eventOrigin;
    private String eventType;
    private String instanceId;
    private String psuId;
    private String psuIdType;
    private String psuCorporateId;
    private String psuCorporateIdType;
    private String tppAuthorisationNumber;
    private String internalRequestId;
    private String xRequestId;
    private String psuExId;
    private String psuExIdType;
    private String psuExCorporateId;
    private String psuExCorporateIdType;
}
