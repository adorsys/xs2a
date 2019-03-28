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

package de.adorsys.psd2.xs2a.service.event;

import de.adorsys.psd2.consent.api.service.EventServiceEncrypted;
import de.adorsys.psd2.xs2a.core.event.Event;
import de.adorsys.psd2.xs2a.core.event.EventOrigin;
import de.adorsys.psd2.xs2a.core.event.EventType;
import de.adorsys.psd2.xs2a.domain.RequestData;
import de.adorsys.psd2.xs2a.domain.event.RequestEventPayload;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.TppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class Xs2aEventService {
    private final TppService tppService;
    private final EventServiceEncrypted eventService;
    private final RequestProviderService requestProviderService;

    /**
     * Records TPP request to the AIS in the CMS in form of TPP event for given consent id and event type
     *
     * @param consentId Consent id that will be recorded along with the event
     * @param eventType Type of the event
     */
    public void recordAisTppRequest(@NotNull String consentId, @NotNull EventType eventType) {
        recordAisTppRequest(consentId, eventType, null);
    }

    /**
     * Records TPP request to the AIS in the CMS in form of TPP event for given consent id, event type and request body
     *
     * @param consentId Consent id that will be recorded along with the event
     * @param eventType Type of the event
     * @param body      Body of the request
     */
    public void recordAisTppRequest(@NotNull String consentId, @NotNull EventType eventType, @Nullable Object body) {
        Event event = buildTppEvent(eventType, body);
        event.setConsentId(consentId);

        recordEventInCms(event);
    }

    /**
     * Records TPP request to the PIS in the CMS in form of TPP event for given payment id and event type
     *
     * @param paymentId Payment id that will be recorded along with the event
     * @param eventType Type of the event
     */
    public void recordPisTppRequest(@NotNull String paymentId, @NotNull EventType eventType) {
        recordPisTppRequest(paymentId, eventType, null);
    }

    /**
     * Records TPP request to the PIS in the CMS in form of TPP event for given payment id, event type and request body
     *
     * @param paymentId Payment id that will be recorded along with the event
     * @param eventType Type of the event
     * @param body      Body of the request
     */
    public void recordPisTppRequest(@NotNull String paymentId, @NotNull EventType eventType, @Nullable Object body) {
        Event event = buildTppEvent(eventType, body);
        event.setPaymentId(paymentId);

        recordEventInCms(event);
    }

    /**
     * Records generic TPP request in the CMS in form of TPP event for given event type
     *
     * @param eventType Type of event
     */
    public void recordTppRequest(@NotNull EventType eventType) {
        recordTppRequest(eventType, null);
    }

    /**
     * Records generic TPP request in the CMS in form of TPP event for given event type and request body
     *
     * @param eventType Type of event
     * @param body      Body of the request
     */
    public void recordTppRequest(@NotNull EventType eventType, @Nullable Object body) {
        Event event = buildTppEvent(eventType, body);

        recordEventInCms(event);
    }

    private void recordEventInCms(Event event) {
        boolean recorded = eventService.recordEvent(event);
        if (!recorded) {
            log.info("X-REQUEST-ID: [{}], TPP ID: [{}]. Couldn't record event from TPP request: {}", event.getXRequestId(), event.getTppAuthorisationNumber(), event);
        }
    }

    private Event buildTppEvent(EventType eventType, Object body) {
        RequestData requestData = requestProviderService.getRequestData();

        Event event = Event.builder()
                          .timestamp(OffsetDateTime.now())
                          .eventOrigin(EventOrigin.TPP)
                          .eventType(eventType)
                          .psuIdData(requestData.getPsuIdData())
                          .xRequestId(requestData.getRequestId())
                          .tppAuthorisationNumber(tppService.getTppInfo().getAuthorisationNumber())
                          .build();
        RequestEventPayload payload = buildRequestEventPayload(requestData, body);
        event.setPayload(payload);

        return event;
    }

    private RequestEventPayload buildRequestEventPayload(RequestData requestData, Object body) {
        RequestEventPayload requestPayload = new RequestEventPayload();
        requestPayload.setTppInfo(tppService.getTppInfo());
        requestPayload.setTppIp(requestData.getIp());
        requestPayload.setUri(requestData.getUri());
        requestPayload.setHeaders(requestData.getHeaders());
        requestPayload.setBody(body);
        return requestPayload;
    }
}
