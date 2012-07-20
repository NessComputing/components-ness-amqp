package com.nesscomputing.amqp;

import java.io.IOException;

import com.google.common.base.Charsets;
import com.rabbitmq.client.AMQP.BasicProperties;

public class StringPublisherCallback implements PublisherCallback<String>
{
    private final BasicProperties props;

    StringPublisherCallback()
    {
        this.props = new BasicProperties.Builder()
        .contentType("text/plain")
        .deliveryMode(1)
        .priority(0)
        .build();
    }

    @Override
    public PublisherData publish(final String data) throws IOException
    {
        if (data != null) {
            return new PublisherData() {
                @Override
                public BasicProperties getProperties() {
                    return props;
                }

                @Override
                public byte [] getData() throws IOException {
                    return data.getBytes(Charsets.UTF_8);
                }
            };
        }

        return null;
    }
}
