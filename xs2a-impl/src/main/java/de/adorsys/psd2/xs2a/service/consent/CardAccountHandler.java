/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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
