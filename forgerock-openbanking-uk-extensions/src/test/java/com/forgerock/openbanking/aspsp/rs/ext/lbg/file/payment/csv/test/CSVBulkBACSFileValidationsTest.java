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
package com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.test;

import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.exception.CSVErrorException;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.exception.CSVErrorType;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.factory.CSVFilePaymentType;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.factory.CSVParserFactory;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.model.CSVFilePayment;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.parser.CSVParser;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.validation.CSVBulkBACSFileValidationService;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.validation.CSVValidation;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.org.openbanking.datamodel.error.OBStandardErrorCodes1;

import java.io.IOException;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

/**
 * Validation test for Bulk BACS file
 */
public class CSVBulkBACSFileValidationsTest extends CSVFileValidationsTest {

    @Before
    public void setup() {
        Exception error = catchThrowableOfType(
                () -> setFile(CSVFilePaymentType.UK_LBG_BACS_BULK_V10),
                Exception.class
        );
        assertThat(error).isNull();
        assertThat(file).isNotNull();
    }
    /**
     * Parse resource file<br/>
     * <li>No errors expected</li>
     */
    @Test
    public void bacs_parseFromFileNoErrors() throws IOException {
        String csvFileContent = getContent(CSVBulkBACSFileValidationsTest.class.getClassLoader().getResource(RESOURCES_PACK + "/Bulk-BACS-file.csv").getFile());
        AtomicReference<CSVParser> parse = new AtomicReference<>();
        AtomicReference<CSVFilePayment> fromFile = new AtomicReference<>();
        Exception error = catchThrowableOfType(
                () -> {
                    parse.set(CSVParserFactory.parse(CSVFilePaymentType.UK_LBG_BACS_BULK_V10, csvFileContent).parse());
                    fromFile.set(parse.get().getCsvFilePayment());
                },
                Exception.class
        );
        assertThat(error).isNull();
        assertThat(fromFile.get()).isNotNull();
    }

    /**
     * Validation Date format: credit rows, payment date<br>
     * <li>Rule: All dates in the file must match with the pattern 'yyyyMMdd'</li>
     * <li>Rule: The debit payment date cannot be in the past from processing day</li>
     * <li>CSVErrorException INVALID_PAYMENT_DATE expected</li>
     */
    @Test
    public void bacs_debitIndicator_paymentDate_past_fails() {
        file.getDebitIndicatorSection().setPaymentDate("20200126");
        CSVErrorException errorException = catchThrowableOfType(
                () -> new CSVBulkBACSFileValidationService(file).validate(),
                CSVErrorException.class
        );
        assertThat(errorException.getCsvErrorType()).isEqualTo(CSVErrorType.INVALID_PAYMENT_DATE);
        assertThat(errorException.getCsvErrorType().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorException.getOBError().getErrorCode()).isEqualTo(OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT.getValue());
        assertThat(errorException.getMessage()).isEqualTo(String.format("Date format error, parse error. The date '20200126' cannot be in the past."));
    }

    /**
     * Validation Date format: credit rows, payment date<br>
     * <li>Rule: All dates in the file must match with the pattern 'yyyyMMdd'</li>
     * <li>Rule: The debit payment date cannot be in the past from processing day</li>
     * <li>Rule: The debit payment date must be at last 2 days later from processing day</li>
     * <li>CSVErrorException INVALID_PAYMENT_DATE_LATER_DAYS expected</li>
     */
    @Test
    public void bacs_debitIndicator_paymentDate_laterDays_fails() {
        file.getDebitIndicatorSection().setPaymentDate(file.getDateTimeFormatter().format(LocalDate.now()));
        CSVErrorException errorException = catchThrowableOfType(
                () -> new CSVBulkBACSFileValidationService(file).validate(),
                CSVErrorException.class
        );
        assertThat(errorException.getCsvErrorType()).isEqualTo(CSVErrorType.INVALID_PAYMENT_DATE_LATER_DAYS);
        assertThat(errorException.getCsvErrorType().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorException.getOBError().getErrorCode()).isEqualTo(OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT.getValue());
        assertThat(errorException.getMessage()).isEqualTo(String.format("Date format error, parse error. The date '%s' must be at last %d days later from processing day.", file.getDebitIndicatorSection().getPaymentDate(), CSVValidation.PAYMENT_LATER_DAYS));
    }

    /**
     * Validation Date format: credit rows, payment date<br>
     * <li>Rule: All dates in the file must match with the pattern 'yyyyMMdd'</li>
     * <li>Rule: The debit payment date cannot be in the past from processing day</li>
     * <li>Rule: The debit payment date must be at last 2 days later from processing day</li>
     * <li>Rule: The debit payment date cannot be beyond 31 days from processing day</li>
     * <li>CSVErrorException INVALID_PAYMENT_BEYOND_DATE expected</li>
     */
    @Test
    public void bacs_debitIndicator_paymentDate_beyond_fails() {
        file.getDebitIndicatorSection().setPaymentDate(file.getDateTimeFormatter().format(LocalDate.now().plusDays(50)));
        CSVErrorException errorException = catchThrowableOfType(
                () -> new CSVBulkBACSFileValidationService(file).validate(),
                CSVErrorException.class
        );
        assertThat(errorException.getCsvErrorType()).isEqualTo(CSVErrorType.INVALID_PAYMENT_BEYOND_DATE);
        assertThat(errorException.getCsvErrorType().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorException.getOBError().getErrorCode()).isEqualTo(OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT.getValue());
        assertThat(errorException.getMessage()).isEqualTo(String.format("Date format error, parse error. The payment date '%s' cannot be beyond %d days from processing day.", file.getDebitIndicatorSection().getPaymentDate(), CSVValidation.BEYOND_PAYMENT_DAYS));
    }

}
