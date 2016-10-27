# Mule API Functional Test With MockServer Netty Integration

This is an example which demonstrates how to instantiate mock services for dependencies in mule flow unit tests.

In src/main/resources there is a mule flow which makes an http request out to a dependent system.

The host and port are injected into the flow.
~~~~
<http:request-config name="HTTPS_Request_Configuration" protocol="HTTPS" host="${host}" port="${port}" doc:name="HTTPS Request Configuration">
~~~~

The unit test can override

The unit test case has startMock and stopMock methods which start a mock server to return a specific mocked response before the unit test is run.
