package com.nesscomputing.amqp.qpid;

import com.nesscomputing.amqp.AbstractTestQueueFactory;
import com.nesscomputing.amqp.AmqpProvider;
import com.nesscomputing.amqp.QPidProvider;

public class TestQueueFactoryQPid extends AbstractTestQueueFactory
{
    private AmqpProvider provider = new QPidProvider();

    @Override
    protected AmqpProvider getProvider()
    {
        return provider;
    }
}
