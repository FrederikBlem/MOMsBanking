# System Integration Exam Assignment & Mini Project 3

MOMsBanking is a project centered on fulfilling the requirements for the subject System Integration's Mini Project 3 and later the Exam Assignment.
The online repo is to be found at [https://github.com/FrederikBlem/MOMsBanking/](https://github.com/FrederikBlem/MOMsBanking/)

## Assignment Documents

[Mini-Project 3 Assignment Doc](https://github.com/FrederikBlem/MOMsBanking/blob/main/AssignmentDocs/A9-MOM.pdf)

[Exam Assignment Doc](https://github.com/FrederikBlem/MOMsBanking/blob/main/AssignmentDocs/SI2021ExamProject.pdf)

## Project Description (for the mini project)

The message oriented middleware this application uses to send data between client and server are Kafka and RabbitMQ.

This project is going with the theme of a small collaborative bank run by mothers and women in general inspired by the Message Oriented Middleware acronym (MOM).

1. MOMsLoanProducer will send a loan request through Kafka and the three banks listen for it, simulating a customer.

2. When the banks(OfficeLadyBankConsumer, MumsBankConsumer, MiasPiggyBankConsumer) receive the loan request with some important details from the customer, they will first calculate a proposal and then send back a proposal with a loan amount, interest rate and so on.

3. The customer can then accept the loan by using the loan number (loanNo) from the loan proposals.

4. The chosen loan's bank receives the message that the loan with the given number is accepted and then sends the details to a contract handler (MOMsContract). The contract handler takes the details of the loan and print them on a document of pdf format as a confirmation file the customer can download.

### Application flow (for the mini project)

1. Start your local Kafka Server and Zookeeper Server on their default ports by navigating to the kafka directory in command prompt and inputting the commands:

```text
bin\windows\zookeeper-server-start.bat config\zookeeper.properties
```

and

```text
bin\windows\kafka-server-start.bat config\server.properties
```

Note that you leave out '\windows\' from the path on other operating systems and change the file ending from '.bat' to '.sh'.

2. Start your local RabbitMQ service on its default port. View [http://localhost:15672/](http://localhost:15672/) and login with default login details 'guest' and 'guest' if curious.

3. Start the 5 different applications' main methods:
- MOMsContract
- OfficeLadyBankConsumer
- MumsBankConsumer
- MiasPiggyBankConsumer
- MOMsLoanProducer
4. From Postman, make a Post-Request to **localhost:8085/loan-request**
- The header "Content-Type = application/json" is required
- This format is needed for a loan request:

```JSON
{
    "customerName": "Lisa Neetz-Brazes",
    "customerHonorific": "Ms.",
    "yearlySalary": "300000",
    "debtAmount": "40000",
    "carOwner": false,
    "houseOwner": true
}
```

5. Make a GetRequest to **localhost:8085/loan-proposals** for the incoming loan proposals from each of the banks. You can select one proposal by copying the loanNo.

6. Make a PostRequest at **localhost:8085/accept-loan/{loanNo}** with the loanNo from the last step.

7. Go to an internet browser and enter **localhost:8085/contract/{loanNo}** in the address bar. A Loan Contract with confirmation of the loan's details should be downloadable.

## Exam Assignment Plans (TODO)

The explanation video can be found in the [ExplanationsAndDiagrams folder](https://github.com/FrederikBlem/MOMsBanking/tree/main/ExplanationsAndDiagrams)

![](https://raw.githubusercontent.com/FrederikBlem/MOMsBanking/main/ExplanationsAndDiagrams/Diagram1.PNG)
The above diagram is the system at its current state as of 13/06 while the one below is a (tentative) sketch for an extended system with (most of) the requirements implemented.
![](https://raw.githubusercontent.com/FrederikBlem/MOMsBanking/main/ExplanationsAndDiagrams/Diagram1.PNG)
