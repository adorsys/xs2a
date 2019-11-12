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

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.AccountAccess;
import de.adorsys.psd2.model.AccountReference;
import de.adorsys.psd2.model.Consents;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.AbstractBodyValidatorImpl;
import de.adorsys.psd2.xs2a.web.validator.body.AccountReferenceValidator;
import de.adorsys.psd2.xs2a.web.validator.body.DateFieldValidator;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aRequestBodyDateFields.AIS_CONSENT_DATE_FIELDS;

@Component
public class AccountAccessValidatorImpl extends AbstractBodyValidatorImpl implements ConsentBodyValidator {

    private final AccountReferenceValidator accountReferenceValidator;

    private DateFieldValidator dateFieldValidator;

    @Autowired
    public AccountAccessValidatorImpl(ErrorBuildingService errorBuildingService, Xs2aObjectMapper xs2aObjectMapper, AccountReferenceValidator accountReferenceValidator, DateFieldValidator dateFieldValidator) {
        super(errorBuildingService, xs2aObjectMapper);
        this.accountReferenceValidator = accountReferenceValidator;
        this.dateFieldValidator = dateFieldValidator;
    }

    @Override
    public MessageError validateBodyFields(HttpServletRequest request, MessageError messageError) {

        Optional<Consents> consentsOptional = mapBodyToInstance(request, messageError, Consents.class);

        // In case of wrong JSON - we don't proceed to the inner fields validation.
        if (!consentsOptional.isPresent()) {
            return messageError;
        }

        Consents consents = consentsOptional.get();

        if (consents.getAccess() == null) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE, "access"));
        } else {
            validateAccountAccess(consents, messageError);
        }

        return messageError;
    }

    @Override
    public MessageError validateRawData(HttpServletRequest request, MessageError messageError) {
        return dateFieldValidator.validateDateFormat(request, AIS_CONSENT_DATE_FIELDS.getDateFields(), messageError);
    }

    private void validateAccountAccess(Consents consents, MessageError messageError) {
        AccountAccess accountAccess = consents.getAccess();

        if (accountAccess.getAccounts() != null) {

            Stream<AccountReference> allReferences = Stream.of(accountAccess.getAccounts(), accountAccess.getBalances(), accountAccess.getTransactions())
                                                         .filter(Objects::nonNull)
                                                         .flatMap(Collection::stream);

            Stream<AccountReference> additionalReferences = Optional.ofNullable(accountAccess.getAdditionalAccountInformation())
                                                                .map(info -> Stream.of(info.getOwnerName(), info.getOwnerAddress())
                                                                                 .filter(Objects::nonNull)
                                                                                 .flatMap(Collection::stream))
                                                                .orElseGet(Stream::empty);

            Stream.concat(allReferences, additionalReferences)
                .distinct()
                .filter(Objects::nonNull)
                .forEach(ar -> accountReferenceValidator.validate(ar, messageError));

            CreateConsentReq createConsent = mapToCreateConsentReq(consents, messageError);

            // This object is checked for null on the level above
            if (areFlagsAndAccountsInvalid(createConsent)) { //NOSONAR
                errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_CONSENT_INCORRECT));
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
                                mapToAccountAccessTypeFromAllPsd2Enum(acs.getAllPsd2()),
                                mapToAccountAccessTypeFromAvailableAccountsWithBalance(acs.getAvailableAccountsWithBalance()),
                                mapToAdditionalInformationAccess(acs.getAdditionalAccountInformation(), messageError)
                            ))
                   .orElse(null);
    }

    private AdditionalInformationAccess mapToAdditionalInformationAccess(de.adorsys.psd2.model.AdditionalInformationAccess additionalInformationAccess, MessageError messageError) {
        if (additionalInformationAccess == null) {
            return null;
        }

        return new AdditionalInformationAccess(
            mapToXs2aAccountReferences(additionalInformationAccess.getOwnerName(), messageError),
            mapToXs2aAccountReferences(additionalInformationAccess.getOwnerAddress(), messageError));
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

    private AccountAccessType mapToAccountAccessTypeFromAvailableAccountsWithBalance(AccountAccess.AvailableAccountsWithBalanceEnum accountsEnum) {
        return Optional.ofNullable(accountsEnum)
                   .flatMap(en -> AccountAccessType.getByDescription(en.toString()))
                   .orElse(null);
    }

    private de.adorsys.psd2.xs2a.core.profile.AccountReference mapToAccountReference(Object reference, MessageError messageError) {
        try {
            return xs2aObjectMapper.convertValue(reference, de.adorsys.psd2.xs2a.core.profile.AccountReference.class);
        } catch (IllegalArgumentException e) {
            // Happens only during Currency field processing, as other fields are of String type.
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_WRONG_FORMAT_VALUE, "currency"));
            return null;
        }
    }
}
