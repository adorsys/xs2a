package de.adorsys.aspsp.xs2a.service.validator;


import de.adorsys.aspsp.xs2a.service.validator.group.AccountIdGroup;
import de.adorsys.aspsp.xs2a.service.validator.group.PeriodGroup;
import de.adorsys.aspsp.xs2a.service.validator.group.TransactionIdGroup;
import lombok.Data;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.constraints.NotNull;
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
        FieldsForValidatorGroupTest fields = new FieldsForValidatorGroupTest();
        fields.setAccountId(ACCOUNT_ID);
        fields.setDateFrom(DATE_FROM);
        fields.setDateTo(DATE_TO);

        //When Then:
        valueValidatorService.validate(fields, AccountIdGroup.class, PeriodGroup.class);
    }

    @Test
    public void validate_AccountAndTransaction() {
        //Given:
        FieldsForValidatorGroupTest fields = new FieldsForValidatorGroupTest();
        fields.setAccountId(ACCOUNT_ID);
        fields.setTransactionId(TRANSACTION_ID);

        //When Then:
        valueValidatorService.validate(fields, AccountIdGroup.class, TransactionIdGroup.class);
    }

    @Test
    public void shouldFail_validate_AccountAndEmptyTransaction() {
        //Given:
        FieldsForValidatorGroupTest fields = new FieldsForValidatorGroupTest();
        fields.setAccountId(ACCOUNT_ID);

        //When Then:
        assertThatThrownBy(() -> valueValidatorService.validate(fields, AccountIdGroup.class, TransactionIdGroup.class))
        .hasMessageContaining("[transactionId : must not be null]");
    }

    @Test
    public void shouldFail_validate_EmptyAccountAndTransaction() {
        //Given:
        FieldsForValidatorGroupTest fields = new FieldsForValidatorGroupTest();
        fields.setTransactionId(TRANSACTION_ID);

        //When Then:
        assertThatThrownBy(() -> valueValidatorService.validate(fields, AccountIdGroup.class, TransactionIdGroup.class))
        .hasMessageContaining("[accountId : must not be null]");
    }

    @Test
    public void shouldFail_validate_AccountAndEmptyDataFrom() {
        //Given:
        FieldsForValidatorGroupTest fields = new FieldsForValidatorGroupTest();
        fields.setAccountId(ACCOUNT_ID);
        fields.setDateTo(DATE_TO);

        //When Then:
        assertThatThrownBy(() -> valueValidatorService.validate(fields, AccountIdGroup.class, PeriodGroup.class))
        .hasMessageContaining("[dateFrom : must not be null]");
    }

    @Data
    public class FieldsForValidatorGroupTest {
        @NotNull(groups = AccountIdGroup.class)
        private String accountId;
        @NotNull(groups = PeriodGroup.class)
        private Date dateFrom;
        @NotNull(groups = PeriodGroup.class)
        private Date dateTo;
        @NotNull(groups = TransactionIdGroup.class)
        private String transactionId;
    }
}
