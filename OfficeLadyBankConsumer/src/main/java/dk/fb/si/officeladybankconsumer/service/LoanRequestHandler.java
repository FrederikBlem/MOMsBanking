package dk.fb.si.officeladybankconsumer.service;

import dk.fb.si.officeladybankconsumer.model.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.rabbitmq.client.*;

@Service
public class LoanRequestHandler {

    private final String bankGroupRequestId = "request-officeladybank";
    private final String bankGroupAcceptId = "accept-officeladybank";

    private final Logger logger = LoggerFactory.getLogger(LoanRequestHandler.class);
    private final List<LoanProposal> proposals = new ArrayList<>();

    @Autowired
    LoanCalculator loanCalculator = new LoanCalculator();
    @Autowired
    SendLoanHandler sendLoanHandler = new SendLoanHandler();

    @KafkaListener(topics = "loan-request", groupId = bankGroupRequestId)
    public void listenForRequests(String message) {
        try {
            // Convert the message into a LoanRequest object from the json format.
            LoanRequest loanRequest = new Gson().fromJson(message, LoanRequest.class);
            // Build a Loan Proposal using the LoanRequest object.
            LoanProposal loanProposal = loanCalculator.buildLoanProposal(loanRequest);
            // Store the LoanProposal for later usage.
            proposals.add(loanProposal);
            // Send the Loan Offer.
            sendLoanHandler.sendLoanProposal(loanProposal);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @KafkaListener(topics = "loan-acceptance", groupId = bankGroupAcceptId)
    public void listenForAcceptance(String message) {
        /*
        String secretAcceptanceCode = "bWluaXNraXJ0";
        if(message.endsWith(secretAcceptanceCode)){
            System.out.println("Acceptance code accepted.");
        }
        */
        logger.info("Loan acceptance received! " + message);
        LoanProposal loanProposal = getProposalById(Integer.parseInt(message.substring(10,18)));
        if (loanProposal != null) {
            logger.info("The loan was found in our databases!");
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost("localhost");
            String queueName = "contract-queue";
            try {
                Connection connection = connectionFactory.newConnection();
                Channel channel = connection.createChannel();
                channel.queueDeclare(queueName, false, false, false, null);
                channel.basicPublish("", queueName, null, loanProposal.toString().getBytes(StandardCharsets.UTF_8));
                logger.info("Sent the data to the contract handler.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            logger.info("The loan was not found in our databases.");
        }
    }

    private LoanProposal getProposalById(int loanNo) {
        for(LoanProposal proposal: proposals){
            if (proposal.getLoanNo() == loanNo){
                return proposal;
            }
        }
        return null;
    }
}
