package dk.fb.si.momsloanproducer.controller;

import dk.fb.si.momsloanproducer.model.*;
import dk.fb.si.momsloanproducer.service.*;

import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.InputStreamResource;
import org.springframework.beans.factory.annotation.*;

import javax.servlet.http.*;
import java.io.*;
import java.util.*;

@RestController
public class LoanController {

    private int loanNo;

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

    @PostMapping(value = "/accept-loan/{loanNo}")
    public String acceptLoan(@PathVariable int loanNo) {
        loanService.sendLoanAcceptance(loanNo);
        this.loanNo = loanNo;
        contractHandler.connectQueue(this.loanNo);
        return "Loan acceptance sent for loan proposal with number: " + loanNo;
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
}
