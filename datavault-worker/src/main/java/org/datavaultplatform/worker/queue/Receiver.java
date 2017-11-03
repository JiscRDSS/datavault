package org.datavaultplatform.worker.queue;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.TimeoutException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;

import org.datavaultplatform.common.task.Task;
import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.worker.WorkerInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ?
 *
 */
public class Receiver {

    private static final Logger logger = LoggerFactory.getLogger(Receiver.class);
    
    private String queueServer;
    private String queueName;
    private String queueUser;
    private String queuePassword;
    private String tempDir;
    private String metaDir;

    /**
     * Set the queue server
     * @param queueServer
     */
    public void setQueueServer(String queueServer) {
        this.queueServer = queueServer;
    }

    /**
     * Set the queue name
     * @param queueName
     */
    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    /**
     * Set the queue user
     * @param queueUser
     */
    public void setQueueUser(String queueUser) {
        this.queueUser = queueUser;
    }

    /**
     * Set the queue password
     * @param queuePassword
     */
    public void setQueuePassword(String queuePassword) {
        this.queuePassword = queuePassword;
    }

    /**
     * Set the temp dir
     * @param tempDir
     */
    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }
    
    /**
     * Set the meta dir
     * @param metaDir
     */
    public void setMetaDir(String metaDir) {
        this.metaDir = metaDir;
    }

    /**
     * 
     * @param events an EventSender object
     * @throws IOException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    public void receive(EventSender events) throws IOException, InterruptedException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(queueServer);
        factory.setUsername(queueUser);
        factory.setPassword(queuePassword);
        
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(queueName, false, false, false, null);
        logger.info("Waiting for messages");
        
        QueueingConsumer consumer = new QueueingConsumer(channel);

        channel.basicQos(1);
        channel.basicConsume(queueName, false, consumer);

        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            
            // Note that the message body might contain keys/credentials
            logger.info("Received " + message.length() + " bytes");
            logger.info("Received message body '" + message + "'");
            
            // Decode and begin the job ...
            
            try {
                ObjectMapper mapper = new ObjectMapper();
                Task commonTask = mapper.readValue(message, Task.class);
                
                Class<?> clazz = Class.forName(commonTask.getTaskClass());
                Task concreteTask = (Task)(mapper.readValue(message, clazz));

                // Is the message a redelivery?
                if (delivery.getEnvelope().isRedeliver()) {
                    concreteTask.setIsRedeliver(true);
                }

                // Set up the worker temporary directory
                Path tempDirPath = Paths.get(tempDir, WorkerInstance.getWorkerName());
                tempDirPath.toFile().mkdir();
                
                Path metaDirPath = Paths.get(metaDir);
                
                Context context = new Context(tempDirPath, metaDirPath, events);
                concreteTask.performAction(context);
                
                // Clean up the temporary directory
                FileUtils.deleteDirectory(tempDirPath.toFile());
                
            } catch (Exception e) {
                logger.error("Error decoding message", e);
            }

            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }
        
        // Unreachable - the receiver never terminates
        // channel.close();
        // connection.close();
    }
}