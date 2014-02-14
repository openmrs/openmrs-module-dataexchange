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
package org.openmrs.module.dataexchange.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.dataexchange.DataExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The main controller.
 */
@Controller
@RequestMapping(value = "/module/dataexchange/manage")
public class  DataExchangeManageController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	@Autowired
	DataExporter dataExporter;
	
	@RequestMapping(method = RequestMethod.GET)
	public void manage(ModelMap model) {
		model.addAttribute("user", Context.getAuthenticatedUser());
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<String> exportConcepts(@RequestParam("conceptIds") String conceptIds) throws IOException {
		
		String[] splitConceptIds = conceptIds.split(",");
		Set<Integer> ids = new HashSet<Integer>();
		
		for (String conceptId : splitConceptIds) {
			ids.add(Integer.valueOf(conceptId));
		}
		
		File tempFile = null;
		FileInputStream tempIN = null;
		String xml;
		try {
			tempFile = File.createTempFile("concepts", ".xml");
			
			dataExporter.exportConcepts(tempFile.getPath(), ids);
			
			tempIN = new FileInputStream(tempFile);
			
			xml = IOUtils.toString(tempIN, "UTF-8");
			
			tempIN.close();
		} finally {
			IOUtils.closeQuietly(tempIN);
			
			if (tempFile != null) {
				tempFile.delete();
			}
		}
		
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(org.springframework.http.MediaType.APPLICATION_XML);
		return new ResponseEntity<String>(xml, responseHeaders, HttpStatus.CREATED);
	}
}
