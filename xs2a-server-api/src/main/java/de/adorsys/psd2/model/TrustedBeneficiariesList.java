package de.adorsys.psd2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import de.adorsys.psd2.model.TrustedBeneficiaries;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * List of trustedBeneficiaries.
 */
@ApiModel(description = "List of trustedBeneficiaries. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-04-15T15:50:07.478677+03:00[Europe/Kiev]")

public class TrustedBeneficiariesList   {
  @JsonProperty("trustedBeneficiaries")
  @Valid
  private List<TrustedBeneficiaries> trustedBeneficiaries = new ArrayList<>();

  public TrustedBeneficiariesList trustedBeneficiaries(List<TrustedBeneficiaries> trustedBeneficiaries) {
    this.trustedBeneficiaries = trustedBeneficiaries;
    return this;
  }

  public TrustedBeneficiariesList addTrustedBeneficiariesItem(TrustedBeneficiaries trustedBeneficiariesItem) {
    this.trustedBeneficiaries.add(trustedBeneficiariesItem);
    return this;
  }

  /**
   * Get trustedBeneficiaries
   * @return trustedBeneficiaries
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("trustedBeneficiaries")
  public List<TrustedBeneficiaries> getTrustedBeneficiaries() {
    return trustedBeneficiaries;
  }

  public void setTrustedBeneficiaries(List<TrustedBeneficiaries> trustedBeneficiaries) {
    this.trustedBeneficiaries = trustedBeneficiaries;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}    TrustedBeneficiariesList trustedBeneficiariesList = (TrustedBeneficiariesList) o;
    return Objects.equals(this.trustedBeneficiaries, trustedBeneficiariesList.trustedBeneficiaries);
  }

  @Override
  public int hashCode() {
    return Objects.hash(trustedBeneficiaries);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TrustedBeneficiariesList {\n");

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

