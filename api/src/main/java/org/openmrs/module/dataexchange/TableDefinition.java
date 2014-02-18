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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class TableDefinition {
	
	private final String tableName;
	
	private final Map<String, TableDefinition> foreignKeys = new HashMap<String, TableDefinition>();
	
	private final Set<String> primaryKeys = new HashSet<String>();
	
	private final Set<Reference> references = new HashSet<Reference>();
	
	private final Set<String> excludedColumns = new HashSet<String>();
	
	private final Set<Integer> fetchedIds = new HashSet<Integer>();
	
	private TableDefinition(String tableName) {
		this.tableName = tableName;
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public Map<String, TableDefinition> getForeignKeys() {
		return Collections.unmodifiableMap(foreignKeys);
	}
	
	public Set<String> getPrimaryKeys() {
		return Collections.unmodifiableSet(primaryKeys);
	}
	
	public String getPrimaryKey() {
		if (primaryKeys.isEmpty()) {
			throw new IllegalStateException("No primary key defined");
		} else if (primaryKeys.size() > 1) {
			throw new UnsupportedOperationException("Complex primary key is not supported");
		}
		
		return primaryKeys.iterator().next();
	}
	
	public Set<Reference> getReferences() {
		return Collections.unmodifiableSet(references);
	}
	
	public Set<Integer> getFetchedIds() {
		return Collections.unmodifiableSet(fetchedIds);
	}
	
	public boolean addFetchedId(Integer id) {
		return fetchedIds.add(id);
	}
	
	public Set<String> getExcludedColumns() {
		return Collections.unmodifiableSet(excludedColumns);
	}
	
	@Override
	public int hashCode() {
		return tableName.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof TableDefinition) {
			return tableName.equals(((TableDefinition) obj).tableName);
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return tableName;
	}
	
	public static class Reference {
		
		private final String foreingKey;
		
		private final TableDefinition table;
		
		public Reference(String foreignKey, TableDefinition table) {
			this.foreingKey = foreignKey;
			this.table = table;
		}

		public String getForeingKey() {
			return foreingKey;
		}

		public TableDefinition getTable() {
			return table;
		}
		
		@Override
		public int hashCode() {
			return new HashCodeBuilder().append(foreingKey).append(table).toHashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			} else if (obj instanceof Reference) {
				Reference other = (Reference) obj;
				return new EqualsBuilder().append(foreingKey, other.foreingKey)
						.append(table, other.table).isEquals();
			}
			return super.equals(obj);
		}
	}
	
	public static class Builder {
		
		private final TableDefinition table;
		
		public Builder(String tableName) {
			table = new TableDefinition(tableName);
		}
		
		public Builder addFK(String column, TableDefinition table) {
			this.table.foreignKeys.put(column, table);
			
			return this;
		}
		
		public Builder addReferenceAndFK(String column, TableDefinition table) {
			this.table.foreignKeys.put(column, table);
			table.references.add(new Reference(column, this.table));
			
			return this;
		}
		
		public Builder addPK(String column) {
			table.primaryKeys.add(column);
			
			return this;
		}
		
		public Builder excludeColumn(String column) {
			table.excludedColumns.add(column);
			
			return this;
		}
		
		public TableDefinition build() {
			if (table.primaryKeys.isEmpty()) {
				table.primaryKeys.add(table.tableName + "_id");
			}
			
			return table;
		}
	}
}
