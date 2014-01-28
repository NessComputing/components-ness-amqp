/**
 * Copyright (C) 2012 Ness Computing, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nesscomputing.amqp;

import java.io.IOException;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.name.Named;
import com.rabbitmq.client.QueueingConsumer.Delivery;

import com.nesscomputing.config.Config;
import com.nesscomputing.config.ConfigModule;
import com.nesscomputing.jackson.NessJacksonModule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestUnknownCaller
{
    @Inject
    @Named("test")
    public AmqpRunnableFactory exchangeRunnableFactory;

    private static String BROKER_URI = "amqp://some-unknown-host:65534";

    @Before
    public void setUp()
    {
        final Config config = Config.getFixedConfig(ImmutableMap.of("ness.amqp.test.enabled", "true",
                                                                    "ness.amqp.test.connection-url", BROKER_URI));

        final Injector injector = Guice.createInjector(Stage.PRODUCTION,
                                                       new Module() {
                                                           @Override
                                                           public void configure(final Binder binder) {
                                                               binder.disableCircularProxies();
                                                               binder.requireExplicitBindings();
                                                           }
                                                       },
                                                       new ConfigModule(config),
                                                       new NessJacksonModule(),
                                                       new AmqpModule(config, "test"));

        injector.injectMembers(this);

        Assert.assertNotNull(exchangeRunnableFactory);
    }

    @Test
    public void testUnknownCaller() throws Exception
    {
        final ConsumerCallback callback = new ConsumerCallback() {

            @Override
            public boolean withDelivery(Delivery delivery) throws IOException {
                return false;
            }
        };

        final ExchangeConsumer exchangeConsumer = exchangeRunnableFactory.createExchangeListener("test-topic", callback);
        final ExchangePublisher<Object> exchangeProducer = exchangeRunnableFactory.createExchangeJsonPublisher("test-topic");
        final Thread consumerThread = new Thread(exchangeConsumer);
        final Thread producerThread = new Thread(exchangeProducer);
        consumerThread.start();
        producerThread.start();

        Thread.sleep(1000L);

        Assert.assertFalse(exchangeConsumer.isConnected());
        Assert.assertFalse(exchangeProducer.isConnected());

        Thread.sleep(10000L);

        Assert.assertFalse(exchangeConsumer.isConnected());
        Assert.assertFalse(exchangeProducer.isConnected());

        exchangeProducer.shutdown();
        exchangeConsumer.shutdown();
        producerThread.interrupt();
        consumerThread.interrupt();
        producerThread.join();
        consumerThread.join();
    }
}

