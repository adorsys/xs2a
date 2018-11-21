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
 * Body of the JSON response for a successfull get consent request.
 */
@ApiModel(description = "Body of the JSON response for a successfull get consent request.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-10-11T14:55" +
    ":45.627+02:00[Europe/Berlin]")
public class ConsentInformationResponse200Json {
    @JsonProperty("access")
    private AccountAccess access = null;
    @JsonProperty("recurringIndicator")
    private Boolean recurringIndicator = null;
    @JsonProperty("validUntil")
    private LocalDate validUntil = null;
    @JsonProperty("frequencyPerDay")
    private Integer frequencyPerDay = null;
    @JsonProperty("lastActionDate")
    private LocalDate lastActionDate = null;
    @JsonProperty("consentStatus")
    private ConsentStatus consentStatus = null;

    public ConsentInformationResponse200Json access(AccountAccess access) {
        this.access = access;
        return this;
    }

    /**
     * Get access
     *
     * @return access
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull
    @Valid
    public AccountAccess getAccess() {
        return access;
    }

    public void setAccess(AccountAccess access) {
        this.access = access;
    }

    public ConsentInformationResponse200Json recurringIndicator(Boolean recurringIndicator) {
        this.recurringIndicator = recurringIndicator;
        return this;
    }

    /**
     * Get recurringIndicator
     *
     * @return recurringIndicator
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull
    public Boolean getRecurringIndicator() {
        return recurringIndicator;
    }

    public void setRecurringIndicator(Boolean recurringIndicator) {
        this.recurringIndicator = recurringIndicator;
    }

    public ConsentInformationResponse200Json validUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
        return this;
    }

    /**
     * Get validUntil
     *
     * @return validUntil
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull
    @Valid
    public LocalDate getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
    }

    public ConsentInformationResponse200Json frequencyPerDay(Integer frequencyPerDay) {
        this.frequencyPerDay = frequencyPerDay;
        return this;
    }

    /**
     * Get frequencyPerDay
     *
     * @return frequencyPerDay
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull
    public Integer getFrequencyPerDay() {
        return frequencyPerDay;
    }

    public void setFrequencyPerDay(Integer frequencyPerDay) {
        this.frequencyPerDay = frequencyPerDay;
    }

    public ConsentInformationResponse200Json lastActionDate(LocalDate lastActionDate) {
        this.lastActionDate = lastActionDate;
        return this;
    }

    /**
     * Get lastActionDate
     *
     * @return lastActionDate
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull
    @Valid
    public LocalDate getLastActionDate() {
        return lastActionDate;
    }

    public void setLastActionDate(LocalDate lastActionDate) {
        this.lastActionDate = lastActionDate;
    }

    public ConsentInformationResponse200Json consentStatus(ConsentStatus consentStatus) {
        this.consentStatus = consentStatus;
        return this;
    }

    /**
     * Get consentStatus
     *
     * @return consentStatus
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull
    @Valid
    public ConsentStatus getConsentStatus() {
        return consentStatus;
    }

    public void setConsentStatus(ConsentStatus consentStatus) {
        this.consentStatus = consentStatus;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConsentInformationResponse200Json consentInformationResponse200Json = (ConsentInformationResponse200Json) o;
        return Objects.equals(this.access, consentInformationResponse200Json.access) && Objects.equals(this.recurringIndicator, consentInformationResponse200Json.recurringIndicator) && Objects.equals(this.validUntil, consentInformationResponse200Json.validUntil) && Objects.equals(this.frequencyPerDay, consentInformationResponse200Json.frequencyPerDay) && Objects.equals(this.lastActionDate, consentInformationResponse200Json.lastActionDate) && Objects.equals(this.consentStatus, consentInformationResponse200Json.consentStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(access, recurringIndicator, validUntil, frequencyPerDay, lastActionDate, consentStatus);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ConsentInformationResponse200Json {\n");
        sb.append("    access: ").append(toIndentedString(access)).append("\n");
        sb.append("    recurringIndicator: ").append(toIndentedString(recurringIndicator)).append("\n");
        sb.append("    validUntil: ").append(toIndentedString(validUntil)).append("\n");
        sb.append("    frequencyPerDay: ").append(toIndentedString(frequencyPerDay)).append("\n");
        sb.append("    lastActionDate: ").append(toIndentedString(lastActionDate)).append("\n");
        sb.append("    consentStatus: ").append(toIndentedString(consentStatus)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

