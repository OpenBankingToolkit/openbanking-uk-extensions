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

import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;

@Data
@Builder
public class CSVHeaderIndicatorSection {
    public final static String HEADER_IND_EXPECTED = "H";
    private String headerIndicator;
    private String fileCreationDate;
    private String uniqueId;
    private int numCredits;
    private BigDecimal valueCreditsSum;


    protected final String toCsvString() {

        StringBuilder result = new StringBuilder();

        //determine fields declared in this class only (no fields of superclass)
        Field[] fields = this.getClass().getDeclaredFields();

        //print field names paired with their values
        for (Field field : fields) {
            try {
                if (Modifier.isPrivate(field.getModifiers()) && field.get(this) != null) {
                    //requires access to private field:
                    result.append(field.get(this)).append(",");
                }
            } catch (IllegalAccessException ex) {
                System.out.println(ex);
            }
        }

        return result.toString().substring(0, result.length() - 1);
    }
}
