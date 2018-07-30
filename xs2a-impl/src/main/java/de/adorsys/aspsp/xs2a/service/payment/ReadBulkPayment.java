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

package de.adorsys.aspsp.xs2a.service.payment;

import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import org.springframework.stereotype.Service;

import java.util.List;

import static de.adorsys.aspsp.xs2a.domain.pis.PaymentType.BULK;

@Service("bulk-payments")
public class ReadBulkPayment extends ReadPayment<List<SinglePayments>> {
    @Override
    public List<SinglePayments> getPayment(String paymentProduct, String paymentId) {
        List<SpiSinglePayments> bulkPayments = paymentSpi.getBulkPaymentById(paymentMapper.mapToSpiPaymentType(BULK), paymentProduct, paymentId,  "ewogIHBheW1lbnRUb2tlbjogQUJDRDEyMzE0MSwKICBzeXN0ZW1JZDogREVEQUlKRUosCiAgbXVsdGl1c2U6IHRydWUsCiAgZXhwaXJlczogMCwKICB0cmFuc2FjdGlvbnM6IFsKICAgIHsKICAgICAgdHJhbnNhY3Rpb25JZDogaWppZWpmaWUyM3IyLAogICAgICBzdGF0dXM6IE9LCiAgICB9LAogICAgewogICAgICB0cmFuc2FjdGlvbklkOiBpamllamZ3cndpZTIzcjIsCiAgICAgIHN0YXR1czogRkFJTEVECiAgICB9LAogICAgewogICAgICB0cmFuc2FjdGlvbklkOiBpamllcnQyamZpZTIzcjIsCiAgICAgIHN0YXR1czogT0sKICAgIH0sCiAgICB7CiAgICAgIHRyYW5zYWN0aW9uSWQ6IGlqMzI0MzJpZWpmaWUyM3IyLAogICAgICBzdGF0dXM6IE9LCiAgICB9CiAgXQp9Cg==".getBytes()).getPayload(); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
        return paymentMapper.mapToBulkPayment(bulkPayments);
    }
}
