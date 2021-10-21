package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * EntryDetailsElement
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-10-19T10:54:53.682382+03:00[Europe/Kiev]")

public class EntryDetailsElement {
    @JsonProperty("endToEndId")
    private String endToEndId = null;

    @JsonProperty("mandateId")
    private String mandateId = null;

    @JsonProperty("checkId")
    private String checkId = null;

    @JsonProperty("creditorId")
    private String creditorId = null;

    @JsonProperty("transactionAmount")
    private Amount transactionAmount = null;

    @JsonProperty("currencyExchange")
    private ReportExchangeRateList currencyExchange = null;

    @JsonProperty("creditorName")
    private String creditorName = null;

    @JsonProperty("creditorAccount")
    private AccountReference creditorAccount = null;

    @JsonProperty("creditorAgent")
    private String creditorAgent = null;

    @JsonProperty("ultimateCreditor")
    private String ultimateCreditor = null;

    @JsonProperty("debtorName")
    private String debtorName = null;

    @JsonProperty("debtorAccount")
    private AccountReference debtorAccount = null;

    @JsonProperty("debtorAgent")
    private String debtorAgent = null;

    @JsonProperty("ultimateDebtor")
    private String ultimateDebtor = null;

    @JsonProperty("remittanceInformationUnstructured")
    private String remittanceInformationUnstructured = null;

    @JsonProperty("remittanceInformationUnstructuredArray")
    private RemittanceInformationUnstructuredArray remittanceInformationUnstructuredArray = null;

    @JsonProperty("remittanceInformationStructured")
    private RemittanceInformationStructured remittanceInformationStructured = null;

    @JsonProperty("remittanceInformationStructuredArray")
    private RemittanceInformationStructuredArray remittanceInformationStructuredArray = null;

    @JsonProperty("purposeCode")
    private PurposeCode purposeCode = null;

    public EntryDetailsElement endToEndId(String endToEndId) {
        this.endToEndId = endToEndId;
        return this;
    }

    /**
     * Unique end to end identity.
     *
     * @return endToEndId
     **/
    @ApiModelProperty(value = "Unique end to end identity.")

    @Size(max = 35)

    @JsonProperty("endToEndId")
    public String getEndToEndId() {
        return endToEndId;
    }

    public void setEndToEndId(String endToEndId) {
        this.endToEndId = endToEndId;
    }

    public EntryDetailsElement mandateId(String mandateId) {
        this.mandateId = mandateId;
        return this;
    }

    /**
     * Identification of Mandates, e.g. a SEPA Mandate ID.
     *
     * @return mandateId
     **/
    @ApiModelProperty(value = "Identification of Mandates, e.g. a SEPA Mandate ID.")

    @Size(max = 35)

    @JsonProperty("mandateId")
    public String getMandateId() {
        return mandateId;
    }

    public void setMandateId(String mandateId) {
        this.mandateId = mandateId;
    }

    public EntryDetailsElement checkId(String checkId) {
        this.checkId = checkId;
        return this;
    }

    /**
     * Identification of a Cheque.
     *
     * @return checkId
     **/
    @ApiModelProperty(value = "Identification of a Cheque.")

    @Size(max = 35)

    @JsonProperty("checkId")
    public String getCheckId() {
        return checkId;
    }

    public void setCheckId(String checkId) {
        this.checkId = checkId;
    }

    public EntryDetailsElement creditorId(String creditorId) {
        this.creditorId = creditorId;
        return this;
    }

    /**
     * Get creditorId
     *
     * @return creditorId
     **/
    @ApiModelProperty(value = "")

    @Size(max = 35)

    @JsonProperty("creditorId")
    public String getCreditorId() {
        return creditorId;
    }

    public void setCreditorId(String creditorId) {
        this.creditorId = creditorId;
    }

    public EntryDetailsElement transactionAmount(Amount transactionAmount) {
        this.transactionAmount = transactionAmount;
        return this;
    }

    /**
     * Get transactionAmount
     *
     * @return transactionAmount
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    @Valid


    @JsonProperty("transactionAmount")
    public Amount getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(Amount transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public EntryDetailsElement currencyExchange(ReportExchangeRateList currencyExchange) {
        this.currencyExchange = currencyExchange;
        return this;
    }

    /**
     * Get currencyExchange
     *
     * @return currencyExchange
     **/
    @ApiModelProperty(value = "")

