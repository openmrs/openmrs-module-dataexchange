package org.openmrs.module.dataexchange;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataImporter {

	@Autowired
	SessionFactory sessionFactory;
	
	@Transactional
	public void importData(String filePath) {
		DatabaseConnection connection = getConnection();
		
		InputStream in = getClass().getClassLoader().getResourceAsStream(filePath);
		try {
			FlatXmlDataSet dataset;
			
			if (in != null) {
				dataset = new FlatXmlDataSetBuilder().build(new InputStreamReader(in, "UTF-8"));
			} else {
				dataset = new FlatXmlDataSetBuilder().build(new File(filePath));
			}
		
			DatabaseOperation.REFRESH.execute(connection, dataset);
			
			if (in != null) {
				in.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	@SuppressWarnings("deprecation")
	private DatabaseConnection getConnection() {
		try {
			return new DatabaseConnection(sessionFactory.getCurrentSession().connection());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
