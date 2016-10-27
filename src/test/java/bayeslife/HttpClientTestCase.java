package bayeslife;


import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.integration.ClientAndProxy;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Delay;
import org.mockserver.model.Header;
import org.mule.DefaultMuleEvent;
import org.mule.DefaultMuleMessage;
import org.mule.MessageExchangePattern;
import org.mule.api.MessagingException;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.processor.MessageProcessor;
import org.mule.construct.Flow;
import org.mule.module.http.internal.request.DefaultHttpRequester;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;


public class HttpClientTestCase extends FunctionalTestCase
{

    private ClientAndProxy proxy;
    private ClientAndServer mockServer;

    Logger logger = Logger.getLogger(HttpClientTestCase.class.getName());

    @Override
    protected String getConfigResources()
    {
        return "http.consumer.xml";
    }

    @Rule
    public SystemProperty hostProp = new SystemProperty("host","localhost");

    @Rule
    public SystemProperty portProp = new DynamicPort("port");

    @Rule
    public SystemProperty pathProp = new SystemProperty("path","/api/foo");

    public boolean startMock = true;


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
                                    .withBody("{ message: 'incorrect username and password combination' }")
                                    .withDelay(new Delay(TimeUnit.SECONDS, 1))
                    );
           }
    }


    public void stopMock() {
        if(startMock)
            mockServer.stop();
    }

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

    public void runTestScenario() throws Exception
    {
        final Flow f = (Flow)muleContext.getRegistry().lookupFlowConstruct("mainflow");

        List<MessageProcessor> mps = f.getMessageProcessors();

        DefaultHttpRequester req= (DefaultHttpRequester) mps.get(0);

        DefaultMuleMessage m = new DefaultMuleMessage("", muleContext);
        final MuleEvent me = new DefaultMuleEvent(m, MessageExchangePattern.REQUEST_RESPONSE,null, f);
        MuleEvent res = f.process(me);
        MuleMessage msg = res.getMessage();

    }

}
