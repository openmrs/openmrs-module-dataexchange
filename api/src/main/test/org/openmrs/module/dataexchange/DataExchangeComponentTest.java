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

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class DataExchangeComponentTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	DataExporter dataExporter;
	
	@Autowired
	DataImporter dataImporter;
	
	@Test
	public void shouldExportAndImportConceptsWithRelatedMetadata() throws Exception {
		File conceptsFile = null;
		try {
			conceptsFile = File.createTempFile("concepts", ".xml");
			
			dataExporter.exportConcepts(conceptsFile.getPath(), new HashSet<Integer>(Arrays.asList(23, 5089)));
			
			dataImporter.importData(conceptsFile.getPath());
		} finally {
			if (conceptsFile != null) {
				conceptsFile.delete();
			}
		}
	}

}
