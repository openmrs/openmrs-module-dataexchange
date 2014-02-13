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

public class Table {
	
	private final String name;
	
	private final Map<String, Table> foreignKeys = new HashMap<String, Table>();
	
	private final Set<String> primaryKeys = new HashSet<String>();
	
	private final Set<Reference> references = new HashSet<Reference>();
	
	private final Set<Integer> fetchedIds = new HashSet<Integer>();
	
	private Table(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public Map<String, Table> getForeignKeys() {
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
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof Table) {
			return name.equals(((Table) obj).name);
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public static class Reference {
		
		private final String foreingKey;
		
		private final Table table;
		
		public Reference(String foreignKey, Table table) {
			this.foreingKey = foreignKey;
			this.table = table;
		}

		public String getForeingKey() {
			return foreingKey;
		}

		public Table getTable() {
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
		
		private final Table table;
		
		public Builder(String tableName) {
			table = new Table(tableName);
		}
		
		public Builder addFK(String column, Table table) {
			this.table.foreignKeys.put(column, table);
			
			return this;
		}
		
		public Builder addReferenceAndFK(String column, Table table) {
			this.table.foreignKeys.put(column, table);
			table.references.add(new Reference(column, this.table));
			
			return this;
		}
		
		public Builder addPK(String column) {
			table.primaryKeys.add(column);
			
			return this;
		}
		
		public Table build() {
			if (table.primaryKeys.isEmpty()) {
				table.primaryKeys.add(table.name + "_id");
			}
			
			return table;
		}
	}
}
