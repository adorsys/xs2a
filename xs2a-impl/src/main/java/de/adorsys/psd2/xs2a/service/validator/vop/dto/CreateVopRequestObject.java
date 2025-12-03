package de.adorsys.psd2.xs2a.service.validator.vop.dto;

import de.adorsys.psd2.xs2a.domain.vop.VopInitiationParameters;
import jakarta.validation.constraints.NotNull;

public record CreateVopRequestObject(
    @NotNull byte[] vop,
    @NotNull VopInitiationParameters vopInitiationParameters
) {
}
