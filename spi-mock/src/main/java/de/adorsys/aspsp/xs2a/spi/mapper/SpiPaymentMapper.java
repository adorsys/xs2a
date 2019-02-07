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

package de.adorsys.aspsp.xs2a.spi.mapper;

import de.adorsys.psd2.aspsp.mock.api.account.AspspAccountReference;
import de.adorsys.psd2.aspsp.mock.api.common.AspspAmount;
import de.adorsys.psd2.aspsp.mock.api.common.AspspTransactionStatus;
import de.adorsys.psd2.aspsp.mock.api.payment.AspspAddress;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiAddress;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SpiPaymentMapper {

    @Nullable AspspTransactionStatus mapToAspspTransactionStatus(TransactionStatus transactionStatus) {
        return Optional.ofNullable(transactionStatus)
            .map(t -> AspspTransactionStatus.valueOf(t.name()))
            .orElse(null);
    }

    public @Nullable TransactionStatus mapToTransactionStatus(AspspTransactionStatus aspspTransactionStatus) {
        return Optional.ofNullable(aspspTransactionStatus)
            .map(t -> TransactionStatus.valueOf(t.name()))
            .orElse(null);
    }

    AspspAmount mapToAspspAmount(@NotNull SpiAmount spiAmount) {
        return new AspspAmount(spiAmount.getCurrency(), spiAmount.getAmount());
    }

    @Nullable AspspAddress mapToAspspAddress(SpiAddress spiAddress) {
        return Optional.ofNullable(spiAddress)
            .map(s -> new AspspAddress(s.getStreet(), s.getBuildingNumber(), s.getCity(), s.getPostalCode(),
                s.getCountry()))
            .orElse(null);
    }

    AspspAccountReference mapToAspspAccountReference(@NotNull SpiAccountReference spiAccountReference) {
        return new AspspAccountReference(spiAccountReference.getResourceId(),
            spiAccountReference.getIban(),
            spiAccountReference.getBban(),
            spiAccountReference.getPan(),
            spiAccountReference.getMaskedPan(),
            spiAccountReference.getMsisdn(),
            spiAccountReference.getCurrency());
    }

    SpiAccountReference mapToSpiAccountReference(@NotNull AspspAccountReference aspspAccountReference) {
        return new SpiAccountReference(aspspAccountReference.getAccountId(),
            aspspAccountReference.getIban(),
            aspspAccountReference.getBban(),
            aspspAccountReference.getPan(),
            aspspAccountReference.getMaskedPan(),
            aspspAccountReference.getMsisdn(),
            aspspAccountReference.getCurrency());
    }

    SpiAmount mapToSpiAmount(@NotNull AspspAmount aspspAmount) {
        return new SpiAmount(aspspAmount.getCurrency(), aspspAmount.getAmount());
    }

    @Nullable SpiAddress mapToSpiAddress(AspspAddress aspspAddress) {
        return Optional.ofNullable(aspspAddress)
            .map(a -> new SpiAddress(a.getStreet(), a.getBuildingNumber(), a.getCity(), a.getPostalCode(),
                a.getCountry()))
            .orElse(null);
    }
}

