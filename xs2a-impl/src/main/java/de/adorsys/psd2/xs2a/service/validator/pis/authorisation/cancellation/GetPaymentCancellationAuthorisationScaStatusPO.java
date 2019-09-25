package de.adorsys.psd2.xs2a.service.validator.pis.authorisation.cancellation;

import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.validator.pis.PaymentTypeAndInfoProvider;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
public class GetPaymentCancellationAuthorisationScaStatusPO implements PaymentTypeAndInfoProvider {
    @NotNull
    private PisCommonPaymentResponse pisCommonPaymentResponse;

    @NotNull
    private String authorisationId;

    @Override
    public TppInfo getTppInfo() {
        return pisCommonPaymentResponse.getTppInfo();
    }

    @Override
    public PaymentType getPaymentType() {
        return pisCommonPaymentResponse.getPaymentType();
    }

    @Override
    public String getPaymentProduct() {
        return pisCommonPaymentResponse.getPaymentProduct();
    }
}
