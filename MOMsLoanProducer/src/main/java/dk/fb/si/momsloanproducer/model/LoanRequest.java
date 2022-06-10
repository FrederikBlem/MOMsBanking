package dk.fb.si.momsloanproducer.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanRequest {
    private String customerId;
    private String customerName;
    private String customerHonorific;
    private int yearlySalary;
    private int debtAmount;
    private boolean carOwner;
    private boolean houseOwner;
}