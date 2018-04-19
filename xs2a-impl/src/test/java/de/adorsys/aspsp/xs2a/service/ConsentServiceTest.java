package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.domain.ais.consent.*;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import de.adorsys.aspsp.xs2a.spi.impl.ConsentSpiImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ConsentServiceTest {
    private final String CORRECT_PSU_ID = "123456789";
    private final String WRONG_PSU_ID = "987654321";
    private final String CORRECT_IBAN = "DE123456789";
    private final String WRONG_IBAN = "DE987654321";
    private final Currency CURRENCY = Currency.getInstance("EUR");

    @Autowired
    private ConsentService consentService;

    @MockBean(name = "consentSpi")
    ConsentSpiImpl consentSpi;

    @Before
    public void setUp() {
        when(consentSpi.createAccountConsents(any(), eq(false), eq(false), eq(CORRECT_PSU_ID)))
        .thenReturn(CORRECT_PSU_ID);
        when(consentSpi.createAccountConsents(any(), eq(false), eq(false), eq(WRONG_PSU_ID)))
        .thenReturn(null);
        when(consentSpi.getAccountConsentById(CORRECT_PSU_ID)).thenReturn(getConsent());
        when(consentSpi.getAccountConsentById(WRONG_PSU_ID)).thenReturn(null);
        consentSpi.deleteAccountConsentsById(anyString());
    }

    @Test
    public void createAccountConsentsWithResponse_Success() {
        //Given:
        boolean withBalance = false;
        boolean tppRedirectPreferred = false;
        //When:
        ResponseObject responseObj = consentService.createAccountConsentsWithResponse(createConsentRequest(CORRECT_IBAN, AccountAccessType.ALL_ACCOUNTS, AccountAccessType.ALL_ACCOUNTS), withBalance, tppRedirectPreferred, CORRECT_PSU_ID);
        CreateConsentResp response = (CreateConsentResp) responseObj.getBody();
        //Then:
        assertThat(response.getConsentId()).isEqualTo(CORRECT_PSU_ID);
    }

    @Test
    public void createAccountConsentsWithResponse_Failure() {
        //Given:
        boolean withBalance = false;
        boolean tppRedirectPreferred = false;
        //When:
        ResponseObject response = consentService.createAccountConsentsWithResponse(createConsentRequest(WRONG_IBAN, AccountAccessType.ALL_ACCOUNTS, AccountAccessType.ALL_ACCOUNTS), withBalance, tppRedirectPreferred, WRONG_PSU_ID);
        //Then:
        assertThat(response.getError().getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);
    }


    @Test
    public void getAccountConsentsStatusById_Success() {
        //When:
        ResponseObject response = consentService.getAccountConsentsStatusById(CORRECT_PSU_ID);
        //Then:
        assertThat(response.getBody()).isEqualTo(TransactionStatus.RCVD);
    }

    @Test
    public void getAccountConsentsStatusById_Failure() {
        //When:
        ResponseObject response = consentService.getAccountConsentsStatusById(WRONG_PSU_ID);
        //Then:
        assertThat(response.getError().getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);
    }

    @Test
    public void getAccountConsentsById_Success() {
        //When:
        ResponseObject response = consentService.getAccountConsentsById(CORRECT_PSU_ID);
        AccountConsent consent = (AccountConsent) response.getBody();
        //Than:
        assertThat(consent.getId()).isEqualTo(CORRECT_PSU_ID);
    }

    @Test
    public void getAccountConsentsById_Failure() {
        //When:
        ResponseObject response = consentService.getAccountConsentsById(WRONG_PSU_ID);
        //Than:
        assertThat(response.getError().getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);
    }

    @Test
    public void deleteAccountConsentsById_Success() {
        //When:
        ResponseObject response = consentService.deleteAccountConsentsById(CORRECT_PSU_ID);
        //Than:
        assertThat(response.getBody()).isEqualTo(true);
    }

    @Test
    public void deleteAccountConsentsById_Failure() {
        //When:
        ResponseObject response = consentService.deleteAccountConsentsById(WRONG_PSU_ID);
        //Than:
        assertThat(response.getError().getTransactionStatus()).isEqualTo(TransactionStatus.RJCT);
    }

    private CreateConsentReq createConsentRequest(String iban, AccountAccessType allAccounts, AccountAccessType allPsd2) {
        AccountReference reference = new AccountReference();
        reference.setIban(iban);
        reference.setCurrency(CURRENCY);
        reference.setAccountId("ACCOUNT_ID");
        AccountReference[] list = new AccountReference[]{reference};

        AccountAccess access = new AccountAccess();
        access.setBalances(list);
        access.setAvailableAccounts(allAccounts);
        access.setAllPsd2(allPsd2);
        CreateConsentReq req = new CreateConsentReq();
        req.setAccess(access);
        req.setValidUntil(new Date());
        req.setFrequencyPerDay(4);
        req.setRecurringIndicator(true);
        req.setCombinedServiceIndicator(false);
        return req;
    }

    private SpiAccountConsent getConsent() {
        SpiAccountAccess acc = new SpiAccountAccess();
        SpiAccountDetails det = new SpiAccountDetails("XXXYYYXXX", CORRECT_IBAN, null, null, null, null, CURRENCY, "Buster", null, null, "", null);
        List<SpiAccountReference> ref = new ArrayList<>();
        ref.add(new SpiAccountReference(det.getId(), det.getIban(), det.getBban(), det.getPan(), det.getMaskedPan(), det.getMsisdn(), det.getCurrency()));
        acc.setBalances(ref);

        return new SpiAccountConsent(CORRECT_PSU_ID, acc, true, new Date(), 4, new Date(), SpiTransactionStatus.RCVD, SpiConsentStatus.VALID, true, true);
    }

    private AccountDetails[] getDetails() {
        AccountDetails[] list = new AccountDetails[]{
        new AccountDetails("9999999", CORRECT_IBAN, "", "", "", "", CURRENCY,
        "David", null, null, "", new ArrayList<>(), new Links())};
        return list;
    }

}
