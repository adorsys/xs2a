package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.AccountReference;
import de.adorsys.aspsp.xs2a.domain.Amount;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.code.BICFI;
import de.adorsys.aspsp.xs2a.domain.code.PurposeCode;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PaymentServiceTest {
    @Autowired
    private PaymentService paymentService;

    @Test
    public void getPaymentStatusById_successesResult() {
        //Given:
        boolean tppRedirectPreferred = false;
        SinglePayments expectedRequest = getCreatePaymentInitiationRequestTest();
        String validAccountConsentsId = paymentService.createPaymentInitiationAndReturnId(expectedRequest,tppRedirectPreferred);
        TransactionStatus expectedStatus = TransactionStatus.ACCP;

        //When:
        TransactionStatus actualStatus = paymentService.getPaymentStatusById(validAccountConsentsId);

        //Then:
        assertThat(actualStatus).isEqualTo(expectedStatus);
    }

    private SinglePayments getCreatePaymentInitiationRequestTest() {
        Amount amount = new Amount();
        amount.setCurrency(Currency.getInstance("EUR"));
        AccountReference accountReference = new AccountReference();
        accountReference.setIban("DE23100120020123456789");
        amount.setContent("123.40");
        SinglePayments singlePayments = new SinglePayments();
        singlePayments.setInstructedAmount(amount);
        singlePayments.setDebtorAccount(accountReference);
        singlePayments.setCreditorName("Merchant123");
        singlePayments.setPurposeCode(new PurposeCode("BEQNSD"));
        singlePayments.setCreditorAgent(new BICFI("q1bjkbs1"));
        singlePayments.setCreditorAccount(accountReference);
        singlePayments.setPurposeCode(new PurposeCode("BCENECEQ"));
        singlePayments.setRemittanceInformationUnstructured("Ref Number Merchant");

        return singlePayments;
    }

    @Test
    public void getPaymentStatusById_wrongId() {
        //Given:
        String wrongId = "111111";

        //When:
        TransactionStatus actualStatus = paymentService.getPaymentStatusById(wrongId);

        //Then:
        assertThat(actualStatus).isNull();
    }
}
