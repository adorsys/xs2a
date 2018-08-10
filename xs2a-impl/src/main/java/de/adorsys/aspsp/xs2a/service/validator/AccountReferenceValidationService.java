package de.adorsys.aspsp.xs2a.service.validator;

import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.account.SupportedAccountReferenceField;
import de.adorsys.aspsp.xs2a.exception.MessageCategory;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.AspspProfileService;
import javafx.util.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        Map<SupportedAccountReferenceField, Optional<Boolean>> validatedFieldsMap = supportedFields.stream()
                                                                                        .map(fld -> new Pair<>(fld, fld.isValid(reference)))
                                                                                        .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        return areValidAllFields(validatedFieldsMap, reference);
    }

    private boolean areValidAllFields(Map<SupportedAccountReferenceField, Optional<Boolean>> validatedFieldsMap, AccountReference reference) {

        List<SupportedAccountReferenceField> validFields = getFilteredFields(validatedFieldsMap, true);
        List<SupportedAccountReferenceField> invalidFields = getFilteredFields(validatedFieldsMap, false);

        boolean areValid = !validFields.isEmpty() && invalidFields.isEmpty();

        if (!areValid) {
            invalidFields.forEach(err -> log.warn("Field {} is not valid in {} reference: ", err, reference));
        }

        return areValid;
    }

    private List<SupportedAccountReferenceField> getFilteredFields(Map<SupportedAccountReferenceField, Optional<Boolean>> validatedFieldsMap, boolean isValidFilter) {
        return validatedFieldsMap.entrySet().stream()
                   .filter(etr -> etr.getValue().isPresent() && etr.getValue().get() == isValidFilter)
                   .map(Map.Entry::getKey)
                   .collect(Collectors.toList());
    }
}
