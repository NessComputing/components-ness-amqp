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
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.name.Named;
import com.nesscomputing.config.Config;
import com.nesscomputing.config.ConfigModule;
import com.nesscomputing.jackson.NessJacksonModule;
import com.nesscomputing.testing.lessio.AllowNetworkListen;
import com.rabbitmq.client.QueueingConsumer.Delivery;

@AllowNetworkListen(ports={0})
public abstract class AbstractTestStrangeStuff
{
    @Inject
    @Named("test")
    public AmqpRunnableFactory exchangeRunnableFactory;

    protected abstract AmqpProvider getProvider();

    @Before
    public void setUp() throws Exception
    {
        getProvider().startup();

        final String brokerUri = getProvider().getUri();

        final Config config = Config.getFixedConfig(ImmutableMap.of("ness.amqp.test.enabled", "true",
                                                                    "ness.amqp.test.connection-url", brokerUri));
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

    @After
    public void tearDown() throws Exception
    {
        getProvider().shutdown();
    }

    @Test
    public void testConsumerDies() throws Exception
    {
        final ConsumerCallback callback = new ConsumerCallback() {

            @Override
            public boolean withDelivery(Delivery delivery) throws IOException {
                return false;
            }
        };

        final ExchangeConsumer topicConsumer = exchangeRunnableFactory.createExchangeListener("test-topic", callback);
        final ExchangePublisher<Object> topicProducer = exchangeRunnableFactory.createExchangeJsonPublisher("test-topic");
        final Thread consumerThread = new Thread(topicConsumer);
        final Thread producerThread = new Thread(topicProducer);
        consumerThread.start();
        producerThread.start();

        Thread.sleep(1000L);

        Assert.assertTrue(topicConsumer.isConnected());
        Assert.assertFalse(topicProducer.isConnected());

        final int maxCount = 1000;
        for (int i = 0; i < maxCount; i++) {
            topicProducer.put("dummy");
        }

        Thread.sleep(100L);
        Assert.assertTrue(topicProducer.isEmpty());
        Assert.assertTrue(topicProducer.isConnected());
        Assert.assertFalse(topicConsumer.isConnected());

        topicProducer.shutdown();
        topicConsumer.shutdown();
        producerThread.interrupt();
        consumerThread.interrupt();
        producerThread.join();
        consumerThread.join();
    }

    @Test
    public void testConsumerGetsInterrupted() throws Exception
    {
        final ConsumerCallback callback = new ConsumerCallback() {

            @Override
            public boolean withDelivery(Delivery delivery) throws IOException {
                return true;
            }
        };

        final ExchangeConsumer topicConsumer = exchangeRunnableFactory.createExchangeListener("test-topic", callback);
        final ExchangePublisher<Object> topicProducer = exchangeRunnableFactory.createExchangeJsonPublisher("test-topic");
        final Thread consumerThread = new Thread(topicConsumer);
        final Thread producerThread = new Thread(topicProducer);
        consumerThread.start();
        producerThread.start();

        Thread.sleep(1000L);

        Assert.assertTrue(topicConsumer.isConnected());
        Assert.assertFalse(topicProducer.isConnected());

        final int maxCount = 1000;
        for (int i = 0; i < maxCount; i++) {
            topicProducer.put("dummy");
            if (i > 500) {
                consumerThread.interrupt();
            }
        }

        Thread.sleep(100L);
        Assert.assertTrue(topicProducer.isEmpty());
        Assert.assertTrue(topicProducer.isConnected());
        Assert.assertFalse(topicConsumer.isConnected());

        topicProducer.shutdown();
        topicConsumer.shutdown();
        producerThread.interrupt();
        consumerThread.interrupt();
        producerThread.join();
        consumerThread.join();
    }

    @Test
    public void testProducerGetsInterrupted() throws Exception
    {
        final ConsumerCallback callback = new ConsumerCallback() {

            @Override
            public boolean withDelivery(Delivery delivery) throws IOException {
                return true;
            }
        };

        final ExchangeConsumer topicConsumer = exchangeRunnableFactory.createExchangeListener("test-topic", callback);
        final ExchangePublisher<Object> topicProducer = exchangeRunnableFactory.createExchangeJsonPublisher("test-topic");
        final Thread consumerThread = new Thread(topicConsumer);
        final Thread producerThread = new Thread(topicProducer);
        consumerThread.start();
        producerThread.start();

        Thread.sleep(1000L);

        Assert.assertTrue(topicConsumer.isConnected());
        Assert.assertFalse(topicProducer.isConnected());

        final int maxCount = 1000;
        int i = 0;

        for (i = 0; i < maxCount; i++) {
            if (!topicProducer.offerWithTimeout("dummy", 50L, TimeUnit.MILLISECONDS)) {
                break;
            }

            if (i > 500) {
                producerThread.interrupt();
            }
        }

        Assert.assertFalse(i == maxCount);
        Assert.assertTrue(i > 500);

        Thread.sleep(500L);
        Assert.assertFalse(topicProducer.isConnected());
        Assert.assertTrue(topicConsumer.isConnected());

        topicProducer.shutdown();
        topicConsumer.shutdown();
        producerThread.interrupt();
        consumerThread.interrupt();
        producerThread.join();
        consumerThread.join();
    }
}

