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

package de.adorsys.psd2.xs2a.domain.pis;

import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.AccountReferenceCollector;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
public class BulkPayment extends CommonPayment implements AccountReferenceCollector {

    @NotNull
    private AccountReference debtorAccount;
    private String debtorName;
    private LocalDate requestedExecutionDate;
    private OffsetDateTime requestedExecutionTime;
    private List<SinglePayment> payments;
    private Boolean batchBookingPreferred;

    @Override
    public Set<AccountReference> getAccountReferences() {
        Set<AccountReference> accountReferences = payments.stream()
                                                      .map(SinglePayment::getAccountReferences)
                                                      .flatMap(Set::stream)
                                                      .collect(Collectors.toSet());
        accountReferences.add(debtorAccount);

        return accountReferences;
    }

    @Override
    public PaymentType getPaymentType() {
        return PaymentType.BULK;
    }
}
