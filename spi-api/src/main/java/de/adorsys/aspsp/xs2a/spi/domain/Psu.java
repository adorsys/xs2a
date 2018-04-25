package de.adorsys.aspsp.xs2a.spi.domain;

import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class Psu {
    private String id;
    private List<SpiAccountDetails> accountDetailsList;
}
