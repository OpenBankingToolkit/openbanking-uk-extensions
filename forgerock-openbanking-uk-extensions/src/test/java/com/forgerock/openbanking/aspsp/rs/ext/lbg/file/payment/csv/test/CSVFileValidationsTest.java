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
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.factory.CSVFilePaymentFactory;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.factory.CSVFilePaymentType;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.factory.CSVParserFactory;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.model.CSVCreditIndicatorRow;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.model.CSVDebitIndicatorSection;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.model.CSVFilePayment;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.model.CSVHeaderIndicatorSection;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.validation.CSVValidation;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.validation.CSVValidationService;
import com.forgerock.openbanking.exceptions.OBErrorException;
import com.forgerock.openbanking.model.error.ErrorCode;
import com.forgerock.openbanking.model.error.OBRIErrorType;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.org.openbanking.datamodel.error.OBStandardErrorCodes1;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@Ignore
public class CSVFileValidationsTest {
    final static String RESOURCES_PACK = "ext/lbg/file/payment/csv";
    CSVFilePayment file;

    @Before
    public void setup() {
        Exception error = catchThrowableOfType(
                () -> setFile(),
                Exception.class
        );
        assertThat(error).isNull();
        assertThat(file).isNotNull();
    }

    /**
     * Validation of header indicator value<br>
     * <li>Must be 'H'</li>
     * <li>CSVErrorException INVALID_HEADER_INDICATOR expected</li>
     */
    @Test
    public void generic_wrongHeaderIndicator() {
        file.getHeaderIndicatorSection().setHeaderIndicator("F");
        CSVErrorException errorException = catchThrowableOfType(
                () -> new CSVValidationService(file).validate(),
                CSVErrorException.class
        );
        assertThat(errorException.getCsvErrorType()).isEqualTo(CSVErrorType.INVALID_HEADER_INDICATOR);
        assertThat(errorException.getCsvErrorType().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorException.getOBError().getErrorCode()).isEqualTo(OBStandardErrorCodes1.UK_OBIE_FIELD_INVALID.getValue());
        assertThat(errorException.getMessage()).isEqualTo("The header indicator must be 'H' but found 'F'");
    }

    /**
     * Validation of header indicator value<br>
     * <li>Cannot be NULL</li>
     * <li>CSVErrorException INVALID_FORMAT expected</li>
     */
    @Test
    public void generic_nullHeaderIndicator() {
        file.getHeaderIndicatorSection().setHeaderIndicator(null);
        CSVErrorException errorException = catchThrowableOfType(
                () -> new CSVValidationService(file).validate(),
                CSVErrorException.class
        );
        assertThat(errorException.getCsvErrorType()).isEqualTo(CSVErrorType.INVALID_FORMAT);
        assertThat(errorException.getCsvErrorType().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorException.getOBError().getErrorCode()).isEqualTo(OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT.getValue());
        assertThat(errorException.getMessage()).isEqualTo("File format error, parse error. The 'Header Indicator value' is null.");
    }

    /**
     * Validation of debit indicator value<br>
     * <li>Must be 'D'</li>
     * <li>CSVErrorException INVALID_DEBIT_INDICATOR expected</li>
     */
    @Test
    public void generic_wrongDebitIndicator() {
        file.getDebitIndicatorSection().setDebitIndicator("F");
        CSVErrorException errorException = catchThrowableOfType(
                () -> new CSVValidationService(file).validate(),
                CSVErrorException.class
        );
        assertThat(errorException.getCsvErrorType()).isEqualTo(CSVErrorType.INVALID_DEBIT_INDICATOR);
        assertThat(errorException.getCsvErrorType().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorException.getOBError().getErrorCode()).isEqualTo(OBStandardErrorCodes1.UK_OBIE_FIELD_INVALID.getValue());
        assertThat(errorException.getMessage()).isEqualTo("The debit indicator must be 'D' but found 'F'");
    }

    /**
     * Validation of header indicator value<br>
     * <li>Cannot be NULL</li>
     * <li>CSVErrorException INVALID_FORMAT expected</li>
     */
    @Test
    public void generic_nullDebitIndicator() {
        file.getDebitIndicatorSection().setDebitIndicator(null);
        CSVErrorException errorException = catchThrowableOfType(
                () -> new CSVValidationService(file).validate(),
                CSVErrorException.class
        );
        assertThat(errorException.getCsvErrorType()).isEqualTo(CSVErrorType.INVALID_FORMAT);
        assertThat(errorException.getCsvErrorType().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorException.getOBError().getErrorCode()).isEqualTo(OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT.getValue());
        assertThat(errorException.getMessage()).isEqualTo("File format error, parse error. The 'Debit Indicator value' is null.");
    }

