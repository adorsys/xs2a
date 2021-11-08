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
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-05T12:22:49.487689+02:00[Europe/Kiev]")

public class AuthorisationConfirmation   {
  @JsonProperty("confirmationCode")
  private String confirmationCode = null;

  public AuthorisationConfirmation confirmationCode(String confirmationCode) {
    this.confirmationCode = confirmationCode;
    return this;
  }

  /**
   * Confirmation Code as retrieved by the TPP from the redirect based SCA process.
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
}    AuthorisationConfirmation authorisationConfirmation = (AuthorisationConfirmation) o;
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

