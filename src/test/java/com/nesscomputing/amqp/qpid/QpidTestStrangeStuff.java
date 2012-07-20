package com.nesscomputing.amqp.qpid;

import com.nesscomputing.amqp.AbstractTestStrangeStuff;
import com.nesscomputing.amqp.AmqpProvider;
import com.nesscomputing.amqp.QPidProvider;

public class QpidTestStrangeStuff extends AbstractTestStrangeStuff
{
    private AmqpProvider provider = new QPidProvider();

    @Override
    protected AmqpProvider getProvider()
    {
        return provider;
    }
}
