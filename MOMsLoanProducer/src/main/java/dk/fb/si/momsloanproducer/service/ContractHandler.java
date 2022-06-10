package dk.fb.si.momsloanproducer.service;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.*;

import java.io.*;
import java.lang.String;


@Service
public class ContractHandler {
    private static final Logger logger = LoggerFactory.getLogger(ContractHandler.class);
    byte[] aByte = new byte[1];
    int bytesRead;

    String dir = System.getProperty("user.dir");
    String fileStorageString = "/src/main/resources/static/contracts/Contract-";
    String currentId;

    public void connectQueue(int loanNo)
    {
        currentId = loanNo + ".pdf";
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        try {
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            // Register for a queue
            channel.queueDeclare("contract-delivery", false, false, false, null);
            logger.info("Ready for incoming contracts...");
            // Get notified, if a message for this receiver arrives
            DeliverCallback deliverCallback = (consumerTag, delivery) ->
            {
                byte[] message = delivery.getBody();
                logger.info("Received a new contract!");
                System.out.println("Converting from a byte Array into PDF...");
                InputStream inputStream = new ByteArrayInputStream(message);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                FileOutputStream fileOutputStream;
                BufferedOutputStream bufferedOutputStream;
                if (!dir.contains("MOMsLoanProducer")){
                    dir = dir + "/MOMsLoanProducer";
                }
                fileOutputStream = new FileOutputStream(dir+fileStorageString+currentId);
                bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                bytesRead = inputStream.read(aByte, 0, aByte.length);
                do {
                    byteArrayOutputStream.write(aByte);
                    bytesRead = inputStream.read(aByte);
                } while (bytesRead != -1);
                bufferedOutputStream.write(byteArrayOutputStream.toByteArray());
                bufferedOutputStream.flush();
                bufferedOutputStream.close();
                System.out.println("Successfully converted the byte array into a PDF document!");
            };
            channel.basicConsume("contract-delivery", true, deliverCallback, consumerTag -> {});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
