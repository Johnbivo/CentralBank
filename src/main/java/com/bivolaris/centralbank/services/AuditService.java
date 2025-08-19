package com.bivolaris.centralbank.services;

import com.bivolaris.centralbank.entities.Bank;
import jakarta.servlet.http.HttpServletRequest;

public interface AuditService {

    public void logEmployeeAction(Long authId, String action);
    public void logBankAction(Bank bank, String action);
    public void logEmployeeBankAction(Long authId, Bank bank, String action);
    public String getClientIpAddress(HttpServletRequest request);
    public void logTransactionAction(Long authId, String action, String fromAccount, String toAccount, String amount);
    public void logAccountAction(Long authId, String action, String accountInfo, String customerInfo);
    public void logSafeAction(Long authId, String action, String... parameters);
    public void logSecureTransactionAction(Long authId, String action, String requestHash);


}
