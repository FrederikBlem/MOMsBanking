package dk.fb.si.momsloanproducer.controller;

import com.google.gson.Gson;
import dk.fb.si.momsloanproducer.model.*;
import dk.fb.si.momsloanproducer.service.*;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.InputStreamResource;
import org.springframework.beans.factory.annotation.*;

import javax.servlet.http.*;
import java.io.*;
import java.util.*;

@RestController
public class LoanController {

    private int loanNo;

    private ArrayList<LoanRequest>  requests = new ArrayList<>();
    private String OLSecretAccessCode = "bWluaXNraXJ0";
    private final String acceptOLTopic = "ol-loan-acceptance";

    @Autowired
    private LoanService loanService;

    @Autowired
    ContractHandler contractHandler;

    @PostMapping(value = "/loan-request")
    public String loanRequest(@RequestBody LoanRequest loanRequest) {
        loanService.sendLoanRequest(loanRequest);
        return "Loan Request Published: " + loanRequest.toString();
    }

    @GetMapping(value = "/loan-proposals")
    public List<LoanProposal> loanProposals() {
        return loanService.getProposals();
    }

    @PostMapping(value = "/accept-loan/{bankName}/{loanNo}")
    public String acceptLoan(@PathVariable("bankName") String bankName ,@PathVariable("loanNo") int loanNo) {
        if (bankName.trim().contains("Office Lady Bank")){
            loanService.sendOLLoanRequest(loanNo);
            contractHandler.connectQueue(this.loanNo);
            return "Direct acceptance of loan no longer available for the " + bankName + ". Customer details must be given to an employee and approved.";
        }
        loanService.sendLoanAcceptance(loanNo);
        this.loanNo = loanNo;
        contractHandler.connectQueue(this.loanNo);
        return "Loan acceptance sent for loan proposal with number: " + loanNo + " from " + bankName;
    }

    @GetMapping(value = "/contract/{loanNo}")
    public InputStreamResource downloadContract(HttpServletResponse response, @PathVariable int loanNo) {
        String dir = System.getProperty("user.dir");
        if (!dir.contains("MOMsLoanProducer")){
            dir = dir + "/MOMsLoanProducer";
        }
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"Contract-"+this.loanNo+".pdf\"");
        try {
            return new InputStreamResource(new FileInputStream(dir+"/src/main/resources/static/contracts/Contract-"+this.loanNo+".pdf"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @KafkaListener(topics = acceptOLTopic, groupId = "my-group")
    public String listenForOLProposal(String message){
        LoanOLProposal loanOLProposal = new Gson().fromJson(message, LoanOLProposal.class);
        if (loanOLProposal.getAccessCode() != OLSecretAccessCode)
        {
            return "Invalid access code.";
        }

        System.out.println("Got OL Proposal: " + loanOLProposal.toString());

        this.loanNo = loanOLProposal.getLoanNo();
        loanService.sendLoanAcceptance(loanNo);

        contractHandler.connectQueue(this.loanNo);
        return "Loan acceptance sent for loan proposal with number: " + loanNo;
    }
}
