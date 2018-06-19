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

package de.adorsys.aspsp.aspspmockserver.repository;

import de.adorsys.aspsp.xs2a.spi.domain.account.SpiTransaction;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

@Repository
@Profile({"mongo", "fongo"})
public interface TransactionRepository extends MongoRepository<SpiTransaction, String> {
    @Query("{$or:[{$and:[{'creditorAccount.iban':?0},{'creditorAccount.currency':?1}]},{$and:[{'debtorAccount.iban':?0},{'debtorAccount.currency':?1}]}],'valueDate':{$gte:?2,$lte:?3}}")
    List<SpiTransaction> findAllByDates(String iban, Currency currency, LocalDate dateFrom, LocalDate dateTo);

    @Query("{$or:[{$and:[{'creditorAccount.iban':?0},{'creditorAccount.currency':?1}]},{$and:[{'debtorAccount.iban':?0},{'debtorAccount.currency':?1}]}],'transactionId':?2}")
    SpiTransaction findOneByTransactionIdAndAccount(String iban, Currency currency, String transactionId);
}
