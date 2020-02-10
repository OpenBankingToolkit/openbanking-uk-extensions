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

public interface CSVValidation {
    int CREDIT_ROWS_ALLOWED = 25;
    String DATE_FORMAT = "yyyyMMdd";
    String REF_WORD_TO_FIND = "CONTRA";
    String REF_PATTERN = "^*(?<![a-z-A-Z])" + REF_WORD_TO_FIND + "(?![a-z-A-Z])^*";
    long BEYOND_PAYMENT_DAYS = 31;
    long PAYMENT_LATER_DAYS = 2;
    String EMPTY_LIKE_NULL = "";
    String PAYMENT_ASAP_VALUES[] = {"Y", "N", EMPTY_LIKE_NULL};

    void validate() throws CSVErrorException;
}
