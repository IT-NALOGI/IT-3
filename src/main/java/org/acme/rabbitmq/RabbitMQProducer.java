package org.acme.rabbitmq;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.PreDestroy;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class RabbitMQProducer {

    private static final Logger LOGGER = Logger.getLogger(RabbitMQProducer.class.getName());

    @ConfigProperty(name = "rabbitmq.hostname")
    String hostname;

    @ConfigProperty(name = "rabbitmq.port")
    int port;

    @ConfigProperty(name = "rabbitmq.username")
    String username;

    @ConfigProperty(name = "rabbitmq.password")
    String password;

    private Channel channel;
    private Connection connection;

    @PostConstruct
    void init() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(hostname);
            factory.setPort(port);
            factory.setUsername(username);
            factory.setPassword(password);
            connection = factory.newConnection();
            channel = connection.createChannel();
        } catch (Exception e) {
            LOGGER.error("Error initializing RabbitMQ", e);
        }
    }

    public Uni<Void> sendMessage(String message, String queueName) {
        return Uni.createFrom().item(() -> {
            try {
                channel.queueDeclare(queueName, true, false, false, null);
                channel.basicPublish("", queueName, null, message.getBytes(StandardCharsets.UTF_8));
                LOGGER.infof("Sent message to RabbitMQ: %s", message);
            } catch (Exception e) {
                LOGGER.error("Failed to send message to RabbitMQ", e);
            }
            return null;
        });
    }

    @PreDestroy
    void close() {
        try {
            channel.close();
            connection.close();
        } catch (Exception e) {
            LOGGER.error("Error closing RabbitMQ connection", e);
        }
    }
}
