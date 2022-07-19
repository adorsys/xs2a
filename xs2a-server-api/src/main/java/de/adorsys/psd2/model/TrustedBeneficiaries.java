package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Array of Trusted Beneficiaries.
 */
@Schema(description = "Array of Trusted Beneficiaries.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-05-06T12:59:08.054254+03:00[Europe/Kiev]")


public class TrustedBeneficiaries   {
  @JsonProperty("trustedBeneficiaries")
  private TrustedBeneficiariesList trustedBeneficiaries = null;

  public TrustedBeneficiaries trustedBeneficiaries(TrustedBeneficiariesList trustedBeneficiaries) {
    this.trustedBeneficiaries = trustedBeneficiaries;
    return this;
  }

  /**
   * Get trustedBeneficiaries
   * @return trustedBeneficiaries
   **/
  @Schema(required = true, description = "")
      @NotNull

    @Valid
    public TrustedBeneficiariesList getTrustedBeneficiaries() {
    return trustedBeneficiaries;
  }

  public void setTrustedBeneficiaries(TrustedBeneficiariesList trustedBeneficiaries) {
    this.trustedBeneficiaries = trustedBeneficiaries;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TrustedBeneficiaries trustedBeneficiaries = (TrustedBeneficiaries) o;
    return Objects.equals(this.trustedBeneficiaries, trustedBeneficiaries.trustedBeneficiaries);
  }

  @Override
  public int hashCode() {
    return Objects.hash(trustedBeneficiaries);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TrustedBeneficiaries {\n");

    sb.append("    trustedBeneficiaries: ").append(toIndentedString(trustedBeneficiaries)).append("\n");
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
