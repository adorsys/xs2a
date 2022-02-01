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

package de.adorsys.psd2.xs2a.service.ais;

import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.ErrorToActionStatusMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiAccountReferenceMapper;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class AccountHelperService {

    private final Xs2aToSpiAccountReferenceMapper xs2aToSpiAccountReferenceMapper;
    private final ErrorToActionStatusMapper errorToActionStatusMapper;
    private final SpiContextDataProvider spiContextDataProvider;
    private final RequestProviderService requestProviderService;

    public SpiAccountReference findAccountReference(List<AccountReference> references, String resourceId) {
        return references.stream()
                   .filter(accountReference -> StringUtils.equals(accountReference.getResourceId(), resourceId))
                   .findFirst()
                   .map(xs2aToSpiAccountReferenceMapper::mapToSpiAccountReference)
                   .orElse(null);
    }

    public SpiContextData getSpiContextData() {
        PsuIdData psuIdData = requestProviderService.getPsuIdData();
        log.info("Corresponding PSU-ID {} was provided from request.", psuIdData);
        return spiContextDataProvider.provideWithPsuIdData(psuIdData);
    }

    ActionStatus createActionStatus(boolean withBalance, TypeAccess access, ResponseObject response) {
        return response.hasError()
                   ? errorToActionStatusMapper.mapActionStatusError(response.getError().getTppMessage().getMessageErrorCode(),
                                                                    withBalance, access)
                   : ActionStatus.SUCCESS;
    }

    boolean needsToUpdateUsage(AisConsent aisConsent) {
        return aisConsent.isOneAccessType() || requestProviderService.isRequestFromTPP();
    }
}
