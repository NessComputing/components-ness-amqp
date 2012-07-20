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

import java.lang.annotation.Annotation;

import javax.annotation.Nullable;

import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Factory to create new Runnables to access exchanges and queues.
 */
@Singleton
public class AmqpRunnableFactory
{
    private final Annotation annotation;

    private ConnectionFactory connectionFactory;
    private AmqpConfig amqpConfig;
    private ObjectMapper objectMapper;

    AmqpRunnableFactory(@Nullable final Annotation annotation)
    {
        this.annotation = annotation;
    }

    @Inject
    void inject(final Injector injector, final ObjectMapper objectMapper)
    {
        if (annotation == null) {
            this.connectionFactory = injector.getInstance(Key.get(ConnectionFactory.class));
            this.amqpConfig = injector.getInstance(Key.get(AmqpConfig.class));
        }
        else {
            this.connectionFactory = injector.getInstance(Key.get(ConnectionFactory.class, annotation));
            this.amqpConfig = injector.getInstance(Key.get(AmqpConfig.class, annotation));
        }

        this.objectMapper = objectMapper;
    }

    /**
     * Creates a new {@link ExchangePublisher}. The callback is called to convert an object that is sent into the ExchangePublisher
     * into an AMQP byte array..
     */
    public <T> ExchangePublisher<T> createExchangePublisher(final String name, final PublisherCallback<T> messageCallback)
    {
        Preconditions.checkState(connectionFactory != null, "connection factory was never injected!");
        return new ExchangePublisher<T>(connectionFactory, amqpConfig, name, messageCallback);
    }

    /**
     * Creates a new {@link QueuePublisher}. The callback is called to convert an object that is sent into the QueuePublisher
     * into an AMQP byte array..
     */
    public <T> QueuePublisher<T> createQueuePublisher(final String name, final PublisherCallback<T> messageCallback)
    {
        Preconditions.checkState(connectionFactory != null, "connection factory was never injected!");
        return new QueuePublisher<T>(connectionFactory, amqpConfig, name, messageCallback);
    }

    /**
     * Creates a new {@link ExchangePublisher}. The Publisher accepts arbitrary objects and uses the Jackson object mapper to convert them into
     * JSON and sends them as a text message.
     */
    public ExchangePublisher<Object> createExchangeJsonPublisher(final String name)
    {
        Preconditions.checkState(connectionFactory != null, "connection factory was never injected!");
        return new ExchangePublisher<Object>(connectionFactory, amqpConfig, name, new JsonPublisherCallback(name, objectMapper));
    }

    /**
     * Creates a new {@link QueuePublisher}. The Publisher accepts arbitrary objects and uses the Jackson object mapper to convert them into
     * JSON and sends them as a text message.
     */
    public QueuePublisher<Object> createQueueJsonPublisher(final String name)
    {
        Preconditions.checkState(connectionFactory != null, "connection factory was never injected!");
        return new QueuePublisher<Object>(connectionFactory, amqpConfig, name, new JsonPublisherCallback(name, objectMapper));
    }

    /**
     * Creates a new {@link ExchangePublisher}. The Publisher accepts strings and sends them as a text message.
     */
    public ExchangePublisher<String> createExchangeTextPublisher(final String name)
    {
        Preconditions.checkState(connectionFactory != null, "connection factory was never injected!");
        return new ExchangePublisher<String>(connectionFactory, amqpConfig, name, new StringPublisherCallback(name));
    }

    /**
     * Creates a new {@link QueuePublisher}. The Publisher accepts strings and sends them as a text message.
     */
    public QueuePublisher<String> createQueueTextPublisher(final String name)
    {
        Preconditions.checkState(connectionFactory != null, "connection factory was never injected!");
        return new QueuePublisher<String>(connectionFactory, amqpConfig, name, new StringPublisherCallback(name));
    }

    /**
     * Creates a new {@link ExchangeConsumer}. For every message received (or when the timeout waiting for messages is hit), the callback
     * is invoked with the message received.
     */
    public ExchangeConsumer createExchangeListener(final String name, final ConsumerCallback messageCallback)
    {
        Preconditions.checkState(connectionFactory != null, "connection factory was never injected!");
        return new ExchangeConsumer(connectionFactory, amqpConfig, name, messageCallback);
    }

    /**
     * Creates a new {@link QueueConsumer}. For every message received (or when the timeout waiting for messages is hit), the callback
     * is invoked with the message received.
     */
    public QueueConsumer createQueueListener(final String name, final ConsumerCallback messageCallback)
    {
        Preconditions.checkState(connectionFactory != null, "connection factory was never injected!");
        return new QueueConsumer(connectionFactory, amqpConfig, name, messageCallback);
    }
}
