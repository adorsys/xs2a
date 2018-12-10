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

import de.adorsys.psd2.consent.domain.BaseEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class BankInstanceIdEventListener implements PreInsertEventListener {
    private static final String DEFAULT_BANK_INSTANCE_ID = "UNDEFINED";
    private static final String INSTANCE_ID_PROPERTY = "instanceId";

    @Value("${bank.instance-id}")
    private String bankInstanceId;

    @PostConstruct
    public void initInitInstanceId() {
        bankInstanceId = StringUtils.isBlank(bankInstanceId)
                             ? DEFAULT_BANK_INSTANCE_ID
                             : bankInstanceId;
    }

    @Override
    public boolean onPreInsert(PreInsertEvent event) {
        Object object = event.getEntity();

        if (object instanceof BaseEntity) {
            BaseEntity entity = (BaseEntity) object;
            String[] propertyNames = event.getPersister()
                                         .getEntityMetamodel()
                                         .getPropertyNames();
            Object[] currentState = event.getState();

            setValue(currentState, propertyNames, bankInstanceId, entity);
        }
        return false;
    }

    private void setValue(Object[] currentState, String[] propertyNames, Object value, Object entity) {
        int index = ArrayUtils.indexOf(propertyNames, INSTANCE_ID_PROPERTY);
        if (index >= 0) {
            currentState[index] = value;
        } else {
            log.error("Field '" + INSTANCE_ID_PROPERTY + "' not found on entity '" + entity.getClass().getName() + "'.");
        }
    }
}