    @Valid


    @JsonProperty("currencyExchange")
    public ReportExchangeRateList getCurrencyExchange() {
        return currencyExchange;
    }

    public void setCurrencyExchange(ReportExchangeRateList currencyExchange) {
        this.currencyExchange = currencyExchange;
    }

    public EntryDetailsElement creditorName(String creditorName) {
        this.creditorName = creditorName;
        return this;
    }

    /**
     * Get creditorName
     *
     * @return creditorName
     **/
    @ApiModelProperty(value = "")

    @Size(max = 70)

    @JsonProperty("creditorName")
    public String getCreditorName() {
        return creditorName;
    }

    public void setCreditorName(String creditorName) {
        this.creditorName = creditorName;
    }

    public EntryDetailsElement creditorAccount(AccountReference creditorAccount) {
        this.creditorAccount = creditorAccount;
        return this;
    }

    /**
     * Get creditorAccount
     *
     * @return creditorAccount
     **/
    @ApiModelProperty(value = "")

    @Valid


    @JsonProperty("creditorAccount")
    public AccountReference getCreditorAccount() {
        return creditorAccount;
    }

    public void setCreditorAccount(AccountReference creditorAccount) {
        this.creditorAccount = creditorAccount;
    }

    public EntryDetailsElement creditorAgent(String creditorAgent) {
        this.creditorAgent = creditorAgent;
        return this;
    }

    /**
     * Get creditorAgent
     *
     * @return creditorAgent
     **/
    @ApiModelProperty(value = "")

    @Pattern(regexp = "[A-Z]{6,6}[A-Z2-9][A-NP-Z0-9]([A-Z0-9]{3,3}){0,1}")

    @JsonProperty("creditorAgent")
    public String getCreditorAgent() {
        return creditorAgent;
    }

    public void setCreditorAgent(String creditorAgent) {
        this.creditorAgent = creditorAgent;
    }

    public EntryDetailsElement ultimateCreditor(String ultimateCreditor) {
        this.ultimateCreditor = ultimateCreditor;
        return this;
    }

    /**
     * Get ultimateCreditor
     *
     * @return ultimateCreditor
     **/
    @ApiModelProperty(value = "")

    @Size(max = 70)

    @JsonProperty("ultimateCreditor")
    public String getUltimateCreditor() {
        return ultimateCreditor;
    }

    public void setUltimateCreditor(String ultimateCreditor) {
        this.ultimateCreditor = ultimateCreditor;
    }

    public EntryDetailsElement debtorName(String debtorName) {
        this.debtorName = debtorName;
        return this;
    }

    /**
     * Get debtorName
     *
     * @return debtorName
     **/
    @ApiModelProperty(value = "")

    @Size(max = 70)

    @JsonProperty("debtorName")
    public String getDebtorName() {
        return debtorName;
    }

    public void setDebtorName(String debtorName) {
        this.debtorName = debtorName;
    }

    public EntryDetailsElement debtorAccount(AccountReference debtorAccount) {
        this.debtorAccount = debtorAccount;
        return this;
    }

    /**
     * Get debtorAccount
     *
     * @return debtorAccount
     **/
    @ApiModelProperty(value = "")

    @Valid


    @JsonProperty("debtorAccount")
    public AccountReference getDebtorAccount() {
        return debtorAccount;
    }

    public void setDebtorAccount(AccountReference debtorAccount) {
        this.debtorAccount = debtorAccount;
    }

    public EntryDetailsElement debtorAgent(String debtorAgent) {
        this.debtorAgent = debtorAgent;
        return this;
    }

    /**
     * Get debtorAgent
     *
     * @return debtorAgent
     **/
    @ApiModelProperty(value = "")

    @Pattern(regexp = "[A-Z]{6,6}[A-Z2-9][A-NP-Z0-9]([A-Z0-9]{3,3}){0,1}")

    @JsonProperty("debtorAgent")
    public String getDebtorAgent() {
        return debtorAgent;
    }

    public void setDebtorAgent(String debtorAgent) {
        this.debtorAgent = debtorAgent;
    }

