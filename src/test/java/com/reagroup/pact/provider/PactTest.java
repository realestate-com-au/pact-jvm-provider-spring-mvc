package com.reagroup.pact.provider;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import sample.MyController;

@RunWith(PactRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContextPactTest.xml"})
@PactFile("file:src/test/resources/consumer-provider.json")
public class PactTest {

    @Autowired
    private MyController myController;

    @ProviderState("response 'world' for 'get' request")
    public MyController shouldGetCorrectResponseIfRequestMethodAreExactlyEqual() {
        return myController;
    }

    @ProviderState("response 'world' for 'gET' request")
    public MyController shouldGetCorrectResponseIfRequestMethodAreCaseInsensitiveEqual() {
        return myController;
    }
}
