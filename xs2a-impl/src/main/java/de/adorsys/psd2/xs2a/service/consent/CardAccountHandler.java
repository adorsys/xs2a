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

package de.adorsys.psd2.xs2a.service.consent;

import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCardAccountDetails;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardAccountHandler {

    @Value("${xs2a.masked-pan-begin-chars:6}")
    private int maskedPanBeginChars;

    @Value("${xs2a.masked-pan-end-chars:4}")
    private int maskedPanEndChars;

    public boolean areAccountsEqual(Xs2aCardAccountDetails aspspAccount, AccountReference tppAccount) {

        String aspspMaskedPan = aspspAccount.getMaskedPan();

        return isMaskedPanCorrespondsToValue(aspspMaskedPan, tppAccount.getMaskedPan()) ||
                   isMaskedPanCorrespondsToValue(aspspMaskedPan, tppAccount.getPan());
    }

    private boolean isMaskedPanCorrespondsToValue(String maskedPan, String comparableValue) {

        if (maskedPan == null || comparableValue == null) {
            return false;
        }

        String panPrefix = StringUtils.left(comparableValue, maskedPanBeginChars);
        String maskedPanPrefix = StringUtils.left(maskedPan, maskedPanBeginChars);

        String panSuffix = StringUtils.right(comparableValue, maskedPanEndChars);
        String maskedPanSuffix = StringUtils.right(maskedPan, maskedPanEndChars);

        return panPrefix.equals(maskedPanPrefix) && panSuffix.equals(maskedPanSuffix);
    }

    public String hidePanInAccountReference(String pan) {

        if (pan == null) {
            return null;
        }

        int numberOfMaskedChars = pan.length() - (maskedPanBeginChars + maskedPanEndChars);
        String maskedPartOfPan = StringUtils.repeat("*", numberOfMaskedChars);

        return StringUtils.overlay(pan, maskedPartOfPan, maskedPanBeginChars, pan.length() - maskedPanEndChars);
    }
}
