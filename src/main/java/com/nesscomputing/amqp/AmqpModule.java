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

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.rabbitmq.client.ConnectionFactory;

import com.nesscomputing.config.Config;
import com.nesscomputing.jackson.JsonMapper;
import com.nesscomputing.logging.Log;

/**
 * Provides access to AMQP queues and exchanges for the Ness platform.
 */
public class AmqpModule extends AbstractModule
{
    private static final Log LOG = Log.findLog();

    private final String connectionName;
    private final Config config;

    public AmqpModule(final Config config, @Nonnull final String connectionName)
    {
        Preconditions.checkNotNull(config, "The config must not be null!");
        Preconditions.checkArgument(connectionName != null, "the connection name can not be empty!");

        this.config = config;
        this.connectionName = connectionName;
    }

    @Override
    protected void configure()
    {
        final Named connectionNamed;
        final AmqpConfig amqpConfig;

        connectionNamed = Names.named(connectionName);

        bind(new TypeLiteral<PublisherCallback<Object>>() {}).annotatedWith(JsonMapper.class).to(JsonPublisherCallback.class).in(Scopes.SINGLETON);
        bind(new TypeLiteral<PublisherCallback<String>>() {}).to(StringPublisherCallback.class).in(Scopes.SINGLETON);

        amqpConfig = config.getBean(AmqpConfig.class, ImmutableMap.of("name", connectionName));
        bind(AmqpConfig.class).annotatedWith(connectionNamed).toInstance(amqpConfig);

        if (amqpConfig.isEnabled()) {
            LOG.info("Enabling AMQP for '%s'", connectionName);

            bind(ConnectionFactory.class).annotatedWith(connectionNamed).toProvider(new AmqpFactoryProvider(amqpConfig)).in(Scopes.SINGLETON);
            bind(AmqpRunnableFactory.class).annotatedWith(connectionNamed).toInstance(new AmqpRunnableFactory(connectionNamed));
        }
        else {
            LOG.info("Disabled AMQP for '%s'", connectionName);
        }
    }

    // NOTE: we intentionally check if the Config is the same, we consider it an error to install two
    // different modules unless the Config is precisely the same as well.

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (config == null ? 0 : config.hashCode());
        result = prime * result + (connectionName == null ? 0 : connectionName.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AmqpModule other = (AmqpModule) obj;
        if (config == null)
        {
            if (other.config != null) {
                return false;
            }
        }
        else if (!config.equals(other.config)) {
            return false;
        }
        if (connectionName == null)
        {
            if (other.connectionName != null) {
                return false;
            }
        }
        else if (!connectionName.equals(other.connectionName)) {
            return false;
        }
        return true;
    }
}
