/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.dataexchange;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.Set;

public class MetadataSharingParserTest {

    MetadataSharingParser parser = new MetadataSharingParser();

    @Test
    public void shouldExtractConceptIdsFromItens() throws Exception {
        InputStream in = MetadataSharingParserTest.class.getResourceAsStream("/Reference_Application_Concepts-23.zip");
        Set<Integer> ids = parser.parseConceptIds(in);

        Assert.assertThat(ids, Matchers.hasItems(159947, 162552, 5242));
    }

    @Test
    public void shouldExtractConceptIdsFromRelatedItems() throws Exception {
        InputStream in = MetadataSharingParserTest.class.getResourceAsStream("/Reference_Application_Diagnoses-11.zip");
        Set<Integer> ids = parser.parseConceptIds(in);

        Assert.assertThat(ids, Matchers.hasItems(160167, 160168));
    }
}
