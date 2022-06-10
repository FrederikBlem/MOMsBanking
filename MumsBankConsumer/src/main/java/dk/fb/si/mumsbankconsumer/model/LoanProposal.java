package dk.fb.si.mumsbankconsumer.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanProposal {
    private int loanNo;
    private String customerId;
    private String bankId;
    private String bankName;
    private String customerName;
    private String customerHonorific;
    private int loanAmount;
    private double loanInterest;
    private int paybackMonths;

    @Override
    public String toString() {
        return "{" +
                "loanNo=" + loanNo +
                ", customerId='" + customerId + '\'' +
                ", bankId='" + bankId + '\'' +
                ", bankName='" + bankName + '\'' +
                ", customerName='" + customerName + '\'' +
                ", customerHonorific='" + customerHonorific + '\'' +
                ", loanAmount=" + loanAmount +
                ", loanInterest=" + loanInterest +
                ", paybackMonths=" + paybackMonths +
                '}';
    }
}
