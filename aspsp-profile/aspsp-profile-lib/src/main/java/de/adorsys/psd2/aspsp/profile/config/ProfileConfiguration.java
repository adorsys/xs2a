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

import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.core.profile.StartAuthorisationMode;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static de.adorsys.psd2.xs2a.core.ais.BookingStatus.BOOKED;

@Data
public class ProfileConfiguration implements InitializingBean {
    private BankProfileSetting setting;

    public void setDefaultProperties() {
        if (setting.getCommon() != null && setting.getAis() != null) {
            setDefaultScaApproach(ScaApproach.REDIRECT);
            setDefaultBookingStatus(BOOKED);
            setDefaultScaRedirectFlow();
            setDefaultStartAuthorisationMode();
        }
    }

    @Override
    public void afterPropertiesSet() {
        setDefaultProperties();
    }

    private void setDefaultScaApproach(ScaApproach scaApproach) {
        if (CollectionUtils.isEmpty(setting.getCommon().getScaApproachesSupported())) {
            setting.getCommon().setScaApproachesSupported(Collections.singletonList(scaApproach));
        }
    }

    private void setDefaultScaRedirectFlow() {
        if (Objects.isNull(setting.getCommon().getScaRedirectFlow())) {
            setting.getCommon().setScaRedirectFlow(ScaRedirectFlow.REDIRECT);
        }
    }

    private void setDefaultStartAuthorisationMode() {
        if (Objects.isNull(setting.getCommon().getStartAuthorisationMode())) {
            setting.getCommon().setStartAuthorisationMode(StartAuthorisationMode.AUTO.getValue());
        }
    }

    private void setDefaultBookingStatus(BookingStatus necessaryStatus) {
        List<BookingStatus> availableBookingStatuses = setting.getAis().getTransactionParameters().getAvailableBookingStatuses();
        if (!availableBookingStatuses.contains(necessaryStatus)) {
            availableBookingStatuses.add(necessaryStatus);
        }
    }
}
