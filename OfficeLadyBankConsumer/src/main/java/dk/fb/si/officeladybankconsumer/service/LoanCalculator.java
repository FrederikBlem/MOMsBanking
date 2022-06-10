package dk.fb.si.officeladybankconsumer.service;


import org.springframework.stereotype.Service;
import dk.fb.si.officeladybankconsumer.model.*;
import java.util.Random;

@Service
public class LoanCalculator {

    Random random = new Random();

    public LoanProposal buildLoanProposal(LoanRequest loanRequest){
        LoanProposal loanProposal = new LoanProposal();

        // Get customer details from the request
        loanProposal.setCustomerId(loanRequest.getCustomerId());
        loanProposal.setCustomerName(loanRequest.getCustomerName());
        loanProposal.setCustomerHonorific(loanRequest.getCustomerHonorific());
        // The Bank details are set
        loanProposal.setBankName("Office Lady Bank");
        loanProposal.setBankId("VGhpZ2ggaGlnaHM=");

        loanProposal.setLoanNo(random.nextInt(89999998)+10000001);

        // Calculating the Loan Amount using the given request details
        int total = 0;

        int salaryMultiplier = 6;
        int debtMultiplier = 3;

        double carMultiplier = 0.9;
        double houseMultiplier = 2.4;

        total += (loanRequest.getYearlySalary()*salaryMultiplier)-(loanRequest.getDebtAmount()*debtMultiplier);
        if (loanRequest.isCarOwner()) {
            total = (int) (total*carMultiplier);
        }
        if (loanRequest.isHouseOwner()) {
            total = (int) (total*houseMultiplier);
        }
        loanProposal.setLoanAmount(total);

        // Calculating the Loan Interest using the given request details
        double divider = 115000.0;
        double interest = total / divider;
        // Reuse the multiplier variables.
        carMultiplier = 1.3;
        houseMultiplier = 0.9;

        if (loanRequest.isCarOwner()) {
            interest += carMultiplier;
        }
        if (loanRequest.isHouseOwner()) {
            interest -= houseMultiplier;
        }

        double roundedInterest = Math.round(interest * 100.0) / 100.0;
        loanProposal.setLoanInterest(roundedInterest);

        // Calculate payback months from the total and the interest.
        divider = 9600;
        loanProposal.setPaybackMonths((int) (total * interest) / (int)divider);

        return loanProposal;
    }
}
