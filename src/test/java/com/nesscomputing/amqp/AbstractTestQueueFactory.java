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

import static java.lang.String.format;

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
import com.nesscomputing.amqp.util.CountingMessageCallback;
import com.nesscomputing.amqp.util.DummyMessageCallback;
import com.nesscomputing.config.Config;
import com.nesscomputing.config.ConfigModule;
import com.nesscomputing.jackson.NessJacksonModule;

public abstract class AbstractTestQueueFactory
{
    public static final long DRAIN_SLEEP = 500L;

    @Inject
    @Named("test")
    public AmqpRunnableFactory queueRunnableFactory;

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

        Assert.assertNotNull(queueRunnableFactory);
    }

    @After
    public void tearDown() throws Exception
    {
        getProvider().shutdown();
    }

    @Test
    public void testSimpleConsumer() throws Exception
    {
        final QueueConsumer queueConsumer = queueRunnableFactory.createQueueListener("test-queue", new DummyMessageCallback());
        Assert.assertNotNull(queueConsumer);
    }

    @Test
    public void testSimpleProducer() throws Exception
    {
        final QueuePublisher<Object> queuePublisher = queueRunnableFactory.createQueueJsonPublisher("test-queue");
        Assert.assertNotNull(queuePublisher);
    }

    @Test
    public void stopConsumerWithNoMessages() throws Exception
    {
        final QueueConsumer queueConsumer = queueRunnableFactory.createQueueListener("test-queue", new DummyMessageCallback());
        Assert.assertNotNull(queueConsumer);

        final Thread queueThread = new Thread(queueConsumer);
        queueThread.start();

        Thread.sleep(2000L);
        Assert.assertTrue(queueConsumer.isConnected());

        queueConsumer.shutdown();
        queueThread.interrupt();
        queueThread.join();
    }

    @Test
    public void stopProducerWithNoMessages() throws Exception
    {
        final QueuePublisher<Object> queuePublisher = queueRunnableFactory.createQueueJsonPublisher("test-queue");
        Assert.assertNotNull(queuePublisher);

        final Thread queueThread = new Thread(queuePublisher);
        queueThread.start();

        Thread.sleep(2000L);
        Assert.assertFalse(queuePublisher.isConnected());

        queuePublisher.shutdown();
        queueThread.interrupt();
        queueThread.join();
    }

    @Test
    public void testProduceConsume() throws Exception
    {
        final CountingMessageCallback cmc = new CountingMessageCallback();
        final QueueConsumer queueConsumer = queueRunnableFactory.createQueueListener("test-queue", cmc);
        final QueuePublisher<Object> queuePublisher = queueRunnableFactory.createQueueJsonPublisher("test-queue");
        final Thread consumerThread = new Thread(queueConsumer);
        final Thread producerThread = new Thread(queuePublisher);
        consumerThread.start();
        producerThread.start();

        Thread.sleep(1000L);

        Assert.assertTrue(queueConsumer.isConnected());
        Assert.assertFalse(queuePublisher.isConnected());

        final int maxCount = 1000;
        for (int i = 0; i < maxCount; i++) {
            queuePublisher.put(format("hello, world %d", i));
        }

        Thread.sleep(DRAIN_SLEEP);
        Assert.assertTrue(queuePublisher.isEmpty());
        Assert.assertEquals(maxCount, cmc.getCount());

        queuePublisher.shutdown();
        queueConsumer.shutdown();
        producerThread.interrupt();
        consumerThread.interrupt();
        producerThread.join();
        consumerThread.join();
    }

    @Test
    public void testOneProducerTwoConsumers() throws Exception
    {
        final CountingMessageCallback cmc1 = new CountingMessageCallback();
        final CountingMessageCallback cmc2 = new CountingMessageCallback();
        final QueueConsumer queueConsumer1 = queueRunnableFactory.createQueueListener("test-queue", cmc1);
        final QueueConsumer queueConsumer2 = queueRunnableFactory.createQueueListener("test-queue", cmc2);
        final QueuePublisher<Object> queuePublisher = queueRunnableFactory.createQueueJsonPublisher("test-queue");
        final Thread consumerThread1 = new Thread(queueConsumer1);
        final Thread consumerThread2 = new Thread(queueConsumer2);
        final Thread producerThread = new Thread(queuePublisher);
        consumerThread1.start();
        consumerThread2.start();
        producerThread.start();

        Thread.sleep(1000L);

        Assert.assertTrue(queueConsumer1.isConnected());
        Assert.assertTrue(queueConsumer2.isConnected());
        Assert.assertFalse(queuePublisher.isConnected());

        final int maxCount = 1000;
        for (int i = 0; i < maxCount; i++) {
            queuePublisher.put(format("hello, world %d", i));
        }

        Thread.sleep(DRAIN_SLEEP);
        Assert.assertTrue(queuePublisher.isEmpty());
        Assert.assertEquals(maxCount, cmc1.getCount() + cmc2.getCount());

        queuePublisher.shutdown();
        queueConsumer1.shutdown();
        queueConsumer2.shutdown();
        producerThread.interrupt();
        consumerThread1.interrupt();
        consumerThread2.interrupt();
        producerThread.join();
        consumerThread1.join();
        consumerThread2.join();
    }

    @Test
    public void testTwoProducersOneConsumer() throws Exception
    {
        final CountingMessageCallback cmc = new CountingMessageCallback();
        final QueueConsumer queueConsumer = queueRunnableFactory.createQueueListener("test-queue", cmc);
        final QueuePublisher<Object> queuePublisher1 = queueRunnableFactory.createQueueJsonPublisher("test-queue");
        final QueuePublisher<Object> queuePublisher2 = queueRunnableFactory.createQueueJsonPublisher("test-queue");
        final Thread consumerThread = new Thread(queueConsumer);
        final Thread producerThread1 = new Thread(queuePublisher1);
        final Thread producerThread2 = new Thread(queuePublisher2);

        consumerThread.start();
        producerThread1.start();
        producerThread2.start();

        Thread.sleep(1000L);

        Assert.assertTrue(queueConsumer.isConnected());
        Assert.assertFalse(queuePublisher1.isConnected());
        Assert.assertFalse(queuePublisher2.isConnected());

        final int maxCount = 1000;
        for (int i = 0; i < maxCount; i++) {
            queuePublisher1.put(format("hello, world %d", i));
            queuePublisher2.put(format("hello, wold %d", i));
        }

        Thread.sleep(DRAIN_SLEEP);
        Assert.assertTrue(queuePublisher1.isEmpty());
        Assert.assertTrue(queuePublisher2.isEmpty());
        Assert.assertEquals(maxCount*2, cmc.getCount());

        queuePublisher1.shutdown();
        queuePublisher2.shutdown();
        queueConsumer.shutdown();
        producerThread1.interrupt();
        producerThread2.interrupt();
        consumerThread.interrupt();
        producerThread1.join();
        producerThread2.join();
        consumerThread.join();
    }

    @Test
    public void testTwoProducersTwoConsumers() throws Exception
    {
        final CountingMessageCallback cmc1 = new CountingMessageCallback();
        final CountingMessageCallback cmc2 = new CountingMessageCallback();
        final QueueConsumer queueConsumer1 = queueRunnableFactory.createQueueListener("test-queue", cmc1);
        final QueueConsumer queueConsumer2 = queueRunnableFactory.createQueueListener("test-queue", cmc2);
        final QueuePublisher<Object> queuePublisher1 = queueRunnableFactory.createQueueJsonPublisher("test-queue");
        final QueuePublisher<Object> queuePublisher2 = queueRunnableFactory.createQueueJsonPublisher("test-queue");
        final Thread consumerThread1 = new Thread(queueConsumer1);
        final Thread consumerThread2 = new Thread(queueConsumer2);
        final Thread producerThread1 = new Thread(queuePublisher1);
        final Thread producerThread2 = new Thread(queuePublisher2);

        consumerThread1.start();
        consumerThread2.start();
        producerThread1.start();
        producerThread2.start();

        Thread.sleep(1000L);

        Assert.assertTrue(queueConsumer1.isConnected());
        Assert.assertTrue(queueConsumer2.isConnected());
        Assert.assertFalse(queuePublisher1.isConnected());
        Assert.assertFalse(queuePublisher2.isConnected());

        final int maxCount = 1000;
        for (int i = 0; i < maxCount; i++) {
            queuePublisher1.put(format("hello, world %d", i));
            queuePublisher2.put(format("hello, wold %d", i));
        }

        Thread.sleep(DRAIN_SLEEP);

        Assert.assertTrue(queuePublisher1.isEmpty());
        Assert.assertTrue(queuePublisher2.isEmpty());

        Assert.assertEquals(maxCount*2, cmc1.getCount() + cmc2.getCount());

        queuePublisher1.shutdown();
        queuePublisher2.shutdown();
        queueConsumer1.shutdown();
        queueConsumer2.shutdown();
        producerThread1.interrupt();
        producerThread2.interrupt();
        consumerThread1.interrupt();
        consumerThread2.interrupt();
        producerThread1.join();
        producerThread2.join();
        consumerThread1.join();
        consumerThread2.join();
    }
}



