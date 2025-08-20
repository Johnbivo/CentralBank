package com.bivolaris.centralbank.dtos;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class FindByBankNameRequest {

    @NotNull
    @NotEmpty
    @Pattern(
            regexp = "^(?!.*(script|javascript|vbscript|onload|onerror|onclick|eval|expression|<|>|&lt;|&gt;|select\\s|insert\\s|update\\s|delete\\s|drop\\s|union\\s|exec\\s|--|;|\\||\\\\)).*$",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Input contains potentially dangerous characters or code"
    )
    private String name;
}
