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

package de.adorsys.psd2.event.persist;

import de.adorsys.psd2.event.core.model.EventOrigin;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.event.persist.model.ReportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.List;

public interface EventReportRepository {

    /**
     * Returns a list of Event objects, recorded in given time period
     *
     * @param start      First date of the period
     * @param end        Last date of the period
     * @param instanceId The id of particular service instance
     * @return List of Event objects, recorded in given time period
     */
    List<ReportEvent> getEventsForPeriod(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @Nullable String instanceId,
                                         @Nullable Integer pageIndex, @Nullable Integer itemsPerPage);

    /**
     * Returns a list of Event objects, recorded in given time period and with the given consentId
     *
     * @param start      First date of the period
     * @param end        Last date of the period
     * @param consentId  Id of the consent
     * @param instanceId The id of particular service instance
     * @return List of Event objects, recorded in given time period and with a given consentId
     */
    List<ReportEvent> getEventsForPeriodAndConsentId(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @NotNull String consentId, @Nullable String instanceId,
                                                     @Nullable Integer pageIndex, @Nullable Integer itemsPerPage);

    /**
     * Returns a list of Event objects, recorded in given time period and with the given paymentId
     *
     * @param start      First date of the period
     * @param end        Last date of the period
     * @param paymentId  Id of the payment
     * @param instanceId The id of particular service instance
     * @return List of Event objects, recorded in given time period and with a given paymentId
     */
    List<ReportEvent> getEventsForPeriodAndPaymentId(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @NotNull String paymentId, @Nullable String instanceId,
                                                     @Nullable Integer pageIndex, @Nullable Integer itemsPerPage);

    /**
     * Returns a list of Event objects of the specific type, recorded in given time period
     *
     * @param start      First date of the period
     * @param end        Last date of the period
     * @param eventType  The searched type of the events
     * @param instanceId The id of particular service instance
     * @return List of Event objects, recorded in given time period and of a specific type
     */
    List<ReportEvent> getEventsForPeriodAndEventType(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @NotNull EventType eventType, @Nullable String instanceId,
                                                     @Nullable Integer pageIndex, @Nullable Integer itemsPerPage);

    /**
     * Returns a list of Event objects from a specific origin, recorded in given time period
     *
     * @param start       First date of the period
     * @param end         Last date of the period
     * @param eventOrigin The searched origin of the events
     * @param instanceId  The id of particular service instance
     * @return List of Event objects, recorded in given time period and from a specific origin
     */
    List<ReportEvent> getEventsForPeriodAndEventOrigin(@NotNull OffsetDateTime start, @NotNull OffsetDateTime end, @NotNull EventOrigin eventOrigin, @Nullable String instanceId,
                                                       @Nullable Integer pageIndex, @Nullable Integer itemsPerPage);
}
