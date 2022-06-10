package dk.fb.si.momscontract;

import dk.fb.si.momscontract.model.ContractDetails;

import com.google.gson.Gson;
import com.itextpdf.text.*;

import com.rabbitmq.client.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication
public class MoMsContractApplication {

    private final static Logger logger = LoggerFactory.getLogger(MoMsContractApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(MoMsContractApplication.class, args);
        try {
            connectQueue();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void connectQueue()
    {
        // Same as the producer: tries to create a queue, if it wasn't already created
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        try {
            // Create connection.
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();

            // Register for a queue
            channel.queueDeclare("contract-queue", false, false, false, null);
            logger.info("Ready for incoming messages...");

            // Get notified, if a message for this receiver arrives
            DeliverCallback deliverCallback = (consumerTag, delivery) ->
            {
                String messageDelivery = new String(delivery.getBody(), StandardCharsets.UTF_8);
                logger.info("Received new contract data: " + messageDelivery);
                ContractDetails contractDetails = new Gson().fromJson(messageDelivery, ContractDetails.class);
                try {
                    //Create a contract in the form of PDF documents.
                    createContractDoc(contractDetails);
                    //Send the contract document file.
                    sendContractDoc(contractDetails.getLoanNo());
                    logger.info("Contract sent back to RabbitMQ with topic 'contract-delivery'");
                } catch (DocumentException e) {
                    e.printStackTrace();
                }
            };
            channel.basicConsume("contract-queue", true, deliverCallback, consumerTag -> {});
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void createContractDoc(ContractDetails contractDetails) throws DocumentException {
        Document contractDoc = new Document();
        try {
            String dir = System.getProperty("user.dir");
            if (!dir.contains("MOMsContract")){
                dir = dir + "/MOMsContract";
            }

            PdfWriter.getInstance(contractDoc, new FileOutputStream(new File(dir+"/src/main/resources/static/contracts/Contract-"+contractDetails.getLoanNo()+".pdf")));
            contractDoc.open();
            Font docFont = FontFactory.getFont(FontFactory.TIMES, 16, BaseColor.BLACK);
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Paragraph paragraph = new Paragraph(sdf.format(calendar.getTime()), docFont);
            contractDoc.add(paragraph);
            contractDoc.add(new Paragraph("\n\nDear " + contractDetails.getCustomerHonorific() + " " + contractDetails.getCustomerName(), docFont));
            contractDoc.add(new Paragraph("\nThis is a confirmation of your accepted loan.\n\nThe details of the loan can be viewed here:\n ", docFont));
            PdfPTable detailsTable = new PdfPTable(2);

            List<PdfPCell> detailCells = Arrays.asList(
                    new PdfPCell(new Phrase("Loan Amount: ", docFont)),
                    new PdfPCell(new Phrase(String.valueOf(contractDetails.getLoanAmount()) + " DKK", docFont)),
                    new PdfPCell(new Phrase("Loan Interest: ", docFont)),
                    new PdfPCell(new Phrase(String.valueOf(contractDetails.getLoanInterest()) + " %", docFont)),
                    new PdfPCell(new Phrase("Payback Months: ", docFont)),
                    new PdfPCell(new Phrase(String.valueOf(contractDetails.getPaybackMonths()) + " mo.", docFont)),
                    new PdfPCell(new Phrase("Monthly payment: ", docFont)),
                    new PdfPCell(new Phrase(String.valueOf(Math.round(contractDetails.getLoanAmount() / contractDetails.getPaybackMonths())) + " DKK", docFont))
            );

            for (PdfPCell detailCell: detailCells)
            {
                detailCell.setBorder(Rectangle.NO_BORDER);
                detailsTable.addCell(detailCell);
            }

            contractDoc.add(detailsTable);
            contractDoc.add(new Paragraph("\nGood luck with your financial endeavours!", docFont));
            contractDoc.add(new Paragraph("\nKind Regards", docFont));
            contractDoc.add(new Paragraph(contractDetails.getBankName(), docFont));
            switch (contractDetails.getBankName()) {
                case "Office Lady Bank": {
                    Image bankImage = Image.getInstance(dir + "/src/main/resources/static/img/OFFICE-LADY-BANK.jpg");
                    bankImage.scaleAbsolute(100, 50);
                    contractDoc.add(bankImage);
                    break;
                }
                case "Mums Bank": {
                    Image bankImage = Image.getInstance(dir + "/src/main/resources/static/img/MUMS-BANK.jpg");
                    bankImage.scaleAbsolute(100, 50);
                    contractDoc.add(bankImage);
                    break;
                }
                case "Mias Piggy Bank": {
                    Image bankImage = Image.getInstance(dir + "/src/main/resources/static/img/MIAS-PIGGY-BANK.jpg");
                    bankImage.scaleAbsolute(100, 50);
                    contractDoc.add(bankImage);
                    break;
                }
            }
            contractDoc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendContractDoc(int id) throws IOException {
        String dir = System.getProperty("user.dir");
        if (!dir.contains("MOMsContract")){
            dir = dir + "/MOMsContract";
        }
        File file = new File(dir+"/src/main/resources/static/contracts/Contract-"+id+".pdf");
        byte[] byteDocument = Files.readAllBytes(file.toPath());
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        String queueName = "contract-delivery";
        try {
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            channel.queueDeclare(queueName, false, false, false, null);
            channel.basicPublish("", queueName, null, byteDocument);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
