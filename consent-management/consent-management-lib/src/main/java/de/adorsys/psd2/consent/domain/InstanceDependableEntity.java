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

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@Getter
@Setter
@MappedSuperclass
public abstract class InstanceDependableEntity {
    // TODO Create integration tests in CMS to verify whether instanceId is set https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/690
    private static final String DEFAULT_SERVICE_INSTANCE_ID = "UNDEFINED";

    @Column(name = "instance_id", nullable = false, updatable = false)
    private String instanceId = DEFAULT_SERVICE_INSTANCE_ID;
}
