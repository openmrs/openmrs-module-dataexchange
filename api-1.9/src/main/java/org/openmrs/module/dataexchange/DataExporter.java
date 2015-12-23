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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.hibernate.jdbc.Work;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.dataexchange.TableDefinition.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataExporter {
	
	@Autowired
	DbSessionFactory sessionFactory;

	@Transactional
	public void exportConcepts(final String filePath, final Set<Integer> ids) throws IOException {
		
		sessionFactory.getCurrentSession().doWork(new Work() {
			public void execute(Connection connection) {
				try {
					exportConcepts(filePath, ids, connection);
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}
		});
	}
	
	
	public void exportConcepts(String filePath, Set<Integer> ids, Connection con) throws IOException {
		DatabaseConnection connection = getConnection(con);
		
		TableDefinition conceptTableDefinition = buildConceptTableDefinition();
		
		List<ITable> tables = new ArrayList<ITable>();
		
		try {
			addTable(connection, tables, conceptTableDefinition, conceptTableDefinition.getPrimaryKey(), ids);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		OutputStreamWriter writer = null;
		try {
			writer = new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8");
			
			FlatXmlDataSet.write(new CompositeDataSet(tables.toArray(new ITable[tables.size()])), writer);
			
			writer.close();
		} catch (DataSetException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}
	
	@Transactional
	public void exportAllConcepts(final String filePath) throws IOException {
		sessionFactory.getCurrentSession().doWork(new Work() {
			public void execute(Connection connection) {
				try {
					exportAllConcepts(filePath, connection);
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}
		});
	}
	
	public void exportAllConcepts(String filePath, Connection con) throws IOException {
		DatabaseConnection connection = getConnection(con);
		
		Set<Integer> conceptIds = new LinkedHashSet<Integer>();
		try {
	        PreparedStatement selectQuery = connection.getConnection().prepareStatement("select concept_id from concept");
	        ResultSet resultSet = selectQuery.executeQuery();
	        while (resultSet.next()) {
	            int id = resultSet.getInt(1);
	            conceptIds.add(id);
            }
        }
        catch (SQLException e) {
	        throw new RuntimeException(e);
        }
		
		exportConcepts(filePath, conceptIds);
	}
	
	@SuppressWarnings("deprecation")
	private DatabaseConnection getConnection(Connection con) {
		try {
			return new DatabaseConnection(con);
		} catch (DatabaseUnitException e) {
			throw new RuntimeException(e);
		}
	}

	private TableDefinition buildConceptTableDefinition() {
		TableDefinition conceptDatatype = new TableDefinition.Builder("concept_datatype").build();
		TableDefinition conceptClass = new TableDefinition.Builder("concept_class").build();
		TableDefinition concept = new TableDefinition.Builder("concept").addFK("datatype_id", conceptDatatype)
				.addFK("class_id", conceptClass).excludeColumn("short_name").excludeColumn("description").build();
		
		new TableDefinition.Builder("concept_numeric").addPK("concept_id")
				.addReferenceAndFK("concept_id", concept).build();
		
		new TableDefinition.Builder("concept_complex").addPK("concept_id")
				.addReferenceAndFK("concept_id", concept);
		
		new TableDefinition.Builder("concept_name").addReferenceAndFK("concept_id", concept).build();
		new TableDefinition.Builder("concept_description").addReferenceAndFK("concept_id", concept).build();
		
		new TableDefinition.Builder("concept_set").addFK("concept_id", concept)
				.addReferenceAndFK("concept_set", concept).build();
		
		TableDefinition drug = new TableDefinition.Builder("drug").addReferenceAndFK("concept_id", concept).addFK("dosage_form", concept)
				.addFK("route", concept).build();
		
		new TableDefinition.Builder("concept_answer").addReferenceAndFK("concept_id", concept)
				.addFK("answer_concept", concept).addFK("answer_drug", drug).build();
		
		TableDefinition conceptSource = new TableDefinition.Builder("concept_reference_source").addPK("concept_source_id").build();
		
		TableDefinition conceptReferenceTerm = new TableDefinition.Builder("concept_reference_term")
				.addFK("concept_source_id", conceptSource).build();
		
		TableDefinition conceptMapType = new TableDefinition.Builder("concept_map_type").build();
		
		new TableDefinition.Builder("concept_reference_term_map")
			.addReferenceAndFK("term_a_id", conceptReferenceTerm).addReferenceAndFK("term_b_id", conceptReferenceTerm)
			.addFK("a_is_to_b_id", conceptMapType).build();
		
		new TableDefinition.Builder("concept_reference_map").addPK("concept_map_id")
				.addFK("concept_map_type_id", conceptMapType).addReferenceAndFK("concept_id", concept)
				.addFK("concept_reference_term_id", conceptReferenceTerm).build();
		return concept;
	}

	private void addTable(DatabaseConnection connection,
			List<ITable> tables, TableDefinition tableDefinition, String key, Set<Integer> ids) throws SQLException, DataSetException {
		ids.remove(null);
		
		if (ids.isEmpty()) {
			return;
		}
		
		StringBuilder select = new StringBuilder("select * from ").append(tableDefinition.getTableName()).append(" where ").append(key).append(" in (?");
		for (int i = 1; i < ids.size(); i++) {
			select.append(", ?");
		}
		select.append(")");
		
		PreparedStatement selectQuery = connection.getConnection().prepareStatement(select.toString());
		
		int index = 1;
		for (Integer id: ids) {
			selectQuery.setInt(index, id);
			index++;
		}
		
		ITable resultTable = connection.createTable(tableDefinition.getTableName(), selectQuery);
		
		if (resultTable.getRowCount() == 0) {
			return;
		}

		Set<Integer> notFetchedIds = new HashSet<Integer>();
		
		for (int i = 0; i < resultTable.getRowCount(); i++) {
			Integer id = (Integer) resultTable.getValue(i, tableDefinition.getPrimaryKey());
			if (tableDefinition.addFetchedId(id)) {
				notFetchedIds.add(id);
			}
		}
		
		for (Entry<String, TableDefinition> fk : tableDefinition.getForeignKeys().entrySet()) {
			Set<Integer> notFetchedForeignIds = new HashSet<Integer>();
			
			for (int i = 0; i < resultTable.getRowCount(); i++) {
				Integer id = (Integer) resultTable.getValue(i, tableDefinition.getPrimaryKey());
				if (!notFetchedIds.contains(id)) {
					continue;
				}
				
				Integer fkValue = (Integer) resultTable.getValue(i, fk.getKey());
				if (!fk.getValue().getFetchedIds().contains(fkValue)) {
					notFetchedForeignIds.add(fkValue);
				}
			}
			
			if (!notFetchedForeignIds.isEmpty()) {
				addTable(connection, tables, fk.getValue(), fk.getValue().getPrimaryKey(), notFetchedForeignIds);
			}
		}
		
		ColumnReplacementTable replacementTable = new ColumnReplacementTable(resultTable);
		replacementTable.addReplacement("creator", 1);
		replacementTable.addReplacement("retired_by", 1);
		replacementTable.addReplacement("changed_by", 1);
		
		resultTable = replacementTable;
		
		if (!tableDefinition.getExcludedColumns().isEmpty()) {
			resultTable = DefaultColumnFilter.excludedColumnsTable(resultTable, 
					tableDefinition.getExcludedColumns().toArray(new String[0]));
		}
		
		tables.add(resultTable);
		
		if (!notFetchedIds.isEmpty()) {
			for (Reference reference : tableDefinition.getReferences()) {
				addTable(connection, tables, reference.getTable(), reference.getForeingKey(), notFetchedIds);
			}
		}
	}
}
