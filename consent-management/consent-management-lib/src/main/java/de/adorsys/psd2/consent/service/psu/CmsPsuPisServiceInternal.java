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

package de.adorsys.psd2.consent.service.psu;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.pis.CmsBasePaymentResponse;
import de.adorsys.psd2.consent.api.pis.CmsCommonPayment;
import de.adorsys.psd2.consent.api.pis.CmsPaymentResponse;
import de.adorsys.psd2.consent.api.pis.UpdatePaymentRequest;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentService;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
import de.adorsys.psd2.consent.psu.api.CmsPsuAuthorisation;
import de.adorsys.psd2.consent.psu.api.CmsPsuPisService;
import de.adorsys.psd2.consent.psu.api.pis.CmsPisPsuDataAuthorisation;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.repository.PisPaymentDataRepository;
import de.adorsys.psd2.consent.repository.specification.AuthorisationSpecification;
import de.adorsys.psd2.consent.repository.specification.PisPaymentDataSpecification;
import de.adorsys.psd2.consent.service.CommonPaymentDataService;
import de.adorsys.psd2.consent.service.CorePaymentsConvertService;
import de.adorsys.psd2.consent.service.mapper.CmsPsuAuthorisationMapper;
import de.adorsys.psd2.consent.service.mapper.CmsPsuPisMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.psu.util.PageRequestBuilder;
import de.adorsys.psd2.consent.service.psu.util.PsuDataUpdater;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.exception.AuthorisationIsExpiredException;
import de.adorsys.psd2.xs2a.core.exception.RedirectUrlIsExpiredException;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthenticationDataHolder;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CmsPsuPisServiceInternal implements CmsPsuPisService {
    private final PisPaymentDataRepository pisPaymentDataRepository;
    private final PisCommonPaymentDataRepository pisCommonPaymentDataRepository;
    private final AuthorisationRepository authorisationRepository;
    private final CmsPsuPisMapper cmsPsuPisMapper;
    private final PisCommonPaymentService pisCommonPaymentService;
    private final CommonPaymentDataService commonPaymentDataService;
    private final PsuDataMapper psuDataMapper;
    private final AuthorisationSpecification authorisationSpecification;
    private final PisPaymentDataSpecification pisPaymentDataSpecification;
    private final CmsPsuService cmsPsuService;
    private final CmsPsuAuthorisationMapper cmsPsuPisAuthorisationMapper;
    private final CorePaymentsConvertService corePaymentsConvertService;
    private final PsuDataUpdater psuDataUpdater;
    private final PageRequestBuilder pageRequestBuilder;

    @Override
    @Transactional
    public boolean updatePsuInPayment(@NotNull PsuIdData psuIdData, @NotNull String authorisationId, @NotNull String instanceId) throws AuthorisationIsExpiredException {
        return getAuthorisationByExternalId(authorisationId, instanceId)
                   .map(auth -> updatePsuData(auth, psuIdData))
                   .orElseGet(() -> {
                       log.info("Authorisation ID [{}], Instance ID: [{}]. Update PSU  in Payment failed, because authorisation not found",
                                authorisationId, instanceId);
                       return false;
                   });
    }

    @Override
    @Transactional
    public @NotNull Optional<CmsPaymentResponse> checkRedirectAndGetPayment(@NotNull String redirectId,
                                                                            @NotNull String instanceId)
        throws RedirectUrlIsExpiredException {

        Optional<AuthorisationEntity> optionalAuthorisation = authorisationRepository
                                                                  .findOne(authorisationSpecification.byExternalIdAndInstanceId(redirectId, instanceId));

        if (optionalAuthorisation.isPresent()) {
            AuthorisationEntity authorisation = optionalAuthorisation.get();
            if (!authorisation.isRedirectUrlNotExpired()) {
                log.info("Authorisation ID [{}], Instance ID: [{}]. Check redirect URL and get payment failed, because redirect URL is expired",
                         authorisation.getExternalId(), instanceId);
                authorisation.setScaStatus(ScaStatus.FAILED);

                throw new RedirectUrlIsExpiredException(authorisation.getTppNokRedirectUri());
            }
            return buildCmsPaymentResponse(authorisation);
        }

        log.info("Authorisation ID [{}], Instance ID: [{}]. Check redirect URL and get payment failed, because authorisation not found or has finalised status",
                 redirectId, instanceId);
        return Optional.empty();
    }

    @Transactional
    @Override
    public @NotNull Optional<CmsBasePaymentResponse> getPayment(@NotNull PsuIdData psuIdData, @NotNull String paymentId, @NotNull String instanceId) {
        if (isPsuDataEquals(paymentId, psuIdData)) {
            List<PisPaymentData> list = pisPaymentDataRepository.findAll(pisPaymentDataSpecification.byPaymentIdAndInstanceId(paymentId, instanceId));

            // todo implementation should be changed https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1143
            if (!list.isEmpty()) {
                return Optional.of(cmsPsuPisMapper.mapToCmsPayment(list));
            } else {
                return commonPaymentDataService.getPisCommonPaymentData(paymentId, instanceId)
                           .map(cmsPsuPisMapper::mapToCmsPayment)
                           .map(CmsCommonPayment.class::cast)
                           .map(corePaymentsConvertService::expandCommonPaymentWithCorePayment);
            }
        }
        log.info("Payment ID: [{}], Instance ID: [{}]. Get payment failed, because given PSU data and PSU data stored in payment are not equal",
                 paymentId, instanceId);
        return Optional.empty();
    }

    @Override
    @Transactional
    public @NotNull Optional<CmsPaymentResponse> checkRedirectAndGetPaymentForCancellation(@NotNull String redirectId,
                                                                                           @NotNull String instanceId)
        throws RedirectUrlIsExpiredException {

        Optional<AuthorisationEntity> optionalAuthorisation = authorisationRepository
                                                                  .findOne(authorisationSpecification.byExternalIdAndInstanceId(redirectId, instanceId))
                                                                  .filter(a -> !a.getScaStatus().isFinalisedStatus());

        if (optionalAuthorisation.isPresent()) {
            AuthorisationEntity authorisation = optionalAuthorisation.get();
            if (!authorisation.isRedirectUrlNotExpired()) {
                log.info("Authorisation ID [{}], Instance ID: [{}]. Check redirect URL and get payment cancellation failed, because authorisation not found or has finalised status",
                         redirectId, instanceId);
                authorisation.setScaStatus(ScaStatus.FAILED);

                throw new RedirectUrlIsExpiredException(authorisation.getTppNokRedirectUri());
            }
            return buildCmsPaymentResponse(authorisation);
        }
        log.info("Authorisation ID [{}], Instance ID: [{}]. Check redirect URL and get payment cancellation failed, because authorisation not found or has finalised status",
                 redirectId, instanceId);
        return Optional.empty();
    }

    @Override
    public @NotNull Optional<CmsPsuAuthorisation> getAuthorisationByAuthorisationId(@NotNull String authorisationId, @NotNull String instanceId) {
        Optional<AuthorisationEntity> optionalAuthorisation = authorisationRepository
                                                                  .findOne(authorisationSpecification.byExternalIdAndInstanceId(authorisationId, instanceId));

        if (optionalAuthorisation.isPresent()) {
            AuthorisationEntity authorisation = optionalAuthorisation.get();
            return Optional.of(cmsPsuPisAuthorisationMapper.mapToCmsPsuAuthorisation(authorisation));
        }

        log.info("Authorisation ID: [{}], Instance ID: [{}]. Get authorisation failed, because authorisation not found",
                 authorisationId, instanceId);

        return Optional.empty();
    }

    @Override
    @Transactional
    public boolean updateAuthorisationStatus(@NotNull PsuIdData psuIdData, @NotNull String paymentId,
                                             @NotNull String authorisationId, @NotNull ScaStatus status,
                                             @NotNull String instanceId, AuthenticationDataHolder authenticationDataHolder) throws AuthorisationIsExpiredException {
        Optional<AuthorisationEntity> pisAuthorisation = getAuthorisationByExternalId(authorisationId, instanceId);

        if (pisAuthorisation.isEmpty()) {
            log.info("Authorisation ID [{}], Instance ID: [{}]. Update authorisation status failed, because authorisation not found.",
                     authorisationId, instanceId);
            return false;
        }

        boolean isValid = pisAuthorisation
                              .map(AuthorisationEntity::getParentExternalId)
                              .map(id -> validateGivenData(id, paymentId, psuIdData))
                              .orElse(false);

        if (!isValid) {
            log.info("Authorisation ID [{}], Instance ID: [{}]. Update authorisation status failed, because request data is not valid",
                     authorisationId, instanceId);
            return false;
        }

        return updateAuthorisationStatusAndSaveAuthorisation(pisAuthorisation.get(), status, authenticationDataHolder);
    }

    @Override
    @Transactional
    public boolean updatePaymentStatus(@NotNull String paymentId, @NotNull TransactionStatus status, @NotNull String instanceId) {
        Optional<PisCommonPaymentData> paymentDataOptional = commonPaymentDataService.getPisCommonPaymentData(paymentId, instanceId);

        return paymentDataOptional
                   .filter(p -> p.getTransactionStatus().isNotFinalisedStatus())
                   .map(pd -> commonPaymentDataService.updateStatusInPaymentData(pd, status))
                   .orElseGet(() -> {
                       log.info("Payment ID [{}], Instance ID: [{}]. Update payment status failed, because common payment data not found",
                                paymentId, instanceId);
                       return false;
                   });
    }

    @Override
    public Optional<List<CmsPisPsuDataAuthorisation>> getPsuDataAuthorisations(@NotNull String paymentId, @NotNull String instanceId, Integer pageIndex, Integer itemsPerPage) {
        if (pageIndex == null && itemsPerPage == null) {
            return commonPaymentDataService.getPisCommonPaymentData(paymentId, instanceId)
                       .map(p -> authorisationRepository.findAllByParentExternalIdAndTypeIn(paymentId,
                                                                                            EnumSet.of(AuthorisationType.PIS_CREATION,
                                                                                                       AuthorisationType.PIS_CANCELLATION)))
                       .map(this::getPsuDataAuthorisations);
        }
        Pageable pageRequest = pageRequestBuilder.getPageable(pageIndex, itemsPerPage);
        return commonPaymentDataService.getPisCommonPaymentData(paymentId, instanceId)
                   .map(p -> authorisationRepository.findAllByParentExternalIdAndTypeIn(paymentId,
                                                                                        EnumSet.of(AuthorisationType.PIS_CREATION,
                                                                                                   AuthorisationType.PIS_CANCELLATION),
                                                                                        pageRequest))
                   .map(this::getPsuDataAuthorisations);
    }

    @Override
    @Transactional
    public boolean updatePayment(UpdatePaymentRequest updatePaymentRequest) {
        Optional<PisCommonPaymentData> paymentDataOptional = commonPaymentDataService.getPisCommonPaymentData(updatePaymentRequest.getPaymentId(), updatePaymentRequest.getInstanceId());

        return paymentDataOptional
                   .filter(p -> p.getTransactionStatus().isNotFinalisedStatus())
                   .map(pd -> commonPaymentDataService.updatePaymentData(pd, updatePaymentRequest.getPayment()))
                   .orElseGet(() -> {
                       log.info("Payment ID [{}], Instance ID: [{}]. Update payment failed, because common payment data not found",
                                updatePaymentRequest.getPaymentId(), updatePaymentRequest.getInstanceId());
                       return false;
                   });
    }

    @NotNull
    private List<CmsPisPsuDataAuthorisation> getPsuDataAuthorisations(List<AuthorisationEntity> authorisations) {
        return authorisations.stream()
                   .filter(auth -> Objects.nonNull(auth.getPsuData()))
                   .map(auth -> new CmsPisPsuDataAuthorisation(psuDataMapper.mapToPsuIdData(auth.getPsuData()),
                                                               auth.getExternalId(),
                                                               auth.getScaStatus(),
                                                               auth.getType()))
                   .collect(Collectors.toList());
    }

    private boolean updatePsuData(AuthorisationEntity authorisation, PsuIdData psuIdData) {
        PsuData newPsuData = psuDataMapper.mapToPsuData(psuIdData, authorisation.getInstanceId());
        if (newPsuData == null || StringUtils.isBlank(newPsuData.getPsuId())) {
            log.info("Authorisation ID [{}]. Update PSU in payment failed in updatePsuData method, because newPsuData or psuId in newPsuData is empty or null",
                     authorisation.getExternalId());
            return false;
        }

        Optional<PsuData> optionalPsuData = Optional.ofNullable(authorisation.getPsuData());
        if (optionalPsuData.isPresent()) {
            newPsuData = psuDataUpdater.updatePsuDataEntity(optionalPsuData.get(), newPsuData);
        } else {
            Optional<PisCommonPaymentData> commonPaymentOptional = pisCommonPaymentDataRepository.findByPaymentId(authorisation.getParentExternalId());

            if (commonPaymentOptional.isEmpty()) {
                log.info("Authorisation ID [{}]. Update PSU data in payment failed, couldn't find payment by the parent ID in the authorisation.",
                         authorisation.getExternalId());
                return false;
            }

            PisCommonPaymentData commonPayment = commonPaymentOptional.get();
            List<PsuData> paymentPsuList = commonPayment.getPsuDataList();
            Optional<PsuData> psuDataOptional = cmsPsuService.definePsuDataForAuthorisation(newPsuData, paymentPsuList);
            if (psuDataOptional.isPresent()) {
                newPsuData = psuDataOptional.get();
                if (AuthorisationType.PIS_CANCELLATION != authorisation.getType()) {
                    commonPayment.setPsuDataList(cmsPsuService.enrichPsuData(newPsuData, paymentPsuList));
                }
            }

            log.info("Authorisation ID [{}]. The payment attached to this authorisation, contains no PSU data with an ID that matches the requested one.", authorisation.getExternalId());
        }

        authorisation.setPsuData(newPsuData);
        return true;
    }

    private boolean validateGivenData(String realPaymentId, String givenPaymentId, PsuIdData psuIdData) {
        return Optional.of(givenPaymentId)
                   .filter(p -> isPsuDataEquals(p, psuIdData))
                   .map(id -> StringUtils.equals(realPaymentId, id))
                   .orElseGet(() -> {
                       log.info("Cannot validate given PSU data, because given payment ID is null");
                       return false;
                   });
    }

    private boolean updateAuthorisationStatusAndSaveAuthorisation(AuthorisationEntity pisAuthorisation, ScaStatus status,
                                                                  AuthenticationDataHolder authenticationDataHolder) {
        if (pisAuthorisation.getScaStatus().isFinalisedStatus()) {
            log.info("Authorisation ID [{}], SCA status: [{}]. Update authorisation status failed in updateAuthorisationStatusAndSaveAuthorisation method, " +
                         "because authorisation has finalised status", pisAuthorisation.getExternalId(), pisAuthorisation.getScaStatus().getValue());
            return false;
        }
        pisAuthorisation.setScaStatus(status);

        if (authenticationDataHolder != null) {
            enrichAuthorisationWithAuthenticationData(pisAuthorisation, authenticationDataHolder);
        }
        return true;
    }

    private boolean isPsuDataEquals(String paymentId, PsuIdData psuIdData) {
        CmsResponse<List<PsuIdData>> psuDataResponse = pisCommonPaymentService.getPsuDataListByPaymentId(paymentId);

        if (psuDataResponse.hasError()) {
            log.info("Payment ID: [{}]. Cannot equal PSU data with payment ID, because PSU data list not found by ID", paymentId);
            return false;
        }

        return psuDataResponse.getPayload().stream().anyMatch(psu -> psu.contentEquals(psuIdData));
    }

    private Optional<CmsPaymentResponse> buildCmsPaymentResponse(AuthorisationEntity authorisation) {
        Optional<PisCommonPaymentData> commonPayment = pisCommonPaymentDataRepository.findByPaymentId(authorisation.getParentExternalId());

        if (commonPayment.isEmpty()) {
            log.info("Authorisation ID [{}]. Creation of CMS payment response has failed, couldn't get payment by parent ID in authorisation",
                     authorisation.getExternalId());
            return Optional.empty();
        }

        CmsBasePaymentResponse payment = cmsPsuPisMapper.mapPaymentDataToCmsPayment(commonPayment.get());

        CmsPaymentResponse cmsPaymentResponse = new CmsPaymentResponse(payment,
                                                                       authorisation.getExternalId(),
                                                                       authorisation.getTppOkRedirectUri(),
                                                                       authorisation.getTppNokRedirectUri());
        return Optional.of(cmsPaymentResponse);
    }

    private Optional<AuthorisationEntity> getAuthorisationByExternalId(@NotNull String authorisationId, @NotNull String instanceId) throws AuthorisationIsExpiredException {
        Optional<AuthorisationEntity> authorization = authorisationRepository.findOne(authorisationSpecification.byExternalIdAndInstanceId(authorisationId, instanceId));

        if (authorization.isPresent() && !authorization.get().isAuthorisationNotExpired()) {
            log.info("Authorisation ID [{}], Instance ID: [{}]. Authorisation is expired", authorisationId, instanceId);
            throw new AuthorisationIsExpiredException(authorization.get().getTppNokRedirectUri());
        }
        return authorization;
    }

    private void enrichAuthorisationWithAuthenticationData(AuthorisationEntity authorisation, AuthenticationDataHolder authenticationDataHolder) {
        if (authenticationDataHolder.getAuthenticationData() != null) {
            authorisation.setScaAuthenticationData(authenticationDataHolder.getAuthenticationData());
        }
        if (authenticationDataHolder.getAuthenticationMethodId() != null) {
            authorisation.setAuthenticationMethodId(authenticationDataHolder.getAuthenticationMethodId());
        }
    }
}
