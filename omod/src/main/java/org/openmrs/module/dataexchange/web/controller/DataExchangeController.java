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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.dataexchange.DataExporter;
import org.openmrs.module.dataexchange.DataImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

/**
 * The main controller.
 */
@Controller
@RequestMapping(value = "/module/dataexchange")
public class  DataExchangeController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	@Autowired
	DataExporter dataExporter;
	
	@Autowired
	DataImporter dataImporter;
	
	@Autowired
	MetadataSharingParser metadataSharingParser;
	
	@RequestMapping(value = "/export", method = RequestMethod.GET)
	public void export() {
	}
	
	@RequestMapping(value = "/exportPackageContent", method = RequestMethod.POST)
	public ResponseEntity<String> exportPackageContent(@RequestParam("file") MultipartFile file) throws IOException, ParserConfigurationException, SAXException {
		Set<Integer> conceptIds = metadataSharingParser.parseConceptIds(file.getInputStream());
		
		return exportConcepts(conceptIds);
	}
	
	@RequestMapping(value = "/export", method = RequestMethod.POST)
	public ResponseEntity<String> exportConcepts(@RequestParam("conceptIds") String conceptIds) throws IOException {
		
		String[] splitConceptIds = conceptIds.split("\\s+");
		Set<Integer> ids = new HashSet<Integer>();
		
		for (String conceptId : splitConceptIds) {
			if (!StringUtils.isBlank(conceptId)) {
				ids.add(Integer.valueOf(conceptId));
			}
		}
		
		return exportConcepts(ids);
	}

	private ResponseEntity<String> exportConcepts(Set<Integer> ids) throws IOException, FileNotFoundException {
	    File tempFile = null;
		FileInputStream tempIN = null;
		String xml;
		try {
			tempFile = File.createTempFile("concepts", ".xml");
			
			if (ids.isEmpty()) {
				dataExporter.exportAllConcepts(tempFile.getPath());
			} else {
				dataExporter.exportConcepts(tempFile.getPath(), ids);
			}
			
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
	
	@RequestMapping(value = "/import", method = RequestMethod.GET)
	public void importData() {
	}
	
	@RequestMapping(value = "/import", method = RequestMethod.POST)
	public void importData(@RequestParam("file") MultipartFile file, Model model) throws IOException {
		Writer writer = null;
		File tempFile = null;
		try {
			tempFile = File.createTempFile("concepts", ".xml");
			writer = new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8");
			IOUtils.copy(file.getInputStream(), writer, "UTF-8");
			writer.close();
			
			dataImporter.importData(tempFile.getPath());
		} finally {
			IOUtils.closeQuietly(writer);
			if (tempFile != null) {
				tempFile.delete();
			}
		}
		
		model.addAttribute("success", true);
	}
}
