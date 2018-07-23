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

package de.adorsys.aspsp.xs2a.config;

import de.adorsys.aspsp.xs2a.domain.BookingStatus;
import de.adorsys.aspsp.xs2a.domain.MulticurrencyAccountLevel;
import de.adorsys.aspsp.xs2a.domain.PaymentType;
import de.adorsys.aspsp.xs2a.domain.ScaApproach;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.PostConstruct;
import java.util.List;

import static de.adorsys.aspsp.xs2a.domain.BookingStatus.BOOKED;

@Data
@Configuration
@PropertySource("classpath:bank_profile.yml")
@ConfigurationProperties(prefix = "setting")
public class ProfileConfiguration {
    private final static boolean isDelayedPaymentTypeAllowedAlways = true;

    /**
     * This field indicates the requested maximum frequency for an access per day
     */
    private int frequencyPerDay;

    /**
     * If "true" indicates that a payment initiation service will be addressed in the same "session"
     */
    private boolean combinedServiceIndicator;

    /**
     * List of payment products supported by ASPSP
     */
    private List<String> availablePaymentProducts;

    /**
     * List of payment types supported by ASPSP
     */
    private List<String> availablePaymentTypes;

    /**
     * SCA Approach supported by ASPSP
     */
    private ScaApproach scaApproach;

    /**
     * A signature of the request by the TPP on application level.
     * If the value is `true`, the signature is mandated by ASPSP.
     * If the value is `false`, the signature can be omitted.
     */
    private boolean tppSignatureRequired;

    /**
     * URL to ASPSP service in order to to work with PIS
     */
    private String pisRedirectUrlToAspsp;

    /**
     * URL to ASPSP service in order to work with AIS
     */
    private String aisRedirectUrlToAspsp;

    /**
     * Multicurrency account types supported by ASPSP
     */
    private MulticurrencyAccountLevel multicurrencyAccountLevel;

    /**
     * Booking statuses supported by ASPSP, such as Booked, Pending and Both
     */
    private List<BookingStatus> availableBookingStatuses;

    @PostConstruct
    private void addDefaultValues() { //NOPMD It is necessary to set single payment and booked booking status available by default
        setDefaultPaymentType(PaymentType.FUTURE_DATED);
        setDefaultBookingStatus(BOOKED);
    }

    private void setDefaultPaymentType(PaymentType necessaryType) {
        if (!availablePaymentTypes.contains(necessaryType.getValue())) {
            availablePaymentTypes.add(necessaryType.getValue());
        }
    }

    private void setDefaultBookingStatus(BookingStatus necessaryStatus) {
        if (!availableBookingStatuses.contains(necessaryStatus)) {
            availableBookingStatuses.add(necessaryStatus);
        }
    }
}
