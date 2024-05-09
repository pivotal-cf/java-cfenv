/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.pivotal.cfenv.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.cedarsoftware.io.JsonIo;
import com.cedarsoftware.io.JsonObject;


public class JsonIoConverter {

	/**
	 * Conveniently takes a Json String as input and uses json-io to unserialize it.
	 * Then, converts all the Object[] into List; and all the longs that could have just been ints into ints
	 *
	 * @param jsonInput the string with the Json content
	 * @return a serialized version of the input in a Map
	 */
	public static Map jsonToJavaWithListsAndInts(String jsonInput) {
		Map args = new HashMap();
		args.put(JsonIo.USE_MAPS, true);
		JsonObject rawServicesMap = JsonIo.toObjects(jsonInput, JsonIo.getReadOptionsBuilder(args).build(), JsonObject.class);
		return convertArraysAndLongs(rawServicesMap);
	}

	private static Map convertArraysAndLongs(Map input) {
		Map resultMap = new LinkedHashMap();
		for (Object entry : input.entrySet()) {
			Map.Entry castedEntry = (Map.Entry) entry;
			Object entryValue = castedEntry.getValue();
			Object entryKey = castedEntry.getKey();
			if (entryValue instanceof Object[] objectArray) {
				resultMap.put(entryKey, convertArray(objectArray));
			} else if (entryValue instanceof JsonObject jsonObject) {
				resultMap.put(entryKey, convertArraysAndLongs(jsonObject));
			} else if(entryValue instanceof Long longValue) {
				resultMap.put(entryKey, tryAndConvertToInt(longValue));
			} else {
				resultMap.put(entryKey, entryValue);
			}
		}
		return resultMap;
	}

	static Number tryAndConvertToInt(Long longValue) {
		int potentialInteger;
		if (longValue.toString().length() <= 9) {
			potentialInteger = longValue.intValue();
			return potentialInteger;
		} else {
			return longValue;
		}
	}

	private static List convertArray(Object[] array) {
		List resultList = new ArrayList();
		for (Object element: array) {
			if (element instanceof JsonObject jsonObject) {
				resultList.add(convertArraysAndLongs(jsonObject));
			} else if (element instanceof Object[] objectArray) {
				resultList.add(convertArray(objectArray));
			} else if(element instanceof Long longValue) {
				resultList.add(tryAndConvertToInt(longValue));
			} else {
				resultList.add(element);
			}
		}
		return resultList;
	}

}
