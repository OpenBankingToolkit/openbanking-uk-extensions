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
package com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.parser;

import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.exception.CSVErrorException;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.exception.CSVErrorType;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.factory.CSVFilePaymentType;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.model.CSVCreditIndicatorRow;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.model.CSVDebitIndicatorSection;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVRecord;

import java.math.BigDecimal;

@Slf4j
public class CSVBulkBACSParser extends CSVParserImpl {

    public CSVBulkBACSParser(String content, CSVFilePaymentType csvFilePaymentType) {
        super(content, csvFilePaymentType);
    }

    @Override
    public void setDebitIndicatorSection(final CSVRecord record) throws CSVErrorException {
        try {
            super.setDebitIndicatorSection(CSVDebitIndicatorSection.builder()
                    .debitIndicator(getValue(record.get(0)))
                    .paymentDate(getValue(record.get(1)))
                    .batchReference(getValue(record.get(2)))
                    .debitAccountDetails(getValue(record.get(3)))
                    .build());
        } catch (Exception e) {
            log.error("Error parsing the debit indicator section for payment type '{}'. {}{}", getCsvFilePaymentType().getFileType(), CSVErrorType.INVALID_FORMAT.getLogMessage(), e.toString());
            throw new CSVErrorException(CSVErrorType.INVALID_FORMAT, e.toString());
        }
    }

    @Override
    public void setCreditIndicatorRow(CSVRecord record) throws CSVErrorException {
        try {
            super.setCreditIndicatorRow(CSVCreditIndicatorRow.builder()
                    .creditIndicator(getValue(record.get(0)))
                    .recipientName(getValue(record.get(1)))
                    .recipientSortCode(getValue(record.get(2)))
                    .accNumber(getValue(record.get(3)))
                    .reference(getValue(record.get(4)))
                    .debitAmount(getValue(record.get(5)) != null ? new BigDecimal(getValue(record.get(5))) : new BigDecimal(0))
                    .build());
        } catch (Exception e) {
            log.error("Error parsing the credit indicator row for payment type '{}'. {}{}", getCsvFilePaymentType().getFileType(), CSVErrorType.INVALID_FORMAT.getLogMessage(), e.toString());
            throw new CSVErrorException(CSVErrorType.INVALID_FORMAT, e.toString());
        }
    }
}
