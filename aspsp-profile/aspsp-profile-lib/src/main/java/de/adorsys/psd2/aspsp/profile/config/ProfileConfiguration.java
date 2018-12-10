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
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static de.adorsys.psd2.aspsp.profile.domain.BookingStatus.BOOKED;

@Data
@Configuration
public class ProfileConfiguration implements InitializingBean {
    private BankProfileSetting setting;

    @Override
    public void afterPropertiesSet() {
        setDefaultPaymentType(PaymentType.SINGLE);
        setDefaultBookingStatus(BOOKED);
        setAvailableAccountReferenceField(SupportedAccountReferenceField.IBAN); //Sets default Account Reference Field
    }

    private void setAvailableAccountReferenceField(SupportedAccountReferenceField defaultSupportedAccountReferenceField) {
        List<SupportedAccountReferenceField> supportedAccountReferenceFields = setting.getSupportedAccountReferenceFields();
        if (!supportedAccountReferenceFields.contains(defaultSupportedAccountReferenceField)) {
            supportedAccountReferenceFields.add(defaultSupportedAccountReferenceField);
        }
    }

    private void setDefaultPaymentType(PaymentType necessaryType) {
        List<PaymentType> availablePaymentTypes = setting.getAvailablePaymentTypes();
        if (!availablePaymentTypes.contains(necessaryType)) {
            availablePaymentTypes.add(necessaryType);
        }
    }

    private void setDefaultBookingStatus(BookingStatus necessaryStatus) {
        List<BookingStatus> availableBookingStatuses = setting.getAvailableBookingStatuses();
        if (!availableBookingStatuses.contains(necessaryStatus)) {
            availableBookingStatuses.add(necessaryStatus);
        }
    }
}
