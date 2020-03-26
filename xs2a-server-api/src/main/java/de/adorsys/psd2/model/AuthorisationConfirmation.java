/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Content of the body of an authorisation confirmation request
 */
@ApiModel(description = "Content of the body of an authorisation confirmation request ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-03-16T13:49:16.891743+02:00[Europe/Kiev]")

public class AuthorisationConfirmation {
    @JsonProperty("confirmationCode")
    private String confirmationCode = null;

    public AuthorisationConfirmation confirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
        return this;
    }

    /**
     * Confirmation Code as retrieved by the TPP from the redirect based SCA process.
     *
     * @return confirmationCode
     **/
    @ApiModelProperty(required = true, value = "Confirmation Code as retrieved by the TPP from the redirect based SCA process.")
    @NotNull


    @JsonProperty("confirmationCode")
    public String getConfirmationCode() {
        return confirmationCode;
    }

    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthorisationConfirmation authorisationConfirmation = (AuthorisationConfirmation) o;
        return Objects.equals(this.confirmationCode, authorisationConfirmation.confirmationCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(confirmationCode);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AuthorisationConfirmation {\n");

        sb.append("    confirmationCode: ").append(toIndentedString(confirmationCode)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

