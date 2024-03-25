package org.acme.rabbitmq;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class RabbitMQProducer {

    @Inject
    @ConfigProperty(name = "rabbitmq.hostname")
    String hostname;

    @Inject
    @ConfigProperty(name = "rabbitmq.port")
    int port;

    @Inject
    @ConfigProperty(name = "rabbitmq.username")
    String username;

    @Inject
    @ConfigProperty(name = "rabbitmq.password")
    String password;

    private ConnectionFactory connectionFactory;
    private Connection connection;
    private Channel channel;

    @PostConstruct
    public void init() {
        try {
            connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(hostname);
            connectionFactory.setPort(port);
            connectionFactory.setUsername(username);
            connectionFactory.setPassword(password);
            connection = connectionFactory.newConnection();
            channel = connection.createChannel();
        } catch (Exception e) {
            throw new RuntimeException("Error initializing RabbitMQ", e);
        }
    }

    public void sendMessage(String message, String queueName) throws Exception {
        channel.queueDeclare(queueName, false, false, false, null);
        channel.basicPublish("", queueName, null, message.getBytes());
    }
}
