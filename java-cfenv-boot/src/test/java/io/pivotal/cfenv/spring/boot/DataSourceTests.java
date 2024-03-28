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
package io.pivotal.cfenv.spring.boot;

import java.io.File;
import java.nio.file.Files;

import org.junit.Test;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.util.ResourceUtils;

import io.pivotal.cfenv.test.AbstractCfEnvTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mark Pollack
 * @author Greg Meyer
 */
public class DataSourceTests extends AbstractCfEnvTests {

	private static final String mysqlJdbcUrl = "jdbc:mysql://10.0.4.35:3306/cf_2e23d10a_8738_8c3c_66cf_13e44422698c?user=mysql_username&password=mysql_password";

	private static final String tlsMysqlJdbcUrl = "jdbc:mysql://10.0.4.35:3306/service_instance_db?permitMysqlScheme&user=mysql_username&password=mysql_password&sslMode=VERIFY_IDENTITY&useSSL=true&requireSSL=true&enabledTLSProtocols=TLSv1.2&serverSslCert=/etc/ssl/certs/ca-certificates.crt";
	
	private final CfDataSourceEnvironmentPostProcessor environmentPostProcessor = new CfDataSourceEnvironmentPostProcessor();

	private final ConfigurableApplicationContext context = new AnnotationConfigApplicationContext();

	@Test
	public void testDataSource() throws Exception {

		// To make CloudPlatform test pass
		try {
			System.setProperty("VCAP_APPLICATION", "yes");

			// To setup values used by CfEnv
			File file = ResourceUtils.getFile("classpath:vcap-services.json");
			String fileContents = new String(Files.readAllBytes(file.toPath()));
			mockVcapServices(fileContents);


			environmentPostProcessor.postProcessEnvironment(this.context.getEnvironment(),
					null);
			assertThat(this.context.getEnvironment().getProperty("spring.datasource.url"))
					.isEqualTo(mysqlJdbcUrl);
			assertThat(
					this.context.getEnvironment().getProperty("spring.datasource.username"))
					.isEqualTo("mysql_username");
			assertThat(
					this.context.getEnvironment().getProperty("spring.datasource.password"))
					.isEqualTo("mysql_password");
			
			assertThat(this.context.getEnvironment().getProperty("spring.r2dbc.url"))
				.isEqualTo(mysqlJdbcUrl.replace("jdbc:", "r2dbc:").substring(0, mysqlJdbcUrl.indexOf("?") + 1));
			assertThat(
				this.context.getEnvironment().getProperty("spring.r2dbc.username"))
				.isEqualTo("mysql_username");
			assertThat(
				this.context.getEnvironment().getProperty("spring.r2dbc.password"))
				.isEqualTo("mysql_password");		

		} finally {
			System.clearProperty("VCAP_APPLICATION");
		}
	}
	
	@Test
	public void testDataSource_r2dbcOptions() throws Exception {

		// To make CloudPlatform test pass
		try {
			System.setProperty("VCAP_APPLICATION", "yes");

			// To setup values used by CfEnv
			File file = ResourceUtils.getFile("classpath:vcap-services-tls.json");
			String fileContents = new String(Files.readAllBytes(file.toPath()));
			mockVcapServices(fileContents);


			environmentPostProcessor.postProcessEnvironment(this.context.getEnvironment(),
					null);
			assertThat(this.context.getEnvironment().getProperty("spring.datasource.url"))
					.isEqualTo(tlsMysqlJdbcUrl);
			assertThat(
					this.context.getEnvironment().getProperty("spring.datasource.username"))
					.isEqualTo("mysql_username");
			assertThat(
					this.context.getEnvironment().getProperty("spring.datasource.password"))
					.isEqualTo("mysql_password");
			
			assertThat(this.context.getEnvironment().getProperty("spring.r2dbc.url"))
				.isEqualTo(tlsMysqlJdbcUrl.replace("jdbc:", "r2dbc:").substring(0, tlsMysqlJdbcUrl.indexOf("?") + 1));
			assertThat(
				this.context.getEnvironment().getProperty("spring.r2dbc.username"))
				.isEqualTo("mysql_username");
			assertThat(
				this.context.getEnvironment().getProperty("spring.r2dbc.password"))
				.isEqualTo("mysql_password");		
			assertThat(
				this.context.getEnvironment().getProperty("spring.r2dbc.properties.user"))
				.isEqualTo("mysql_username");		
			assertThat(
				this.context.getEnvironment().getProperty("spring.r2dbc.properties.password"))
				.isEqualTo("mysql_password");	
			assertThat(
				this.context.getEnvironment().getProperty("spring.r2dbc.properties.sslMode"))
				.isEqualTo("VERIFY_IDENTITY");				
			assertThat(
				this.context.getEnvironment().getProperty("spring.r2dbc.properties.useSSL"))
				.isEqualTo("true");				
			assertThat(
				this.context.getEnvironment().getProperty("spring.r2dbc.properties.requireSSL"))
				.isEqualTo("true");		
			assertThat(
				this.context.getEnvironment().getProperty("spring.r2dbc.properties.tlsVersion"))
				.isEqualTo("TLSv1.2");				
			assertThat(
				this.context.getEnvironment().getProperty("spring.r2dbc.properties.serverSslCert"))
				.isEqualTo("/etc/ssl/certs/ca-certificates.crt");
			assertThat(
				this.context.getEnvironment().getProperty("spring.r2dbc.properties.serverSslCert"))
				.isEqualTo("/etc/ssl/certs/ca-certificates.crt");			
			
		} finally {
			System.clearProperty("VCAP_APPLICATION");
		}
	}	
}
