package de.adorsys.aspsp.xs2a.service.validator;



import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ValueValidatorServiceTest {
    private static final String ACCOUNT_ID = "11111111";
    private static final String TRANSACTION_ID = "22222222";
    private static final Date DATE_FROM = new Date();
    private static final Date DATE_TO = new Date();

    @Autowired
    private ValueValidatorService valueValidatorService;

    @Test
    public void validate_AccountAndPeriod() {
        //Given:
        ValidationGroup fields = new ValidationGroup();
        fields.setAccountId(ACCOUNT_ID);
        fields.setDateFrom(DATE_FROM);
        fields.setDateTo(DATE_TO);

        //When Then:
        valueValidatorService.validate(fields, ValidationGroup.AccountIdGroup.class, ValidationGroup.PeriodGroup.class);
    }

    @Test
    public void validate_AccountAndTransaction() {
        //Given:
        ValidationGroup fields = new ValidationGroup();
        fields.setAccountId(ACCOUNT_ID);
        fields.setTransactionId(TRANSACTION_ID);

        //When Then:
        valueValidatorService.validate(fields, ValidationGroup.AccountIdGroup.class, ValidationGroup.TransactionIdGroup.class);
    }

    @Test
    public void shouldFail_validate_AccountAndEmptyTransaction() {
        //Given:
        ValidationGroup fields = new ValidationGroup();
        fields.setAccountId(ACCOUNT_ID);

        //When Then:
        assertThatThrownBy(() -> valueValidatorService.validate(fields, ValidationGroup.AccountIdGroup.class, ValidationGroup.TransactionIdGroup.class))
        .hasMessageContaining("[transactionId : may not be null]");
    }

    @Test
    public void shouldFail_validate_EmptyAccountAndTransaction() {
        //Given:
        ValidationGroup fields = new ValidationGroup();
        fields.setTransactionId(TRANSACTION_ID);

        //When Then:
        assertThatThrownBy(() -> valueValidatorService.validate(fields, ValidationGroup.AccountIdGroup.class, ValidationGroup.TransactionIdGroup.class))
        .hasMessageContaining("[accountId : may not be null]");
    }

    @Test
    public void shouldFail_validate_AccountAndEmptyDataFrom() {
        //Given:
        ValidationGroup fields = new ValidationGroup();
        fields.setAccountId(ACCOUNT_ID);
        fields.setDateTo(DATE_TO);

        //When Then:
        assertThatThrownBy(() -> valueValidatorService.validate(fields, ValidationGroup.AccountIdGroup.class, ValidationGroup.PeriodGroup.class))
        .hasMessageContaining("[dateFrom : may not be null]");
    }
}
