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

    private final String acceptOLTopic = "ol-loan-acceptance";

    private final Logger logger = LoggerFactory.getLogger(LoanRequestHandler.class);
    private final List<LoanOLProposal> proposals = new ArrayList<>();

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
            LoanOLProposal loanProposal = loanCalculator.buildLoanProposal(loanRequest);
            // Store the LoanProposal for later usage.
            proposals.add(loanProposal);

            logger.info("The proposals now include: " + loanProposal.toString());

            // Send the Loan Offer.
            sendLoanHandler.sendLoanProposal(loanProposal);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @KafkaListener(topics = acceptOLTopic, groupId = bankGroupAcceptId)
    public void listenForAcceptance(String message) {
        String secretAcceptanceCode = "bWluaXNraXJ0";
        LoanOLProposal loanOLProposal = new Gson().fromJson(message, LoanOLProposal.class);

        //Hope this check works!
        if (!loanOLProposal.getAccessCode().contains(secretAcceptanceCode)){
            logger.info("Invalid Acceptance code!");
        }

        logger.info("Loan acceptance received! " + loanOLProposal);

        LoanOLProposal loanProposal = getProposalById(loanOLProposal.getLoanNo());
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

    private LoanOLProposal getProposalById(int loanNo) {
        for(LoanOLProposal proposal: proposals){
            if (proposal.getLoanNo() == loanNo){
                return proposal;
            }
        }
        return null;
    }

    public String buildProposalString(LoanOLProposal loanOLProposal){
        return "{" +
                "loanNo=" + loanOLProposal.getLoanNo() +
                ", customerId='" + loanOLProposal.getCustomerId() + '\'' +
                ", bankId='" + loanOLProposal.getBankId() + '\'' +
                ", bankName='" + loanOLProposal.getBankName() + '\'' +
                ", customerName='" + loanOLProposal.getCustomerName() + '\'' +
                ", customerHonorific='" + loanOLProposal.getCustomerHonorific() + '\'' +
                ", loanAmount=" + loanOLProposal.getLoanAmount() +
                ", loanInterest=" + loanOLProposal.getLoanInterest() +
                ", paybackMonths=" + loanOLProposal.getPaybackMonths() +
                '}';
    }
}
