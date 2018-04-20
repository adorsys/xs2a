package de.adorsys.aspsp.xs2a.web;

import de.adorsys.aspsp.xs2a.domain.AccountReport;
import de.adorsys.aspsp.xs2a.service.AccountService;
import de.adorsys.aspsp.xs2a.web.util.ApiDateConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ValidationException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountControllerTest {
    private final String ACCOUNT_ID = "33333-999999999";
    private final String TRANSACTION_ID = "1234578";

    @Autowired
    private AccountController accountController;

    @Autowired
    private AccountService accountService;

    @Test
    public void getTransactions_withPeriodAndTransactionIdNoPsuInvolved() {
        //Given:
        Date dateFrom = new Date();
        Date dateTo = new Date();
        boolean psuInvolved = false;

        checkTransactionResults(ACCOUNT_ID, dateFrom, dateTo, TRANSACTION_ID, psuInvolved);
    }

    @Test
    public void getTransactions_onlyByPeriod() {
        //Given:
        Date dateFrom = getDateFromDateString("2017-12-12");
        Date dateTo = getDateFromDateString("2018-12-12");
        String transactionId = "";
        boolean psuInvolved = false;

        checkTransactionResults(ACCOUNT_ID, dateFrom, dateTo, transactionId, psuInvolved);
    }

    @Test
    public void shouldFail_getTransactions_noTransactionIdNoPsuInvolved() {
        //Given:
        String transactionId = "";
        boolean psuInvolved = false;

        //When Then:
        assertThatThrownBy(() -> accountController.getTransactions(ACCOUNT_ID, null, null, transactionId, psuInvolved, "both", false, false))
        .isInstanceOf(ValidationException.class);
    }

    private void checkTransactionResults(String accountId, Date dateFrom, Date dateTo, String transactionId,
                                         boolean psuInvolved) {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;

        AccountReport expectedResult = accountService.getAccountReport(accountId, dateFrom, dateTo, transactionId, psuInvolved, "both", false, false).getBody();

        //When:
        ResponseEntity<AccountReport> actualResponse = accountController.getTransactions(accountId, dateFrom, dateTo, transactionId, psuInvolved, "both", false, false);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        AccountReport actualResult = actualResponse.getBody();

        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    private static Date getDateFromDateString(String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(ApiDateConstants.DATE_PATTERN);
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }
}
