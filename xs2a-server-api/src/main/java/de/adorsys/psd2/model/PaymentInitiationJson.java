package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Generic Body for a payment initation via JSON.  This generic JSON body can be used to represent valid payment initiations for the following JSON based payment product,  which where defined in the Implementation Guidelines:    * sepa-credit-transfers   * instant-sepa-credit-transfers   * target-2-payments   * cross-border-credit-transfers  For the convenience of the implementer additional which are already predefinded in the Implementation Guidelines  are included (but commented in source code), such that an ASPSP may add them easily.  Take care: Since the format is intended to fit for all payment products  there are additional conditions which are NOT covered by this specification. Please check the Implementation Guidelines for detailes.   The following data element are depending on the actual payment product available (in source code):             &lt;table style&#x3D;\&quot;width:100%\&quot;&gt;  &lt;tr&gt;&lt;th&gt;Data Element&lt;/th&gt;&lt;th&gt;SCT EU Core&lt;/th&gt;&lt;th&gt;SCT INST EU Core&lt;/th&gt;&lt;th&gt;Target2 Paym. Core&lt;/th&gt;&lt;th&gt;Cross Border CT Core&lt;/th&gt;&lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;endToEndIdentification&lt;/td&gt;&lt;td&gt; optional&lt;/td&gt; &lt;td&gt;optional&lt;/td&gt; &lt;td&gt;optional&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;instructionIdentification&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;debtorName&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;debtorAccount&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;debtorId&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;ultimateDebtor&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;instructedAmount&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;currencyOfTransfer&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;exchangeRateInformation&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt;&lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;creditorAccount&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;creditorAgent&lt;/td&gt; &lt;td&gt;optional&lt;/td&gt; &lt;td&gt;optional&lt;/td&gt; &lt;td&gt;optional&lt;/td&gt; &lt;td&gt;conditional &lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;creditorAgentName&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;creditorName&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;td&gt;mandatory&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;creditorId&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;creditorAddress&lt;/td&gt;optional&lt;/td&gt; &lt;td&gt;optional&lt;/td&gt; &lt;td&gt;optional&lt;/td&gt; &lt;td&gt;conditional &lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;creditorNameAndAddress&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;ultimateCreditor&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;purposeCode&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;chargeBearer&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;optional&lt;/td&gt; &lt;td&gt;conditional &lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;serviceLevel&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a. &lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;remittanceInformationUnstructured&lt;/td&gt; &lt;td&gt;optional&lt;/td&gt; &lt;td&gt;optional&lt;/td&gt; &lt;td&gt; optional&lt;/td&gt; &lt;td&gt;optional&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;remittanceInformationUnstructuredArray&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;remittanceInformationStructured&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;remittanceInformationStructuredArray&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;requestedExecutionDate&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;&lt;td&gt;requestedExecutionTime&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;td&gt;n.a.&lt;/td&gt; &lt;/tr&gt;     &lt;/td&gt;&lt;/tr&gt;   &lt;/table&gt;    IMPORTANT: In this API definition the following holds:   *  All data elements mentioned above are defined, but some of them are commented,      i.e. they are only visible in the source code and can be used by uncommenting them.   * Data elements which are mandatory in the table above for all payment products      are set to be mandatory in this specification.   * Data elements which are indicated in the table above as n.a. for all payment products are commented in the source code.   * Data elements which are indicated to be option, conditional or mandatory for at least one payment product      in the table above are set to be optional in the s specification except the case where all are definde to be mandatory.    * Data element which are inticated to be n.a. can be used by the ASPS if needed.      In this case uncomment tthe the relatetd lines in the source code.   * If one uses this data types for some payment products he has to ensure that the used data type is      valid according to the underlying payment product, e.g. by some appropriate validations.
 */
