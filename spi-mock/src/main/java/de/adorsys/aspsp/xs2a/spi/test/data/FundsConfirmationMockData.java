package de.adorsys.aspsp.xs2a.spi.test.data;

import de.adorsys.aspsp.xs2a.spi.domain.fund.SpiFundsConfirmationRequest;

public final class FundsConfirmationMockData {
    private FundsConfirmationMockData(){}

    public static boolean fundsConfirmation(SpiFundsConfirmationRequest request){
        return true;
    }
}
