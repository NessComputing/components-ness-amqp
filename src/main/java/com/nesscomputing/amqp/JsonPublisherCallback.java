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
import com.google.inject.Inject;
import com.nesscomputing.jackson.Json;
import com.rabbitmq.client.AMQP.BasicProperties;

/**
 * Converts arbitrary objects into JSON and returns a text message containing it.
 */
public final class JsonPublisherCallback implements PublisherCallback<Object>
{
    private ObjectMapper mapper = null;

    private final BasicProperties props;

    @Inject
    JsonPublisherCallback(@Json final ObjectMapper mapper)
    {
        this.mapper = mapper;

        this.props = new BasicProperties.Builder()
        .contentType("application/json")
        .deliveryMode(1)
        .priority(0)
        .build();
    }

    @Override
    public PublisherData publish(final Object data) throws IOException
    {
        Preconditions.checkState(mapper != null, "need object mapper configured!");

        if (data != null) {
            return new PublisherData() {
                @Override
                public BasicProperties getProperties() {
                    return props;
                }

                @Override
                public byte [] getData() throws IOException {
                    return mapper.writeValueAsBytes(data);
                }
            };
        }
        else {
            return null;
        }
    }
}
