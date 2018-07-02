/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function sendPaymentRequestAndGetResponse(productNumber) {
    var paymentResponse = new Object();

    var settings = getPaymentAjaxSettings(productNumber);
    $.ajax(settings)
        .done(function (resp) {
            console.log("complete : " + JSON.stringify(resp));
            paymentResponse = resp;

        })
        .fail(function (e) {
            console.log("ERROR: ", e);
            alert("Connection error!");
        });

    return paymentResponse;
}

function getPaymentAjaxSettings(productNumber) {

    var paymentReqJson = getPaymentInitiationRequestJson(productNumber);
    var headers = getRequestHeaders();
    var xs2aUrl = getXs2aUrl();

    return {
        "async": false,
        "crossDomain": true,
        "url": xs2aUrl,
        "method": "POST",
        "headers": headers,
        "processData": false,
        "data": paymentReqJson
    };

}

function getPaymentInitiationRequestJson(productNumber) {
    var formObject = new Object();

    var debtorAccount = new Object();
    var creditorAccount = new Object();
    var instructedAmount = new Object();

    debtorAccount.iban = $("#debtorIban").val();
    debtorAccount.currency = $("#debtorCurrency").val();

    creditorAccount.iban = $("#creditorIban").val();
    creditorAccount.currency = $("#creditorCurrency").val();

    instructedAmount.content = $("#amount" + productNumber).val();
    instructedAmount.currency = $("#currency" + productNumber).val();

    formObject.debtorAccount = debtorAccount;
    formObject.creditorAccount = creditorAccount;
    formObject.instructedAmount = instructedAmount;

    formObject.ultimateDebtor = $("#debtorName").val();
    formObject.creditorName = $("#creditorName").val();
    formObject.ultimateCreditor = $("#creditorName").val();

    var dateTime = new Date();
    dateTime.setDate(dateTime.getDate() + 1);
    var formattedDateTimeString = dateTime.toISOString();

    var formattedDate = formattedDateTimeString.substring(10, 0);
    var formattedDateTime = formattedDateTimeString.substring(23, 0);

    formObject.requestedExecutionDate = formattedDate;
    formObject.requestedExecutionTime = formattedDateTime;

    return JSON.stringify(formObject);
}

function getRequestHeaders() {

    var headers = new Object();

    headers["tpp-transaction-id"] = "16d40f49-a110-4344-a949-f99828ae13c9";
    headers["tpp-request-id"] = "2f77a125-aa7a-45c0-b414-cea25a116035";
    headers["psu-ip-address"] = "192.168.8.78";
    headers["content-type"] = "application/json";

    return headers;
}

function getXs2aUrl() {
    if (document.domain === "localhost") {
        return "http://localhost:8080/api/v1/payments/sepa-credit-transfers?tppRedirectPreferred=true";
    } else if (document.domain === "xs2a.integ.cloud.adorsys.de") {
        return "https://xs2a.integ.cloud.adorsys.de/api/v1/payments/sepa-credit-transfers?tppRedirectPreferred=true";
    } else {
        return "";
    }
}
