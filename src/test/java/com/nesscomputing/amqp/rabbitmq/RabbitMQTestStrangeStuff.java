package com.nesscomputing.amqp.rabbitmq;

import com.nesscomputing.amqp.AbstractTestStrangeStuff;
import com.nesscomputing.amqp.AmqpProvider;
import com.nesscomputing.amqp.RabbitMQProvider;

public class RabbitMQTestStrangeStuff extends AbstractTestStrangeStuff
{
    private AmqpProvider provider = new RabbitMQProvider();

    @Override
    protected AmqpProvider getProvider()
    {
        return provider;
    }
}
