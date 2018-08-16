package de.adorsys.aspsp.xs2a.service.validator;

import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.account.SupportedAccountReferenceField;
import de.adorsys.aspsp.xs2a.service.AccountReferenceValidationService;
import de.adorsys.aspsp.xs2a.service.AspspProfileService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccountReferenceValidationServiceTest {
    private final String IBAN = "DE 8937 0400 4405 3201 3000";
    private final String WRONG_IBAN = "123456789";
    private final String BBAN = "3704 0044 0532 0130 00";
    private final String PAN = "1111 1111 1111 1111";
    private final String MASKED_PAN = "1234*********1234";
    private final String MSISDN = "919961345678";
    private final String WRONG_MSISDN = "123A";

    @InjectMocks
    private AccountReferenceValidationService validationService;
    @Mock
    private AspspProfileService profileService;

    @Before
    public void setUpAccountServiceMock() {
        when(profileService.getSupportedAccountReferenceFields()).thenReturn(Arrays.asList(SupportedAccountReferenceField.IBAN, SupportedAccountReferenceField.BBAN));
    }

    @Test
    public void validateAccountReferences_Success() {
        //Given:
        Set<AccountReference> references = new HashSet<>(Arrays.asList(getReference(IBAN, BBAN, PAN, MASKED_PAN, MSISDN), getReference(null, BBAN, null, null, WRONG_MSISDN)));
        //When:
        ResponseObject error = validationService.validateAccountReferences(references);
        //Then:
        assertThat(error.hasError()).isFalse();
    }

    @Test
    public void validateAccountReferences_Failure_Not_in_ASPSP_profile() {
        //Given:
        Set<AccountReference> references = new HashSet<>(Arrays.asList(getReference(null, null, PAN, MASKED_PAN, MSISDN), getReference(null, BBAN, null, null, WRONG_MSISDN)));
        //When:
        ResponseObject error = validationService.validateAccountReferences(references);
        //Then:
        assertThat(error.hasError()).isTrue();
    }

    @Test
    public void validateAccountReferences_Failure_wrong_iban() {
        //Given:
        Set<AccountReference> references = new HashSet<>(Arrays.asList(getReference(WRONG_IBAN, null, null, null, null), getReference(null, BBAN, null, null, null)));
        //When:
        ResponseObject error = validationService.validateAccountReferences(references);
        //Then:
        assertThat(error.hasError()).isTrue();
    }

    private AccountReference getReference(String iban, String bban, String pan, String masked, String msisdn) {
        AccountReference reference = new AccountReference();
        reference.setIban(iban);
        reference.setBban(bban);
        reference.setPan(pan);
        reference.setMaskedPan(masked);
        reference.setMsisdn(msisdn);
        return reference;
    }
}
