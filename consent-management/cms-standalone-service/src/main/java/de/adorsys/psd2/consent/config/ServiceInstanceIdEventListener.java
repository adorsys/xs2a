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

package de.adorsys.psd2.consent.config;

import de.adorsys.psd2.consent.domain.InstanceDependableEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ServiceInstanceIdEventListener implements PreInsertEventListener {
    private static final String SERVICE_INSTANCE_ID_PROPERTY = "instanceId";

    @Value("${xs2a.cms.service.instance-id:UNDEFINED}")
    private String serviceInstanceId;

    @Override
    public boolean onPreInsert(PreInsertEvent event) {
        Object object = event.getEntity();

        if (object instanceof InstanceDependableEntity) {
            InstanceDependableEntity entity = (InstanceDependableEntity) object;
            if (StringUtils.isBlank(entity.getInstanceId())) {
                String[] propertyNames = event.getPersister()
                                             .getEntityMetamodel()
                                             .getPropertyNames();
                Object[] currentState = event.getState();

                int serviceInstanceIdPropertyIndex = ArrayUtils.indexOf(propertyNames, SERVICE_INSTANCE_ID_PROPERTY);
                doUpdateServiceInstanceIdProperty(entity, currentState, serviceInstanceIdPropertyIndex);
            }
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
