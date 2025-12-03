package de.adorsys.psd2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

/**
 * See document \\\&quot;openFinance API Framework Data Dictionary\\\&quot;, section \\\&quot;Original Transaction Information and Status\\\&quot; for more details.
 */
@Schema(description = "See document \\\"openFinance API Framework Data Dictionary\\\", section \\\"Original Transaction Information and Status\\\" for more details. ")
@Validated
@jakarta.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2025-10-20T11:14:45.442141177+01:00[Africa/Douala]")


public class OriginalTransactionInformationAndStatus   {
  @JsonProperty("originalInstructionId")
  private String originalInstructionId = null;

  @JsonProperty("originalEndToEndId")
  private String originalEndToEndId = null;

  @JsonProperty("originalUetr")
  private UUID originalUetr = null;

  @JsonProperty("transactionStatus")
  private TransactionStatus transactionStatus = null;

  @JsonProperty("statusReasonInformationCode")
  private StatusReasonCode statusReasonInformationCode = null;

  @JsonProperty("statusReasonInformationProprietary")
  private String statusReasonInformationProprietary = null;

  @JsonProperty("statusReasonAdditionalInformation")
  private String statusReasonAdditionalInformation = null;

  public OriginalTransactionInformationAndStatus originalInstructionId(String originalInstructionId) {
    this.originalInstructionId = originalInstructionId;
    return this;
  }

  /**
   * Unique identification, as assigned by the original instructing party for the original instructed party, to unambiguously identify the original instruction.
   * @return originalInstructionId
   **/
  @Schema(description = "Unique identification, as assigned by the original instructing party for the original instructed party, to unambiguously identify the original instruction. ")
  @JsonProperty("originalInstructionId")

  public String getOriginalInstructionId() {
    return originalInstructionId;
  }

  public void setOriginalInstructionId(String originalInstructionId) {
    this.originalInstructionId = originalInstructionId;
  }

  public OriginalTransactionInformationAndStatus originalEndToEndId(String originalEndToEndId) {
    this.originalEndToEndId = originalEndToEndId;
    return this;
  }

  /**
   * Unique identification, as assigned by the original initiating party to unambiguously identify the original transaction
   * @return originalEndToEndId
   **/
  @Schema(description = "Unique identification, as assigned by the original initiating party to unambiguously identify the original transaction ")
  @JsonProperty("originalEndToEndId")

  public String getOriginalEndToEndId() {
    return originalEndToEndId;
  }

  public void setOriginalEndToEndId(String originalEndToEndId) {
    this.originalEndToEndId = originalEndToEndId;
  }

  public OriginalTransactionInformationAndStatus originalUetr(UUID originalUetr) {
    this.originalUetr = originalUetr;
    return this;
  }

  /**
   * Universally unique identifier to provide the original end-to-end reference of a payment transaction.
   * @return originalUetr
   **/
  @Schema(description = "Universally unique identifier to provide the original end-to-end reference of a payment transaction. ")
  @JsonProperty("originalUetr")

  @Valid
  public UUID getOriginalUetr() {
    return originalUetr;
  }

  public void setOriginalUetr(UUID originalUetr) {
    this.originalUetr = originalUetr;
  }

  public OriginalTransactionInformationAndStatus transactionStatus(TransactionStatus transactionStatus) {
    this.transactionStatus = transactionStatus;
    return this;
  }

  /**
   * Get transactionStatus
   * @return transactionStatus
   **/
  @Schema(required = true, description = "")
  @JsonProperty("transactionStatus")
    @NotNull

  @Valid
  public TransactionStatus getTransactionStatus() {
    return transactionStatus;
  }

  public void setTransactionStatus(TransactionStatus transactionStatus) {
    this.transactionStatus = transactionStatus;
  }

  public OriginalTransactionInformationAndStatus statusReasonInformationCode(StatusReasonCode statusReasonInformationCode) {
    this.statusReasonInformationCode = statusReasonInformationCode;
    return this;
  }

