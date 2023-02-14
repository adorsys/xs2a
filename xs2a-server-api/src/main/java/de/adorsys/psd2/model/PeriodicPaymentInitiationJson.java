/*
 * Copyright 2018-2023 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Generic Body for a periodic payment initation via JSON.  This generic JSON body can be used to represent valid periodic payment initiations for the following JSON based payment product,  which where defined in the Implementation Guidelines:    * sepa-credit-transfers   * instant-sepa-credit-transfers   * target-2-payments   * cross-border-credit-transfers  For the convenience of the implementer additional which are already predefinded in the Implementation Guidelines  are included (but commented in source code), such that an ASPSP may add them easily.  Take care: Since the format is intended to fit for all payment products  there are additional conditions which are NOT covered by this specification. Please check the Implementation Guidelines for detailes.   The following data element are depending on the actual payment product available (in source code):             &lt;table style&#x3D;\&quot;width:100%\&quot;&gt;  &lt;tr&gt;&lt;th&gt;Data Element&lt;/th&gt;&lt;th&gt;SCT EU Core&lt;/th&gt;&lt;th&gt;SCT INST EU Core&lt;/th&gt;&lt;th&gt;Target2 Paym. Core&lt;/th&gt;&lt;th&gt;Cross Border CT Core&lt;/th&gt;&lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;endToEndIdentification&lt;/td&gt;&lt;td&gt; optional&lt;/td&gt; &lt;td&gt;optional&lt;/td&gt; &lt;td&gt;optional&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;instructionIdentification&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;debtorName&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;debtorAccount&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;debtorId&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;ultimateDebtor&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;instructedAmount&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;currencyOfTransfer&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;exchangeRateInformation&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt;&lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;creditorAccount&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;creditorAgent&lt;/td&gt; &lt;td&gt;optional&lt;/td&gt; &lt;td&gt;optional&lt;/td&gt; &lt;td&gt;optional&lt;/td&gt; &lt;td&gt;conditional &lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;creditorAgentName&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;creditorName&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;creditorId&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;creditorAddress&lt;/td&gt;optional&lt;/td&gt; &lt;td&gt;optional&lt;/td&gt; &lt;td&gt;optional&lt;/td&gt; &lt;td&gt;conditional &lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;creditorNameAndAddress&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;ultimateCreditor&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;purposeCode&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;chargeBearer&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;optional&lt;/td&gt; &lt;td&gt;conditional &lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;serviceLevel&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a. &lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;remittanceInformationUnstructured&lt;/td&gt; &lt;td&gt;optional&lt;/td&gt; &lt;td&gt;optional&lt;/td&gt; &lt;td&gt; optional&lt;/td&gt; &lt;td&gt;optional&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;remittanceInformationUnstructuredArray&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;remittanceInformationStructured&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;remittanceInformationStructuredArray&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;requestedExecutionDate&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;requestedExecutionTime&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;startDate&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;executionRule&lt;/td&gt; &lt;td&gt;optional&lt;/td&gt; &lt;td&gt;optional&lt;/td&gt; &lt;td&gt;optional&lt;/td&gt; &lt;td&gt;optional&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;endDate&lt;/td&gt; &lt;td&gt;optional&lt;/td&gt; &lt;td&gt;optional&lt;/td&gt; &lt;td&gt;optional&lt;/td&gt; &lt;td&gt;optional&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;frequency&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;dayOfExecution&lt;/td&gt; &lt;td&gt;conditional&lt;/td&gt; &lt;td&gt;conditional&lt;/td&gt; &lt;td&gt;conditional&lt;/td&gt; &lt;td&gt;conditional&lt;/td&gt; &lt;/tr&gt;     &lt;/td&gt;&lt;/tr&gt;   &lt;/table&gt;    IMPORTANT: In this API definition the following holds:   *  All data elements mentioned above are defined, but some of them are commented,      i.e. they are only visible in the source code and can be used by uncommenting them.   * Data elements which are mandatory in the table above for all payment products      are set to be mandatory in this specification.   * Data elements which are indicated in the table above as n.a. for all payment products are commented in the source code.   * Data elements which are indicated to be option, conditional or mandatory for at least one payment product      in the table above are set to be optional in the s specification except the case where all are definde to be mandatory.    * Data element which are inticated to be n.a. can be used by the ASPS if needed.      In this case uncomment tthe the relatetd lines in the source code.   * If one uses this data types for some payment products he has to ensure that the used data type is      valid according to the underlying payment product, e.g. by some appropriate validations.
 */
