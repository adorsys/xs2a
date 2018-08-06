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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
        List<Boolean> list = supportedFields.stream()
                                 .map(f -> f.isValid(reference))
                                 .filter(Optional::isPresent)
                                 .map(Optional::get)
                                 .collect(Collectors.toList());
        return list.contains(true) && !list.contains(false);
    }
}