    /**
     * Validation of credit indicator row value<br>
     * <li>Must be 'C'</li>
     * <li>CSVErrorException INVALID_CREDIT_INDICATOR expected</li>
     */
    @Test
    public void generic_wrongCreditIndicator() {
        file.getCreditIndicatorRows().add(CSVCreditIndicatorRow.builder().creditIndicator("F").build());
        file.getHeaderIndicatorSection().setNumCredits(file.getCreditIndicatorRows().size());
        CSVErrorException errorException = catchThrowableOfType(
                () -> new CSVValidationService(file).validate(),
                CSVErrorException.class
        );
        assertThat(errorException.getCsvErrorType()).isEqualTo(CSVErrorType.INVALID_CREDIT_INDICATOR);
        assertThat(errorException.getCsvErrorType().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorException.getOBError().getErrorCode()).isEqualTo(OBStandardErrorCodes1.UK_OBIE_FIELD_INVALID.getValue());
        assertThat(errorException.getMessage()).isEqualTo("The credit indicator must be 'C' but found 1 credit rows with wrong credit indicator.");
    }

    /**
     * Validation of credit indicator row value<br>
     * <li>Cannot be NULL</li>
     * <li>CSVErrorException INVALID_FORMAT expected</li>
     */
    @Test
    public void generic_nullCreditIndicator() {
        file.getCreditIndicatorRows().add(CSVCreditIndicatorRow.builder().creditIndicator(null).build());
        file.getHeaderIndicatorSection().setNumCredits(file.getCreditIndicatorRows().size());
        CSVErrorException errorException = catchThrowableOfType(
                () -> new CSVValidationService(file).validate(),
                CSVErrorException.class
        );
        assertThat(errorException.getCsvErrorType()).isEqualTo(CSVErrorType.INVALID_FORMAT);
        assertThat(errorException.getCsvErrorType().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorException.getOBError().getErrorCode()).isEqualTo(OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT.getValue());
        assertThat(errorException.getMessage()).isEqualTo("File format error, parse error. The 'Credit Indicator row value' is null.");
    }

    /**
     * Validation payment type from file<br>
     * <li>Must be 'UK.LBG.O4B.BATCH.FPS' or 'UK.LBG.O4B.BULK.BACS'</li>
     * <li>CSVErrorException UNSUPPORTED_PAYMENT_TYPE expected</li>
     */
    @Test
    public void generic_badTypeFromFile() {
        CSVErrorException errorException = catchThrowableOfType(
                () -> CSVParserFactory.parse(CSVFilePaymentType.fromStringType("badtype"), "xxx").parse(),
                CSVErrorException.class
        );
        assertThat(errorException.getCsvErrorType()).isEqualTo(CSVErrorType.UNSUPPORTED_PAYMENT_TYPE);
        assertThat(errorException.getCsvErrorType().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorException.getOBError().getErrorCode()).isEqualTo(OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT.getValue());
        assertThat(errorException.getMessage()).isEqualTo("Unsupported csv payment file type: 'badtype', the supported payment csv types are 'UK.LBG.O4B.BATCH.FPS' 'UK.LBG.O4B.BULK.BACS' ");
    }

