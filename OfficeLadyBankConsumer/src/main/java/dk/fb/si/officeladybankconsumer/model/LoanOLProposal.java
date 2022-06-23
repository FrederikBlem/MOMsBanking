package dk.fb.si.officeladybankconsumer.model;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanOLProposal {
    private int loanNo;
    private String customerId;
    private String bankId;
    private String bankName;
    private String customerName;
    private String customerHonorific;
    private int loanAmount;
    private double loanInterest;
    private int paybackMonths;

    private String accessCode;

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
                ", accessCode=" + accessCode +
                '}';
    }
}
