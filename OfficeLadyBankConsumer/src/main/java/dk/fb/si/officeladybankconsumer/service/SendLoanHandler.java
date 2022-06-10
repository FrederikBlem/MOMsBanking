package dk.fb.si.officeladybankconsumer.service;

import dk.fb.si.officeladybankconsumer.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import org.json.JSONObject;
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Service
public class SendLoanHandler {
    private final String topic = "loan-proposal";
    private final Logger logger = LoggerFactory.getLogger(SendLoanHandler.class);

    @Autowired
    KafkaTemplate<String, String> template;

    public void sendLoanProposal(LoanProposal loanProposal) {
        String proposal = convertProposalToString(loanProposal);
        template.send(topic, proposal);
        logger.info("Sent the Loan Proposal to Kafka.");
        template.flush();
    }

    public String convertProposalToString(LoanProposal loanProposal) {
        try {
            JSONObject jsonProposal = new JSONObject();
            jsonProposal.put("loanNo", loanProposal.getLoanNo());
            jsonProposal.put("customerId", loanProposal.getCustomerId());
            jsonProposal.put("customerName", loanProposal.getCustomerName());
            jsonProposal.put("customerHonorific", loanProposal.getCustomerHonorific());
            jsonProposal.put("bankId", loanProposal.getBankId());
            jsonProposal.put("bankName", loanProposal.getBankName());
            jsonProposal.put("loanAmount", loanProposal.getLoanAmount());
            jsonProposal.put("loanInterest", loanProposal.getLoanInterest());
            jsonProposal.put("paybackMonths", loanProposal.getPaybackMonths());
            return jsonProposal.toString();
        } catch (Exception ex) {
            System.out.println("Error converting the proposal to string");
            ex.printStackTrace();
        }
        return null;
    }
}
