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

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Stage;
import com.google.inject.name.Names;
import com.nesscomputing.config.Config;
import com.nesscomputing.config.ConfigModule;
import com.rabbitmq.client.ConnectionFactory;

public class TestAmqpModule
{
    @Test(expected=CreationException.class)
    public void testNamedNoEmptyUri()
    {
        Config config = Config.getFixedConfig(ImmutableMap.of("ness.amqp.enabled", "true"));

        Guice.createInjector(Stage.PRODUCTION,
                             new ConfigModule(config),
                             new AmqpModule(config, "test"));
    }

    @Test
    public void testNamedWorksGlobalDisabled()
    {
        final Config config = Config.getFixedConfig(ImmutableMap.of("ness.amqp.enabled", "false"));

        final Injector injector = Guice.createInjector(Stage.PRODUCTION,
                                                       new ConfigModule(config),
                                                       new AmqpModule(config, "test"));

        final AmqpConfig amqpConfig = injector.getInstance(Key.get(AmqpConfig.class, Names.named("test")));
        Assert.assertNotNull(amqpConfig);
        Assert.assertFalse(amqpConfig.isEnabled());
        Assert.assertNull(injector.getExistingBinding(Key.get(ConnectionFactory.class, Names.named("test"))));
    }

    @Test
    public void testNamedWorksLocalDisabled()
    {
        final Config config = Config.getFixedConfig(ImmutableMap.of("ness.amqp.enabled", "true",
                                                                    "ness.amqp.test.enabled", "false"));

        final Injector injector = Guice.createInjector(Stage.PRODUCTION,
                                                       new ConfigModule(config),
                                                       new AmqpModule(config, "test"));

        final AmqpConfig amqpConfig = injector.getInstance(Key.get(AmqpConfig.class, Names.named("test")));
        Assert.assertNotNull(amqpConfig);
        Assert.assertFalse(amqpConfig.isEnabled());
        Assert.assertNull(injector.getExistingBinding(Key.get(ConnectionFactory.class, Names.named("test"))));
    }

    @Test
    public void testNamedWorksWithGlobalUri()
    {
        final Config config = Config.getFixedConfig(ImmutableMap.of("ness.amqp.test.enabled", "true",
                                                                    "ness.amqp.connection-url", "amqp://localhost/"));

        final Injector injector = Guice.createInjector(Stage.PRODUCTION,
                                                       new ConfigModule(config),
                                                       new AmqpModule(config, "test"));

        final AmqpConfig amqpConfig = injector.getInstance(Key.get(AmqpConfig.class, Names.named("test")));
        Assert.assertNotNull(amqpConfig);
        Assert.assertTrue(amqpConfig.isEnabled());

        final ConnectionFactory factory = injector.getInstance(Key.get(ConnectionFactory.class, Names.named("test")));
        Assert.assertNotNull(factory);
    }

    @Test
    public void testNamedWorksWithLocalUri()
    {
        final Config config = Config.getFixedConfig(ImmutableMap.of("ness.amqp.test.enabled", "true",
                                                                    "ness.amqp.test.connection-url", "amqp://localhost/"));

        final Injector injector = Guice.createInjector(Stage.PRODUCTION,
                                                       new ConfigModule(config),
                                                       new AmqpModule(config, "test"));

        final AmqpConfig amqpConfig = injector.getInstance(Key.get(AmqpConfig.class, Names.named("test")));
        Assert.assertNotNull(amqpConfig);
        Assert.assertTrue(amqpConfig.isEnabled());

        final ConnectionFactory factory = injector.getInstance(Key.get(ConnectionFactory.class, Names.named("test")));
        Assert.assertNotNull(factory);
    }

    @Test(expected=CreationException.class)
    public void testNoEmptyUri()
    {
        final Config config = Config.getFixedConfig(ImmutableMap.of("ness.amqp.enabled", "true"));

        Guice.createInjector(Stage.PRODUCTION,
                             new ConfigModule(config),
                             new AmqpModule(config, "test"));
    }

    @Test
    public void testMultipleModules()
    {
        final Config config = Config.getFixedConfig(ImmutableMap.of("ness.amqp.enabled", "true",
                                                                    "ness.amqp.connection-url", "amqp://localhost/"));

        final Injector injector = Guice.createInjector(Stage.PRODUCTION,
                new ConfigModule(config),
                new AmqpModule(config, "test"),
                new AmqpModule(config, "test2"));

        final AmqpConfig AmqpConfig = injector.getInstance(Key.get(AmqpConfig.class, Names.named("test2")));
        Assert.assertNotNull(AmqpConfig);
        Assert.assertTrue(AmqpConfig.isEnabled());

        final ConnectionFactory factory = injector.getInstance(Key.get(ConnectionFactory.class, Names.named("test2")));
        Assert.assertNotNull(factory);

        Assert.assertNotNull(injector.getInstance(Key.get(ConnectionFactory.class, Names.named("test"))));
    }
}
