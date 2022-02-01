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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.core.domain.address.Xs2aAddress;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTrustedBeneficiaries;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiTrustedBeneficiaries;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiAddress;
import de.adorsys.xs2a.reader.JsonReader;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpiToXs2aTrustedBeneficiariesMapperTest {
    @InjectMocks
    private SpiToXs2aTrustedBeneficiariesMapper spiToXs2aTrustedBeneficiariesMapper;
    @Mock
    private SpiToXs2aAccountReferenceMapper accountReferenceMapper;
    @Mock
    private SpiToXs2aAddressMapper addressMapper;

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void mapToXs2aTrustedBeneficiaries() {
        // Given
        Xs2aTrustedBeneficiaries expected = jsonReader.getObjectFromFile("json/service/mapper/trusted-beneficiaries-model-mapper/xs2a-trusted-beneficiaries.json", Xs2aTrustedBeneficiaries.class);
        SpiTrustedBeneficiaries input = jsonReader.getObjectFromFile("json/service/mapper/trusted-beneficiaries-model-mapper/spi-trusted-beneficiaries.json", SpiTrustedBeneficiaries.class);
        SpiAccountReference spiDebtor = jsonReader.getObjectFromFile("json/service/mapper/trusted-beneficiaries-model-mapper/debtor-account.json", SpiAccountReference.class);
        SpiAccountReference spiCreditor = jsonReader.getObjectFromFile("json/service/mapper/trusted-beneficiaries-model-mapper/creditor-account.json", SpiAccountReference.class);
        AccountReference xs2aDebtor = jsonReader.getObjectFromFile("json/service/mapper/trusted-beneficiaries-model-mapper/debtor-account.json", AccountReference.class);
        AccountReference xs2aCreditor = jsonReader.getObjectFromFile("json/service/mapper/trusted-beneficiaries-model-mapper/creditor-account.json", AccountReference.class);
        SpiAddress spiAddress = jsonReader.getObjectFromFile("json/service/mapper/trusted-beneficiaries-model-mapper/creditor-address.json", SpiAddress.class);
        Xs2aAddress xs2aAddress = jsonReader.getObjectFromFile("json/service/mapper/trusted-beneficiaries-model-mapper/xs2a-address.json", Xs2aAddress.class);

        when(accountReferenceMapper.mapToXs2aAccountReference(spiDebtor)).thenReturn(xs2aDebtor);
        when(accountReferenceMapper.mapToXs2aAccountReference(spiCreditor)).thenReturn(xs2aCreditor);
        when(addressMapper.mapToAddress(spiAddress)).thenReturn(xs2aAddress);

        // When
        Xs2aTrustedBeneficiaries actual = spiToXs2aTrustedBeneficiariesMapper.mapToXs2aTrustedBeneficiaries(input);

        // Then
        assertThat(actual).isNotNull().isEqualTo(expected);
    }

    @Test
    void mapToXs2aTrustedBeneficiaries_null() {
        // When
        Xs2aTrustedBeneficiaries actual = spiToXs2aTrustedBeneficiariesMapper.mapToXs2aTrustedBeneficiaries(null);

        // Then
        assertThat(actual).isNull();
    }

    @Test
    void mapToXs2aTrustedBeneficiariesList() {
        // Given
        Xs2aTrustedBeneficiaries xs2aTrustedBeneficiaries = jsonReader.getObjectFromFile("json/service/mapper/trusted-beneficiaries-model-mapper/xs2a-trusted-beneficiaries.json", Xs2aTrustedBeneficiaries.class);
        List<Xs2aTrustedBeneficiaries> expected = Collections.singletonList(xs2aTrustedBeneficiaries);

        SpiTrustedBeneficiaries spiTrustedBeneficiaries = jsonReader.getObjectFromFile("json/service/mapper/trusted-beneficiaries-model-mapper/spi-trusted-beneficiaries.json", SpiTrustedBeneficiaries.class);

        SpiAccountReference spiDebtor = jsonReader.getObjectFromFile("json/service/mapper/trusted-beneficiaries-model-mapper/debtor-account.json", SpiAccountReference.class);
        SpiAccountReference spiCreditor = jsonReader.getObjectFromFile("json/service/mapper/trusted-beneficiaries-model-mapper/creditor-account.json", SpiAccountReference.class);
        AccountReference xs2aDebtor = jsonReader.getObjectFromFile("json/service/mapper/trusted-beneficiaries-model-mapper/debtor-account.json", AccountReference.class);
        AccountReference xs2aCreditor = jsonReader.getObjectFromFile("json/service/mapper/trusted-beneficiaries-model-mapper/creditor-account.json", AccountReference.class);
        SpiAddress spiAddress = jsonReader.getObjectFromFile("json/service/mapper/trusted-beneficiaries-model-mapper/creditor-address.json", SpiAddress.class);
        Xs2aAddress xs2aAddress = jsonReader.getObjectFromFile("json/service/mapper/trusted-beneficiaries-model-mapper/xs2a-address.json", Xs2aAddress.class);

        when(accountReferenceMapper.mapToXs2aAccountReference(spiDebtor)).thenReturn(xs2aDebtor);
        when(accountReferenceMapper.mapToXs2aAccountReference(spiCreditor)).thenReturn(xs2aCreditor);
        when(addressMapper.mapToAddress(spiAddress)).thenReturn(xs2aAddress);

        // When
        List<Xs2aTrustedBeneficiaries> actual = spiToXs2aTrustedBeneficiariesMapper.mapToXs2aTrustedBeneficiariesList(Collections.singletonList(spiTrustedBeneficiaries));

        // Then
        assertThat(CollectionUtils.isNotEmpty(actual)).isTrue();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void mapToXs2aTrustedBeneficiariesList_empty() {
        // When
        List<Xs2aTrustedBeneficiaries> actual = spiToXs2aTrustedBeneficiariesMapper.mapToXs2aTrustedBeneficiariesList(Collections.emptyList());

        // Then
        assertThat(CollectionUtils.isEmpty(actual)).isTrue();
    }
}
