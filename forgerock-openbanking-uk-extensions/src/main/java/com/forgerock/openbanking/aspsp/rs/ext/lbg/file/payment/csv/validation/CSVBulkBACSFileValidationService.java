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
package com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.validation;

import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.exception.CSVErrorException;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.exception.CSVErrorType;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.model.CSVFilePayment;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;

@Slf4j
public class CSVBulkBACSFileValidationService extends CSVValidationService {
    private final CSVFilePayment file;

    public CSVBulkBACSFileValidationService(final CSVFilePayment file) {
        super(file);
        this.file = file;
    }

    /**
     * Debit section validation:<br/>
     * <li>Debit indicator must be 'D', not null, not empty or blank</li>
     * <li>The payment date must not be in the past</li>
     */
    protected void validateDebitIndicator() {
        super.validateDebitIndicator();
        validatePaymentDate(file.getDebitIndicatorSection().getPaymentDate());
    }

    /**
     * Credit Rows section validation:<br/>
     * <li>Not allowed more than 25 credit rows</li>
     * <li>The number of credits value from header section match with the number of credit rows</li>
     * <li>The credit value from header match with the sum of debit amount of credit rows</li>
     * <li>The credit row indicator must be 'C', not null, not empty or blank</li>
     * <li>The reference not contains the word 'CONTRA', case insensitive, accepted it is part of another string</li>
     */
    @Override
    protected void validateCreditRows() {
        checkNotNull(file.getCreditIndicatorRows(), "Credit Indicator Rows");
        if (file.getCreditIndicatorRows().isEmpty()) {
            log.error("{} There are no 'Credit Indicator Rows'.", CSVErrorType.INVALID_FORMAT.getLogMessage());
            throw new CSVErrorException(CSVErrorType.INVALID_FORMAT);
        } else {
            validateCreditRowsAllowed();
            validateNumberOfCredits();
            validateCreditRowsIndicator();
            validateCreditRowsReference();
            validateCreditSum();
        }
    }

    /**
     * Credit Rows payment Date validation:<br/>
     * <li>The date cannot be in the past, processing day and later allowed</li>
     * <li>The date must be beyond 2 days later from processing day</li>
     * <li>The date cannot be beyond 31 days from processing day</li>
     * <li>Te date must match with the pattern 'yyyyMMdd'</li>
     */
    @Override
    protected void validatePaymentDate(String date) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        try {
            TemporalAccessor ta = dateTimeFormatter.parse(date);
            LocalDate ldtToday = LocalDate.now();
            LocalDate ldtRow = LocalDate.from(ta);
            // the date cannot be in the past, compare with today
            if (ldtRow.compareTo(ldtToday) < 0) {
                log.error(CSVErrorType.INVALID_PAYMENT_DATE.getLogMessage(), date);
                throw new CSVErrorException(CSVErrorType.INVALID_PAYMENT_DATE, date);
            } else if (ldtRow.compareTo(ldtToday.plusDays(PAYMENT_LATER_DAYS)) < 0) {
                // the payment date must be beyond 2 days later from processing day (now)
                log.error(CSVErrorType.INVALID_PAYMENT_DATE_LATER_DAYS.getLogMessage(), date, PAYMENT_LATER_DAYS);
                throw new CSVErrorException(CSVErrorType.INVALID_PAYMENT_DATE_LATER_DAYS, date, PAYMENT_LATER_DAYS);
            } else {
                // the payment date cannot be beyond 31 days from processing day (now)
                if (ldtToday.plusDays(BEYOND_PAYMENT_DAYS).compareTo(ldtRow) < 0) {
                    log.error(CSVErrorType.INVALID_PAYMENT_BEYOND_DATE.getLogMessage(), date, BEYOND_PAYMENT_DAYS);
                    throw new CSVErrorException(CSVErrorType.INVALID_PAYMENT_BEYOND_DATE, date, BEYOND_PAYMENT_DAYS);
                }
            }
        } catch (DateTimeParseException dtpe) {
            log.error(CSVErrorType.INVALID_DATE_FORMAT.getLogMessage(), date, DATE_FORMAT);
            throw new CSVErrorException(CSVErrorType.INVALID_DATE_FORMAT, date, DATE_FORMAT);
        }
    }
}
