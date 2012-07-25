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

import org.skife.config.Config;
import org.skife.config.Default;
import org.skife.config.DefaultNull;
import org.skife.config.TimeSpan;

/**
 * Hold configuration parameters for the AMQP infrastructure.
 */
public abstract class AmqpConfig
{
    /**
     * Enable or disable an AMQP connection.
     */
    @Config({"ness.amqp.${name}.enabled", "ness.amqp.enabled"})
    @Default("true")
    public boolean isEnabled()
    {
        return true;
    }

    /**
     * Whether the transmitter connects immediately or lazy.
     */
    @Config({"ness.amqp.${name}.lazy-transmitter-connect", "ness.amqp.lazy-transmitter-connect"})
    @Default("true")
    public boolean isLazyTransmitterConnect()
    {
        return true;
    }

    /**
     * Connection URL for AMQP.
     */
    @Config({"ness.amqp.${name}.connection-url", "ness.amqp.connection-url"})
    @DefaultNull
    public URI getAmqpConnectionUrl()
    {
        return null;
    }

    @Config({"ness.amqp.${name}.exchange-type", "ness.amqp.exchange-type"})
    @Default("fanout")
    public String getExchangeType()
    {
        return "fanout";
    }

    @Config({"ness.amqp.${name}.routing-key", "ness.amqp.routing-key"})
    @Default("default")
    public String getRoutingKey()
    {
        return "default";
    }

    /**
     * If the broker is connected, the message callback will be called after this many millis, no matter whether a message was
     * received or not.
     */
    @Config({"ness.amqp.${name}.tick-timeout", "ness.amqp.tick-timeout"})
    @Default("5s")
    public TimeSpan getTickTimeout()
    {
        return new TimeSpan("5s");
    }

    /**
     * If a problem occurs, the backoff delay calculated with this delay.
     */
    @Config({"ness.amqp.${name}.backoff-delay", "ness.amqp.backoff-delay"})
    @Default("3s")
    public TimeSpan getBackoffDelay()
    {
        return new TimeSpan("3s");
    }

    /**
     * maximum shift factor (2^1 .. 2^x) for the exponential backoff.
     */
    @Config({"ness.amqp.${name}.max-backoff-factor", "ness.amqp.max-backoff-factor"})
    @Default("6")
    public int getMaxBackoffFactor()
    {
        return 6;
    }

    /**
     * Internal queue length for the publisher thread.
     */
    @Config({"ness.amqp.${name}.publisher-queue-length", "ness.amqp.publisher-queue-length"})
    @Default("20")
    public int getPublisherQueueLength()
    {
        return 20;
    }

    /**
     * Maximum amount of time that the transmitter tries to
     * enqueue an event onto the exchange or queue before it
     * gives up and drops the event.
     */
    @Config({"ness.amqp.${name}.transmit-timeout", "ness.amqp.transmit-timeout"})
    @Default("10ms")
    public TimeSpan getTransmitTimeout()
    {
        return new TimeSpan("10ms");
    }

    /**
     * Should the AMQP connector explicitly declare the exchange or queue it is connecting to.
     * 
     * Set to false if the exchange or queue on the Broker is known to exist.
     */
    @Config({"ness.amqp.${name}.declaring", "ness.amqp.declaring"})
    @Default("true")
    public boolean isDeclaring()
    {
        return true;
    }

    /**
     * Declare a queue as exclusive.
     */
    @Config({"ness.amqp.${name}.exclusive", "ness.amqp.exclusive"})
    @Default("false")
    public boolean isExclusive()
    {
        return false;
    }

    /**
     * Declare the exchange or queue as durable.
     */
    @Config({"ness.amqp.${name}.durable", "ness.amqp.durable"})
    @Default("false")
    public boolean isDurable()
    {
        return false;
    }

    /**
     * Declare the exchange or queue as auto-deleting.
     */
    @Config({"ness.amqp.${name}.auto-delete", "ness.amqp.auto-delete"})
    @Default("false")
    public boolean isAutoDelete()
    {
        return false;
    }
}
