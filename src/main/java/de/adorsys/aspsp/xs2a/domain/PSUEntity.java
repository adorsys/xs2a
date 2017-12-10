package de.adorsys.aspsp.xs2a.domain;

import domain.Bank;
import domain.BankApi;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Created by alexg on 08.05.17.
 */
@Data
@Document
public class PSUEntity extends AIC{

    @Id
    private String aic_id;
    private String process_id;
	private String request_id;
	private String resource_id;

}
