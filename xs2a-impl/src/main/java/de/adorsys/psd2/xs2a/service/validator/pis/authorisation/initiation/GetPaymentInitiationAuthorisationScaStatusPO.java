package de.adorsys.psd2.xs2a.service.validator.pis.authorisation.initiation;

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.validator.pis.PaymentTypeAndInfoProvider;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
public class GetPaymentInitiationAuthorisationScaStatusPO implements PaymentTypeAndInfoProvider {
    @NotNull
    private PisCommonPaymentResponse pisCommonPaymentResponse;

    @NotNull
    private String authorisationId;
    private PaymentType paymentType;
    private String paymentProduct;

    @Override
    public TppInfo getTppInfo() {
        return pisCommonPaymentResponse.getTppInfo();
    }
}
