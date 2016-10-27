# Mule API Functional Test With MockServer Netty Integration

This is an example which demonstrates how to instantiate mock services for dependencies in mule flow unit tests.

Using this approach unit tests can be written which validate the behaviour of the API with any number of downstream systems behaviours.
The mockserver can be set up to mock http,https,different payloads, timeout, refused connections and more.

Mocks are provided at run time through the [Mock Netty](https://github.com/jamesdbloom/mockserver).

In src/main/resources there is a mule flow which makes an http request out to a dependent system.

The host and port are injected into the flow.
~~~~
<http:request-config name="HTTPS_Request_Configuration" protocol="HTTPS" host="${host}" port="${port}" doc:name="HTTPS Request Configuration">
~~~~

The unit test case has startMock and stopMock methods which start a mock server to return a specific mocked response before the unit test is run.
~~~~
public void startMock() {
       if(startMock) {

           mockServer = startClientAndServer(Integer.parseInt(portProp.getValue()));

           mockServer.when(
                   request()
                           .withMethod("GET")
                           .withPath("/api/foo")

           )
                   .respond(
                           response()
                                   .withStatusCode(202)
                                   .withHeaders(
                                           new Header("Content-Type", "application/json; charset=utf-8"),
                                           new Header("Cache-Control", "public, max-age=86400")
                                   )
                                   .withBody("{ property: 'value' }")
                                   .withDelay(new Delay(TimeUnit.SECONDS, 1))
                   );
          }
   }
~~~~
The mock server is started on a Dynamic Port.
~~~~
@Rule
public SystemProperty portProp = new DynamicPort("port");
~~~~

The unit test overrides the host and port of the mule flow to point to the instantiated mock.

The unit test class has both positive and negative scenarios defined.
~~~~
@Test
   public void startPositiveTestScenarion() throws Exception{

       startMock();
       try {
           runTestScenario();
       }catch(Exception ex){
           ex.printStackTrace();
           Assert.fail();
       }finally {
           stopMock();
       }
   }

   @Test
   public void startNegativeTestScenarion() throws Exception{
       try {
           runTestScenario();
           Assert.fail();
       }catch(MessagingException ex){
           //MessagingException is expected
       }
   }
   ~~~~

##    Running the unit test

- Clone the Repository
- Open the pom.xml file in your IDE of choice.
- Open the unit test case at src/test/java/bayeslife/HttpClientTestCase.java
- Run the class file as a junit test.
