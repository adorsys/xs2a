package de.adorsys.aspsp.xs2a.spi.domain;


import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * Created by aro on 27.11.17.
 */
@Data
@ApiModel(description = "Response for created by some methods inthe consent Service")
public class AICResponse{

    private Authentification[] sca_methods;
    private Authentification  chosen_sca_method;
    private Challange sca_challange_data;
    
    private Links _links;
    private String psu_message;
    
    

}


