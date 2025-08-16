package com.bivolaris.centralbank.repositories;


import com.bivolaris.centralbank.entities.Fraudcase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FraudCaseRepository extends JpaRepository<Fraudcase, UUID> {
}