    /**
     * Validation payment media type<br>
     * <li>Must be 'UK.LBG.O4B.BATCH.FPS' or 'UK.LBG.O4B.BULK.BACS'</li>
     * <li>CSVErrorException REQUEST_MEDIA_TYPE_NOT_SUPPORTED expected</li>
     */
    @Test
    public void generic_badType() {
        OBErrorException errorException = catchThrowableOfType(
                () -> CSVParserFactory.parse(CSVFilePaymentType.UK_LBG_ONLY_TEST, "xxx").parse(),
                OBErrorException.class
        );
        assertThat(errorException.getObriErrorType()).isEqualTo(OBRIErrorType.REQUEST_MEDIA_TYPE_NOT_SUPPORTED);
        assertThat(errorException.getObriErrorType().getHttpStatus()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        assertThat(errorException.getOBError().getErrorCode()).isEqualTo(ErrorCode.OBRI_REQUEST_MEDIA_TYPE_NOT_SUPPORTED.getValue());
        assertThat(errorException.getMessage()).isEqualTo("Media type 'BAD_TYPE' is not supported for this request. Supported media type are 'UK.LBG.O4B.BATCH.FPS' 'UK.LBG.O4B.BULK.BACS' ");
    }

    /**
     * Validation format, no Header<br>
     * <li>Cannot be null</li>
     * <li>CSVErrorException INVALID_FORMAT expected</li>
     */
    @Test
    public void generic_invalidFormat() {
        file.setHeaderIndicator(null);
        CSVErrorException errorException = catchThrowableOfType(
                () -> new CSVValidationService(file).validate(),
                CSVErrorException.class
        );
        assertThat(errorException.getCsvErrorType()).isEqualTo(CSVErrorType.INVALID_FORMAT);
        assertThat(errorException.getCsvErrorType().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorException.getOBError().getErrorCode()).isEqualTo(OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT.getValue());
        assertThat(errorException.getMessage()).isEqualTo("File format error, parse error. The 'Header Indicator section' is null.");
    }

    /**
     * Validation number of credits rows not match with the credit numbers header entry<br>
     * <li>Must match num credits rows with credit num from header</li>
     * <li>CSVErrorException NUMBER_CREDITS_NOT_MATCH expected</li>
     */
    @Test
    public void generic_numCreditsRows_NotMatchWithHeader() {
        IntStream.range(0, 2).parallel().forEach(i ->
                file.getCreditIndicatorRows().add(CSVCreditIndicatorRow
                        .builder().creditIndicator("C").build())
        );
        CSVErrorException errorException = catchThrowableOfType(
                () -> new CSVValidationService(file).validate(),
                CSVErrorException.class
        );
        assertThat(errorException.getCsvErrorType()).isEqualTo(CSVErrorType.NUMBER_CREDITS_NOT_MATCH);
        assertThat(errorException.getCsvErrorType().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorException.getOBError().getErrorCode()).isEqualTo(OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT.getValue());
        assertThat(errorException.getMessage()).isEqualTo("File format error. The number of credits not match with the number of credit rows.");
    }

    /**
     * Validation number of credit rows<br>
     * <li>More than 25 credit rows not allowed</li>
     * <li>CSVErrorException NUMBER_CREDITS_ROWS_NOT_ALLOWED expected</li>
     */
    @Test
    public void generic_numCreditsRows_NotAllowed() {
        IntStream.range(0, 30).parallel().forEach(i ->
                file.getCreditIndicatorRows().add(CSVCreditIndicatorRow.builder()
                        .creditIndicator(CSVCreditIndicatorRow.CREDIT_IND_EXPECTED)
                        .recipientName("Beneficiary name")
                        .accNumber("12345678")
                        .recipientSortCode("301763")
                        .reference("Beneficiary ref.")
                        .debitAmount(new BigDecimal(10.10).setScale(2, RoundingMode.CEILING))
                        .paymentASAP(CSVValidation.PAYMENT_ASAP_VALUES[0])
                        .paymentDate("")
                        .eToEReference("EtoEReference")
                        .build())
        );
        file.getHeaderIndicatorSection().setValueCreditsSum(file.getCreditRowsTotalDebitAmount());
        file.getHeaderIndicatorSection().setNumCredits(file.getCreditIndicatorRows().size());
        CSVErrorException errorException = catchThrowableOfType(
                () -> new CSVValidationService(file).validate(),
                CSVErrorException.class
        );
        assertThat(errorException.getCsvErrorType()).isEqualTo(CSVErrorType.NUMBER_CREDITS_ROWS_NOT_ALLOWED);
        assertThat(errorException.getCsvErrorType().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorException.getOBError().getErrorCode()).isEqualTo(OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT.getValue());
        assertThat(errorException.getMessage()).isEqualTo(String.format("The number of credit rows exceeds the %d allowed rows. Current rows = %d", CSVValidation.CREDIT_ROWS_ALLOWED, file.getHeaderIndicatorSection().getNumCredits()));
    }

    /**
     * Validation credit rows: debit amount sum against header value entry<br>
     * <li>Rule: The sum of debit amount of all credit rows must match with the header value entry</li>
     * <li>no errors expected</li>
     */
    @Test
    public void generic_sumCreditsRows_MatchWithHeaderValue() {
        CSVErrorException errorException = catchThrowableOfType(
                () -> new CSVValidationService(file).validate(),
                CSVErrorException.class
        );
        assertThat(errorException).isNull();
    }

    /**
     * Validation credit rows: debit amount sum against header value entry<br>
     * <li>Rule: The sum of debit amount of all credit rows must match with the header value entry</li>
     * <li>CSVErrorException INVALID_CREDIT_AMOUNT expected</li>
     */
    @Test
    public void generic_sumCreditsRows_NOT_MatchWithHeaderValue() {
        IntStream.range(0, 10).parallel().forEach(i ->
                file.getCreditIndicatorRows().add(CSVCreditIndicatorRow.builder()
                        .creditIndicator(CSVCreditIndicatorRow.CREDIT_IND_EXPECTED)
                        .recipientName("Beneficiary name")
                        .accNumber("12345678")
                        .recipientSortCode("301763")
                        .reference("Beneficiary ref.")
                        .debitAmount(new BigDecimal(10.10).setScale(2, RoundingMode.CEILING))
                        .paymentASAP(CSVValidation.PAYMENT_ASAP_VALUES[0])
                        .paymentDate("")
                        .eToEReference("EtoEReference")
                        .build())
        );
        file.getHeaderIndicatorSection().setNumCredits(file.getCreditIndicatorRows().size());
        CSVErrorException errorException = catchThrowableOfType(
                () -> new CSVValidationService(file).validate(),
                CSVErrorException.class
        );
        assertThat(errorException.getCsvErrorType()).isEqualTo(CSVErrorType.INVALID_CREDIT_AMOUNT);
        assertThat(errorException.getCsvErrorType().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorException.getOBError().getErrorCode()).isEqualTo(OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT.getValue());
        assertThat(errorException.getMessage()).isEqualTo(String.format("The credit amount value %s not match with the total sum amount of credit rows %s", file.getHeaderIndicatorSection().getValueCreditsSum().toPlainString(), file.getCreditRowsTotalDebitAmount()));
    }

    /**
     * Validation Date format: file creation date<br>
     * <li>Rule: All dates in the file must match with the pattern 'yyyyMMdd'</li>
     * <li>CSVErrorException INVALID_DATE_FORMAT expected</li>
     */
    @Test
    public void generic_headerIndicator_dateFormat_fails() {
        file.getHeaderIndicatorSection().setFileCreationDate("02022020");
        CSVErrorException errorException = catchThrowableOfType(
                () -> new CSVValidationService(file).validate(),
                CSVErrorException.class
        );
        assertThat(errorException.getCsvErrorType()).isEqualTo(CSVErrorType.INVALID_DATE_FORMAT);
        assertThat(errorException.getCsvErrorType().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorException.getOBError().getErrorCode()).isEqualTo(OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT.getValue());
        assertThat(errorException.getMessage()).isEqualTo(String.format("Date format error, parse error. The date '02022020' not match with the date format '%s' expected.", CSVValidation.DATE_FORMAT));
    }

    /**
     * Validation credit rows: reference<br>
     * <li>Rule: The reference cannot contains the string 'CONTRA'</li>
     * <li>Rule: The word 'CONTRA' not is a part of another string</li>
     * <li>CSVErrorException INVALID_REFERENCE_FORMAT expected</li>
     */
    @Test
    public void generic_creditsRowReference() {
        file.getCreditIndicatorRows().get(0).setReference("contains the word contra. Reject!");
        assertThat(file).isNotNull();
        CSVErrorException errorException = catchThrowableOfType(
                () -> new CSVValidationService(file).validate(),
                CSVErrorException.class
        );
        assertThat(errorException.getCsvErrorType()).isEqualTo(CSVErrorType.INVALID_REFERENCE_FORMAT);
        assertThat(errorException.getCsvErrorType().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorException.getOBError().getErrorCode()).isEqualTo(OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT.getValue());
        assertThat(errorException.getMessage()).isEqualTo(String.format("Reference format error, parse error. Has been Found %d references that contains the word '%s'.", 1, CSVValidation.REF_WORD_TO_FIND));
    }

    /**
     * Get the file content like a string
     *
     * @param filePath
     * @return String file content
     * @throws IOException
     */
    @Ignore
    static final String getContent(String filePath) throws IOException {
        return Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
    }

    /**
     * Create a instance of CSVFilePayment
     * or reset the fields values to reuse it on every test
     *
     * @throws OBErrorException
     */
    @Ignore
    private void setFile() throws OBErrorException {
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
    }
}
