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
