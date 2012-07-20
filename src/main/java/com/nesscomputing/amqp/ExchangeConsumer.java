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

import javax.annotation.Nonnull;

import com.google.inject.Inject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

/**
 * A general runnable that will keep the connection to an exchange alive and dispatch messages
 * received on the exchange to an instance that implements {@link ConsumerCallback}.
 */
public final class ExchangeConsumer extends AbstractConsumer
{
    @Inject
    public ExchangeConsumer(@Nonnull final ConnectionFactory connectionFactory,
                            @Nonnull final AmqpConfig amqpConfig,
                            @Nonnull final String name,
                            @Nonnull final ConsumerCallback consumerCallback)
    {
        super(connectionFactory, amqpConfig, name, consumerCallback);
    }

    @Override
    protected String getServiceType()
    {
        return "exchange-consumer";
    }

    @Override
    protected void connectCallback(@Nonnull final Channel channel) throws IOException
    {
        super.connectCallback(channel);

        channel.exchangeDeclare(getName(), getConfig().getExchangeType(), false, false, null);
        final String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, getName(), "");

        channel.basicConsume(queueName, false, getConsumer());
    }
}
