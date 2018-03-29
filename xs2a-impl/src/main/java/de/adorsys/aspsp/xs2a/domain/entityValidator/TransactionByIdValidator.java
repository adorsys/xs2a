package de.adorsys.aspsp.xs2a.domain.entityValidator;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
public class TransactionByIdValidator {
    @NotNull
    @Size(min = 5)
    private final String accountId;

    @NotNull
    private final String transactionId;
}
