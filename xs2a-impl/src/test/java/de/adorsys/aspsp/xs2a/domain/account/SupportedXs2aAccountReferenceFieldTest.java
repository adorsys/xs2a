package de.adorsys.aspsp.xs2a.domain.account;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class SupportedXs2aAccountReferenceFieldTest {
    private final String IBAN = "DE 8937 0400 4405 3201 3000";
    private final String WRONG_IBAN = "123456789";
    private final String BBAN = "3704 0044 0532-0130 0000 0000";
    private final String WRONG_BBAN = "0000-0000-0";
    private final String PAN = "5169 3305-1488 9218";
    private final String WRONG_PAN = "0000 0000 0000 0000";
    private final String MASKED_PAN = "1234 - **** **** - 1234";
    private final String WRONG_MASKED_PAN = "******";
    private final List<String> MSISDN = getMsisdns();
    private final String WRONG_MSISDN = "123A";

    @Test
    public void isValidIBAN_Success() {
        //When:
        Optional<Boolean> result = SupportedAccountReferenceField.IBAN.isValid(getReference(IBAN, null, null, null, null));
        //Then:
        successTest(result);
    }

    @Test
    public void isValidIBAN_Fail_wrong() {
        //When:
        Optional<Boolean> result = SupportedAccountReferenceField.IBAN.isValid(getReference(WRONG_IBAN, null, null, null, null));
        //Then:
        failWrong(result);
    }

    @Test
    public void isValidIBAN_Fail_null() {
        //When:
        Optional<Boolean> valid = SupportedAccountReferenceField.IBAN.isValid(getReference(null, null, null, null, null));
        //Then:
        assertThat(valid.isPresent()).isFalse();
    }

    @Test
    public void isValidBBAN_Success() {
        //When:
        Optional<Boolean> result = SupportedAccountReferenceField.BBAN.isValid(getReference(null, BBAN, null, null, null));
        //Then:
        successTest(result);
    }

    @Test
    public void isValidBBAN_Fail_wrong() {
        //When:
        Optional<Boolean> result = SupportedAccountReferenceField.BBAN.isValid(getReference(null, WRONG_BBAN, null, null, null));
        //Then:
        failWrong(result);
    }

    @Test
    public void isValidBBAN_Fail_null() {
        //When:
        Optional<Boolean> result = SupportedAccountReferenceField.BBAN.isValid(getReference(null, null, null, null, null));
        //Then:
        assertThat(result.isPresent()).isFalse();
    }

    @Test
    public void isValidPAN_Success() {
        //When:
        Optional<Boolean> result = SupportedAccountReferenceField.PAN.isValid(getReference(null, null, PAN, null, null));
        //Then:
        successTest(result);
    }

    @Test
    public void isValidPAN_Fail_wrong() {
        //When:
        Optional<Boolean> result = SupportedAccountReferenceField.PAN.isValid(getReference(null, null, WRONG_PAN, null, null));
        //Then:
        failWrong(result);
    }

    @Test
    public void isValidPAN_Fail_null() {
        //When:
        Optional<Boolean> result = SupportedAccountReferenceField.PAN.isValid(getReference(null, null, null, null, null));
        //Then:
        assertThat(result.isPresent()).isFalse();
    }

    @Test
    public void isValidMaskedPAN_Success() {
        //When:
        Optional<Boolean> result = SupportedAccountReferenceField.MASKEDPAN.isValid(getReference(null, null, null, MASKED_PAN, null));
        //Then:
        successTest(result);
    }

    @Test
    public void isValidMaskedPAN_Fail_wrong() {
        //When:
        Optional<Boolean> result = SupportedAccountReferenceField.MASKEDPAN.isValid(getReference(null, null, null, WRONG_MASKED_PAN, null));
        //Then:
        failWrong(result);
    }

    @Test
    public void isValidMaskedPAN_Fail_null() {
        //When:
        Optional<Boolean> result = SupportedAccountReferenceField.MASKEDPAN.isValid(getReference(null, null, null, null, null));
        //Then:
        assertThat(result.isPresent()).isFalse();
    }

    @Test
    public void isValidMSISDN_Success() {
        for (String s : MSISDN) {
            //When:
            Optional<Boolean> result = SupportedAccountReferenceField.MSISDN.isValid(getReference(null, null, null, null, s));
            //Then:
            successTest(result);
        }
    }

    @Test
    public void isValidMSISDN_Fail_wrong() {
        //When:
        Optional<Boolean> result = SupportedAccountReferenceField.MSISDN.isValid(getReference(null, null, null, null, WRONG_MSISDN));
        //Then:
        failWrong(result);
    }

    @Test
    public void isValidMSISDN_Fail_null() {
        //When:
        Optional<Boolean> result = SupportedAccountReferenceField.MSISDN.isValid(getReference(null, null, null, null, null));
        //Then:
        assertThat(result.isPresent()).isFalse();
    }

    private void successTest(Optional<Boolean> result) {
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get()).isTrue();
    }

    private void failWrong(Optional<Boolean> result) {
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get()).isFalse();
    }

    private Xs2aAccountReference getReference(String iban, String bban, String pan, String masked, String msisdn) {
        Xs2aAccountReference reference = new Xs2aAccountReference();
        reference.setIban(iban);
        reference.setBban(bban);
        reference.setPan(pan);
        reference.setMaskedPan(masked);
        reference.setMsisdn(msisdn);
        return reference;
    }

    private List<String> getMsisdns() {
        return Arrays.asList(
            "+380 (67) 422 22 22",
            "+7 495 721-91-00", //Raiffeisen bank RU
            "+44 20 7933 8000", //Raiffeisen UK
            "+359 2 9198 5101", //Raiffeisen Bulgaria
            "+43-1-71707-0", //Raiffeisen Austria
            "+38(067)422-22-22",
            "+49(0)911 360698-0", //Adorsys GmbH
            "00 49 69 91010039", //Deutsche bank
            "00 1 315-724-4022" //Bank of America
        );
    }
}
