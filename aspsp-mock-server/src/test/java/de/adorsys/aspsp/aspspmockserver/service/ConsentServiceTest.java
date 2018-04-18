package de.adorsys.aspsp.aspspmockserver.service;

import de.adorsys.aspsp.aspspmockserver.repository.ConsentRepository;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccessType;
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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ConsentServiceTest {
    private final String CORRECT_PSU_ID = "123456789";
    private final String WRONG_PSU_ID = "987654321";
    private final String CORRECT_IBAN = "DE123456789";
    private final String WRONG_IBAN = "DE987654321";

    @Autowired
    private ConsentService consentService;

    @MockBean(name = "consentRepository")
    private ConsentRepository consentRepository;

    @Before
    public void setUp() {
        when(consentRepository.save(any(SpiAccountConsent.class))).thenReturn(null);
        when(consentService.createConsentAndReturnId(any(), eq(WRONG_PSU_ID))).thenReturn(null);
    }

    @Test
    public void createAccountConsentTest_Success() {
        //When:
        String expectedResponse = consentService.createConsentAndReturnId(createConsentRequestBalances(CORRECT_IBAN,null,null), CORRECT_PSU_ID);

        //Then:
        assertThat(expectedResponse).isEqualTo(CORRECT_PSU_ID);
    }

    private SpiCreateConsentRequest createConsentRequestBalances(String iban, SpiAccountAccessType allAccounts, SpiAccountAccessType allPsd2) {
        List<SpiAccountReference> list = new ArrayList<>();
        list.add(new SpiAccountReference("",iban,"","","","", Currency.getInstance("EUR")));
        SpiAccountAccess access = new SpiAccountAccess();
        access.setBalances(list);
        access.setAvailableAccounts(allAccounts);
        access.setAllPsd2(allPsd2);
        return new SpiCreateConsentRequest(access, true, new Date(), 4, false);
    }
}
