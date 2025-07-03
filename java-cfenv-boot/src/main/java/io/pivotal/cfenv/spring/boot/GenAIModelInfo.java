package io.pivotal.cfenv.spring.boot;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents information about a GenAI model including its name and capabilities.
 *
 * @author Corby Page
 */
public class GenAIModelInfo {

    public enum Capability {
        CHAT,
        EMBEDDING,
        TOOLS,
        VISION
    }

    private final String name;
    private final List<Capability> capabilities;

    public GenAIModelInfo(String name, List<Capability> capabilities) {
        this.name = Objects.requireNonNull(name, "Model name cannot be null");
        this.capabilities = capabilities != null ?
                Collections.unmodifiableList(capabilities) :
                Collections.emptyList();
    }

    public String getName() {
        return name;
    }

    public List<Capability> getCapabilities() {
        return capabilities;
    }

    public boolean hasCapability(Capability capability) {
        return capabilities.contains(capability);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenAIModelInfo that = (GenAIModelInfo) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(capabilities, that.capabilities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, capabilities);
    }

    @Override
    public String toString() {
        return "GenAIModelInfo{" +
                "name='" + name + '\'' +
                ", capabilities=" + capabilities +
                '}';
    }
}