@ApiModel(description = "Generic Body for a payment initation via JSON.  This generic JSON body can be used to represent valid payment initiations for the following JSON based payment product,  which where defined in the Implementation Guidelines:    * sepa-credit-transfers   * instant-sepa-credit-transfers   * target-2-payments   * cross-border-credit-transfers  For the convenience of the implementer additional which are already predefinded in the Implementation Guidelines  are included (but commented in source code), such that an ASPSP may add them easily.  Take care: Since the format is intended to fit for all payment products  there are additional conditions which are NOT covered by this specification. Please check the Implementation Guidelines for detailes.   The following data element are depending on the actual payment product available (in source code):             <table style=\"width:100%\">  <tr><th>Data Element</th><th>SCT EU Core</th><th>SCT INST EU Core</th><th>Target2 Paym. Core</th><th>Cross Border CT Core</th></tr>  <tr><td>endToEndIdentification</td><td> optional</td> <td>optional</td> <td>optional</td> <td>n.a.</td> </tr>  <tr><td>instructionIdentification</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> </tr>  <tr><td>debtorName</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> </tr>  <tr><td>debtorAccount</td> <td>mandatory</td> <td>mandatory</td> <td>mandatory</td> <td>mandatory</td> </tr>  <tr><td>debtorId</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> </tr>  <tr><td>ultimateDebtor</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> </tr>  <tr><td>instructedAmount</td> <td>mandatory</td> <td>mandatory</td> <td>mandatory</td> <td>mandatory</td> </tr>  <tr><td>currencyOfTransfer</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> </tr>  <tr><td>exchangeRateInformation</td> <td>n.a.</td> <td>n.a.</td><td>n.a.</td> <td>n.a.</td> </tr>  <tr><td>creditorAccount</td> <td>mandatory</td> <td>mandatory</td> <td>mandatory</td> <td>mandatory</td> </tr>  <tr><td>creditorAgent</td> <td>optional</td> <td>optional</td> <td>optional</td> <td>conditional </td> </tr>  <tr><td>creditorAgentName</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> </tr>  <tr><td>creditorName</td> <td>mandatory</td> <td>mandatory</td> <td>mandatory</td> <td>mandatory</td> </tr>  <tr><td>creditorId</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> </tr>  <tr><td>creditorAddress</td>optional</td> <td>optional</td> <td>optional</td> <td>conditional </td> </tr>  <tr><td>creditorNameAndAddress</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> </tr>  <tr><td>ultimateCreditor</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> </tr>  <tr><td>purposeCode</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> </tr>  <tr><td>chargeBearer</td> <td>n.a.</td> <td>n.a.</td> <td>optional</td> <td>conditional </td> </tr>  <tr><td>serviceLevel</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> <td>n.a. </td> </tr>  <tr><td>remittanceInformationUnstructured</td> <td>optional</td> <td>optional</td> <td> optional</td> <td>optional</td> </tr>  <tr><td>remittanceInformationUnstructuredArray</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> </tr>  <tr><td>remittanceInformationStructured</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> </tr>  <tr><td>remittanceInformationStructuredArray</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> </tr>  <tr><td>requestedExecutionDate</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> </tr>  <tr><td>requestedExecutionTime</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> <td>n.a.</td> </tr>     </td></tr>   </table>    IMPORTANT: In this API definition the following holds:   *  All data elements mentioned above are defined, but some of them are commented,      i.e. they are only visible in the source code and can be used by uncommenting them.   * Data elements which are mandatory in the table above for all payment products      are set to be mandatory in this specification.   * Data elements which are indicated in the table above as n.a. for all payment products are commented in the source code.   * Data elements which are indicated to be option, conditional or mandatory for at least one payment product      in the table above are set to be optional in the s specification except the case where all are definde to be mandatory.    * Data element which are inticated to be n.a. can be used by the ASPS if needed.      In this case uncomment tthe the relatetd lines in the source code.   * If one uses this data types for some payment products he has to ensure that the used data type is      valid according to the underlying payment product, e.g. by some appropriate validations. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-05T12:22:49.487689+02:00[Europe/Kiev]")

public class PaymentInitiationJson   {
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

  @JsonProperty("creditorAgentName")
  private String creditorAgentName = null;

  @JsonProperty("creditorName")
  private String creditorName = null;

  @JsonProperty("creditorAddress")
  private Address creditorAddress = null;

  @JsonProperty("creditorId")
  private String creditorId = null;

  @JsonProperty("ultimateCreditor")
  private String ultimateCreditor = null;

  @JsonProperty("purposeCode")
  private PurposeCode purposeCode = null;

  @JsonProperty("chargeBearer")
  private ChargeBearer chargeBearer = null;

  @JsonProperty("remittanceInformationUnstructured")
  private String remittanceInformationUnstructured = null;

  @JsonProperty("remittanceInformationStructured")
  private String remittanceInformationStructured = null;

  @JsonProperty("remittanceInformationStructuredArray")
  private RemittanceInformationStructuredArray remittanceInformationStructuredArray = null;

  @JsonProperty("requestedExecutionDate")
  private LocalDate requestedExecutionDate = null;

  public PaymentInitiationJson endToEndIdentification(String endToEndIdentification) {
    this.endToEndIdentification = endToEndIdentification;
    return this;
  }

