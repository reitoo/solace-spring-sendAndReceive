package com.solacesystems.jms

import com.solacesystems.jms.property.JMSProperties

class CustomSolConnection(
    properties: JMSProperties, connectionTypeXA: Boolean, userName: String? = null, password: String? = null
) : SolConnection(properties, connectionTypeXA, userName, password) {

    override fun deleteTemporaryQueue(queue: SolTemporaryQueueIF) {
        // ignore for performance reason. Solace will delete the tmp queue when the session is closed
    }
}

class CustomSolQueueConnection(
    properties: JMSProperties, connectionTypeXA: Boolean, userName: String? = null, password: String? = null
) : SolQueueConnection(properties, connectionTypeXA, userName, password) {

    override fun deleteTemporaryQueue(queue: SolTemporaryQueueIF) {
        // ignore for performance reason. Solace will delete the tmp queue when the session is closed
    }
}
