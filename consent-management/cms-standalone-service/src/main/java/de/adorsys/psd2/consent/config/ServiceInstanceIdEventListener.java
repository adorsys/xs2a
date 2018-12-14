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

package de.adorsys.psd2.consent.config;

import de.adorsys.psd2.consent.domain.InstanceDependableEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ServiceInstanceIdEventListener implements PreInsertEventListener {
    private static final String SERVICE_INSTANCE_ID_PROPERTY = "instanceId";

    @Value("${cms.service.instance-id:UNDEFINED}")
    private String serviceInstanceId;

    @Override
    public boolean onPreInsert(PreInsertEvent event) {
        Object object = event.getEntity();

        if (object instanceof InstanceDependableEntity) {
            InstanceDependableEntity entity = (InstanceDependableEntity) object;
            String[] propertyNames = event.getPersister()
                                         .getEntityMetamodel()
                                         .getPropertyNames();
            Object[] currentState = event.getState();

            int serviceInstanceIdPropertyIndex = ArrayUtils.indexOf(propertyNames, SERVICE_INSTANCE_ID_PROPERTY);
            doUpdateServiceInstanceIdProperty(entity, currentState, serviceInstanceIdPropertyIndex);
        }
        return false;
    }

    private void doUpdateServiceInstanceIdProperty(InstanceDependableEntity entity, Object[] currentState, int serviceInstanceIdPropertyIndex) {
        if (serviceInstanceIdPropertyIndex >= 0) {
            currentState[serviceInstanceIdPropertyIndex] = serviceInstanceId;
        } else {
            log.error("Field '" + SERVICE_INSTANCE_ID_PROPERTY + "' not found on entity '" + entity.getClass().getName() + "'.");
        }
    }
}
