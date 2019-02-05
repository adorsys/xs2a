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

package de.adorsys.psd2.aspsp.profile.config;

import de.adorsys.psd2.aspsp.profile.domain.BookingStatus;
import de.adorsys.psd2.aspsp.profile.domain.SupportedAccountReferenceField;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;

import java.util.Collections;
import java.util.List;

import static de.adorsys.psd2.aspsp.profile.domain.BookingStatus.BOOKED;

@Data
public class ProfileConfiguration implements InitializingBean {
    private BankProfileSetting setting;

    @Override
    public void afterPropertiesSet() {
        setDefaultScaApproach(ScaApproach.REDIRECT);
        setDefaultBookingStatus(BOOKED);
        setAvailableAccountReferenceField(SupportedAccountReferenceField.IBAN); //Sets default Account Reference Field
    }

    private void setDefaultScaApproach(ScaApproach scaApproach) {
        if (CollectionUtils.isEmpty(setting.getScaApproaches())) {
            setting.setScaApproaches(Collections.singletonList(scaApproach));
        }
    }

    private void setAvailableAccountReferenceField(SupportedAccountReferenceField defaultSupportedAccountReferenceField) {
        List<SupportedAccountReferenceField> supportedAccountReferenceFields = setting.getSupportedAccountReferenceFields();
        if (!supportedAccountReferenceFields.contains(defaultSupportedAccountReferenceField)) {
            supportedAccountReferenceFields.add(defaultSupportedAccountReferenceField);
        }
    }

    private void setDefaultBookingStatus(BookingStatus necessaryStatus) {
        List<BookingStatus> availableBookingStatuses = setting.getAvailableBookingStatuses();
        if (!availableBookingStatuses.contains(necessaryStatus)) {
            availableBookingStatuses.add(necessaryStatus);
        }
    }
}
