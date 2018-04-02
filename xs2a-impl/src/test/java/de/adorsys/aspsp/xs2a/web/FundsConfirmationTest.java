package de.adorsys.aspsp.xs2a.web;

import de.adorsys.aspsp.xs2a.domain.AccountReference;
import de.adorsys.aspsp.xs2a.domain.Amount;
import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationRequest;
import de.adorsys.aspsp.xs2a.util.FileReaderUtil;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static de.adorsys.aspsp.xs2a.util.JsonUtil.toJson;
import static java.util.Currency.getInstance;

public class FundsConfirmationTest {
    private final String EXPECTED_FUNDS_CONFIRMATION_PATH = "FundsConfirmationRequestTest.json";

    @Test
    public void shouldReturnExpectedJson() throws JSONException {
        //Given:
        String expectedJson = FileReaderUtil.readContent(EXPECTED_FUNDS_CONFIRMATION_PATH);

        System.out.println("expectedJson: " + expectedJson);

        //When:
        String actual = toJson(buildConfirmationRequest());

        System.out.println("actual: " + actual);

        //Then:
        JSONAssert.assertEquals(expectedJson, actual, false);
    }


    private FundsConfirmationRequest buildConfirmationRequest(){
        FundsConfirmationRequest request = new FundsConfirmationRequest();
        request.setCardNumber("12345");
        request.setPayee("Check24");
        request.setInstructedAmount(buildAmount());
        request.setPsuAccount(buildAccountReference());
        return request;
    }

    private Amount buildAmount(){
        return Amount.builder()
        .content("1000.00")
        .currency(getInstance("EUR"))
        .build();
    }

    private AccountReference buildAccountReference(){
        return AccountReference.builder()
        .bban("1111111111")
        .currency(getInstance("EUR"))
        .iban("DE2310010010123456789")
        .maskedPan("23456xxxxxx1234")
        .msisdn("0172/1111111")
        .pan("1111")
        .build();
    }
}
