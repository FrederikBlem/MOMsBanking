package dk.fb.si;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import javax.inject.Named;

@Named
public class EvaluateCustomerDetails implements JavaDelegate {
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

            //String secretAcceptanceCode = "bWluaXNraXJ0";


        }
        else{
            throw new BpmnError("Invalid_Details", "Your given email address is an invalid email address!");
        }
    }
}
