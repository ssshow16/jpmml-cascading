/*
 * Copyright (c) 2013 Villu Ruusmann
 *
 * This file is part of JPMML-Cascading
 *
 * JPMML-Cascading is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPMML-Cascading is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with JPMML-Cascading.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jpmml.cascading;

import java.lang.reflect.*;
import java.util.*;

import cascading.tuple.*;

import org.jpmml.evaluator.*;

import org.dmg.pmml.*;
import org.dmg.pmml.Field;

public class FieldsUtil {

	private FieldsUtil(){
	}

	static
	public Fields getActiveFields(Evaluator evaluator){
		return getDataFields(evaluator, evaluator.getActiveFields());
	}

	static
	public Fields getGroupFields(Evaluator evaluator){
		return getDataFields(evaluator, evaluator.getGroupFields());
	}

	static
	public Fields getTargetFields(Evaluator evaluator){
		return getDataFields(evaluator, evaluator.getTargetFields());
	}

	static
	private Fields getDataFields(Evaluator evaluator, List<FieldName> dataFields){
		Fields result = new Fields();

		for(FieldName dataField : dataFields){
			DataField field = evaluator.getDataField(dataField);

			result = result.append(createFields(dataField.getValue(), field));
		}

		return result;
	}

	static
	public Fields getOutputFields(Evaluator evaluator){
		Fields result = new Fields();

		List<FieldName> outputFields = evaluator.getOutputFields();
		for(FieldName outputField : outputFields){
			OutputField field = evaluator.getOutputField(outputField);

			result = result.append(createFields(outputField.getValue(), field));
		}

		return result;
	}

	static
	private Fields createFields(String name, Field field){
		DataType dataType = field.getDataType();
		if(dataType == null){
			dataType = DataType.STRING;
		}

		return new Fields(name, getType(dataType));
	}

	static
	private Type getType(DataType dataType){

		switch(dataType){
			case STRING:
				return String.class;
			case INTEGER:
				return Integer.class;
			case FLOAT:
			case DOUBLE:
				return Double.class;
			default:
				throw new IllegalArgumentException();
		}
	}
}