    public EntryDetailsElement ultimateDebtor(String ultimateDebtor) {
        this.ultimateDebtor = ultimateDebtor;
        return this;
    }

    /**
     * Get ultimateDebtor
     *
     * @return ultimateDebtor
     **/
    @ApiModelProperty(value = "")

    @Size(max = 70)

    @JsonProperty("ultimateDebtor")
    public String getUltimateDebtor() {
        return ultimateDebtor;
    }

    public void setUltimateDebtor(String ultimateDebtor) {
        this.ultimateDebtor = ultimateDebtor;
    }

    public EntryDetailsElement remittanceInformationUnstructured(String remittanceInformationUnstructured) {
        this.remittanceInformationUnstructured = remittanceInformationUnstructured;
        return this;
    }

    /**
     * Get remittanceInformationUnstructured
     *
     * @return remittanceInformationUnstructured
     **/
    @ApiModelProperty(value = "")

    @Size(max = 140)

    @JsonProperty("remittanceInformationUnstructured")
    public String getRemittanceInformationUnstructured() {
        return remittanceInformationUnstructured;
    }

    public void setRemittanceInformationUnstructured(String remittanceInformationUnstructured) {
        this.remittanceInformationUnstructured = remittanceInformationUnstructured;
    }

    public EntryDetailsElement remittanceInformationUnstructuredArray(RemittanceInformationUnstructuredArray remittanceInformationUnstructuredArray) {
        this.remittanceInformationUnstructuredArray = remittanceInformationUnstructuredArray;
        return this;
    }

    /**
     * Get remittanceInformationUnstructuredArray
     *
     * @return remittanceInformationUnstructuredArray
     **/
    @ApiModelProperty(value = "")

    @Valid


    @JsonProperty("remittanceInformationUnstructuredArray")
    public RemittanceInformationUnstructuredArray getRemittanceInformationUnstructuredArray() {
        return remittanceInformationUnstructuredArray;
    }

    public void setRemittanceInformationUnstructuredArray(RemittanceInformationUnstructuredArray remittanceInformationUnstructuredArray) {
        this.remittanceInformationUnstructuredArray = remittanceInformationUnstructuredArray;
    }

    public EntryDetailsElement remittanceInformationStructured(RemittanceInformationStructured remittanceInformationStructured) {
        this.remittanceInformationStructured = remittanceInformationStructured;
        return this;
    }

    /**
     * Get remittanceInformationStructured
     *
     * @return remittanceInformationStructured
     **/
    @ApiModelProperty(value = "")

    @Valid


    @JsonProperty("remittanceInformationStructured")
    public RemittanceInformationStructured getRemittanceInformationStructured() {
        return remittanceInformationStructured;
    }

    public void setRemittanceInformationStructured(RemittanceInformationStructured remittanceInformationStructured) {
        this.remittanceInformationStructured = remittanceInformationStructured;
    }

    public EntryDetailsElement remittanceInformationStructuredArray(RemittanceInformationStructuredArray remittanceInformationStructuredArray) {
        this.remittanceInformationStructuredArray = remittanceInformationStructuredArray;
        return this;
    }

    /**
     * Get remittanceInformationStructuredArray
     *
     * @return remittanceInformationStructuredArray
     **/
    @ApiModelProperty(value = "")

    @Valid


    @JsonProperty("remittanceInformationStructuredArray")
    public RemittanceInformationStructuredArray getRemittanceInformationStructuredArray() {
        return remittanceInformationStructuredArray;
    }

    public void setRemittanceInformationStructuredArray(RemittanceInformationStructuredArray remittanceInformationStructuredArray) {
        this.remittanceInformationStructuredArray = remittanceInformationStructuredArray;
    }

    public EntryDetailsElement purposeCode(PurposeCode purposeCode) {
        this.purposeCode = purposeCode;
        return this;
    }

    /**
     * Get purposeCode
     *
     * @return purposeCode
     **/
    @ApiModelProperty(value = "")

    @Valid


    @JsonProperty("purposeCode")
    public PurposeCode getPurposeCode() {
        return purposeCode;
    }

