package de.adorsys.aspsp.xs2a.domain.entityValidator.impl;

import de.adorsys.aspsp.xs2a.domain.entityValidator.EntityValidator;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Is uses for validation arguments
 */
@Data
public class TransactionByIdRequestValidator implements EntityValidator {
    @NotNull
    @NotEmpty
    private final String accountId;

    @NotNull
    @NotEmpty
    private final String transactionId;
}
