/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.web.aspect;

import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.domain.fund.CreatePiisConsentRequest;
import de.adorsys.psd2.xs2a.service.link.ConsentAspectService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ConsentAspect {
    private ConsentAspectService consentAspectService;

    public ConsentAspect(ConsentAspectService consentAspectService) {
        this.consentAspectService = consentAspectService;
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.ConsentService.createAccountConsentsWithResponse(..)) && args( request, psuData, explicitPreferred)", returning = "result", argNames = "result,request,psuData,explicitPreferred")
    public ResponseObject<CreateConsentResponse> invokeCreateAccountConsentAspect(ResponseObject<CreateConsentResponse> result, CreateConsentReq request, PsuIdData psuData, boolean explicitPreferred) {
        return consentAspectService.invokeCreateAccountConsentAspect(result, explicitPreferred);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.ConsentService.createAisAuthorisation(..)) && args( psuData,  consentId,  password)", returning = "result", argNames = "result, psuData,  consentId,  password")
    public ResponseObject<AuthorisationResponse> invokeCreateConsentPsuDataAspect(ResponseObject<AuthorisationResponse> result, PsuIdData psuData, String consentId, String password) {
        return consentAspectService.invokeCreateConsentPsuDataAspect(result);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.ConsentService.updateConsentPsuData(..)) && args(updatePsuData)", returning = "result", argNames = "result,updatePsuData")
    public ResponseObject<UpdateConsentPsuDataResponse> invokeUpdateConsentPsuDataAspect(ResponseObject<UpdateConsentPsuDataResponse> result, UpdateConsentPsuDataReq updatePsuData) {
        return consentAspectService.invokeUpdateConsentPsuDataAspect(result);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.PiisConsentService.createPiisConsentWithResponse(..)) && args( request, psuData, explicitPreferred)", returning = "result", argNames = "result,request,psuData,explicitPreferred")
    public ResponseObject<Xs2aConfirmationOfFundsResponse> createPiisConsentWithResponse(ResponseObject<Xs2aConfirmationOfFundsResponse> result, CreatePiisConsentRequest request, PsuIdData psuData, boolean explicitPreferred) {
        return consentAspectService.createPiisConsentWithResponse(result, explicitPreferred);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.PiisConsentService.createPiisAuthorisation(..)) && args( psuData,  consentId,  password)", returning = "result", argNames = "result, psuData,  consentId,  password")
    public ResponseObject<AuthorisationResponse> createPiisAuthorisationAspect(ResponseObject<AuthorisationResponse> result, PsuIdData psuData, String consentId, String password) {
        return consentAspectService.invokeCreatePiisAuthorisationAspect(result);
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.PiisConsentService.updateConsentPsuData(..)) && args(updatePsuData)", returning = "result", argNames = "result,updatePsuData")
    public ResponseObject<UpdateConsentPsuDataResponse> invokeUpdatePiisConsentPsuDataAspect(ResponseObject<UpdateConsentPsuDataResponse> result, UpdateConsentPsuDataReq updatePsuData) {
        return consentAspectService.invokeUpdatePiisConsentPsuDataAspect(result);
    }
}
