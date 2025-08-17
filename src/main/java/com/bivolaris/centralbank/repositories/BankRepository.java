package com.bivolaris.centralbank.repositories;


import com.bivolaris.centralbank.entities.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankRepository extends JpaRepository<Bank, UUID> {

    @Query("SELECT c FROM Bank c WHERE c.name = :bankName ")
    Optional<Bank> findByBankName(@Param("bankName") String bankName);
}
