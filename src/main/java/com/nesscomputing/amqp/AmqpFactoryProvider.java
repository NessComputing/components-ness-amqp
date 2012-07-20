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

import java.net.URI;

import javax.annotation.Nonnull;

import com.google.common.base.Throwables;
import com.google.inject.Provider;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Provides a Connection factory for AMQP connections.
 */
final class AmqpFactoryProvider implements Provider<ConnectionFactory>
{
    private final AmqpConfig amqpConfig;

    AmqpFactoryProvider(@Nonnull final AmqpConfig amqpConfig)
    {
        this.amqpConfig = amqpConfig;
    }

    @Override
    public ConnectionFactory get()
    {
        final URI amqpUri = amqpConfig.getAmqpConnectionUrl();

        try {
            final ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setUri(amqpUri);
            return connectionFactory;
        }
        catch (Exception e) {
            Throwables.propagateIfPossible(e);
        }
        return null;
    }
}
