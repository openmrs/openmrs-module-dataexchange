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

import java.util.HashMap;
import java.util.Map;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;


public class ColumnReplacementTable implements ITable {
	
	private ITable table;
	
	private Map<String, Object> replacements = new HashMap<String, Object>();
	
	public ColumnReplacementTable(ITable table) {
		this.table = table;
	}

	@Override
    public ITableMetaData getTableMetaData() {
	    return table.getTableMetaData();
    }

	@Override
    public int getRowCount() {
	    return table.getRowCount();
    }
	
	public Object addReplacement(String column, Object value) {
		return replacements.put(column, value);
	}

	@Override
    public Object getValue(int row, String column) throws DataSetException {
		if (replacements.containsKey(column)) {
			return replacements.get(column);
		} else {
			Object value = table.getValue(row, column);
			return value;
		}
    }
	
}
