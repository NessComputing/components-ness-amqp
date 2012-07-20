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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.nesscomputing.logging.Log;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Base code for the exchange and queue runnables.
 */
public abstract class AbstractAmqpRunnable implements Runnable
{
    protected final Log LOG = Log.forClass(this.getClass());

    private AtomicReference<Connection> connectionHolder = new AtomicReference<Connection>();
    private AtomicReference<Channel> channelHolder = new AtomicReference<Channel>();
    private AtomicBoolean running = new AtomicBoolean(true);

    private int backoff = 1;

    private final ConnectionFactory connectionFactory;
    private final AmqpConfig amqpConfig;
    private final String name;

    protected AbstractAmqpRunnable(@Nonnull final ConnectionFactory connectionFactory,
                                   @Nonnull final AmqpConfig amqpConfig,
                                   @Nonnull final String name)
    {
        Preconditions.checkState(name != null, "The name can not be null!");
        this.connectionFactory = connectionFactory;
        this.amqpConfig = amqpConfig;
        this.name = name;
    }

    public void shutdown()
    {
        running.set(false);
    }

    public boolean isRunning()
    {
        return running.get();
    }

    public Channel getChannel()
    {
        return channelHolder.get();
    }

    @Override
    public void run()
    {
        LOG.debug("Starting %s for '%s'", getServiceType(), name);
        try {
            while (running.get()) {
                try {
                    if (!process()) {
                        break; // while
                    }

                }
                catch (IOException ioe) {
                    backoff(ioe);
                }
                // Catch all exceptions here, not just IOException. This makes sure that
                // with a catastrophic failure in the processor, the thread does not die.
                catch (RuntimeException re) {
                    LOG.warnDebug(re, "Caught an exception in time before killing the AMQP runnable!");
                    backoff(re);
                }
            }
        }
        catch (InterruptedException ie) {
            running.set(false);
            LOG.trace("Terminated by interrupt");
        }
        finally {
            LOG.debug("Stopping %s for '%s'", getServiceType(), name);
            channelDisconnect();
        }
    }

    private void backoff(final Throwable t) throws InterruptedException
    {
        final long backoffTime = amqpConfig.getBackoffDelay().getMillis() * backoff;
        LOG.warnDebug(t, "Could not connect to Broker, sleeping for %d ms...", backoffTime);

        Thread.sleep(backoffTime);
        if (backoff != 1 << amqpConfig.getMaxBackoffFactor()) {
            backoff <<= 1;
        }
        channelDisconnect();
    }

    protected final String getName()
    {
        return name;
    }

    protected final AmqpConfig getConfig()
    {
        return amqpConfig;
    }


    protected abstract String getServiceType();

    protected abstract void connectCallback(final Channel channel) throws IOException;

    protected void disconnectCallback(@Nullable final Channel channel)
    {
    }

    protected abstract boolean process() throws IOException, InterruptedException;

    protected void channelDisconnect()
    {
        final Channel channel = channelHolder.getAndSet(null);

        disconnectCallback(channel);

        AmqpUtils.closeQuietly(channel);

        final Connection connection = connectionHolder.getAndSet(null);
        AmqpUtils.closeQuietly(connection);
    }

    protected Channel channelConnect() throws IOException
    {
        Channel channel = channelHolder.get();
        if (channel == null) {
            final Connection connection = connectionFactory.newConnection();
            connectionHolder.set(connection);
            channel = connection.createChannel();
            channelHolder.set(channel);

            connectCallback(channel);
        }
        return channel;
    }
}
