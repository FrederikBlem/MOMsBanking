package dk.fb.si.momsloanproducer.service;

import dk.fb.si.momsloanproducer.model.*;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import com.google.gson.Gson;
import org.json.JSONObject;

@Service
public class LoanService {

    private final String requestTopic = "loan-request";
    private final String acceptTopic = "loan-acceptance";

    private final String requestOLTopic = "ol-loan-request";
    private final String acceptOLTopic = "ol-loan-accept";


    private final Logger logger = LoggerFactory.getLogger(LoanService.class);
    private final List<LoanProposal> proposals = new ArrayList<>();
    private int loanNo;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    KafkaTemplate<String, String> template;

    public void sendLoanRequest(LoanRequest loanRequest) {
        try {
            IdBuilder idBuilder = new IdBuilder();

            JSONObject request = new JSONObject();
            request.put("customerId", idBuilder.getId());
            request.put("customerName", loanRequest.getCustomerName());
            request.put("customerHonorific", loanRequest.getCustomerHonorific());
            request.put("yearlySalary", loanRequest.getYearlySalary());
            request.put("debtAmount", loanRequest.getDebtAmount());
            request.put("carOwner", loanRequest.isCarOwner());
            request.put("houseOwner", loanRequest.isHouseOwner());
            template.send(requestTopic, request.toString());
            logger.info("Sent Loan Request to Kafka - " + request.toString());
            template.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @KafkaListener(topics = "loan-proposal", groupId = "my-group")
    public LoanProposal listenForProposal(String message) {
        LoanProposal loanProposal = new Gson().fromJson(message, LoanProposal.class);
        logger.info("Received a loan proposal from " + loanProposal.getBankName() + ".");
        proposals.add(loanProposal);
        return loanProposal;
    }



    public List<LoanProposal> getProposals() {
        return proposals;
    }

    public void sendLoanAcceptance(int loanNo) {
        this.loanNo = loanNo;
        try {
            JSONObject request = new JSONObject();
            request.put("loanNo", loanNo);
            request.put("status", "accept");
            //template.send(acceptTopic, request.toString());
            template.send(acceptOLTopic, request.toString());
            logger.info("Sent acceptance for loan offer with ID: " +loanNo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendOLLoanRequest(int loanNo) {
        this.loanNo = loanNo;
        try {
            JSONObject request = new JSONObject();
            request.put("loanNo", loanNo);
            request.put("status", "request");
            template.send(requestOLTopic, request.toString());
            logger.info("Sent request for OL loan offer with ID: " +loanNo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
