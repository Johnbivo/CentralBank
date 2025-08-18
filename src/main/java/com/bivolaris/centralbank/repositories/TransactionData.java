package com.bivolaris.centralbank.repositories;

import com.bivolaris.centralbank.entities.Account;
import com.bivolaris.centralbank.entities.Bank;

public interface TransactionData {
    Account getFromAccount();
    Account getToAccount();
    Bank getFromBank();
    Bank getToBank();
}
