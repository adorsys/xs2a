/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.web.validator.body.consent;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.model.AccountAccess;
import de.adorsys.psd2.model.AccountReference;
import de.adorsys.psd2.model.Consents;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.AbstractBodyValidatorImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.IBANValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AccountAccessValidatorImpl extends AbstractBodyValidatorImpl implements ConsentBodyValidator {

    @Autowired
    public AccountAccessValidatorImpl(ErrorBuildingService errorBuildingService, ObjectMapper objectMapper) {
        super(errorBuildingService, objectMapper);
    }

    @Override
    public void validate(HttpServletRequest request, MessageError messageError) {

        Optional<Consents> consentsOptional = mapBodyToInstance(request, messageError, Consents.class);

        // In case of wrong JSON - we don't proceed the inner fields validation.
        if (!consentsOptional.isPresent()) {
            return;
        }

        Consents consents = consentsOptional.get();

        if (Objects.isNull(consents.getAccess())) {
            errorBuildingService.enrichMessageError(messageError, "Value 'access' should not be null");
        } else {
            validateAccountAccess(consents, messageError);
        }
    }

    private void validateAccountAccess(Consents consents, MessageError messageError) {
        AccountAccess accountAccess = consents.getAccess();
        if (Objects.nonNull(accountAccess.getAccounts())) {
            List<AccountReference> accountAccesses = accountAccess.getAccounts();
            accountAccesses.addAll(accountAccess.getBalances());
            accountAccesses.addAll(accountAccess.getTransactions());

            if (CollectionUtils.isNotEmpty(accountAccesses)) {
                accountAccesses.forEach(ar -> validateAccountReference(ar, messageError));
            }

            CreateConsentReq createConsent = mapToCreateConsentReq(consents, messageError);

            if (areFlagsAndAccountsInvalid(createConsent)) {
                errorBuildingService.enrichMessageError(messageError, "Consent object can not contain both list of accounts and the flag allPsd2 or availableAccounts");
            }
        }
    }

    private boolean areFlagsAndAccountsInvalid(CreateConsentReq request) {
        Xs2aAccountAccess access = request.getAccess();
        if (access.isNotEmpty()) {
            return !(CollectionUtils.isEmpty(request.getAccountReferences()) || areFlagsEmpty(access));
        }
        return false;
    }

    private boolean areFlagsEmpty(Xs2aAccountAccess access) {
        return Objects.isNull(access.getAvailableAccounts()) && Objects.isNull(access.getAllPsd2());
    }

    private void validateAccountReference(AccountReference accountReference, MessageError messageError) {
        if (StringUtils.isNotBlank(accountReference.getIban()) && !isValidIban(accountReference.getIban())) {
            errorBuildingService.enrichMessageError(messageError, "Invalid IBAN format");
        }
        if (StringUtils.isNotBlank(accountReference.getBban()) && !isValidBban(accountReference.getBban())) {
            errorBuildingService.enrichMessageError(messageError, "Invalid BBAN format");
        }
        checkOptionalFieldForMaxLength(accountReference.getPan(), "PAN", 35, messageError);
        checkOptionalFieldForMaxLength(accountReference.getMaskedPan(), "Masked PAN", 35, messageError);
        checkOptionalFieldForMaxLength(accountReference.getMsisdn(), "MSISDN", 35, messageError);

        validateCurrency(accountReference, messageError);
    }

    private void validateCurrency(AccountReference accountReference, MessageError messageError) {
        if (!isValidCurrency(accountReference.getCurrency())) {
            errorBuildingService.enrichMessageError(messageError, "Invalid currency code format");
        }
    }

    private boolean isValidIban(String iban) {
        IBANValidator validator = IBANValidator.getInstance();
        return validator.isValid(normalizeString(iban));
    }

    private boolean isValidBban(String bban) {
        return normalizeString(bban).length() >= 11
                   && normalizeString(bban).length() <= 28; // Can be extended with aprox 50 country specific masks
    }

    private boolean isValidCurrency(String currency) {
        try {
            Currency.getInstance(currency);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    private String normalizeString(String string) {
        return string.replaceAll("[^a-zA-Z0-9]", "");
    }

    private CreateConsentReq mapToCreateConsentReq(Consents consent, MessageError messageError) {
        return Optional.ofNullable(consent)
                   .map(cnst -> {
                       CreateConsentReq createAisConsentRequest = new CreateConsentReq();
                       createAisConsentRequest.setAccess(mapToAccountAccessInner(cnst.getAccess(), messageError));
                       return createAisConsentRequest;
                   })
                   .orElse(null);
    }

    private Xs2aAccountAccess mapToAccountAccessInner(AccountAccess accountAccess, MessageError messageError) {
        return Optional.ofNullable(accountAccess)
                   .map(acs ->
                            new Xs2aAccountAccess(
                                mapToXs2aAccountReferences(acs.getAccounts(), messageError),
                                mapToXs2aAccountReferences(acs.getBalances(), messageError),
                                mapToXs2aAccountReferences(acs.getTransactions(), messageError),
                                mapToAccountAccessTypeFromAvailableAccounts(acs.getAvailableAccounts()),
                                mapToAccountAccessTypeFromAllPsd2Enum(acs.getAllPsd2())
                            ))
                   .orElse(null);
    }

    private List<de.adorsys.psd2.xs2a.core.profile.AccountReference> mapToXs2aAccountReferences(List<de.adorsys.psd2.model.AccountReference> references, MessageError messageError) { // NOPMD
        return Optional.ofNullable(references)
                   .map(ref -> ref.stream()
                                   .map((AccountReference reference) -> mapToAccountReference(reference, messageError))
                                   .collect(Collectors.toList()))
                   .orElseGet(Collections::emptyList);
    }

    private AccountAccessType mapToAccountAccessTypeFromAvailableAccounts(AccountAccess.AvailableAccountsEnum accountsEnum) {
        return Optional.ofNullable(accountsEnum)
                   .flatMap(en -> AccountAccessType.getByDescription(en.toString()))
                   .orElse(null);
    }

    private AccountAccessType mapToAccountAccessTypeFromAllPsd2Enum(AccountAccess.AllPsd2Enum allPsd2Enum) {
        return Optional.ofNullable(allPsd2Enum)
                   .flatMap(en -> AccountAccessType.getByDescription(en.toString()))
                   .orElse(null);
    }

    private de.adorsys.psd2.xs2a.core.profile.AccountReference mapToAccountReference(Object reference, MessageError messageError) {
        try {
            return objectMapper.convertValue(reference, de.adorsys.psd2.xs2a.core.profile.AccountReference.class);
        } catch (IllegalArgumentException e) {
            // Happens only during Currency field processing, as other fields are of String type.
            errorBuildingService.enrichMessageError(messageError, "Invalid currency code format");
            return null;
        }
    }
}
