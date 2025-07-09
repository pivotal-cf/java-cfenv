# Tanzu GenAI support

This library is for use when accessing a Tanzu GenAI tile (version >= 10.2) configured plan with CF.  This library uses the `VCAP_SERVICES` environment data to set properties that will enable a GenAILocator.

## Spring Applications

Spring Applications can use this library to auto-configure a GenAILocator that can be used to determine which models/mcp servers are available, what capabilities they support and a method of accessing them.

This service provides the following properties to your spring application:

| Property Name            | Value                           |
|--------------------------|---------------------------------|
| genai.locator.config-url | config_url (from VCAP_SERVICES) |
| genai.locator.api-base   | api_base (from VCAP_SERVICES)   |
| genai.locator.api-key    | api_key (from VCAP_SERVICES)    |

Please see the Sample Apps below for more information.

### Sample Apps

Sample apps using this library are available at TODO.