package com.nesscomputing.amqp.rabbitmq;

import org.junit.Ignore;

import com.nesscomputing.amqp.AbstractTestQueueFactory;
import com.nesscomputing.amqp.AmqpProvider;
import com.nesscomputing.amqp.RabbitMQProvider;

@Ignore
public class TestQueueFactoryRabbitMQ extends AbstractTestQueueFactory
{
    private AmqpProvider provider = new RabbitMQProvider();

    @Override
    protected AmqpProvider getProvider()
    {
        return provider;
    }
}