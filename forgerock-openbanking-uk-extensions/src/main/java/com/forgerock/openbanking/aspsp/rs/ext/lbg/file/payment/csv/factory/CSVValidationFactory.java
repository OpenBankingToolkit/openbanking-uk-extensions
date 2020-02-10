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

import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.model.CSVFilePayment;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.validation.CSVBatchFPSFileValidationService;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.validation.CSVBulkBACSFileValidationService;
import com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment.csv.validation.CSVValidation;
import com.forgerock.openbanking.exceptions.OBErrorException;
import com.forgerock.openbanking.model.error.OBRIErrorType;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CSVValidationFactory {

    public static CSVValidation getValidationServiceInstance(CSVFilePayment filePayment) throws OBErrorException {
        Preconditions.checkNotNull(filePayment, "Cannot have a null content file");
        switch (filePayment.getFilePaymentType()) {
            case UK_LBG_FPS_BATCH_V10:
                log.trace("Validation service for '{}' ", CSVFilePaymentType.UK_LBG_FPS_BATCH_V10.getFileType());
                return new CSVBatchFPSFileValidationService(filePayment);
            case UK_LBG_BACS_BULK_V10:
                log.trace("Validation service for '{}' ", CSVFilePaymentType.UK_LBG_BACS_BULK_V10.getFileType());
                return new CSVBulkBACSFileValidationService(filePayment);
            default:
                log.error(String.format(OBRIErrorType.REQUEST_MEDIA_TYPE_NOT_SUPPORTED.getMessage(), filePayment.getFilePaymentType(), CSVFilePaymentType.getSupportedTypes()));
                throw new OBErrorException(OBRIErrorType.REQUEST_MEDIA_TYPE_NOT_SUPPORTED, filePayment.getFilePaymentType(), CSVFilePaymentType.getSupportedTypes());
        }
    }
}
