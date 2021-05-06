/**
 * Copyright 2019 ForgeRock AS.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.model;

import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.factory.CSVFilePaymentType;
import com.forgerock.openbanking.common.model.openbanking.domain.common.FRAmount;
import com.forgerock.openbanking.common.model.openbanking.forgerock.filepayment.v3_0.FRFilePayment;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CSVFilePaymentImpl implements CSVFilePayment {

    private CSVHeaderIndicatorSection headerIndicatorSection;
    private CSVDebitIndicatorSection debitIndicatorSection;
    private List<CSVCreditIndicatorRow> creditIndicatorRows;
    private final CSVFilePaymentType filePaymentType;

    public CSVFilePaymentImpl(final CSVFilePaymentType filePaymentType) {
        this.filePaymentType = filePaymentType;
    }

    @Override
    public void setHeaderIndicator(CSVHeaderIndicatorSection headerIndicator) {
        this.headerIndicatorSection = headerIndicator;
    }

    @Override
    public CSVHeaderIndicatorSection getHeaderIndicatorSection() {
        return headerIndicatorSection;
    }

    @Override
    public CSVDebitIndicatorSection getDebitIndicatorSection() {
        return debitIndicatorSection;
    }

    @Override
    public List<CSVCreditIndicatorRow> getCreditIndicatorRows() {
        return creditIndicatorRows;
    }

    @Override
    public CSVFilePaymentType getFilePaymentType() {
        return filePaymentType;
    }

    @Override
    public List<FRFilePayment> toFRFilePaymentList() {
        List<FRFilePayment> frFilePaymentList = new ArrayList<>();
        for (CSVCreditIndicatorRow csvCreditIndicatorRow : creditIndicatorRows) {
            String e2eId = csvCreditIndicatorRow.getEToEReference() != null ? csvCreditIndicatorRow.getEToEReference() : Strings.EMPTY;
            String remittanceUnstructured = csvCreditIndicatorRow.getReference() + (e2eId.isEmpty() ? e2eId : " - " + e2eId);
            frFilePaymentList.add(
                    FRFilePayment.builder()
                            .instructionIdentification(headerIndicatorSection.getUniqueId())
                            .endToEndIdentification(e2eId)
                            .status(FRFilePayment.PaymentStatus.PENDING)
                            .created(DateTime.now())
                            .remittanceReference(csvCreditIndicatorRow.getReference())
                            .remittanceUnstructured(remittanceUnstructured)
                            .instructedAmount(
                                    FRAmount.builder()
                                            .amount(csvCreditIndicatorRow.getDebitAmount().toPlainString())
                                            .currency(GBP).build()
                            )
                            .creditorAccountIdentification(csvCreditIndicatorRow.getAccNumber()
                                    + csvCreditIndicatorRow.getRecipientSortCode())
                            .build()
            );
        }
        return frFilePaymentList;
    }

    @Override
    public void setDebitIndicator(CSVDebitIndicatorSection debitIndicator) {
        this.debitIndicatorSection = debitIndicator;
    }

    @Override
    public void setCreditIndicatorRows(List<CSVCreditIndicatorRow> creditIndicatorRows) {
        this.creditIndicatorRows = creditIndicatorRows;
    }

    @Override
    public BigDecimal getCreditRowsTotalDebitAmount() {
        return creditIndicatorRows.stream().map(CSVCreditIndicatorRow::getDebitAmount).reduce(BigDecimal::add).get();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(headerIndicatorSection.toCsvString());
        sb.append("\n");
        sb.append(debitIndicatorSection.toCsvString());
        sb.append("\n");
        creditIndicatorRows.stream().forEach(r -> {
                    sb.append(r.toCsvString()).append("\n");
                }
        );
        return sb.toString();
    }
}
