package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Content of the body of a transaction authorisation request
 */
@Schema(description = "Content of the body of a transaction authorisation request ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-05-06T13:00:42.214155+03:00[Europe/Kiev]")


public class TransactionAuthorisation {
  @JsonProperty("scaAuthenticationData")
  private String scaAuthenticationData = null;

  public TransactionAuthorisation scaAuthenticationData(String scaAuthenticationData) {
    this.scaAuthenticationData = scaAuthenticationData;
    return this;
  }

  /**
   * SCA authentication data, depending on the chosen authentication method.  If the data is binary, then it is base64 encoded.
   * @return scaAuthenticationData
   **/
  @Schema(required = true, description = "SCA authentication data, depending on the chosen authentication method.  If the data is binary, then it is base64 encoded. ")
      @NotNull

    public String getScaAuthenticationData() {
    return scaAuthenticationData;
  }

  public void setScaAuthenticationData(String scaAuthenticationData) {
    this.scaAuthenticationData = scaAuthenticationData;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TransactionAuthorisation transactionAuthorisation = (TransactionAuthorisation) o;
    return Objects.equals(this.scaAuthenticationData, transactionAuthorisation.scaAuthenticationData);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scaAuthenticationData);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TransactionAuthorisation {\n");

    sb.append("    scaAuthenticationData: ").append(toIndentedString(scaAuthenticationData)).append("\n");
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
