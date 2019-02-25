package io.pivotal.cfenv.spring.boot;

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfEnv;
import io.pivotal.cfenv.core.CfService;

import java.util.Map;

public interface CfEnvProcessor {
	CfService findService(CfEnv cfEnv);

	String getPropertySourceName();

	void process(CfCredentials cfCredentials, Map<String, Object> properties);
}