  /**
   * Get endToEndIdentification
   * @return endToEndIdentification
  **/
  @ApiModelProperty(value = "")

@Size(max=35)

  @JsonProperty("endToEndIdentification")
  public String getEndToEndIdentification() {
    return endToEndIdentification;
  }

  public void setEndToEndIdentification(String endToEndIdentification) {
    this.endToEndIdentification = endToEndIdentification;
  }

  public PaymentInitiationJson instructionIdentification(String instructionIdentification) {
    this.instructionIdentification = instructionIdentification;
    return this;
  }

  /**
   * Get instructionIdentification
   * @return instructionIdentification
  **/
  @ApiModelProperty(value = "")

@Size(max=35)

  @JsonProperty("instructionIdentification")
  public String getInstructionIdentification() {
    return instructionIdentification;
  }

  public void setInstructionIdentification(String instructionIdentification) {
    this.instructionIdentification = instructionIdentification;
  }

  public PaymentInitiationJson debtorName(String debtorName) {
    this.debtorName = debtorName;
    return this;
  }

  /**
   * Get debtorName
   * @return debtorName
  **/
  @ApiModelProperty(value = "")

@Size(max=70)

  @JsonProperty("debtorName")
  public String getDebtorName() {
    return debtorName;
  }

  public void setDebtorName(String debtorName) {
    this.debtorName = debtorName;
  }

  public PaymentInitiationJson debtorAccount(AccountReference debtorAccount) {
    this.debtorAccount = debtorAccount;
    return this;
  }

  /**
   * Get debtorAccount
   * @return debtorAccount
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("debtorAccount")
  public AccountReference getDebtorAccount() {
    return debtorAccount;
  }

  public void setDebtorAccount(AccountReference debtorAccount) {
    this.debtorAccount = debtorAccount;
  }

  public PaymentInitiationJson ultimateDebtor(String ultimateDebtor) {
    this.ultimateDebtor = ultimateDebtor;
    return this;
  }

  /**
   * Get ultimateDebtor
   * @return ultimateDebtor
  **/
  @ApiModelProperty(value = "")

@Size(max=70)

  @JsonProperty("ultimateDebtor")
  public String getUltimateDebtor() {
    return ultimateDebtor;
  }

  public void setUltimateDebtor(String ultimateDebtor) {
    this.ultimateDebtor = ultimateDebtor;
  }

  public PaymentInitiationJson instructedAmount(Amount instructedAmount) {
    this.instructedAmount = instructedAmount;
    return this;
  }

  /**
   * Get instructedAmount
   * @return instructedAmount
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("instructedAmount")
  public Amount getInstructedAmount() {
    return instructedAmount;
  }

  public void setInstructedAmount(Amount instructedAmount) {
    this.instructedAmount = instructedAmount;
  }

  public PaymentInitiationJson creditorAccount(AccountReference creditorAccount) {
    this.creditorAccount = creditorAccount;
    return this;
  }

  /**
   * Get creditorAccount
   * @return creditorAccount
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("creditorAccount")
  public AccountReference getCreditorAccount() {
    return creditorAccount;
  }

  public void setCreditorAccount(AccountReference creditorAccount) {
    this.creditorAccount = creditorAccount;
  }

  public PaymentInitiationJson creditorAgent(String creditorAgent) {
    this.creditorAgent = creditorAgent;
    return this;
  }

  /**
   * Get creditorAgent
   * @return creditorAgent
  **/
  @ApiModelProperty(value = "")

@Pattern(regexp="[A-Z]{6,6}[A-Z2-9][A-NP-Z0-9]([A-Z0-9]{3,3}){0,1}")

  @JsonProperty("creditorAgent")
  public String getCreditorAgent() {
    return creditorAgent;
  }

  public void setCreditorAgent(String creditorAgent) {
    this.creditorAgent = creditorAgent;
  }

  public PaymentInitiationJson creditorAgentName(String creditorAgentName) {
    this.creditorAgentName = creditorAgentName;
    return this;
  }

  /**
   * Get creditorAgentName
   * @return creditorAgentName
  **/
  @ApiModelProperty(value = "")

@Size(max=140)

  @JsonProperty("creditorAgentName")
  public String getCreditorAgentName() {
    return creditorAgentName;
  }

  public void setCreditorAgentName(String creditorAgentName) {
    this.creditorAgentName = creditorAgentName;
  }

  public PaymentInitiationJson creditorName(String creditorName) {
    this.creditorName = creditorName;
    return this;
  }

