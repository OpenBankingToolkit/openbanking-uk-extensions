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
package com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.exception;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.forgerock.openbanking.serialiser.OBRIErrorTypeDeserializer;
import com.forgerock.openbanking.serialiser.OBRIErrorTypeSerializer;
import org.springframework.http.HttpStatus;
import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.error.OBStandardErrorCodes1;
import uk.org.openbanking.datamodel.error.StandardErrorCode;

@JsonDeserialize(
        using = OBRIErrorTypeDeserializer.class
)
@JsonSerialize(
        using = OBRIErrorTypeSerializer.class
)
public enum CSVErrorType {
    INVALID_HEADER_INDICATOR(HttpStatus.BAD_REQUEST, OBStandardErrorCodes1.UK_OBIE_FIELD_INVALID, "The header indicator must be '%s' but found '%s'"),
    INVALID_DEBIT_INDICATOR(HttpStatus.BAD_REQUEST, OBStandardErrorCodes1.UK_OBIE_FIELD_INVALID, "The debit indicator must be '%s' but found '%s'"),
    INVALID_CREDIT_INDICATOR(HttpStatus.BAD_REQUEST, OBStandardErrorCodes1.UK_OBIE_FIELD_INVALID, "The credit indicator must be '%s' but found %d credit rows with wrong credit indicator."),
    NUMBER_CREDITS_NOT_MATCH(HttpStatus.BAD_REQUEST, OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT, "File format error. The number of credits not match with the number of credit rows."),
    NUMBER_CREDITS_ROWS_NOT_ALLOWED(HttpStatus.BAD_REQUEST, OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT, "The number of credit rows exceeds the %d allowed rows. Current rows = %d"),
    INVALID_CREDIT_AMOUNT(HttpStatus.BAD_REQUEST, OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT, "The credit amount value %s not match with the total sum amount of credit rows %s"),
    INVALID_FORMAT(HttpStatus.BAD_REQUEST, OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT, "File format error, parse error. %s"),
    INVALID_DATE_FORMAT(HttpStatus.BAD_REQUEST, OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT, "Date format error, parse error. The date '%s' not match with the date format '%s' expected."),
    INVALID_PAYMENT_DATE(HttpStatus.BAD_REQUEST, OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT, "Date format error, parse error. The date '%s' cannot be in the past."),
    INVALID_PAYMENT_DATE_LATER_DAYS(HttpStatus.BAD_REQUEST, OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT, "Date format error, parse error. The date '%s' must be at last %d days later from processing day."),
    INVALID_PAYMENT_ASAP_FORMAT(HttpStatus.BAD_REQUEST, OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT, "File format error, parse error. The payment ASAP will be '%s'"),
    INVALID_PAYMENT_ASAP_EMPTY(HttpStatus.BAD_REQUEST, OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT, "File format error, parse error. If the payment ASAP value is '%s' it cannot have a payment date entry."),
    INVALID_PAYMENT_ASAP_ENTRY(HttpStatus.BAD_REQUEST, OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT, "File format error, parse error. If the payment ASAP value is '%s' it must have a payment date entry."),
    INVALID_CREDIT_PAYMENT(HttpStatus.BAD_REQUEST, OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT, "File format error, parse error. The payment ASAP and payment date cannot both be empty."),
    INVALID_PAYMENT_BEYOND_DATE(HttpStatus.BAD_REQUEST, OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT, "Date format error, parse error. The payment date '%s' cannot be beyond %d days from processing day."),
    INVALID_REFERENCE_FORMAT(HttpStatus.BAD_REQUEST, OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT, "Reference format error, parse error. Has been Found %d references that contains the word '%s'."),
    UNSUPPORTED_PAYMENT_TYPE(HttpStatus.BAD_REQUEST, OBStandardErrorCodes1.UK_OBIE_RESOURCE_INVALID_FORMAT, "Unsupported csv payment file type: '%s', the supported payment csv types are %s");

    private HttpStatus httpStatus;
    private StandardErrorCode code;
    private String message;

    CSVErrorType(HttpStatus httpStatus, StandardErrorCode code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }

    public StandardErrorCode getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

    public String getLogMessage() {
        return this.message.replace("%s", "{}").replace("%d", "{}");
    }

    public OBError1 toOBError1(Object... args) {
        return (new OBError1()).errorCode(this.getCode().getValue()).message(String.format(this.getMessage(), args));
    }
}
