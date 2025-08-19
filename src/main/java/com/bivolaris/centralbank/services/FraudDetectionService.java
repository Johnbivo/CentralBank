package com.bivolaris.centralbank.services;

import com.bivolaris.centralbank.entities.Employee;
import com.bivolaris.centralbank.entities.FraudStatus;
import com.bivolaris.centralbank.entities.Fraudcase;
import com.bivolaris.centralbank.entities.Transaction;

import java.util.List;

public interface FraudDetectionService {

    public boolean detectFraud(Transaction transaction);
    public Fraudcase flagTransactionForFraud(Transaction transaction, String reason);
    public boolean reviewFraudCase(String fraudCaseId, FraudStatus newStatus, Employee reviewedBy);
    public List<Fraudcase> getPendingFraudCases();
    public List<Fraudcase> getFraudCasesByStatus(FraudStatus status);


}
