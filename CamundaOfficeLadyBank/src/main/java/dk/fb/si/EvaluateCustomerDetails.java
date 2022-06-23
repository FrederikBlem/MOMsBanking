package dk.fb.si;

import dk.fb.si.model.LoanOLProposal;


import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import com.google.gson.Gson;
import org.json.JSONObject;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import okhttp3.*;
import org.springframework.http.HttpStatus;

import javax.inject.Named;

@Named
public class EvaluateCustomerDetails implements JavaDelegate {

    private String OLSecretAccessCode = "bWluaXNraXJ0";
    private final List<LoanOLProposal> proposals = new ArrayList<>();

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    KafkaTemplate<String, String> template;

    private final String requestOLTopic = "ol-loan-request";
    private final String acceptOLTopic = "ol-loan-acceptance";



    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        String email = "invalid";
        int loanNo = 0;

        email = (String) delegateExecution.getVariable("email");
        loanNo = (int) delegateExecution.getVariable("loanNo");

        ValidateEmail emailValidator = new ValidateEmail();

        if (emailValidator.isValidEmail(email)){
            // Call other service here
            System.out.println("Email validated!");

            String secretAcceptanceCode = "bWluaXNraXJ0";

            for (LoanOLProposal olProposal : proposals)
            {
                if (olProposal.getLoanNo() == loanNo){
                    SendOLLoanAccept(convertOLProposalToString(olProposal));
                }
            }

        }
        else{
            throw new BpmnError("Invalid_Details", "Your given email address is an invalid email address!");
        }
    }

    @KafkaListener(topics = requestOLTopic, groupId = "my-group")
    public LoanOLProposal listenForProposal(String message) {
        LoanOLProposal loanOLProposal = new Gson().fromJson(message, LoanOLProposal.class);
        proposals.add(loanOLProposal);
        return loanOLProposal;
    }

    public String convertOLProposalToString(LoanOLProposal loanProposal) {
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
            jsonProposal.put("accessCode", OLSecretAccessCode);
            return jsonProposal.toString();
        } catch (Exception ex) {
            System.out.println("Error converting the proposal to string");
            ex.printStackTrace();
        }
        return null;
    }

    public void SendOLLoanAccept(String OLProposalMessage) {
        try {
            template.send(acceptOLTopic, OLProposalMessage);
            template.flush();
            //logger.info("Sent request for OL loan offer with ID: " +loanNo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
