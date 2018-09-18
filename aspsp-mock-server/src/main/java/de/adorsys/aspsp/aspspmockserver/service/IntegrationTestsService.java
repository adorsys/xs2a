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

package de.adorsys.aspsp.aspspmockserver.service;

import de.adorsys.aspsp.aspspmockserver.data.test.AccountMockServerData;
import de.adorsys.aspsp.aspspmockserver.repository.PaymentRepository;
import de.adorsys.aspsp.aspspmockserver.repository.PsuRepository;
import de.adorsys.aspsp.aspspmockserver.repository.TanRepository;
import de.adorsys.aspsp.aspspmockserver.repository.TransactionRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class IntegrationTestsService {
    private final PaymentRepository paymentRepository;
    private final PsuRepository psuRepository;
    private final TransactionRepository transactionRepository;
    private final TanRepository tanRepository;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    DefaultListableBeanFactory beanFactory;

    public void refreshTestingData() {
        paymentRepository.deleteAll();
        psuRepository.deleteAll();
        transactionRepository.deleteAll();
        tanRepository.deleteAll();

        AccountMockServerData accountMockServerData = applicationContext.getBean(AccountMockServerData.class);
        beanFactory.destroyBean(accountMockServerData);
        beanFactory.createBean(AccountMockServerData.class);
    }
}
