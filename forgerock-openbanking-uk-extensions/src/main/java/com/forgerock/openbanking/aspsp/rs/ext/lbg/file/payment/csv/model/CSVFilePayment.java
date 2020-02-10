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
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.validation.CSVValidationService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * CSV file payment interface model
 */
public interface CSVFilePayment {

    void setHeaderIndicator(CSVHeaderIndicatorSection headerIndicator);

    void setDebitIndicator(CSVDebitIndicatorSection debitIndicator);

    void setCreditIndicatorRows(List<CSVCreditIndicatorRow> creditIndicatorRows);

    CSVHeaderIndicatorSection getHeaderIndicatorSection();

    CSVDebitIndicatorSection getDebitIndicatorSection();

    List<CSVCreditIndicatorRow> getCreditIndicatorRows();

    CSVFilePaymentType getFilePaymentType();

    default BigDecimal getCreditRowsTotalDebitAmount() {
        return new BigDecimal(0.00).setScale(2, RoundingMode.CEILING);
    }

    default DateTimeFormatter getDateTimeFormatter() {
        return DateTimeFormatter.ofPattern(CSVValidationService.DATE_FORMAT);
    }

}