@Schema(description = "Generic Body for a periodic payment initation via JSON.  This generic JSON body can be used to represent valid periodic payment initiations for the following JSON based payment product,  which where defined in the Implementation Guidelines:    * sepa-credit-transfers   * instant-sepa-credit-transfers   * target-2-payments   * cross-border-credit-transfers  For the convenience of the implementer additional which are already predefinded in the Implementation Guidelines  are included (but commented in source code), such that an ASPSP may add them easily.  Take care: Since the format is intended to fit for all payment products  there are additional conditions which are NOT covered by this specification. Please check the Implementation Guidelines for detailes.   The following data element are depending on the actual payment product available (in source code):             <table style=\"width:100%\">  <tr><th>Data Element</th><th>SCT EU Core</th><th>SCT INST EU Core</th><th>Target2 Paym. Core</th><th>Cross Border CT Core</th></tr>  <tr><td>endToEndIdentification</td><td> optional</td> <td>optional</td> <td>optional</td> <td>n.a.</td> </tr>  <tr><td>instructionIdentification</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> </tr>  <tr><td>debtorName</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> </tr>  <tr><td>debtorAccount</td> <td>mandatory</td> <td>mandatory</td> <td>mandatory</td> <td>mandatory</td> </tr>  <tr><td>debtorId</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> </tr>  <tr><td>ultimateDebtor</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> </tr>  <tr><td>instructedAmount</td> <td>mandatory</td> <td>mandatory</td> <td>mandatory</td> <td>mandatory</td> </tr>  <tr><td>currencyOfTransfer</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> </tr>  <tr><td>exchangeRateInformation</td> <td>n.a.</td> <td>n.a.</td><td>n.a.</td> <td>n.a.</td> </tr>  <tr><td>creditorAccount</td> <td>mandatory</td> <td>mandatory</td> <td>mandatory</td> <td>mandatory</td> </tr>  <tr><td>creditorAgent</td> <td>optional</td> <td>optional</td> <td>optional</td> <td>conditional </td> </tr>  <tr><td>creditorAgentName</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> </tr>  <tr><td>creditorName</td> <td>mandatory</td> <td>mandatory</td> <td>mandatory</td> <td>mandatory</td> </tr>  <tr><td>creditorId</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> </tr>  <tr><td>creditorAddress</td>optional</td> <td>optional</td> <td>optional</td> <td>conditional </td> </tr>  <tr><td>creditorNameAndAddress</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> </tr>  <tr><td>ultimateCreditor</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> </tr>  <tr><td>purposeCode</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> </tr>  <tr><td>chargeBearer</td> <td>n.a.</td> <td>n.a.</td> <td>optional</td> <td>conditional </td> </tr>  <tr><td>serviceLevel</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> <td>n.a. </td> </tr>  <tr><td>remittanceInformationUnstructured</td> <td>optional</td> <td>optional</td> <td> optional</td> <td>optional</td> </tr>  <tr><td>remittanceInformationUnstructuredArray</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> </tr>  <tr><td>remittanceInformationStructured</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> </tr>  <tr><td>remittanceInformationStructuredArray</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> </tr>  <tr><td>requestedExecutionDate</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> </tr>  <tr><td>requestedExecutionTime</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> </tr>  <tr><td>startDate</td> <td>mandatory</td> <td>mandatory</td> <td>mandatory</td> <td>mandatory</td> </tr>  <tr><td>executionRule</td> <td>optional</td> <td>optional</td> <td>optional</td> <td>optional</td> </tr>  <tr><td>endDate</td> <td>optional</td> <td>optional</td> <td>optional</td> <td>optional</td> </tr>  <tr><td>frequency</td> <td>mandatory</td> <td>mandatory</td> <td>mandatory</td> <td>mandatory</td> </tr>  <tr><td>dayOfExecution</td> <td>conditional</td> <td>conditional</td> <td>conditional</td> <td>conditional</td> </tr>     </td></tr>   </table>    IMPORTANT: In this API definition the following holds:   *  All data elements mentioned above are defined, but some of them are commented,      i.e. they are only visible in the source code and can be used by uncommenting them.   * Data elements which are mandatory in the table above for all payment products      are set to be mandatory in this specification.   * Data elements which are indicated in the table above as n.a. for all payment products are commented in the source code.   * Data elements which are indicated to be option, conditional or mandatory for at least one payment product      in the table above are set to be optional in the s specification except the case where all are definde to be mandatory.    * Data element which are inticated to be n.a. can be used by the ASPS if needed.      In this case uncomment tthe the relatetd lines in the source code.   * If one uses this data types for some payment products he has to ensure that the used data type is      valid according to the underlying payment product, e.g. by some appropriate validations. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


public class PeriodicPaymentInitiationJson   {
  @JsonProperty("endToEndIdentification")
  private String endToEndIdentification = null;

  @JsonProperty("instructionIdentification")
  private String instructionIdentification = null;

  @JsonProperty("debtorName")
  private String debtorName = null;

  @JsonProperty("debtorAccount")
  private AccountReference debtorAccount = null;

  @JsonProperty("ultimateDebtor")
  private String ultimateDebtor = null;

  @JsonProperty("instructedAmount")
  private Amount instructedAmount = null;

  @JsonProperty("creditorAccount")
  private AccountReference creditorAccount = null;

  @JsonProperty("creditorAgent")
  private String creditorAgent = null;

  @JsonProperty("creditorId")
  private String creditorId = null;

  @JsonProperty("creditorName")
  private String creditorName = null;

  @JsonProperty("creditorAddress")
  private Address creditorAddress = null;

  @JsonProperty("ultimateCreditor")
  private String ultimateCreditor = null;

  @JsonProperty("purposeCode")
  private PurposeCode purposeCode = null;

  @JsonProperty("remittanceInformationUnstructured")
  private String remittanceInformationUnstructured = null;

  @JsonProperty("remittanceInformationUnstructuredArray")
  private RemittanceInformationUnstructuredArray remittanceInformationUnstructuredArray = null;

  @JsonProperty("remittanceInformationStructured")
  private RemittanceInformationStructuredMax140 remittanceInformationStructured = null;

  @JsonProperty("remittanceInformationStructuredArray")
  private RemittanceInformationStructuredArray remittanceInformationStructuredArray = null;

  @JsonProperty("startDate")
  private LocalDate startDate = null;

  @JsonProperty("endDate")
  private LocalDate endDate = null;

  @JsonProperty("executionRule")
  private ExecutionRule executionRule = null;

  @JsonProperty("frequency")
  private FrequencyCode frequency = null;

  @JsonProperty("dayOfExecution")
  private DayOfExecution dayOfExecution = null;

  @JsonProperty("monthsOfExecution")
  private MonthsOfExecution monthsOfExecution = null;

  public PeriodicPaymentInitiationJson endToEndIdentification(String endToEndIdentification) {
    this.endToEndIdentification = endToEndIdentification;
    return this;
  }

    /**
     * Get endToEndIdentification
     *
     * @return endToEndIdentification
     **/
    @Schema(description = "")
    @JsonProperty("endToEndIdentification")

    @Size(max = 35)
    public String getEndToEndIdentification() {
        return endToEndIdentification;
    }

  public void setEndToEndIdentification(String endToEndIdentification) {
    this.endToEndIdentification = endToEndIdentification;
  }

  public PeriodicPaymentInitiationJson instructionIdentification(String instructionIdentification) {
    this.instructionIdentification = instructionIdentification;
    return this;
  }

    /**
     * Get instructionIdentification
     *
     * @return instructionIdentification
     **/
    @Schema(description = "")
    @JsonProperty("instructionIdentification")

    @Size(max = 35)
    public String getInstructionIdentification() {
        return instructionIdentification;
  }

  public void setInstructionIdentification(String instructionIdentification) {
    this.instructionIdentification = instructionIdentification;
  }

  public PeriodicPaymentInitiationJson debtorName(String debtorName) {
    this.debtorName = debtorName;
      return this;
  }

    /**
     * Debtor name.
     *
     * @return debtorName
     **/
    @Schema(example = "Debtor Name", description = "Debtor name.")
    @JsonProperty("debtorName")

    @Size(max = 70)
    public String getDebtorName() {
        return debtorName;
  }

  public void setDebtorName(String debtorName) {
    this.debtorName = debtorName;
  }

  public PeriodicPaymentInitiationJson debtorAccount(AccountReference debtorAccount) {
    this.debtorAccount = debtorAccount;
      return this;
  }

    /**
     * Get debtorAccount
     *
     * @return debtorAccount
     **/
    @Schema(required = true, description = "")
    @JsonProperty("debtorAccount")
    @NotNull

    @Valid
    public AccountReference getDebtorAccount() {
    return debtorAccount;
  }

  public void setDebtorAccount(AccountReference debtorAccount) {
    this.debtorAccount = debtorAccount;
  }

  public PeriodicPaymentInitiationJson ultimateDebtor(String ultimateDebtor) {
    this.ultimateDebtor = ultimateDebtor;
      return this;
  }

    /**
     * Ultimate debtor.
     *
     * @return ultimateDebtor
     **/
    @Schema(example = "Ultimate Debtor", description = "Ultimate debtor.")
    @JsonProperty("ultimateDebtor")

    @Size(max = 70)
    public String getUltimateDebtor() {
    return ultimateDebtor;
  }

  public void setUltimateDebtor(String ultimateDebtor) {
    this.ultimateDebtor = ultimateDebtor;
  }

  public PeriodicPaymentInitiationJson instructedAmount(Amount instructedAmount) {
    this.instructedAmount = instructedAmount;
      return this;
  }

    /**
     * Get instructedAmount
     *
     * @return instructedAmount
     **/
    @Schema(required = true, description = "")
    @JsonProperty("instructedAmount")
    @NotNull

    @Valid
    public Amount getInstructedAmount() {
    return instructedAmount;
  }

  public void setInstructedAmount(Amount instructedAmount) {
    this.instructedAmount = instructedAmount;
  }

  public PeriodicPaymentInitiationJson creditorAccount(AccountReference creditorAccount) {
      this.creditorAccount = creditorAccount;
      return this;
  }

    /**
     * Get creditorAccount
     *
     * @return creditorAccount
     **/
    @Schema(required = true, description = "")
    @JsonProperty("creditorAccount")
    @NotNull

    @Valid
    public AccountReference getCreditorAccount() {
    return creditorAccount;
  }

  public void setCreditorAccount(AccountReference creditorAccount) {
    this.creditorAccount = creditorAccount;
  }

  public PeriodicPaymentInitiationJson creditorAgent(String creditorAgent) {
      this.creditorAgent = creditorAgent;
      return this;
  }

    /**
     * BICFI
     *
     * @return creditorAgent
     **/
    @Schema(example = "AAAADEBBXXX", description = "BICFI ")
    @JsonProperty("creditorAgent")

    @Pattern(regexp = "[A-Z]{6,6}[A-Z2-9][A-NP-Z0-9]([A-Z0-9]{3,3}){0,1}")   public String getCreditorAgent() {
    return creditorAgent;
  }

  public void setCreditorAgent(String creditorAgent) {
    this.creditorAgent = creditorAgent;
  }

  public PeriodicPaymentInitiationJson creditorId(String creditorId) {
      this.creditorId = creditorId;
      return this;
  }

    /**
     * Identification of Creditors, e.g. a SEPA Creditor ID.
     *
     * @return creditorId
     **/
    @Schema(description = "Identification of Creditors, e.g. a SEPA Creditor ID.")
    @JsonProperty("creditorId")

@Size(max=35)   public String getCreditorId() {
    return creditorId;
  }

  public void setCreditorId(String creditorId) {
    this.creditorId = creditorId;
  }

  public PeriodicPaymentInitiationJson creditorName(String creditorName) {
      this.creditorName = creditorName;
      return this;
  }

    /**
     * Creditor name.
     *
     * @return creditorName
     **/
    @Schema(example = "Creditor Name", required = true, description = "Creditor name.")
    @JsonProperty("creditorName")
    @NotNull

@Size(max=70)   public String getCreditorName() {
    return creditorName;
  }

  public void setCreditorName(String creditorName) {
    this.creditorName = creditorName;
  }

  public PeriodicPaymentInitiationJson creditorAddress(Address creditorAddress) {
      this.creditorAddress = creditorAddress;
      return this;
  }

    /**
     * Get creditorAddress
     *
     * @return creditorAddress
     **/
    @Schema(description = "")
    @JsonProperty("creditorAddress")

    @Valid
    public Address getCreditorAddress() {
    return creditorAddress;
  }

  public void setCreditorAddress(Address creditorAddress) {
    this.creditorAddress = creditorAddress;
  }

  public PeriodicPaymentInitiationJson ultimateCreditor(String ultimateCreditor) {
      this.ultimateCreditor = ultimateCreditor;
      return this;
  }

    /**
     * Ultimate creditor.
     *
     * @return ultimateCreditor
     **/
    @Schema(example = "Ultimate Creditor", description = "Ultimate creditor.")
  @JsonProperty("ultimateCreditor")

@Size(max=70)   public String getUltimateCreditor() {
    return ultimateCreditor;
  }

  public void setUltimateCreditor(String ultimateCreditor) {
    this.ultimateCreditor = ultimateCreditor;
  }

    public PeriodicPaymentInitiationJson purposeCode(PurposeCode purposeCode) {
        this.purposeCode = purposeCode;
        return this;
    }

    /**
     * Get purposeCode
     * @return purposeCode
   **/
  @Schema(description = "")
  @JsonProperty("purposeCode")

  @Valid
  public PurposeCode getPurposeCode() {
    return purposeCode;
  }

  public void setPurposeCode(PurposeCode purposeCode) {
    this.purposeCode = purposeCode;
  }

  public PeriodicPaymentInitiationJson remittanceInformationUnstructured(String remittanceInformationUnstructured) {
      this.remittanceInformationUnstructured = remittanceInformationUnstructured;
      return this;
  }

    /**
     * Unstructured remittance information.
     *
     * @return remittanceInformationUnstructured
     **/
    @Schema(example = "Ref Number Merchant", description = "Unstructured remittance information. ")
    @JsonProperty("remittanceInformationUnstructured")

@Size(max=140)   public String getRemittanceInformationUnstructured() {
    return remittanceInformationUnstructured;
  }

  public void setRemittanceInformationUnstructured(String remittanceInformationUnstructured) {
    this.remittanceInformationUnstructured = remittanceInformationUnstructured;
  }

  public PeriodicPaymentInitiationJson remittanceInformationUnstructuredArray(RemittanceInformationUnstructuredArray remittanceInformationUnstructuredArray) {
      this.remittanceInformationUnstructuredArray = remittanceInformationUnstructuredArray;
      return this;
  }

    /**
     * Get remittanceInformationUnstructuredArray
     *
     * @return remittanceInformationUnstructuredArray
     **/
    @Schema(description = "")
    @JsonProperty("remittanceInformationUnstructuredArray")

    @Valid
    public RemittanceInformationUnstructuredArray getRemittanceInformationUnstructuredArray() {
    return remittanceInformationUnstructuredArray;
  }

  public void setRemittanceInformationUnstructuredArray(RemittanceInformationUnstructuredArray remittanceInformationUnstructuredArray) {
    this.remittanceInformationUnstructuredArray = remittanceInformationUnstructuredArray;
  }

  public PeriodicPaymentInitiationJson remittanceInformationStructured(RemittanceInformationStructuredMax140 remittanceInformationStructured) {
      this.remittanceInformationStructured = remittanceInformationStructured;
      return this;
  }

    /**
     * Get remittanceInformationStructured
     *
     * @return remittanceInformationStructured
     **/
    @Schema(description = "")
    @JsonProperty("remittanceInformationStructured")

    @Valid
    public RemittanceInformationStructuredMax140 getRemittanceInformationStructured() {
    return remittanceInformationStructured;
  }

  public void setRemittanceInformationStructured(RemittanceInformationStructuredMax140 remittanceInformationStructured) {
    this.remittanceInformationStructured = remittanceInformationStructured;
  }

  public PeriodicPaymentInitiationJson remittanceInformationStructuredArray(RemittanceInformationStructuredArray remittanceInformationStructuredArray) {
      this.remittanceInformationStructuredArray = remittanceInformationStructuredArray;
      return this;
  }

    /**
     * Get remittanceInformationStructuredArray
     *
     * @return remittanceInformationStructuredArray
     **/
    @Schema(description = "")
    @JsonProperty("remittanceInformationStructuredArray")

    @Valid
    public RemittanceInformationStructuredArray getRemittanceInformationStructuredArray() {
    return remittanceInformationStructuredArray;
  }

  public void setRemittanceInformationStructuredArray(RemittanceInformationStructuredArray remittanceInformationStructuredArray) {
    this.remittanceInformationStructuredArray = remittanceInformationStructuredArray;
  }

    public PeriodicPaymentInitiationJson startDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    /**
     * The first applicable day of execution starting from this date is the first payment.
     *
     * @return startDate
     **/
    @Schema(required = true, description = "The first applicable day of execution starting from this date is the first payment. ")
  @JsonProperty("startDate")
    @NotNull

  @Valid
  public LocalDate getStartDate() {
    return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public PeriodicPaymentInitiationJson endDate(LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }

    /**
     * The last applicable day of execution. If not given, it is an infinite standing order.
     * @return endDate
   **/
  @Schema(description = "The last applicable day of execution. If not given, it is an infinite standing order. ")
  @JsonProperty("endDate")

  @Valid
  public LocalDate getEndDate() {
    return endDate;
  }

  public void setEndDate(LocalDate endDate) {
      this.endDate = endDate;
  }

    public PeriodicPaymentInitiationJson executionRule(ExecutionRule executionRule) {
        this.executionRule = executionRule;
        return this;
  }

  /**
   * Get executionRule
   * @return executionRule
   **/
  @Schema(description = "")
  @JsonProperty("executionRule")

  @Valid
  public ExecutionRule getExecutionRule() {
    return executionRule;
  }

  public void setExecutionRule(ExecutionRule executionRule) {
      this.executionRule = executionRule;
  }

    public PeriodicPaymentInitiationJson frequency(FrequencyCode frequency) {
        this.frequency = frequency;
        return this;
    }

  /**
   * Get frequency
   * @return frequency
   **/
  @Schema(required = true, description = "")
  @JsonProperty("frequency")
    @NotNull

  @Valid
  public FrequencyCode getFrequency() {
    return frequency;
  }

  public void setFrequency(FrequencyCode frequency) {
      this.frequency = frequency;
  }

    public PeriodicPaymentInitiationJson dayOfExecution(DayOfExecution dayOfExecution) {
        this.dayOfExecution = dayOfExecution;
        return this;
  }

  /**
   * Get dayOfExecution
   * @return dayOfExecution
   **/
  @Schema(description = "")
  @JsonProperty("dayOfExecution")

  @Valid
  public DayOfExecution getDayOfExecution() {
    return dayOfExecution;
  }

  public void setDayOfExecution(DayOfExecution dayOfExecution) {
      this.dayOfExecution = dayOfExecution;
  }

    public PeriodicPaymentInitiationJson monthsOfExecution(MonthsOfExecution monthsOfExecution) {
        this.monthsOfExecution = monthsOfExecution;
        return this;
    }

    /**
     * Get monthsOfExecution
   * @return monthsOfExecution
   **/
  @Schema(description = "")
  @JsonProperty("monthsOfExecution")

  @Valid
  public MonthsOfExecution getMonthsOfExecution() {
    return monthsOfExecution;
  }

  public void setMonthsOfExecution(MonthsOfExecution monthsOfExecution) {
    this.monthsOfExecution = monthsOfExecution;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PeriodicPaymentInitiationJson periodicPaymentInitiationJson = (PeriodicPaymentInitiationJson) o;
    return Objects.equals(this.endToEndIdentification, periodicPaymentInitiationJson.endToEndIdentification) &&
        Objects.equals(this.instructionIdentification, periodicPaymentInitiationJson.instructionIdentification) &&
        Objects.equals(this.debtorName, periodicPaymentInitiationJson.debtorName) &&
        Objects.equals(this.debtorAccount, periodicPaymentInitiationJson.debtorAccount) &&
        Objects.equals(this.ultimateDebtor, periodicPaymentInitiationJson.ultimateDebtor) &&
        Objects.equals(this.instructedAmount, periodicPaymentInitiationJson.instructedAmount) &&
        Objects.equals(this.creditorAccount, periodicPaymentInitiationJson.creditorAccount) &&
        Objects.equals(this.creditorAgent, periodicPaymentInitiationJson.creditorAgent) &&
        Objects.equals(this.creditorId, periodicPaymentInitiationJson.creditorId) &&
        Objects.equals(this.creditorName, periodicPaymentInitiationJson.creditorName) &&
        Objects.equals(this.creditorAddress, periodicPaymentInitiationJson.creditorAddress) &&
        Objects.equals(this.ultimateCreditor, periodicPaymentInitiationJson.ultimateCreditor) &&
        Objects.equals(this.purposeCode, periodicPaymentInitiationJson.purposeCode) &&
        Objects.equals(this.remittanceInformationUnstructured, periodicPaymentInitiationJson.remittanceInformationUnstructured) &&
        Objects.equals(this.remittanceInformationUnstructuredArray, periodicPaymentInitiationJson.remittanceInformationUnstructuredArray) &&
        Objects.equals(this.remittanceInformationStructured, periodicPaymentInitiationJson.remittanceInformationStructured) &&
        Objects.equals(this.remittanceInformationStructuredArray, periodicPaymentInitiationJson.remittanceInformationStructuredArray) &&
        Objects.equals(this.startDate, periodicPaymentInitiationJson.startDate) &&
        Objects.equals(this.endDate, periodicPaymentInitiationJson.endDate) &&
        Objects.equals(this.executionRule, periodicPaymentInitiationJson.executionRule) &&
        Objects.equals(this.frequency, periodicPaymentInitiationJson.frequency) &&
        Objects.equals(this.dayOfExecution, periodicPaymentInitiationJson.dayOfExecution) &&
        Objects.equals(this.monthsOfExecution, periodicPaymentInitiationJson.monthsOfExecution);
  }

  @Override
  public int hashCode() {
    return Objects.hash(endToEndIdentification, instructionIdentification, debtorName, debtorAccount, ultimateDebtor, instructedAmount, creditorAccount, creditorAgent, creditorId, creditorName, creditorAddress, ultimateCreditor, purposeCode, remittanceInformationUnstructured, remittanceInformationUnstructuredArray, remittanceInformationStructured, remittanceInformationStructuredArray, startDate, endDate, executionRule, frequency, dayOfExecution, monthsOfExecution);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PeriodicPaymentInitiationJson {\n");

    sb.append("    endToEndIdentification: ").append(toIndentedString(endToEndIdentification)).append("\n");
    sb.append("    instructionIdentification: ").append(toIndentedString(instructionIdentification)).append("\n");
    sb.append("    debtorName: ").append(toIndentedString(debtorName)).append("\n");
    sb.append("    debtorAccount: ").append(toIndentedString(debtorAccount)).append("\n");
    sb.append("    ultimateDebtor: ").append(toIndentedString(ultimateDebtor)).append("\n");
    sb.append("    instructedAmount: ").append(toIndentedString(instructedAmount)).append("\n");
    sb.append("    creditorAccount: ").append(toIndentedString(creditorAccount)).append("\n");
    sb.append("    creditorAgent: ").append(toIndentedString(creditorAgent)).append("\n");
    sb.append("    creditorId: ").append(toIndentedString(creditorId)).append("\n");
    sb.append("    creditorName: ").append(toIndentedString(creditorName)).append("\n");
    sb.append("    creditorAddress: ").append(toIndentedString(creditorAddress)).append("\n");
    sb.append("    ultimateCreditor: ").append(toIndentedString(ultimateCreditor)).append("\n");
    sb.append("    purposeCode: ").append(toIndentedString(purposeCode)).append("\n");
    sb.append("    remittanceInformationUnstructured: ").append(toIndentedString(remittanceInformationUnstructured)).append("\n");
    sb.append("    remittanceInformationUnstructuredArray: ").append(toIndentedString(remittanceInformationUnstructuredArray)).append("\n");
    sb.append("    remittanceInformationStructured: ").append(toIndentedString(remittanceInformationStructured)).append("\n");
    sb.append("    remittanceInformationStructuredArray: ").append(toIndentedString(remittanceInformationStructuredArray)).append("\n");
    sb.append("    startDate: ").append(toIndentedString(startDate)).append("\n");
    sb.append("    endDate: ").append(toIndentedString(endDate)).append("\n");
    sb.append("    executionRule: ").append(toIndentedString(executionRule)).append("\n");
    sb.append("    frequency: ").append(toIndentedString(frequency)).append("\n");
    sb.append("    dayOfExecution: ").append(toIndentedString(dayOfExecution)).append("\n");
    sb.append("    monthsOfExecution: ").append(toIndentedString(monthsOfExecution)).append("\n");
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
