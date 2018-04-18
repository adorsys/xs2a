package de.adorsys.aspsp.aspspmockserver.service;

import de.adorsys.aspsp.aspspmockserver.repository.ConsentRepository;
import de.adorsys.aspsp.aspspmockserver.repository.PsuRepository;
import de.adorsys.aspsp.xs2a.spi.domain.Psu;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccessType;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiCreateConsentRequest;
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
import static org.mockito.Matchers.any;
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

    @MockBean(name = "consentRepository")
    private ConsentRepository consentRepository;
    @MockBean(name = "psuRepository")
    private PsuRepository psuRepository;

    @Before
    public void setUp() {
        when(consentRepository.save(any(SpiAccountConsent.class))).thenReturn(getConsent());
        when(psuRepository.findPsuByAccountDetailsList_Iban(CORRECT_IBAN)).thenReturn(getPsu(CORRECT_IBAN));
        when(psuRepository.findPsuByAccountDetailsList_Iban(WRONG_IBAN)).thenReturn(null);
        when(psuRepository.findOne(CORRECT_PSU_ID)).thenReturn(getPsu(CORRECT_IBAN));
        when(psuRepository.findOne(WRONG_PSU_ID)).thenReturn(null);
    }

    @Test
    public void createAccountConsentTest_Success() {
        //When:
        String response = consentService.createConsentAndReturnId(createConsentRequestBalances(CORRECT_IBAN, null, null), CORRECT_PSU_ID);

        //Then:
        assertThat(response).isEqualTo(CORRECT_PSU_ID);
    }

    @Test
    public void createAccountConsentTest_Failure() {
        //When:
        String response = consentService.createConsentAndReturnId(createConsentRequestBalances(WRONG_IBAN, null, null), CORRECT_PSU_ID);

        //Then:
        assertThat(response).isNullOrEmpty();
    }

    @Test
    public void createAccountConsentTest_allAccounts_Success() {
        //When:
        String response = consentService.createConsentAndReturnId(createConsentRequestBalances("", SpiAccountAccessType.ALL_ACCOUNTS, null), CORRECT_PSU_ID);

        //Then:
        assertThat(response).isEqualTo(CORRECT_PSU_ID);
    }

    @Test
    public void createAccountConsentTest_allAccounts_Failure() {
        //When:
        String response = consentService.createConsentAndReturnId(createConsentRequestBalances("", SpiAccountAccessType.ALL_ACCOUNTS, null), WRONG_PSU_ID);

        //Then:
        assertThat(response).isNullOrEmpty();
    }

    private SpiCreateConsentRequest createConsentRequestBalances(String iban, SpiAccountAccessType allAccounts, SpiAccountAccessType allPsd2) {
        List<SpiAccountReference> list = new ArrayList<>();
        list.add(new SpiAccountReference("", iban, "", "", "", "", CURRENCY));
        SpiAccountAccess access = new SpiAccountAccess();
        access.setBalances(list);
        access.setAvailableAccounts(allAccounts);
        access.setAllPsd2(allPsd2);
        return new SpiCreateConsentRequest(access, true, new Date(), 4, false);
    }

    private Psu getPsu(String id) {
        List<SpiAccountDetails> list = getDetails();
        return new Psu(CORRECT_PSU_ID, list);
    }

    private SpiAccountConsent getConsent() {
        SpiAccountAccess acc = new SpiAccountAccess();
        SpiAccountDetails det = getPsu(CORRECT_PSU_ID).getAccountDetailsList().get(0);
        List<SpiAccountReference> ref = new ArrayList<>();
        ref.add(new SpiAccountReference(det.getId(),det.getIban(),det.getBban(),det.getPan(),det.getMaskedPan(),det.getMsisdn(),det.getCurrency()));
        acc.setBalances(ref);

        return new SpiAccountConsent(CORRECT_PSU_ID,acc,true,new Date(),4,new Date(), SpiTransactionStatus.RCVD, SpiConsentStatus.VALID,true,true);
    }

    private List<SpiAccountDetails> getDetails(){
        List<SpiAccountDetails> list = new ArrayList<>();
        list.add(new SpiAccountDetails("9999999", CORRECT_IBAN, "", "", "",
        "", CURRENCY, "David", null, null, "", null));
        return list;
    }
}
