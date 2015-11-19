/**
 * Tomitribe Confidential
 * <p/>
 * Copyright(c) Tomitribe Corporation. 2014
 * <p/>
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 * <p/>
 */
package org.apache.openejb.resource.activemq;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionFactoryWrapper implements ConnectionFactory, TopicConnectionFactory, QueueConnectionFactory {

    private static final ArrayList<ConnectionWrapper> connections = new ArrayList<ConnectionWrapper>();

    private final org.apache.activemq.ra.ActiveMQConnectionFactory factory;

    public ConnectionFactoryWrapper(final Object factory) {
        this.factory = org.apache.activemq.ra.ActiveMQConnectionFactory.class.cast(factory);
    }

    @Override
    public Connection createConnection() throws JMSException {
        return getConnection(this.factory.createConnection());
    }

    @Override
    public Connection createConnection(final String userName, final String password) throws JMSException {
        return getConnection(this.factory.createConnection(userName, password));
    }

    private static Connection getConnection(final Connection connection) {
        final ConnectionWrapper wrapper = new ConnectionWrapper(connection);
        connections.add(wrapper);
        return wrapper;
    }

    protected static void remove(final ConnectionWrapper connectionWrapper) {
        connections.remove(connectionWrapper);
    }

    public static void closeConnections() {
        final Iterator<ConnectionWrapper> iterator = connections.iterator();

        while (iterator.hasNext()) {
            final ConnectionWrapper next = iterator.next();
            iterator.remove();
            try {
                next.close();
            } catch (final Exception e) {
                //no-op
            } finally {
                Logger.getLogger(ConnectionFactoryWrapper.class.getName()).log(Level.SEVERE, "Closed a JMS connection. You have an application that fails to close this connection");
            }
        }
    }

    @Override
    public QueueConnection createQueueConnection() throws JMSException {
        return QueueConnection.class.cast(getConnection(this.factory.createQueueConnection()));
    }

    @Override
    public QueueConnection createQueueConnection(final String userName, final String password) throws JMSException {
        return QueueConnection.class.cast(getConnection(this.factory.createQueueConnection(userName, password)));
    }

    @Override
    public TopicConnection createTopicConnection() throws JMSException {
        return TopicConnection.class.cast(getConnection(this.factory.createTopicConnection()));
    }

    @Override
    public TopicConnection createTopicConnection(final String userName, final String password) throws JMSException {
        return TopicConnection.class.cast(getConnection(this.factory.createTopicConnection(userName, password)));
    }
}
