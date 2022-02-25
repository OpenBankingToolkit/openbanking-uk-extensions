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
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.validation.CSVBatchFPSFileValidationService;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.validation.CSVValidation;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.org.openbanking.datamodel.error.OBStandardErrorCodes1;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

/**
 * Validation test for Batch FPS file
 */
public class CSVBatchFPSFileValidationsTest extends CSVFileValidationsTest {

    @Before
    public void setup() {
        Exception error = catchThrowableOfType(
                () -> setFile(CSVFilePaymentType.UK_LBG_FPS_BATCH_V10),
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
    public void fps_parseFromFileNoErrors() throws IOException {
        String csvFileContent = getContent(CSVBatchFPSFileValidationsTest.class.getClassLoader().getResource(RESOURCES_PACK + "/Batch-FPS-file.csv").getFile());
        AtomicReference<CSVParser> parse = new AtomicReference<>();
        AtomicReference<CSVFilePayment> fromFile = new AtomicReference<>();
        Exception error = catchThrowableOfType(
                () -> {
                    parse.set(CSVParserFactory.parse(CSVFilePaymentType.UK_LBG_FPS_BATCH_V10, csvFileContent).parse());
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
     * <li>CSVErrorException INVALID_DATE_FORMAT expected</li>
     */
    @Test
    public void fps_creditsRows_paymentDateFormat_fails() {
        file.getCreditIndicatorRows().get(0).setPaymentASAP(CSVValidation.PAYMENT_ASAP_VALUES[1]);
        file.getCreditIndicatorRows().get(0).setPaymentDate("20200141");
        CSVErrorException errorException = catchThrowableOfType(
                () -> new CSVBatchFPSFileValidationService(file).validate(),
                CSVErrorException.class
        );
        assertThat(errorException.getCsvErrorType()).isEqualTo(CSVErrorType.INVALID_DATE_FORMAT);
        assertThat(errorException.getCsvErrorType().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorException.getOBError().getErrorCode()).isEqualTo(OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT.getValue());
        assertThat(errorException.getMessage()).isEqualTo(String.format("Date format error, parse error. The date '20200141' not match with the date format '%s' expected.", CSVValidation.DATE_FORMAT));
    }

    /**
     * Validation credit rows: payment Date<br>
     * <li>Rule: All dates in the file must match with the pattern 'yyyyMMdd'</li>
     * <li>Rule: The payment date cannot be beyond 31 from processing day</li>
     * <li>CSVErrorException INVALID_PAYMENT_BEYOND_DATE expected</li>
     */
    @Test
    public void fps_creditsRows_paymentDateBeyondKO() {
        file.getCreditIndicatorRows().get(0).setPaymentASAP(CSVValidation.PAYMENT_ASAP_VALUES[1]);
        String date = file.getDateTimeFormatter().format(LocalDate.now().plusDays(50));
        file.getCreditIndicatorRows().get(0).setPaymentDate(date);
        CSVErrorException errorException = catchThrowableOfType(
                () -> new CSVBatchFPSFileValidationService(file).validate(),
                CSVErrorException.class
        );
        assertThat(errorException.getCsvErrorType()).isEqualTo(CSVErrorType.INVALID_PAYMENT_BEYOND_DATE);
        assertThat(errorException.getCsvErrorType().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorException.getOBError().getErrorCode()).isEqualTo(OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT.getValue());
        assertThat(errorException.getMessage()).isEqualTo(String.format("Date format error, parse error. The payment date '%s' cannot be beyond %d days from processing day.", date, CSVValidation.BEYOND_PAYMENT_DAYS));
    }

    /**
     * Validation credit rows: payment Date<br>
     * <li>Rule: All dates in the file must match with the pattern 'yyyyMMdd'</li>
     * <li>Rule: The payment date cannot be in the past from processing day</li>
     * <li>CSVErrorException INVALID_PAYMENT_DATE expected</li>
     */
    @Test
    public void fps_creditsRows_paymentDate_fails() {
        file.getCreditIndicatorRows().get(0).setPaymentASAP(CSVValidation.PAYMENT_ASAP_VALUES[1]);
        file.getCreditIndicatorRows().get(0).setPaymentDate("20200126");
        CSVErrorException errorException = catchThrowableOfType(
                () -> new CSVBatchFPSFileValidationService(file).validate(),
                CSVErrorException.class
        );
        assertThat(errorException.getCsvErrorType()).isEqualTo(CSVErrorType.INVALID_PAYMENT_DATE);
        assertThat(errorException.getCsvErrorType().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorException.getOBError().getErrorCode()).isEqualTo(OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT.getValue());
        assertThat(errorException.getMessage()).isEqualTo(String.format("Date format error, parse error. The date '20200126' cannot be in the past."));
    }

    /**
     * Validation credit rows: payment ASAP<br>
     * <li>Rule: Payment ASAP value must be 'Y', 'N', null, empty or blank</li>
     * <li>CSVErrorException INVALID_PAYMENT_ASAP_FORMAT expected</li>
     */
    @Test
    public void fps_creditsRows_paymentASAP_fails() {
        file.getCreditIndicatorRows().get(0).setPaymentASAP("ASAPXX");
        CSVErrorException errorException = catchThrowableOfType(
                () -> new CSVBatchFPSFileValidationService(file).validate(),
                CSVErrorException.class
        );
        assertThat(errorException.getCsvErrorType()).isEqualTo(CSVErrorType.INVALID_PAYMENT_ASAP_FORMAT);
        assertThat(errorException.getCsvErrorType().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorException.getOBError().getErrorCode()).isEqualTo(OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT.getValue());
        assertThat(errorException.getMessage()).isEqualTo(String.format("File format error, parse error. The payment ASAP will be '%s'", Arrays.toString(CSVValidation.PAYMENT_ASAP_VALUES)));
    }

    /**
     * Validation credit rows: payment ASAP 'Y'<br>
     * <li>Rule: If payment ASAP = 'Y' it don't expected payment date entry</li>
     * <li>CSVErrorException INVALID_PAYMENT_ASAP_EMPTY expected</li>
     */
    @Test
    public void fps_creditsRows_paymentASAP_Y_fails() {
        file.getCreditIndicatorRows().get(0).setPaymentDate("20200101");
        CSVErrorException errorException = catchThrowableOfType(
                () -> new CSVBatchFPSFileValidationService(file).validate(),
                CSVErrorException.class
        );
        assertThat(errorException.getCsvErrorType()).isEqualTo(CSVErrorType.INVALID_PAYMENT_ASAP_EMPTY);
        assertThat(errorException.getCsvErrorType().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorException.getOBError().getErrorCode()).isEqualTo(OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT.getValue());
        assertThat(errorException.getMessage()).isEqualTo(String.format("File format error, parse error. If the payment ASAP value is '%s' it cannot have a payment date entry.", CSVValidation.PAYMENT_ASAP_VALUES[0]));
    }

    /**
     * Validation credit rows: payment ASAP 'N' and payment date empty<br>
     * <li>Rule: If payment ASAP = 'N' it must have a payment date entry</li>
     * <li>CSVErrorException INVALID_PAYMENT_ASAP_ENTRY expected</li>
     */
    @Test
    public void fps_creditsRows_paymentASAP_N_fails() {
        file.getCreditIndicatorRows().get(0).setPaymentASAP(CSVValidation.PAYMENT_ASAP_VALUES[1]);
        file.getCreditIndicatorRows().get(0).setPaymentDate("");
        CSVErrorException errorException = catchThrowableOfType(
                () -> new CSVBatchFPSFileValidationService(file).validate(),
                CSVErrorException.class
        );
        assertThat(errorException.getCsvErrorType()).isEqualTo(CSVErrorType.INVALID_PAYMENT_ASAP_ENTRY);
        assertThat(errorException.getCsvErrorType().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorException.getOBError().getErrorCode()).isEqualTo(OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT.getValue());
        assertThat(errorException.getMessage()).isEqualTo(String.format("File format error, parse error. If the payment ASAP value is '%s' it must have a payment date entry.", CSVValidation.PAYMENT_ASAP_VALUES[1]));
    }

    /**
     * Validation credit rows: payment ASAP 'N' and payment date null<br>
     * <li>Rule: If payment ASAP = 'N' it must have a payment date entry</li>
     * <li>CSVErrorException INVALID_PAYMENT_ASAP_ENTRY expected</li>
     */
    @Test
    public void fps_creditsRows_paymentASAP_N_fails_null() {
        file.getCreditIndicatorRows().get(0).setPaymentASAP(CSVValidation.PAYMENT_ASAP_VALUES[1]);
        file.getCreditIndicatorRows().get(0).setPaymentDate(null);
        CSVErrorException errorException = catchThrowableOfType(
                () -> new CSVBatchFPSFileValidationService(file).validate(),
                CSVErrorException.class
        );
        assertThat(errorException.getCsvErrorType()).isEqualTo(CSVErrorType.INVALID_PAYMENT_ASAP_ENTRY);
        assertThat(errorException.getCsvErrorType().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorException.getOBError().getErrorCode()).isEqualTo(OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT.getValue());
        assertThat(errorException.getMessage()).isEqualTo(String.format("File format error, parse error. If the payment ASAP value is '%s' it must have a payment date entry.", CSVValidation.PAYMENT_ASAP_VALUES[1]));
    }

    /**
     * Validation credit rows: payment ASAP and payment date null<br>
     * <li>Rule: payment ASAP and payment date cannot both null, empty or blank</li>
     * <li>CSVErrorException INVALID_CREDIT_PAYMENT expected</li>
     */
    @Test
    public void fps_creditsRows_paymentASAP_EMPTY_fails() {
        file.getCreditIndicatorRows().get(0).setPaymentASAP(null);
        file.getCreditIndicatorRows().get(0).setPaymentDate(null);
        CSVErrorException errorException = catchThrowableOfType(
                () -> new CSVBatchFPSFileValidationService(file).validate(),
                CSVErrorException.class
        );
        assertThat(errorException.getCsvErrorType()).isEqualTo(CSVErrorType.INVALID_CREDIT_PAYMENT);
        assertThat(errorException.getCsvErrorType().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorException.getOBError().getErrorCode()).isEqualTo(OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT.getValue());
        assertThat(errorException.getMessage()).isEqualTo(String.format("File format error, parse error. The payment ASAP and payment date cannot both be empty."));
    }

}
