package dk.fb.si.momscontract.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractDetails {

    private int loanNo;
    private String customerId;
    private String customerName;
    private String customerHonorific;
    private String bankName;
    private int loanAmount;
    private double loanInterest;
    private int paybackMonths;

    @Override
    public String toString() {
        return "ContractDetails{" +
                "loanNo=" + loanNo +
                ", customerId='" + customerId + '\'' +
                ", customerName='" + customerName + '\'' +
                ", customerHonorific='" + customerHonorific + '\'' +
                ", bankName='" + bankName + '\'' +
                ", loanAmount=" + loanAmount +
                ", loanInterest=" + loanInterest +
                ", paybackMonths=" + paybackMonths +
                '}';
    }
}

