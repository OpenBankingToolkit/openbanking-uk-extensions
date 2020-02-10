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
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.factory.CSVFilePaymentType;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.model.CSVCreditIndicatorRow;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.model.CSVDebitIndicatorSection;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.model.CSVFilePayment;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.model.CSVHeaderIndicatorSection;
import com.forgerock.openbanking.exceptions.OBErrorException;
import org.apache.commons.csv.CSVRecord;

public interface CSVParser {

    CSVFilePayment getCsvFilePayment();

    CSVFilePaymentType getCsvFilePaymentType();

    default CSVParser parse() throws OBErrorException {
        return this;
    }

    void setHeaderIndicatorSection(final CSVRecord record) throws CSVErrorException;

    void setHeaderIndicatorSection(final CSVHeaderIndicatorSection section);

    void setDebitIndicatorSection(final CSVRecord record) throws CSVErrorException;

    void setDebitIndicatorSection(final CSVDebitIndicatorSection section);

    void setCreditIndicatorRow(final CSVRecord record) throws CSVErrorException;

    void setCreditIndicatorRow(final CSVCreditIndicatorRow row);

    default String getValue(final String value) {
        return (value.isEmpty() | value.isBlank()) ? null : value;
    }
}
