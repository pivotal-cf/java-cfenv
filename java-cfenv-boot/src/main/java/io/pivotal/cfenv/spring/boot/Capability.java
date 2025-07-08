/*
 * Copyright 2025 the original author or authors.
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

import java.util.Objects;

/**
 * Represents model capabilities supported by the GenAI tile
 *
 * @author Corby Page
 */
public class Capability {
    public static final Capability CHAT = new Capability("CHAT");
    public static final Capability EMBEDDING = new Capability("EMBEDDING");
    public static final Capability TOOLS = new Capability("TOOLS");
    public static final Capability VISION = new Capability("VISION");

    private final String value;

    private Capability(String value) {
        this.value = Objects.requireNonNull(value, "Capability value cannot be null");
    }

    public static Capability fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Capability value cannot be null");
        }
        return new Capability(value.toUpperCase());
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Capability that = (Capability) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
