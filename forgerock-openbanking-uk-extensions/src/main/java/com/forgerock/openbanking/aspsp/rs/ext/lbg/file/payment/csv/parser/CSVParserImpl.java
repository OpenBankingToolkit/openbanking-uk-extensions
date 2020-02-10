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
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.factory.CSVFilePaymentFactory;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.factory.CSVFilePaymentType;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.model.CSVCreditIndicatorRow;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.model.CSVDebitIndicatorSection;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.model.CSVFilePayment;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.model.CSVHeaderIndicatorSection;
import com.forgerock.openbanking.exceptions.OBErrorException;
import com.forgerock.openbanking.model.error.OBRIErrorType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CSVParserImpl implements CSVParser {
    private final String content;
    private final CSVFilePaymentType csvFilePaymentType;
    private CSVFilePayment csvFilePayment;
    // contents
    private CSVHeaderIndicatorSection csvHeaderIndicatorSection;
    private CSVDebitIndicatorSection csvDebitIndicatorSection;

    private List<CSVCreditIndicatorRow> csvCreditIndicatorRowList;

    public CSVParserImpl(final String content, final CSVFilePaymentType csvFilePaymentType) {
        this.content = content;
        this.csvFilePaymentType = csvFilePaymentType;
        csvCreditIndicatorRowList = new ArrayList<>();
    }

    @Override
    public CSVParser parse() throws OBErrorException {
        try {
            org.apache.commons.csv.CSVParser parser = org.apache.commons.csv.CSVParser.parse(content, CSVFormat.DEFAULT);
            parser.forEach(r -> {
                if (r.getRecordNumber() == 1) {
                    setHeaderIndicatorSection(r);
                } else if (r.getRecordNumber() == 2) {
                    setDebitIndicatorSection(r);
                } else {
                    setCreditIndicatorRow(r);
                }
            });
        } catch (IOException ioException) {
            log.error("Error parsing the content for payment type '{}'. {}{}", csvFilePaymentType.getFileType(), ioException);
            throw new OBErrorException(OBRIErrorType.REQUEST_UNDEFINED_ERROR_YET, ioException.getMessage());
        }

        csvFilePayment = CSVFilePaymentFactory.create(csvFilePaymentType);
        csvFilePayment.setHeaderIndicator(csvHeaderIndicatorSection);
        csvFilePayment.setDebitIndicator(csvDebitIndicatorSection);
        csvFilePayment.setCreditIndicatorRows(csvCreditIndicatorRowList);
        return this;
    }

    @Override
    public CSVFilePayment getCsvFilePayment() {
        return csvFilePayment;
    }

    @Override
    public CSVFilePaymentType getCsvFilePaymentType() {
        return csvFilePaymentType;
    }

    @Override
    public void setHeaderIndicatorSection(final CSVRecord record) throws CSVErrorException {
        try {
            csvHeaderIndicatorSection = CSVHeaderIndicatorSection.builder()
                    .headerIndicator(getValue(record.get(0)))
                    .fileCreationDate(getValue(record.get(1)))
                    .uniqueId(getValue(record.get(2)))
                    .numCredits(Integer.parseInt(getValue(record.get(3))))
                    .valueCreditsSum(getValue(record.get(4)) != null ? new BigDecimal(getValue(record.get(4))) : new BigDecimal(0))
                    .build();
        } catch (Exception e) {
            log.error("Error parsing the header indicator section for payment type '{}'. {}{}", csvFilePaymentType.getFileType(), CSVErrorType.INVALID_FORMAT.getLogMessage(), e.toString());
            throw new CSVErrorException(CSVErrorType.INVALID_FORMAT, e.toString());
        }
    }

    @Override
    public void setHeaderIndicatorSection(CSVHeaderIndicatorSection section) {
        csvHeaderIndicatorSection = section;
    }

    @Override
    public void setDebitIndicatorSection(final CSVRecord record) throws CSVErrorException {
        try {
            csvDebitIndicatorSection = CSVDebitIndicatorSection.builder()
                    .debitIndicator(getValue(record.get(0)))
                    .batchReference(getValue(record.get(1)))
                    .debitAccountDetails(getValue(record.get(2)))
                    .build();
        } catch (Exception e) {
            log.error("Error parsing the debit indicator section for payment type '{}'. {}{}", csvFilePaymentType.getFileType(), CSVErrorType.INVALID_FORMAT.getLogMessage(), e.toString());
            throw new CSVErrorException(CSVErrorType.INVALID_FORMAT, e.toString());
        }
    }

    @Override
    public void setDebitIndicatorSection(CSVDebitIndicatorSection section) {
        csvDebitIndicatorSection = section;
    }

    @Override
    public void setCreditIndicatorRow(final CSVRecord record) throws CSVErrorException {
        try {
            csvCreditIndicatorRowList.add(
                    CSVCreditIndicatorRow.builder()
                            .creditIndicator(getValue(record.get(0)))
                            .recipientName(getValue(record.get(1)))
                            .accNumber(getValue(record.get(2)))
                            .recipientSortCode(getValue(record.get(3)))
                            .reference(getValue(record.get(4)))
                            .debitAmount(getValue(record.get(5)) != null ? new BigDecimal(getValue(record.get(5))) : new BigDecimal(0))
                            .paymentASAP(getValue(record.get(6)))
                            .paymentDate(getValue(record.get(7)))
                            .eToEReference(getValue(record.get(8)))
                            .build()
            );
        } catch (Exception e) {
            log.error("Error parsing the credit indicator row for payment type '{}'. {}{}", csvFilePaymentType.getFileType(), CSVErrorType.INVALID_FORMAT.getLogMessage(), e.toString());
            throw new CSVErrorException(CSVErrorType.INVALID_FORMAT, e.toString());
        }
    }

    @Override
    public void setCreditIndicatorRow(CSVCreditIndicatorRow row) {
        csvCreditIndicatorRowList.add(row);
    }

}