  /**
   * Get creditorName
   * @return creditorName
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

@Size(max=70)

  @JsonProperty("creditorName")
  public String getCreditorName() {
    return creditorName;
  }

  public void setCreditorName(String creditorName) {
    this.creditorName = creditorName;
  }

  public PaymentInitiationJson creditorAddress(Address creditorAddress) {
    this.creditorAddress = creditorAddress;
    return this;
  }

  /**
   * Get creditorAddress
   * @return creditorAddress
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("creditorAddress")
  public Address getCreditorAddress() {
    return creditorAddress;
  }

  public void setCreditorAddress(Address creditorAddress) {
    this.creditorAddress = creditorAddress;
  }

  public PaymentInitiationJson creditorId(String creditorId) {
    this.creditorId = creditorId;
    return this;
  }

  /**
   * Identification of Creditors, e.g. a SEPA Creditor ID.
   * @return creditorId
  **/
  @ApiModelProperty(value = "Identification of Creditors, e.g. a SEPA Creditor ID.")

@Size(max=35)

  @JsonProperty("creditorId")
  public String getCreditorId() {
    return creditorId;
  }

  public void setCreditorId(String creditorId) {
    this.creditorId = creditorId;
  }

  public PaymentInitiationJson ultimateCreditor(String ultimateCreditor) {
    this.ultimateCreditor = ultimateCreditor;
    return this;
  }

  /**
   * Get ultimateCreditor
   * @return ultimateCreditor
  **/
  @ApiModelProperty(value = "")

@Size(max=70)

  @JsonProperty("ultimateCreditor")
  public String getUltimateCreditor() {
    return ultimateCreditor;
  }

  public void setUltimateCreditor(String ultimateCreditor) {
    this.ultimateCreditor = ultimateCreditor;
  }

  public PaymentInitiationJson purposeCode(PurposeCode purposeCode) {
    this.purposeCode = purposeCode;
    return this;
  }

  /**
   * Get purposeCode
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

  public PaymentInitiationJson chargeBearer(ChargeBearer chargeBearer) {
    this.chargeBearer = chargeBearer;
    return this;
  }

  /**
   * Get chargeBearer
   * @return chargeBearer
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("chargeBearer")
  public ChargeBearer getChargeBearer() {
    return chargeBearer;
  }

  public void setChargeBearer(ChargeBearer chargeBearer) {
    this.chargeBearer = chargeBearer;
  }

  public PaymentInitiationJson remittanceInformationUnstructured(String remittanceInformationUnstructured) {
    this.remittanceInformationUnstructured = remittanceInformationUnstructured;
    return this;
  }

  /**
   * Get remittanceInformationUnstructured
   * @return remittanceInformationUnstructured
  **/
  @ApiModelProperty(value = "")

@Size(max=140)

  @JsonProperty("remittanceInformationUnstructured")
  public String getRemittanceInformationUnstructured() {
    return remittanceInformationUnstructured;
  }

  public void setRemittanceInformationUnstructured(String remittanceInformationUnstructured) {
    this.remittanceInformationUnstructured = remittanceInformationUnstructured;
  }

  public PaymentInitiationJson remittanceInformationStructured(String remittanceInformationStructured) {
    this.remittanceInformationStructured = remittanceInformationStructured;
    return this;
  }

  /**
   * Get remittanceInformationStructured
   * @return remittanceInformationStructured
  **/
  @ApiModelProperty(value = "")

@Size(max=140)

  @JsonProperty("remittanceInformationStructured")
  public String getRemittanceInformationStructured() {
    return remittanceInformationStructured;
  }

  public void setRemittanceInformationStructured(String remittanceInformationStructured) {
    this.remittanceInformationStructured = remittanceInformationStructured;
  }

  public PaymentInitiationJson remittanceInformationStructuredArray(RemittanceInformationStructuredArray remittanceInformationStructuredArray) {
    this.remittanceInformationStructuredArray = remittanceInformationStructuredArray;
    return this;
  }

