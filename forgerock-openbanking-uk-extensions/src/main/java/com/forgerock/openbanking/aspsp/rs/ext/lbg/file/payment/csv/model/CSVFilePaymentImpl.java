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

import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.factory.CSVFilePaymentFactory;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.factory.CSVFilePaymentType;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.validation.CSVValidation;
import com.forgerock.openbanking.exceptions.OBErrorException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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

    public static void main(String[] args) throws OBErrorException {
        CSVFilePayment file = null;
        if (file == null) {
            file = CSVFilePaymentFactory.create(CSVFilePaymentType.UK_LBG_FPS_BATCH_V10);
        }
        file.setHeaderIndicator(
                CSVHeaderIndicatorSection.builder()
                        .headerIndicator(CSVHeaderIndicatorSection.HEADER_IND_EXPECTED)
                        .fileCreationDate(file.getDateTimeFormatter().format(LocalDate.now()))
                        .uniqueId("ID001")
                        .numCredits(1)
                        .valueCreditsSum(new BigDecimal(10.10).setScale(2, RoundingMode.CEILING))
                        .build()
        );

        file.setDebitIndicator(
                CSVDebitIndicatorSection.builder()
                        .debitIndicator(CSVDebitIndicatorSection.DEBIT_IND_EXPECTED)
                        .paymentDate(file.getDateTimeFormatter().format(LocalDate.now().plusDays(2)))
                        .batchReference("Payments")
                        .debitAccountDetails("301775-12345678")
                        .build()
        );

        List row = new ArrayList<CSVCreditIndicatorRow>();
        row.add(
                CSVCreditIndicatorRow.builder()
                        .creditIndicator(CSVCreditIndicatorRow.CREDIT_IND_EXPECTED)
                        .recipientName("Beneficiary name")
                        .accNumber("12345678")
                        .recipientSortCode("301763")
                        .reference("Beneficiary ref.")
                        .debitAmount(new BigDecimal(10.10).setScale(2, RoundingMode.CEILING))
                        .paymentASAP(CSVValidation.PAYMENT_ASAP_VALUES[0])
                        .paymentDate("")
                        .eToEReference("EtoEReference")
                        .build()
        );

        file.setCreditIndicatorRows(row);

        System.out.println(file.toString());
    }
}
