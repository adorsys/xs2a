package de.adorsys.aspsp.xs2a.domain;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;

@ApiModel(description = "OperationStatus", value = "OperationStatus")
@AllArgsConstructor
public enum OperationStatus {
    SUCCESS, FAILURE
}
//TODO Agree upon nessesity of such ENUM as a field of ResponseObject