  /**
   * Get remittanceInformationStructuredArray
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

  public PaymentInitiationJson requestedExecutionDate(LocalDate requestedExecutionDate) {
    this.requestedExecutionDate = requestedExecutionDate;
    return this;
  }

  /**
   * Get requestedExecutionDate
   * @return requestedExecutionDate
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("requestedExecutionDate")
  public LocalDate getRequestedExecutionDate() {
    return requestedExecutionDate;
  }

  public void setRequestedExecutionDate(LocalDate requestedExecutionDate) {
    this.requestedExecutionDate = requestedExecutionDate;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}    PaymentInitiationJson paymentInitiationJson = (PaymentInitiationJson) o;
    return Objects.equals(this.endToEndIdentification, paymentInitiationJson.endToEndIdentification) &&
    Objects.equals(this.instructionIdentification, paymentInitiationJson.instructionIdentification) &&
    Objects.equals(this.debtorName, paymentInitiationJson.debtorName) &&
    Objects.equals(this.debtorAccount, paymentInitiationJson.debtorAccount) &&
    Objects.equals(this.ultimateDebtor, paymentInitiationJson.ultimateDebtor) &&
    Objects.equals(this.instructedAmount, paymentInitiationJson.instructedAmount) &&
    Objects.equals(this.creditorAccount, paymentInitiationJson.creditorAccount) &&
    Objects.equals(this.creditorAgent, paymentInitiationJson.creditorAgent) &&
    Objects.equals(this.creditorAgentName, paymentInitiationJson.creditorAgentName) &&
    Objects.equals(this.creditorName, paymentInitiationJson.creditorName) &&
    Objects.equals(this.creditorAddress, paymentInitiationJson.creditorAddress) &&
    Objects.equals(this.creditorId, paymentInitiationJson.creditorId) &&
    Objects.equals(this.ultimateCreditor, paymentInitiationJson.ultimateCreditor) &&
    Objects.equals(this.purposeCode, paymentInitiationJson.purposeCode) &&
    Objects.equals(this.chargeBearer, paymentInitiationJson.chargeBearer) &&
    Objects.equals(this.remittanceInformationUnstructured, paymentInitiationJson.remittanceInformationUnstructured) &&
    Objects.equals(this.remittanceInformationStructured, paymentInitiationJson.remittanceInformationStructured) &&
    Objects.equals(this.remittanceInformationStructuredArray, paymentInitiationJson.remittanceInformationStructuredArray) &&
    Objects.equals(this.requestedExecutionDate, paymentInitiationJson.requestedExecutionDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(endToEndIdentification, instructionIdentification, debtorName, debtorAccount, ultimateDebtor, instructedAmount, creditorAccount, creditorAgent, creditorAgentName, creditorName, creditorAddress, creditorId, ultimateCreditor, purposeCode, chargeBearer, remittanceInformationUnstructured, remittanceInformationStructured, remittanceInformationStructuredArray, requestedExecutionDate);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PaymentInitiationJson {\n");

    sb.append("    endToEndIdentification: ").append(toIndentedString(endToEndIdentification)).append("\n");
    sb.append("    instructionIdentification: ").append(toIndentedString(instructionIdentification)).append("\n");
    sb.append("    debtorName: ").append(toIndentedString(debtorName)).append("\n");
    sb.append("    debtorAccount: ").append(toIndentedString(debtorAccount)).append("\n");
    sb.append("    ultimateDebtor: ").append(toIndentedString(ultimateDebtor)).append("\n");
    sb.append("    instructedAmount: ").append(toIndentedString(instructedAmount)).append("\n");
    sb.append("    creditorAccount: ").append(toIndentedString(creditorAccount)).append("\n");
    sb.append("    creditorAgent: ").append(toIndentedString(creditorAgent)).append("\n");
    sb.append("    creditorAgentName: ").append(toIndentedString(creditorAgentName)).append("\n");
    sb.append("    creditorName: ").append(toIndentedString(creditorName)).append("\n");
    sb.append("    creditorAddress: ").append(toIndentedString(creditorAddress)).append("\n");
    sb.append("    creditorId: ").append(toIndentedString(creditorId)).append("\n");
    sb.append("    ultimateCreditor: ").append(toIndentedString(ultimateCreditor)).append("\n");
    sb.append("    purposeCode: ").append(toIndentedString(purposeCode)).append("\n");
    sb.append("    chargeBearer: ").append(toIndentedString(chargeBearer)).append("\n");
    sb.append("    remittanceInformationUnstructured: ").append(toIndentedString(remittanceInformationUnstructured)).append("\n");
    sb.append("    remittanceInformationStructured: ").append(toIndentedString(remittanceInformationStructured)).append("\n");
    sb.append("    remittanceInformationStructuredArray: ").append(toIndentedString(remittanceInformationStructuredArray)).append("\n");
    sb.append("    requestedExecutionDate: ").append(toIndentedString(requestedExecutionDate)).append("\n");
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

