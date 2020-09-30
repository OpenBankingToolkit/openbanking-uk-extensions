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
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.model.CSVCreditIndicatorRow;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.model.CSVDebitIndicatorSection;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.model.CSVFilePayment;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.model.CSVHeaderIndicatorSection;
import com.forgerock.openbanking.common.model.openbanking.persistence.payment.FRFileConsent;
import com.forgerock.openbanking.exceptions.OBErrorException;
import com.forgerock.openbanking.model.error.OBRIErrorType;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class CSVValidationService implements CSVValidation {

    private final CSVFilePayment file;

    public CSVValidationService(final CSVFilePayment file) {
        this.file = file;
    }

    /**
     * main method to validate
     * @throws CSVErrorException
     */
    @Override
    public void validate() throws CSVErrorException {
        validateHeaderIndicator();
        validateDebitIndicator();
        validateCreditRows();
    }

    /**
     * Header section validation:<br>
     * <li>The header indicator must be 'H', not null, not empty or blank</li>
     * <li>The date must match with the pattern 'yyyyMMdd'</li>
     */
    public void validateHeaderIndicator() {
        checkNotNull(file.getHeaderIndicatorSection(), "Header Indicator section");
        checkNotNull(file.getHeaderIndicatorSection().getHeaderIndicator(), "Header Indicator value");
        if (!file.getHeaderIndicatorSection().getHeaderIndicator().equals(CSVHeaderIndicatorSection.HEADER_IND_EXPECTED)) {
            log.error(CSVErrorType.INVALID_HEADER_INDICATOR.getLogMessage(), CSVHeaderIndicatorSection.HEADER_IND_EXPECTED, file.getHeaderIndicatorSection().getHeaderIndicator());
            throw new CSVErrorException(CSVErrorType.INVALID_HEADER_INDICATOR, CSVHeaderIndicatorSection.HEADER_IND_EXPECTED, file.getHeaderIndicatorSection().getHeaderIndicator());
        }
        validateDateFormat(file.getHeaderIndicatorSection().getFileCreationDate());
    }

    /**
     * Debit section validation:<br/>
     * <li>Debit indicator must be 'D', not null, not empty or blank</li>
     */
    protected void validateDebitIndicator() {
        checkNotNull(file.getDebitIndicatorSection(), "Debit Indicator section");
        checkNotNull(file.getDebitIndicatorSection().getDebitIndicator(), "Debit Indicator value");
        if (!file.getDebitIndicatorSection().getDebitIndicator().equals(CSVDebitIndicatorSection.DEBIT_IND_EXPECTED)) {
            log.error(CSVErrorType.INVALID_DEBIT_INDICATOR.getLogMessage(), CSVDebitIndicatorSection.DEBIT_IND_EXPECTED, file.getDebitIndicatorSection().getDebitIndicator());
            throw new CSVErrorException(CSVErrorType.INVALID_DEBIT_INDICATOR, CSVDebitIndicatorSection.DEBIT_IND_EXPECTED, file.getDebitIndicatorSection().getDebitIndicator());
        }
    }

    /**
     * Credit Rows section validation:<br/>
     * <li>Not allowed more than 25 credit rows</li>
     * <li>The number of credits value from header section match with the number of credit rows</li>
     * <li>The credit value from header match with the sum of debit amount of credit rows</li>
     * <li>The credit row indicator must be 'C', not null, not empty or blank</li>
     * <li>The reference not contains the word 'CONTRA', case insensitive, accepted it is part of another string</li>
     * <li>Payment ASAP must be 'Y', 'N', null or empty</li>
     * <li>When payment ASAP = 'Y' then cannot have payment Date entry</li>
     * <li>When payment ASAP = 'N' then must be a valid payment Date entry</li>
     * <li>When payment ASAP and payment Date cannot both be empty</li>
     * <li>When the payment Date exist must not be in the past, same day or later accepted</li>
     * <li>When the payment Date exist cannot be beyond 31 days from processing day</li>
     * <li>The payment date must match the pattern 'yyyyMMdd'</li>
     */
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
            validatePaymentCreditRow();
        }
    }

    /**
     * Credit Rows section validation:<br/>
     * <li>Not allowed more than 25 credit rows</li>
     */
    protected void validateCreditRowsAllowed() {
        if (file.getCreditIndicatorRows().size() > CREDIT_ROWS_ALLOWED | file.getHeaderIndicatorSection().getNumCredits() > CREDIT_ROWS_ALLOWED) {
            log.error(CSVErrorType.NUMBER_CREDITS_ROWS_NOT_ALLOWED.getLogMessage(), CREDIT_ROWS_ALLOWED, file.getCreditIndicatorRows().size());
            throw new CSVErrorException(CSVErrorType.NUMBER_CREDITS_ROWS_NOT_ALLOWED, CREDIT_ROWS_ALLOWED, file.getCreditIndicatorRows().size());
        }
    }

    /**
     * Credit Rows section validation:<br/>
     * <li>The number of credits value from header section match with the number of credit rows</li>
     */
    protected void validateNumberOfCredits() {
        if (!(file.getHeaderIndicatorSection().getNumCredits() == file.getCreditIndicatorRows().size())) {
            log.error(CSVErrorType.NUMBER_CREDITS_NOT_MATCH.getLogMessage());
            throw new CSVErrorException(CSVErrorType.NUMBER_CREDITS_NOT_MATCH);
        }
    }

    /**
     * Credit Rows indicator validation:<br/>
     * <li>The credit row indicator must be 'C', not null, not empty or blank</li>
     */
    protected void validateCreditRowsIndicator() {
        AtomicInteger founds = new AtomicInteger();
        if (file.getCreditIndicatorRows().isEmpty()) {
            log.error("{} There are no 'Credit Indicator Rows'.", CSVErrorType.INVALID_FORMAT.getLogMessage());
            throw new CSVErrorException(CSVErrorType.INVALID_FORMAT);
        } else {
            file.getCreditIndicatorRows().forEach(c -> {
                checkNotNull(c.getCreditIndicator(), "Credit Indicator row value");
                if (!c.getCreditIndicator().equals(CSVCreditIndicatorRow.CREDIT_IND_EXPECTED)) {
                    founds.getAndIncrement();
                }
            });
        }
        if (founds.get() > 0) {
            log.error(CSVErrorType.INVALID_CREDIT_INDICATOR.getLogMessage(), CSVCreditIndicatorRow.CREDIT_IND_EXPECTED, founds.get());
            throw new CSVErrorException(CSVErrorType.INVALID_CREDIT_INDICATOR, CSVCreditIndicatorRow.CREDIT_IND_EXPECTED, founds.get());
        }
    }

    /**
     * Credit Rows reference validation:<br/>
     * <li>The reference not contains the word 'CONTRA', case insensitive, accepted it is part of another string</li>
     */
    protected void validateCreditRowsReference() {
        AtomicInteger founds = new AtomicInteger();
        if (file.getCreditIndicatorRows().isEmpty()) {
            log.error("{} There are no 'Credit Indicator Rows'.", CSVErrorType.INVALID_FORMAT.getLogMessage());
            throw new CSVErrorException(CSVErrorType.INVALID_FORMAT);
        } else {
            file.getCreditIndicatorRows().forEach(c -> {
                Matcher matcher = Pattern.compile(REF_PATTERN, Pattern.CASE_INSENSITIVE).matcher(c.getReference());
                if (matcher.find()) {
                    founds.getAndIncrement();

                }
            });
        }
        if (founds.get() > 0) {
            log.error(CSVErrorType.INVALID_REFERENCE_FORMAT.getLogMessage(), founds.get(), REF_WORD_TO_FIND);
            throw new CSVErrorException(CSVErrorType.INVALID_REFERENCE_FORMAT, founds.get(), REF_WORD_TO_FIND);
        }
    }

    /**
     * Credit Rows payment validation
     */
    protected void validatePaymentCreditRow() {
        isCreditRowsEmpty();
        file.getCreditIndicatorRows().forEach(c -> {
            validatePaymentASAP(c);
        });
    }

    /**
     * Credit Rows sum debit amount validation:<br/>
     * <li>The credit value from header must match with the sum of debit amount rows</li>
     */
    protected void validateCreditSum() {
        if (file.getHeaderIndicatorSection().getValueCreditsSum().compareTo(file.getCreditRowsTotalDebitAmount())!=0) {
            log.error(CSVErrorType.INVALID_CREDIT_AMOUNT.getLogMessage(), file.getHeaderIndicatorSection().getValueCreditsSum().toPlainString(), file.getCreditRowsTotalDebitAmount().toPlainString());
            throw new CSVErrorException(CSVErrorType.INVALID_CREDIT_AMOUNT, file.getHeaderIndicatorSection().getValueCreditsSum().toPlainString(), file.getCreditRowsTotalDebitAmount().toPlainString());
        }
    }

    /**
     * DAte format validation:<br/>
     * <li>The date must match with the pattern 'yyyyMMdd'</li>
     */
    protected void validateDateFormat(String date) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        try {
            dateTimeFormatter.parse(date);
        } catch (DateTimeParseException dtpe) {
            log.error(CSVErrorType.INVALID_DATE_FORMAT.getLogMessage(), date, DATE_FORMAT);
            throw new CSVErrorException(CSVErrorType.INVALID_DATE_FORMAT, date, DATE_FORMAT);
        }
    }

    /**
     * Credit Rows payment Date validation:<br/>
     * <li>The date cannot be in the past, processing day and later allowed</li>
     * <li>The date cannot be beyond 31 days from processing day</li>
     */
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

    /**
     * Credit Rows payment ASAP validation:<br/>
     * <li>Payment ASAP must be 'Y', 'N', null or empty</li>
     * <li>When payment ASAP = 'Y' then cannot have payment Date entry</li>
     * <li>When payment ASAP = 'N' then must be a valid payment Date entry</li>
     * <li>When payment ASAP and payment Date cannot both be empty</li>
     */
    protected void validatePaymentASAP(CSVCreditIndicatorRow csvCreditIndicatorRow) {
        if (csvCreditIndicatorRow.getPaymentASAP() == null && csvCreditIndicatorRow.getPaymentDate() == null) {
            log.error(CSVErrorType.INVALID_CREDIT_PAYMENT.getLogMessage(), PAYMENT_ASAP_VALUES[1]);
            throw new CSVErrorException(CSVErrorType.INVALID_CREDIT_PAYMENT, PAYMENT_ASAP_VALUES[1]);
        } else if (PAYMENT_ASAP_VALUES[2].equals(csvCreditIndicatorRow.getPaymentASAP().toUpperCase())) {
            if (EMPTY_LIKE_NULL.equals(csvCreditIndicatorRow.getPaymentDate()) | csvCreditIndicatorRow.getPaymentDate() == null) {
                log.error(CSVErrorType.INVALID_CREDIT_PAYMENT.getLogMessage(), PAYMENT_ASAP_VALUES[1]);
                throw new CSVErrorException(CSVErrorType.INVALID_CREDIT_PAYMENT, PAYMENT_ASAP_VALUES[1]);
            } else {
                validateDateFormat(csvCreditIndicatorRow.getPaymentDate());
                validatePaymentDate(csvCreditIndicatorRow.getPaymentDate());
            }
        } else if (PAYMENT_ASAP_VALUES[0].equals(csvCreditIndicatorRow.getPaymentASAP().toUpperCase())) {
            if (!(EMPTY_LIKE_NULL.equals(csvCreditIndicatorRow.getPaymentDate()) | csvCreditIndicatorRow.getPaymentDate() == null)) {
                log.error(CSVErrorType.INVALID_PAYMENT_ASAP_EMPTY.getLogMessage(), PAYMENT_ASAP_VALUES[0]);
                throw new CSVErrorException(CSVErrorType.INVALID_PAYMENT_ASAP_EMPTY, PAYMENT_ASAP_VALUES[0]);
            }
        } else if (PAYMENT_ASAP_VALUES[1].equals(csvCreditIndicatorRow.getPaymentASAP().toUpperCase())) {
            if (EMPTY_LIKE_NULL.equals(csvCreditIndicatorRow.getPaymentDate()) | csvCreditIndicatorRow.getPaymentDate() == null) {
                log.error(CSVErrorType.INVALID_PAYMENT_ASAP_ENTRY.getLogMessage(), PAYMENT_ASAP_VALUES[1]);
                throw new CSVErrorException(CSVErrorType.INVALID_PAYMENT_ASAP_ENTRY, PAYMENT_ASAP_VALUES[1]);
            } else {
                validateDateFormat(csvCreditIndicatorRow.getPaymentDate());
                validatePaymentDate(csvCreditIndicatorRow.getPaymentDate());
            }
        } else {
            log.error(CSVErrorType.INVALID_PAYMENT_ASAP_FORMAT.getLogMessage(), Arrays.toString(PAYMENT_ASAP_VALUES));
            throw new CSVErrorException(CSVErrorType.INVALID_PAYMENT_ASAP_FORMAT, Arrays.toString(PAYMENT_ASAP_VALUES));
        }
    }

    /**
     * Credit rows empty validation
     */
    protected void isCreditRowsEmpty() {
        if (file.getCreditIndicatorRows().isEmpty()) {
            log.error("{} There are no 'Credit Indicator Rows'.", CSVErrorType.INVALID_FORMAT.getLogMessage());
            throw new CSVErrorException(CSVErrorType.INVALID_FORMAT);
        }
    }

    /**
     * Check null object
     *
     * @param obj    object to check
     * @param object identification name (section, field) to check
     */
    protected void checkNotNull(Object obj, String object) {
        if (obj == null) {
            log.error("{} The '{}' is null.", CSVErrorType.INVALID_FORMAT.getLogMessage(), object);
            throw new CSVErrorException(CSVErrorType.INVALID_FORMAT, String.format("The '%s' is null.", object));
        }
    }

    /*
    CONSENT VALIDATIONS
     */

    public static class Consent {
        /**
         * Check that the provided payment file and payment file consent metadata have the same control sum (defined as sum of all transaction amounts in file).
         * This is an extra validation step to ensure correct and valid file has been uploaded for the consent
         * @param fileConsent Payment file body
         * @param filePayment Payment file consent
         * @throws OBErrorException Validation failed
         */
        public static void numTransactions(FRFileConsent fileConsent, CSVFilePayment filePayment) throws OBErrorException {
            if(fileConsent.getInitiation().getNumberOfTransactions()!=null | !fileConsent.getInitiation().getNumberOfTransactions().isBlank() | !fileConsent.getInitiation().getNumberOfTransactions().isEmpty()) {
                log.debug("Metadata indicates expected transaction count of '{}'. File contains '{}' transactions", fileConsent.getInitiation().getNumberOfTransactions(), filePayment.getHeaderIndicatorSection().getNumCredits());
                if (filePayment.getHeaderIndicatorSection().getNumCredits() != Integer.valueOf(fileConsent.getInitiation().getNumberOfTransactions())) {
                    log.warn("File consent metadata indicated {} transactions would be present but found {} in uploaded file", fileConsent.getInitiation().getNumberOfTransactions(), filePayment.getHeaderIndicatorSection().getNumCredits());
                    throw new OBErrorException(OBRIErrorType.REQUEST_FILE_WRONG_NUMBER_OF_TRANSACTIONS, String.valueOf(filePayment.getHeaderIndicatorSection().getNumCredits()), fileConsent.getInitiation().getNumberOfTransactions());
                }
                log.debug("File transaction count is correct for consent id: {}", fileConsent.getId());
            }else{
                log.warn("The consent id {} for the type {} don't have number of transactions entry value, ignoring the validation.", fileConsent.getId(), filePayment.getFilePaymentType().getFileType());
            }
        }

        /**
         * Check that the provided payment file and payment file consent metadata have the same control sum (defined as sum of all transaction amounts in file).
         * This is an extra validation step to ensure correct and valid file has been uploaded for the consent
         * @param fileConsent Payment file body
         * @param filePayment Payment file consent
         * @throws OBErrorException Validation failed
         */
        public static void controlSum(FRFileConsent fileConsent, CSVFilePayment filePayment) throws OBErrorException {
            if(fileConsent.getInitiation().getControlSum()!=null) {
                BigDecimal fileControlSum = filePayment.getCreditRowsTotalDebitAmount();
                log.debug("Metadata indicates expected control sum of '{}'. File contains actual control sum of '{}'", fileConsent.getInitiation().getControlSum(), fileControlSum);
                if (fileControlSum.compareTo(fileConsent.getInitiation().getControlSum()) != 0) {
                    log.warn("File consent metadata indicated control sum of '{}' but found a control sum of '{}' in uploaded file", fileConsent.getInitiation().getControlSum(), fileControlSum);
                    throw new OBErrorException(OBRIErrorType.REQUEST_FILE_INCORRECT_CONTROL_SUM, fileControlSum.toPlainString(), fileConsent.getInitiation().getControlSum().toPlainString());
                }
                log.debug("File control sum count is correct for consent id: {}", fileConsent.getId());
            }else{
                log.warn("The consent id {} for the type {} don't have control sum entry value, ignoring the validation.", fileConsent.getId(), filePayment.getFilePaymentType().getFileType());
            }
        }
    }
}
