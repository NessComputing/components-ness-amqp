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

import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.base.Preconditions;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;

/**
 * Converts arbitrary objects into JSON and returns a text message containing it.
 */
public final class JsonPublisherCallback implements PublisherCallback<Object>
{
    private ObjectMapper mapper = null;

    private final String name;
    private final BasicProperties props;

    JsonPublisherCallback(final String name, final ObjectMapper mapper)
    {
        this.name = name;
        this.mapper = mapper;

        this.props = new BasicProperties.Builder()
        .contentType("application/json")
        .deliveryMode(0)
        .priority(0)
        .build();
    }

    @Override
    public boolean publish(final Channel channel, final Object data) throws IOException
    {
        Preconditions.checkState(mapper != null, "need object mapper configured!");

        if (data != null) {
            final byte [] result = mapper.writeValueAsBytes(data);
            channel.basicPublish(name, null, props, result);
        }
        return true;
    }
}
