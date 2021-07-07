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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

/**
 * Access individual volume.
 *
 * @author Paul Warren
 */
public class CfVolume {

	private static final String MODE_READONLY = "r";

	private final Map<String, String> volumeData;

	public CfVolume(Map<String, String> volumeData) {
		Objects.requireNonNull(volumeData);
		this.volumeData = volumeData;
	}

	public Map<String, String> getMap() {
		return volumeData;
	}

	public Path getPath() {
		return Paths.get(volumeData.get("container_dir"));
	}

	public Mode getMode() {
		if (MODE_READONLY.equals(volumeData.get("mode"))) {
			return Mode.READ_ONLY;
		}
		return Mode.READ_WRITE;
	}

	public enum Mode {
		READ_ONLY,
		READ_WRITE,
	}
}
