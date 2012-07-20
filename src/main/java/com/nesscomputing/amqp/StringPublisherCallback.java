package com.nesscomputing.amqp;

import java.io.IOException;

import com.google.common.base.Charsets;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;

public class StringPublisherCallback implements PublisherCallback<String>
{
    private final String name;
    private final BasicProperties props;

    StringPublisherCallback(final String name)
    {
        this.name = name;

        this.props = new BasicProperties.Builder()
        .contentType("text/plain")
        .deliveryMode(0)
        .priority(0)
        .build();
    }

    @Override
    public boolean publish(final Channel channel, final String data) throws IOException
    {
        if (data != null) {
            channel.basicPublish(name, null, props, data.getBytes(Charsets.UTF_8));
        }
        return true;
    }
}
