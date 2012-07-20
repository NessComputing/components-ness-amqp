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
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

/**
 * A general runnable that will keep the connection to a queue or exchange alive and dispatch messages
 * received to an instance that implements {@link ConsumerCallback}.
 */
public abstract class AbstractConsumer extends AbstractAmqpRunnable
{
    private final AtomicReference<QueueingConsumer> consumerHolder = new AtomicReference<QueueingConsumer>(null);

    private final long tickTimeout;

    private final ConsumerCallback consumerCallback;

    protected AbstractConsumer(@Nonnull final ConnectionFactory connectionFactory,
                               @Nonnull final AmqpConfig amqpConfig,
                               @Nonnull final String name,
                               @Nonnull final ConsumerCallback consumerCallback)
    {
        super(connectionFactory, amqpConfig, name);

        this.consumerCallback = consumerCallback;
        this.tickTimeout = getConfig().getTickTimeout().getMillis();

    }

    @Override
    protected void connectCallback(final Channel channel) throws IOException
    {
        final QueueingConsumer consumer = new QueueingConsumer(channel);
        consumerHolder.set(consumer);

        channel.basicConsume(getName(), false, consumer);
    }

    @Override
    protected void disconnectCallback(@Nullable final Channel channel)
    {
        final QueueingConsumer queueingConsumer = consumerHolder.get();

        if (queueingConsumer != null) {
            final String consumerTag = queueingConsumer.getConsumerTag();

            if (consumerTag != null) {
                try {
                    channel.basicCancel(consumerTag);
                }
                catch (IOException ioe) {
                    LOG.warnDebug(ioe, "While cancelling subscription for %s", consumerTag);
                }
            }
        }
    }

    @Override
    protected boolean process() throws IOException, InterruptedException
    {
        channelConnect();
        final QueueingConsumer consumer = consumerHolder.get();

        if (consumer == null) {
            // When the channel is not connected, this can happen. Simulate a tick.
            LOG.warn("Channel not connected!");
            Thread.sleep(tickTimeout);
            return true;
        }
        else {
            final QueueingConsumer.Delivery delivery = consumer.nextDelivery(tickTimeout);
            if (delivery != null) {
                try {
                    return consumerCallback.withDelivery(delivery);
                }
                finally {
                    final Channel channel = getChannel();
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }
            }
            else {
                LOG.trace("Tick...");
            }
            return true;
        }
    }
}
