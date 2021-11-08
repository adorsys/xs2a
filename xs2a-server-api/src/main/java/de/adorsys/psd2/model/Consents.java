package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Content of the body of a consent request.
 */
@ApiModel(description = "Content of the body of a consent request. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-05T12:22:49.487689+02:00[Europe/Kiev]")

public class Consents   {
  @JsonProperty("access")
  private AccountAccess access = null;

  @JsonProperty("recurringIndicator")
  private Boolean recurringIndicator = null;

  @JsonProperty("validUntil")
  private LocalDate validUntil = null;

  @JsonProperty("frequencyPerDay")
  private Integer frequencyPerDay = null;

  @JsonProperty("combinedServiceIndicator")
  private Boolean combinedServiceIndicator = null;

  public Consents access(AccountAccess access) {
    this.access = access;
    return this;
  }

  /**
   * Get access
   * @return access
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("access")
  public AccountAccess getAccess() {
    return access;
  }

  public void setAccess(AccountAccess access) {
    this.access = access;
  }

  public Consents recurringIndicator(Boolean recurringIndicator) {
    this.recurringIndicator = recurringIndicator;
    return this;
  }

  /**
   * Get recurringIndicator
   * @return recurringIndicator
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull



  @JsonProperty("recurringIndicator")
  public Boolean getRecurringIndicator() {
    return recurringIndicator;
  }

  public void setRecurringIndicator(Boolean recurringIndicator) {
    this.recurringIndicator = recurringIndicator;
  }

  public Consents validUntil(LocalDate validUntil) {
    this.validUntil = validUntil;
    return this;
  }

  /**
   * Get validUntil
   * @return validUntil
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("validUntil")
  public LocalDate getValidUntil() {
    return validUntil;
  }

  public void setValidUntil(LocalDate validUntil) {
    this.validUntil = validUntil;
  }

  public Consents frequencyPerDay(Integer frequencyPerDay) {
    this.frequencyPerDay = frequencyPerDay;
    return this;
  }

  /**
   * Get frequencyPerDay
   * @return frequencyPerDay
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull



  @JsonProperty("frequencyPerDay")
  public Integer getFrequencyPerDay() {
    return frequencyPerDay;
  }

  public void setFrequencyPerDay(Integer frequencyPerDay) {
    this.frequencyPerDay = frequencyPerDay;
  }

  public Consents combinedServiceIndicator(Boolean combinedServiceIndicator) {
    this.combinedServiceIndicator = combinedServiceIndicator;
    return this;
  }

  /**
   * If \"true\" indicates that a payment initiation service will be addressed in the same \"session\".
   * @return combinedServiceIndicator
  **/
  @ApiModelProperty(required = true, value = "If \"true\" indicates that a payment initiation service will be addressed in the same \"session\". ")
  @NotNull



  @JsonProperty("combinedServiceIndicator")
  public Boolean isCombinedServiceIndicator() {
    return combinedServiceIndicator;
  }

  public void setCombinedServiceIndicator(Boolean combinedServiceIndicator) {
    this.combinedServiceIndicator = combinedServiceIndicator;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}    Consents consents = (Consents) o;
    return Objects.equals(this.access, consents.access) &&
    Objects.equals(this.recurringIndicator, consents.recurringIndicator) &&
    Objects.equals(this.validUntil, consents.validUntil) &&
    Objects.equals(this.frequencyPerDay, consents.frequencyPerDay) &&
    Objects.equals(this.combinedServiceIndicator, consents.combinedServiceIndicator);
  }

  @Override
  public int hashCode() {
    return Objects.hash(access, recurringIndicator, validUntil, frequencyPerDay, combinedServiceIndicator);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Consents {\n");

    sb.append("    access: ").append(toIndentedString(access)).append("\n");
    sb.append("    recurringIndicator: ").append(toIndentedString(recurringIndicator)).append("\n");
    sb.append("    validUntil: ").append(toIndentedString(validUntil)).append("\n");
    sb.append("    frequencyPerDay: ").append(toIndentedString(frequencyPerDay)).append("\n");
    sb.append("    combinedServiceIndicator: ").append(toIndentedString(combinedServiceIndicator)).append("\n");
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

