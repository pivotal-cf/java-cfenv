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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.core.io.ClassPathResource;

class JsonIoConverterTest {

	@Test
	void jsonToJavaWithListsAndInts_Application_Test() throws IOException {
		var vcapApplication = new Scanner(new ClassPathResource("test/vcap-application.json").getInputStream()).useDelimiter("\\A").next();
		var vcapApplicationAsMap = JsonIoConverter.jsonToJavaWithListsAndInts(vcapApplication);
		var applicationUris = vcapApplicationAsMap.get("application_uris");
		Assertions.assertInstanceOf(List.class, applicationUris);
		Assertions.assertEquals(1, ((List)applicationUris).size());
		Assertions.assertEquals("my-app.example.com", ((List)applicationUris).get(0));
		Assertions.assertEquals(61857, vcapApplicationAsMap.get("port"));
		Assertions.assertEquals(1376265929L, vcapApplicationAsMap.get("state_timestamp"));
	}

	@Test
	void jsonToJavaWithListsAndInts_Services_Test() throws IOException {
		var vcapServices = new Scanner(new ClassPathResource("vcap-services.json").getInputStream()).useDelimiter("\\A").next();
		var vcapServicesAsMap = JsonIoConverter.jsonToJavaWithListsAndInts(vcapServices);
		var tags =((List<Map>) vcapServicesAsMap.get("p-mysql")).get(0).get("tags");
		Assertions.assertInstanceOf(List.class, tags);
		Assertions.assertEquals(2, ((List)tags).size());
		Assertions.assertEquals("mysql", ((List)tags).get(0));
		Assertions.assertEquals("relational", ((List)tags).get(1));
		Assertions.assertEquals(45470, ((List<Map<String,Map<String, Object>>>) vcapServicesAsMap.get("p-redis")).get(0).get("credentials").get("port"));
	}

	@Test
	void tryAndConvertToIntTest() {
		Long smallValue = 61857L;
		Number actual = JsonIoConverter.tryAndConvertToInt(smallValue);
		Assertions.assertEquals(61857, actual);

		Long bigValue = 1376265929L;
		Assertions.assertEquals(1376265929L, JsonIoConverter.tryAndConvertToInt(bigValue));
	}
}
