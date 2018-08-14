package de.adorsys.aspsp.xs2a.service.validator;

import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.account.SupportedAccountReferenceField;
import de.adorsys.aspsp.xs2a.exception.MessageCategory;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.AspspProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountReferenceValidationService {
    private final AspspProfileService profileService;

    public Optional<MessageError> validateAccountReferences(Set<AccountReference> references) {
        List<SupportedAccountReferenceField> supportedFields = profileService.getSupportedAccountReferenceFields();

        boolean isInvalidReferenceSet = references.stream()
                                            .map(ar -> isValidAccountReference(ar, supportedFields))
                                            .anyMatch(Predicate.isEqual(false));

        return isInvalidReferenceSet
                   ? Optional.of(new MessageError(TransactionStatus.RJCT, new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.FORMAT_ERROR)))
                   : Optional.empty();
    }

    private boolean isValidAccountReference(AccountReference reference, List<SupportedAccountReferenceField> supportedFields) {
        Map<SupportedAccountReferenceField, Boolean> validatedFieldsMap = supportedFields.stream()
                                                                              .map(fld -> Pair.of(fld, fld.isValid(reference)))
                                                                              .filter(p -> p.getValue().isPresent())
                                                                              .collect(Collectors.toMap(Pair::getKey, p -> p.getValue().get()));
        return areValidAllFields(validatedFieldsMap, reference);
    }

    private boolean areValidAllFields(Map<SupportedAccountReferenceField, Boolean> validatedFieldsMap, AccountReference reference) {

        List<SupportedAccountReferenceField> validFields = getFilteredFields(validatedFieldsMap, true);
        List<SupportedAccountReferenceField> invalidFields = getFilteredFields(validatedFieldsMap, false);

        boolean areValid = !validFields.isEmpty() && invalidFields.isEmpty();

        if (!areValid) {
            invalidFields.forEach(err -> log.warn("Field {} is not valid in reference: {}", err, reference));
        }

        return areValid;
    }

    private List<SupportedAccountReferenceField> getFilteredFields(Map<SupportedAccountReferenceField, Boolean> validatedFieldsMap, boolean isValidFilter) {
        return validatedFieldsMap.entrySet().stream()
                   .filter(etr -> etr.getValue() == isValidFilter)
                   .map(Map.Entry::getKey)
                   .collect(Collectors.toList());
    }
}
