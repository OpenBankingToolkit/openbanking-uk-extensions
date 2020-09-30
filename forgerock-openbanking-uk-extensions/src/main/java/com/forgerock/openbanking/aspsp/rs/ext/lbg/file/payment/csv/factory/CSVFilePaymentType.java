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
package com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.factory;

import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.exception.CSVErrorException;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.exception.CSVErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;

import java.util.Arrays;

@Slf4j
public enum CSVFilePaymentType {
    UK_LBG_FPS_BATCH_V10("UK.LBG.O4B.BATCH.FPS"),
    UK_LBG_BACS_BULK_V10("UK.LBG.O4B.BULK.BACS"),
    UK_LBG_ONLY_TEST("BAD_TYPE");

    private final String fileType;

    CSVFilePaymentType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileType() {
        return fileType;
    }

    public String getContentType() {
        return MediaType.TEXT_PLAIN_VALUE;
    }

    public boolean isSupported(String fileType) {
        return Arrays.stream(CSVFilePaymentType.values())
                .anyMatch(e -> e.fileType.equals(fileType));
    }

    public static CSVFilePaymentType fromStringType(String value) {
        for (CSVFilePaymentType csvFilePaymentType : CSVFilePaymentType.values()) {
            if (csvFilePaymentType.fileType.equals(value)) {
                return csvFilePaymentType;
            }
        }
        log.error(CSVErrorType.UNSUPPORTED_PAYMENT_TYPE.getLogMessage(), value, getSupportedTypes());
        throw new CSVErrorException(CSVErrorType.UNSUPPORTED_PAYMENT_TYPE, value, getSupportedTypes());
    }

    public static String getSupportedTypes() {
        return new StringBuilder()
                .append("'")
                .append(UK_LBG_FPS_BATCH_V10.getFileType()).append("' ")
                .append("'")
                .append(UK_LBG_BACS_BULK_V10.getFileType()).append("' ").toString();
    }
}
