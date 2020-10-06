package com.solacesystems.jms

import com.solacesystems.jms.impl.Validator
import javax.jms.Connection
import javax.jms.QueueConnection


class CustomSolConnectionFactory : SolConnectionFactoryImpl() {

    override fun createConnection(): Connection {
        Validator.checkClientId(mBean.jndiClientID, mBean.clientID)
        return CustomSolConnection(mProperties, false)

    }

    override fun createConnection(userName: String?, password: String?): Connection {
        Validator.checkClientId(mBean.jndiClientID, mBean.clientID)
        return CustomSolConnection(mProperties, false, userName, password)
    }

    override fun createQueueConnection(): QueueConnection {
        Validator.checkClientId(mBean.jndiClientID, mBean.clientID)
        return CustomSolQueueConnection(mProperties, false)
    }

    override fun createQueueConnection(userName: String?, password: String?): QueueConnection {
        Validator.checkClientId(mBean.jndiClientID, mBean.clientID)
        return CustomSolQueueConnection(mProperties, false, userName, password)
    }


}
