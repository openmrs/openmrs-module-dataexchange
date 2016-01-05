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
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.openmrs.api.db.hibernate.DbSessionFactory;  
import org.openmrs.util.OpenmrsClassLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.jdbc.Work;
import java.sql.Connection;
import java.sql.SQLException;

@Service
public class DataImporter {

	class ImportDataWork implements Work {
		String filePath;
		
		public ImportDataWork(String filePath)
		{
			this.filePath = filePath;
		}
		
		public void execute(Connection jdbcConnection) throws SQLException {
			
			InputStream in = OpenmrsClassLoader.getInstance().getResourceAsStream(filePath);
			try {
				DatabaseConnection connection = new DatabaseConnection(jdbcConnection);
				
				FlatXmlDataSet dataSet;
				
				if (in != null) {
					dataSet = new FlatXmlDataSetBuilder().setColumnSensing(true).build(new InputStreamReader(in, "UTF-8"));
				} else {
					dataSet = new FlatXmlDataSetBuilder().setColumnSensing(true).build(new File(filePath));
				}
				
				ReplacementDataSet replacementDataSet = new ReplacementDataSet(dataSet);
				replacementDataSet.addReplacementObject("[NULL]", null);
				
				DatabaseOperation.REFRESH.execute(connection, replacementDataSet);
				
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				IOUtils.closeQuietly(in);
			}
		}
	}
	
	@Autowired
	DbSessionFactory sessionFactory;
	
	@Transactional
	public void importData(String filePath) {
		sessionFactory.getCurrentSession().doWork(new ImportDataWork(filePath));
	}
}
