/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
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
    public ResponseObject<UpdateConsentPsuDataResponse> invokeUpdateConsentPsuDataAspect(ResponseObject<UpdateConsentPsuDataResponse> result, ConsentAuthorisationsParameters updatePsuData) {
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
    public ResponseObject<UpdateConsentPsuDataResponse> invokeUpdatePiisConsentPsuDataAspect(ResponseObject<UpdateConsentPsuDataResponse> result, ConsentAuthorisationsParameters updatePsuData) {
        return consentAspectService.invokeUpdatePiisConsentPsuDataAspect(result);
    }
}
