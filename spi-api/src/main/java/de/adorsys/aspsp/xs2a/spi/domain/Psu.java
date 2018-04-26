package de.adorsys.aspsp.xs2a.spi.domain;

import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.NonFinal;
import org.springframework.data.annotation.Id;

import java.util.List;

@AllArgsConstructor
@Data
public class Psu {
    @Id
    private String id;
    private List<SpiAccountDetails> accountDetailsList;
}
