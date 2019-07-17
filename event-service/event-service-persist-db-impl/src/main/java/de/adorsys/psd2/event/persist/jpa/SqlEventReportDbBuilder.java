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

package de.adorsys.psd2.event.persist.jpa;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@Profile("!cms-oracle-db")
public class SqlEventReportDbBuilder extends MapSqlParameterSource implements SqlEventReportBuilder {
    private StringBuilder sqlRequest = new StringBuilder(getBasePartOfRequest());

    @Override
    public SqlEventReportDbBuilder period() {
        sqlRequest.append("timestamp between :periodFrom and :periodTo ");
        return this;
    }

    @Override
    public SqlEventReportDbBuilder instanceId() {
        sqlRequest.append("and ev.instance_id = :instanceId ");
        return this;
    }

    @Override
    public SqlEventReportDbBuilder consentId() {
        sqlRequest.append("and ev.consent_id = :consentId ");
        return this;
    }

    @Override
    public SqlEventReportDbBuilder paymentId() {
        sqlRequest.append("and ev.payment_id = :paymentId ");
        return this;
    }

    @Override
    public SqlEventReportDbBuilder eventType() {
        sqlRequest.append("and ev.event_type = :eventType ");
        return this;
    }

    @Override
    public SqlEventReportDbBuilder eventOrigin() {
        sqlRequest.append("and ev.event_origin = :eventOrigin ");
        return this;
    }

    public SqlEventReportDbBuilder() {
    }

    private String getBasePartOfRequest() {
        return "select ev.* , " +
                   "case " +
                   "    when ev.consent_id is  not  null then  cst_psu_data.psu_id " +
                   "    else  pmt_psu_data.psu_id " +
                   "end as  psu_ex_id,  " +
                   "case " +
                   "    when ev.consent_id is  not null  then  cst_psu_data.psu_id_type " +
                   "    else  pmt_psu_data.psu_id_type " +
                   "end as  psu_ex_id_type,  " +
                   "case " +
                   "    when ev.consent_id is  not null  then  cst_psu_data.psu_corporate_id " +
                   "    else  pmt_psu_data.psu_corporate_id " +
                   "end as psu_ex_corporate_id,  " +
                   "case " +
                   "    when ev.consent_id is  not null then  cst_psu_data.psu_corporate_id_type " +
                   "    else  pmt_psu_data.psu_corporate_id_type " +
                   "end as  psu_ex_corporate_id_type  " +
                   "from consent.event ev " +
                   "left join consent.pis_common_payment pmt " +
                   "on ev.payment_id = pmt.payment_id " +
                   "left join consent.ais_consent cst " +
                   "on ev.consent_id = cst.external_id " +
                   "left join consent.pis_common_payment_psu_data pmt_psu " +
                   "on pmt.id = pmt_psu.pis_common_payment_id " +
                   "left join consent.ais_consent_psu_data cst_psu " +
                   "on cst.id = cst_psu.ais_consent_id " +
                   "left join consent.psu_data pmt_psu_data " +
                   "on pmt_psu_data.id = pmt_psu.psu_data_id " +
                   "left join consent.psu_data cst_psu_data " +
                   "on cst_psu_data.id = cst_psu.psu_data_id " +
                   "where ";
    }

    @Override
    public String build() {
        return sqlRequest.toString();
    }
}
