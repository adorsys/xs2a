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

package de.adorsys.psd2.core.data.piis.v1;

import de.adorsys.psd2.core.data.Consent;
import de.adorsys.psd2.core.data.piis.PiisConsentData;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

public class PiisConsent extends Consent<PiisConsentData> {

    public PiisConsent() {
    }

    public PiisConsent(ConsentType consentType) {
        setConsentType(consentType);
    }

    public AccountReference getAccountReference() {
        List<AccountReference> accounts = getAspspAccountAccesses().getAccounts();

        if (CollectionUtils.isNotEmpty(accounts)) {
            return accounts.get(0);
        }

        List<AccountReference> tppAccounts = getTppAccountAccesses().getAccounts();
        if (CollectionUtils.isNotEmpty(tppAccounts)) {
            return tppAccounts.get(0);
        }

        return null;
    }

    public PsuIdData getPsuIdData() {
        List<PsuIdData> psuIdDataList = getPsuIdDataList();

        if (CollectionUtils.isNotEmpty(psuIdDataList)) {
            return psuIdDataList.get(0);
        }

        return null;
    }
}
