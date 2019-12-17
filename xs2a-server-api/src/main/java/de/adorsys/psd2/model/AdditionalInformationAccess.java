package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Additional account information
 */
@ApiModel(description = "Additional account information ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-12-11T13:12:30.656354+02:00[Europe/Kiev]")

public class AdditionalInformationAccess   {
  @JsonProperty("ownerName")
  @Valid
  private List<AccountReference> ownerName = null;

  public AdditionalInformationAccess ownerName(List<AccountReference> ownerName) {
    this.ownerName = ownerName;
    return this;
  }

  public AdditionalInformationAccess addOwnerNameItem(AccountReference ownerNameItem) {
    if (this.ownerName == null) {
      this.ownerName = new ArrayList<>();
    }
    this.ownerName.add(ownerNameItem);
    return this;
  }

  /**
   * Is asking for account owner name of the accounts referenced within.
   * @return ownerName
  **/
  @ApiModelProperty(value = "Is asking for account owner name of the accounts referenced within. ")

  @Valid


  @JsonProperty("ownerName")
  public List<AccountReference> getOwnerName() {
    return ownerName;
  }

  public void setOwnerName(List<AccountReference> ownerName) {
    this.ownerName = ownerName;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AdditionalInformationAccess additionalInformationAccess = (AdditionalInformationAccess) o;
    return Objects.equals(this.ownerName, additionalInformationAccess.ownerName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ownerName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AdditionalInformationAccess {\n");

    sb.append("    ownerName: ").append(toIndentedString(ownerName)).append("\n");
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

