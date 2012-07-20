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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.skife.config.TimeSpan;

import com.google.common.base.Preconditions;
import com.nesscomputing.amqp.PublisherCallback.PublisherData;
import com.nesscomputing.logging.Log;
import com.rabbitmq.client.ConnectionFactory;

/**
 * AMQP object publisher. Puts objects onto an AMQP queue.
 */
public abstract class AbstractPublisher<T>  extends AbstractAmqpRunnable
{
    protected final Log LOG = Log.forClass(this.getClass());

    private final BlockingQueue<T> messageQueue;
    private final TimeSpan tickTimeout;
    private final TimeSpan transmitTimeout;

    private final PublisherCallback<? super T> publisherCallback;

    protected AbstractPublisher(@Nonnull final ConnectionFactory connectionFactory,
                                @Nonnull final AmqpConfig amqpConfig,
                                @Nonnull final String name,
                                @Nonnull final PublisherCallback<? super T> publisherCallback)

    {
        super(connectionFactory, amqpConfig, name);

        this.publisherCallback = publisherCallback;

        this.messageQueue = new ArrayBlockingQueue<T>(amqpConfig.getPublisherQueueLength());

        this.transmitTimeout = amqpConfig.getTransmitTimeout();
        this.tickTimeout = amqpConfig.getTickTimeout();
    }

    public boolean offer(@Nonnull final T data)
    {
        Preconditions.checkNotNull(data, "the message can not be null!");

        return messageQueue.offer(data);
    }

    public boolean offerWithTimeout(@Nonnull final T data)
    {
        Preconditions.checkNotNull(data, "the message can not be null!");

        try {
            return messageQueue.offer(data, transmitTimeout.getPeriod(), transmitTimeout.getUnit());
        }
        catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public boolean offerWithTimeout(@Nonnull final T data, final long timeout, final TimeUnit unit)
    {
        Preconditions.checkNotNull(data, "the message can not be null!");

        try {
            return messageQueue.offer(data, timeout, unit);
        }
        catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public void put(@Nonnull final T data)
    {
        Preconditions.checkNotNull(data, "the message can not be null!");

        try {
            messageQueue.put(data);
        }
        catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    public int remainingCapacity()
    {
        return messageQueue.remainingCapacity();
    }

    public boolean isEmpty()
    {
        return messageQueue.isEmpty();
    }

    @Override
    protected boolean process() throws IOException, InterruptedException
    {
        // Only connect the transmitter if lazy-connect is false.
        if (!getConfig().isLazyTransmitterConnect()) {
            channelConnect();
        }

        final T data = messageQueue.poll(tickTimeout.getPeriod(), tickTimeout.getUnit());
        if (data != null) {
            final PublisherData publisherData = publisherCallback.publish(data);

            if (publisherData != null) {
                publish(publisherData);
                return getChannel().isOpen() && publisherData.isHealthy();
            }
        }

        return true;
    }

    protected abstract void publish(PublisherData publisherData) throws IOException;
}
