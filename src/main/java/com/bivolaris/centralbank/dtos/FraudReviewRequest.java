package com.bivolaris.centralbank.dtos;

import com.bivolaris.centralbank.entities.FraudStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FraudReviewRequest {
    private String fraudCaseId;
    private FraudStatus decision;
    private String reviewComments;
}
