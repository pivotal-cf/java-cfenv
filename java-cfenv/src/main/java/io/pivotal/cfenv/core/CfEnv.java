/*
 * Copyright 2019 the original author or authors.
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provides access to Cloud Foundry environment variables.
 *
 * @author Mark Pollack
 */
public class CfEnv {
	private static final String VCAP_APPLICATION = "VCAP_APPLICATION";
	private static final String VCAP_SERVICES = "VCAP_SERVICES";

	private final List<CfService> cfServices = new ArrayList<>();

	private CfApplication cfApplication;

	public CfEnv() {
		this(System.getenv(VCAP_APPLICATION), System.getenv(VCAP_SERVICES));
	}

	public CfEnv(String vcapApplicationJson, String vcapServicesJson) {
		parseVcapServices(vcapServicesJson);
		parseVcapApplication(vcapApplicationJson);
	}

	private void parseVcapApplication(String vcapApplicationJson) {
		try {
			if (vcapApplicationJson != null && vcapApplicationJson.length() > 0) {
				Map<String, Object> applicationData = JsonIoConverter.jsonToJavaWithListsAndInts(vcapApplicationJson);
				this.cfApplication = new CfApplication(applicationData);
			}
		} catch (Exception e) {
			 throw new IllegalStateException("Could not parse " + VCAP_APPLICATION + "environment variable.", e);
		}
	}

	private void parseVcapServices(String vcapServicesJson) {
		try {
			if (vcapServicesJson != null && vcapServicesJson.length() > 0) {
				Map<String, List<Map<String, Object>>> rawServicesMap = JsonIoConverter.jsonToJavaWithListsAndInts(vcapServicesJson);
				rawServicesMap.values().stream()
						.flatMap(Collection::stream)
						.forEach(serviceData -> cfServices.add(new CfService(serviceData)));
			}
		} catch (Exception e) {
			throw new IllegalStateException("Could not parse " + VCAP_SERVICES + " environment variable.", e);
		}
	}

	public CfApplication getApp() {
		return this.cfApplication;
	}

	public List<CfService> findAllServices() {
		return cfServices;
	}

	public List<CfService> findServicesByName(String... spec) {
		if (spec == null || spec.length == 0) {
			return Collections.emptyList();
		}
		return Arrays.stream(spec)
				.flatMap(regex -> this.cfServices.stream().filter(cfService -> {
					String name = cfService.getName();
					return name != null && name.length() > 0 && name.matches(regex);
				}))
				.distinct()
				.collect(Collectors.toList());
	}

	public CfService findServiceByName(String... spec) {
		List<CfService> cfServices = findServicesByName(spec);
		if (cfServices.size() == 1) {
			return cfServices.stream().findFirst().get();
		}
		String specMessage = (spec == null) ? "null" : String.join(", ", spec);
		throwExceptionIfMultipleMatches(cfServices, specMessage, "name");
		throw new IllegalArgumentException(
				"No service with name [" + specMessage + "] was found.");
	}

	private void throwExceptionIfMultipleMatches(List<CfService> cfServices,
			String specMessage, String operation) {
		if (cfServices.size() > 1) {
			String[] names = cfServices.stream().map(CfService::getName)
					.toArray(String[]::new);
			throw new IllegalArgumentException(
					"No unique service matching by " + operation + " [" + specMessage
							+ "] was found.  Matching service names are ["
							+ String.join(", ", names) + "]");
		}
	}

	public List<CfService> findServicesByLabel(String... spec) {
		if (spec == null || spec.length == 0) {
			return Collections.emptyList();
		}
		return Arrays.stream(spec)
				.flatMap(regex -> this.cfServices.stream().filter(cfService -> {
					String label = cfService.getLabel();
					return label != null && label.length() > 0 && label.matches(regex);
				}))
				.distinct()
				.collect(Collectors.toList());
	}

	public CfService findServiceByLabel(String... spec) {
		List<CfService> cfServices = findServicesByLabel(spec);
		if (cfServices.size() == 1) {
			return cfServices.stream().findFirst().get();
		}
		String message = (spec == null) ? "null" : String.join(", ", spec);
		throwExceptionIfMultipleMatches(cfServices, message, "label");
		throw new IllegalArgumentException(
				"No service with label [" + message + "] was found.");
	}

	public CfService findServiceByTag(String... spec) {
		List<CfService> cfServices = findServicesByTag(spec);
		if (cfServices.size() == 1) {
			return cfServices.stream().findFirst().get();
		}
		String message = (spec == null) ? "null" : String.join(", ", spec);
		throwExceptionIfMultipleMatches(cfServices, message, "tag");
		throw new IllegalArgumentException(
				"No service with tag [" + message + "] was found.");
	}

	public List<CfService> findServicesByTag(String... spec) {
		if (spec == null || spec.length == 0) {
			return Collections.emptyList();
		}
		return Arrays.stream(spec)
				.flatMap(regex -> cfServices.stream()
						.filter(cfService -> cfService.getTags().stream()
								.anyMatch(tag -> tag != null && tag.matches(regex))))
				.distinct()
				.collect(Collectors.toList());
	}

	public CfCredentials findCredentialsByName(String... spec) {
		CfService cfService = findServiceByName(spec);
		return cfService.getCredentials();
	}

	public CfCredentials findCredentialsByLabel(String... spec) {
		CfService cfService = findServiceByLabel(spec);
		return cfService.getCredentials();
	}

	public CfCredentials findCredentialsByTag(String... spec) {
		CfService cfService = findServiceByTag(spec);
		return cfService.getCredentials();
	}

	/**
	 * Checks that the value of the environment variable VCAP_APPLICATION is not null, usually
	 * indicating that this application is running inside of Cloud Foundry
	 * @return {@code true} if the environment variable VCAP_APPLICATION is not null,
	 * {@code false} otherwise.
	 */
	public boolean isInCf() {
		return System.getenv(VCAP_APPLICATION) != null;
	}

}