    public void setPurposeCode(PurposeCode purposeCode) {
        this.purposeCode = purposeCode;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EntryDetailsElement entryDetailsElement = (EntryDetailsElement) o;
        return Objects.equals(this.endToEndId, entryDetailsElement.endToEndId) &&
                   Objects.equals(this.mandateId, entryDetailsElement.mandateId) &&
                   Objects.equals(this.checkId, entryDetailsElement.checkId) &&
                   Objects.equals(this.creditorId, entryDetailsElement.creditorId) &&
                   Objects.equals(this.transactionAmount, entryDetailsElement.transactionAmount) &&
                   Objects.equals(this.currencyExchange, entryDetailsElement.currencyExchange) &&
                   Objects.equals(this.creditorName, entryDetailsElement.creditorName) &&
                   Objects.equals(this.creditorAccount, entryDetailsElement.creditorAccount) &&
                   Objects.equals(this.creditorAgent, entryDetailsElement.creditorAgent) &&
                   Objects.equals(this.ultimateCreditor, entryDetailsElement.ultimateCreditor) &&
                   Objects.equals(this.debtorName, entryDetailsElement.debtorName) &&
                   Objects.equals(this.debtorAccount, entryDetailsElement.debtorAccount) &&
                   Objects.equals(this.debtorAgent, entryDetailsElement.debtorAgent) &&
                   Objects.equals(this.ultimateDebtor, entryDetailsElement.ultimateDebtor) &&
                   Objects.equals(this.remittanceInformationUnstructured, entryDetailsElement.remittanceInformationUnstructured) &&
                   Objects.equals(this.remittanceInformationUnstructuredArray, entryDetailsElement.remittanceInformationUnstructuredArray) &&
                   Objects.equals(this.remittanceInformationStructured, entryDetailsElement.remittanceInformationStructured) &&
                   Objects.equals(this.remittanceInformationStructuredArray, entryDetailsElement.remittanceInformationStructuredArray) &&
                   Objects.equals(this.purposeCode, entryDetailsElement.purposeCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endToEndId, mandateId, checkId, creditorId, transactionAmount, currencyExchange, creditorName, creditorAccount, creditorAgent, ultimateCreditor, debtorName, debtorAccount, debtorAgent, ultimateDebtor, remittanceInformationUnstructured, remittanceInformationUnstructuredArray, remittanceInformationStructured, remittanceInformationStructuredArray, purposeCode);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class EntryDetailsElement {\n");

        sb.append("    endToEndId: ").append(toIndentedString(endToEndId)).append("\n");
        sb.append("    mandateId: ").append(toIndentedString(mandateId)).append("\n");
        sb.append("    checkId: ").append(toIndentedString(checkId)).append("\n");
        sb.append("    creditorId: ").append(toIndentedString(creditorId)).append("\n");
        sb.append("    transactionAmount: ").append(toIndentedString(transactionAmount)).append("\n");
        sb.append("    currencyExchange: ").append(toIndentedString(currencyExchange)).append("\n");
        sb.append("    creditorName: ").append(toIndentedString(creditorName)).append("\n");
        sb.append("    creditorAccount: ").append(toIndentedString(creditorAccount)).append("\n");
        sb.append("    creditorAgent: ").append(toIndentedString(creditorAgent)).append("\n");
        sb.append("    ultimateCreditor: ").append(toIndentedString(ultimateCreditor)).append("\n");
        sb.append("    debtorName: ").append(toIndentedString(debtorName)).append("\n");
        sb.append("    debtorAccount: ").append(toIndentedString(debtorAccount)).append("\n");
        sb.append("    debtorAgent: ").append(toIndentedString(debtorAgent)).append("\n");
        sb.append("    ultimateDebtor: ").append(toIndentedString(ultimateDebtor)).append("\n");
        sb.append("    remittanceInformationUnstructured: ").append(toIndentedString(remittanceInformationUnstructured)).append("\n");
        sb.append("    remittanceInformationUnstructuredArray: ").append(toIndentedString(remittanceInformationUnstructuredArray)).append("\n");
        sb.append("    remittanceInformationStructured: ").append(toIndentedString(remittanceInformationStructured)).append("\n");
        sb.append("    remittanceInformationStructuredArray: ").append(toIndentedString(remittanceInformationStructuredArray)).append("\n");
        sb.append("    purposeCode: ").append(toIndentedString(purposeCode)).append("\n");
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

