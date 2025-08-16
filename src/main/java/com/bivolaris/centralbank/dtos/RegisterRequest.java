package com.bivolaris.centralbank.dtos;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotEmpty(message = "Username must not be empty.")
    private String username;
    @NotEmpty(message = "Password must not be empty.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must be at least 8 characters and include one uppercase letter, one lowercase letter, one number, and one special character."
    )
    private String password;

    @NotEmpty(message = "Confirm password must not be empty.")
    private String confirmPassword;

    @NotEmpty(message = "First name must not be empty.")
    private String firstName;

    @NotEmpty(message = "Last name must not be empty.")
    private String lastName;

    @NotEmpty(message = "Email must not be empty.")
    @Email
    private String email;
    @NotEmpty(message = "Phone number must not be empty.")
    private String phoneNumber;

    @NotEmpty(message = "Address must not be empty.")
    private String address;
    @NotEmpty(message = "City must not be empty.")
    private String city;
    @NotEmpty(message = "Profession must not be empty.")
    private String profession;

    @AssertTrue(message = "You must agree to the terms.")
    private boolean agreedToTerms;


    @AssertTrue(message = "Passwords do not match.")
    public boolean isPasswordsMatching() {
        if (password == null || confirmPassword == null) return false;
        return password.equals(confirmPassword);
    }
}