  /**
   * Get statusReasonInformationCode
   * @return statusReasonInformationCode
   **/
  @Schema(description = "")
  @JsonProperty("statusReasonInformationCode")

  @Valid
  public StatusReasonCode getStatusReasonInformationCode() {
    return statusReasonInformationCode;
  }

  public void setStatusReasonInformationCode(StatusReasonCode statusReasonInformationCode) {
    this.statusReasonInformationCode = statusReasonInformationCode;
  }

  public OriginalTransactionInformationAndStatus statusReasonInformationProprietary(String statusReasonInformationProprietary) {
    this.statusReasonInformationProprietary = statusReasonInformationProprietary;
    return this;
  }

  /**
   * Proprietary additional information on the reason for e.g. rejecting the request.
   * @return statusReasonInformationProprietary
   **/
  @Schema(description = "Proprietary additional information on the reason for e.g. rejecting the request. ")
  @JsonProperty("statusReasonInformationProprietary")

  public String getStatusReasonInformationProprietary() {
    return statusReasonInformationProprietary;
  }

  public void setStatusReasonInformationProprietary(String statusReasonInformationProprietary) {
    this.statusReasonInformationProprietary = statusReasonInformationProprietary;
  }

  public OriginalTransactionInformationAndStatus statusReasonAdditionalInformation(String statusReasonAdditionalInformation) {
    this.statusReasonAdditionalInformation = statusReasonAdditionalInformation;
    return this;
  }

  /**
   * Further details on the status reason.
   * @return statusReasonAdditionalInformation
   **/
  @Schema(description = "Further details on the status reason. ")
  @JsonProperty("statusReasonAdditionalInformation")

  public String getStatusReasonAdditionalInformation() {
    return statusReasonAdditionalInformation;
  }

  public void setStatusReasonAdditionalInformation(String statusReasonAdditionalInformation) {
    this.statusReasonAdditionalInformation = statusReasonAdditionalInformation;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OriginalTransactionInformationAndStatus originalTransactionInformationAndStatus = (OriginalTransactionInformationAndStatus) o;
    return Objects.equals(this.originalInstructionId, originalTransactionInformationAndStatus.originalInstructionId) &&
        Objects.equals(this.originalEndToEndId, originalTransactionInformationAndStatus.originalEndToEndId) &&
        Objects.equals(this.originalUetr, originalTransactionInformationAndStatus.originalUetr) &&
        Objects.equals(this.transactionStatus, originalTransactionInformationAndStatus.transactionStatus) &&
        Objects.equals(this.statusReasonInformationCode, originalTransactionInformationAndStatus.statusReasonInformationCode) &&
        Objects.equals(this.statusReasonInformationProprietary, originalTransactionInformationAndStatus.statusReasonInformationProprietary) &&
        Objects.equals(this.statusReasonAdditionalInformation, originalTransactionInformationAndStatus.statusReasonAdditionalInformation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(originalInstructionId, originalEndToEndId, originalUetr, transactionStatus, statusReasonInformationCode, statusReasonInformationProprietary, statusReasonAdditionalInformation);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OriginalTransactionInformationAndStatus {\n");

    sb.append("    originalInstructionId: ").append(toIndentedString(originalInstructionId)).append("\n");
    sb.append("    originalEndToEndId: ").append(toIndentedString(originalEndToEndId)).append("\n");
    sb.append("    originalUetr: ").append(toIndentedString(originalUetr)).append("\n");
    sb.append("    transactionStatus: ").append(toIndentedString(transactionStatus)).append("\n");
    sb.append("    statusReasonInformationCode: ").append(toIndentedString(statusReasonInformationCode)).append("\n");
    sb.append("    statusReasonInformationProprietary: ").append(toIndentedString(statusReasonInformationProprietary)).append("\n");
    sb.append("    statusReasonAdditionalInformation: ").append(toIndentedString(statusReasonAdditionalInformation)).append("\n");
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
