package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Content of the body of a consent request.
 */
@Schema(description = "Content of the body of a consent request. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-05-06T13:00:42.214155+03:00[Europe/Kiev]")


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
  @Schema(required = true, description = "")
      @NotNull

    @Valid
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
   * \"true\", if the consent is for recurring access to the account data.  \"false\", if the consent is for one access to the account data.
   * @return recurringIndicator
   **/
  @Schema(example = "false", required = true, description = "\"true\", if the consent is for recurring access to the account data.  \"false\", if the consent is for one access to the account data. ")
      @NotNull

    public Boolean isRecurringIndicator() {
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
   * This parameter is defining a valid until date (including the mentioned date) for the requested consent.  The content is the local ASPSP date in ISO-Date format, e.g. 2017-10-30.  Future dates might get adjusted by ASPSP.   If a maximal available date is requested, a date in far future is to be used: \"9999-12-31\".   In both cases the consent object to be retrieved by the get consent request will contain the adjusted date.
   * @return validUntil
   **/
  @Schema(example = "Thu Dec 31 02:00:00 EET 2020", required = true, description = "This parameter is defining a valid until date (including the mentioned date) for the requested consent.  The content is the local ASPSP date in ISO-Date format, e.g. 2017-10-30.  Future dates might get adjusted by ASPSP.   If a maximal available date is requested, a date in far future is to be used: \"9999-12-31\".   In both cases the consent object to be retrieved by the get consent request will contain the adjusted date. ")
      @NotNull

    @Valid
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
   * This field indicates the requested maximum frequency for an access without PSU involvement per day. For a one-off access, this attribute is set to \"1\".  The frequency needs to be greater equal to one.   If not otherwise agreed bilaterally between TPP and ASPSP, the frequency is less equal to 4.
   * minimum: 1
   * @return frequencyPerDay
   **/
  @Schema(example = "4", required = true, description = "This field indicates the requested maximum frequency for an access without PSU involvement per day. For a one-off access, this attribute is set to \"1\".  The frequency needs to be greater equal to one.   If not otherwise agreed bilaterally between TPP and ASPSP, the frequency is less equal to 4. ")
      @NotNull

  @Min(1)  public Integer getFrequencyPerDay() {
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
  @Schema(example = "false", required = true, description = "If \"true\" indicates that a payment initiation service will be addressed in the same \"session\". ")
      @NotNull

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
    }
    Consents consents = (Consents) o;
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
