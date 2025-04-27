package com.spring.boot.secure.banking.project;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts")
@Data
public class Account {

    @Id
    private String accountNumber;

    private String password;

    private String accountType;

    private double balance = 0.0;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> transactionHistory = new ArrayList<>();

    // Constructors
    public Account() {
    }

    public Account(String accountNumber, String password, String accountType) {
        this.accountNumber = accountNumber;
        this.password = password;
        this.accountType = accountType;
    }

    // Getters and Setters

    public boolean authenticate(String inputPassword) {
        return this.password.equals(inputPassword);
    }

    public void deposit(double amount) {
        this.balance += amount;
        transactionHistory.add("Deposited: $" + amount);
    }

    public void withdraw(double amount) {
        if (balance >= amount) {
            this.balance -= amount;
            transactionHistory.add("Withdrew: $" + amount);
        } else {
            transactionHistory.add("Failed Withdrawal Attempt: $" + amount);
        }
    }

    public void transfer(Account recipient, double amount) {
        if (balance >= amount) {
            this.withdraw(amount);
            recipient.deposit(amount);
            transactionHistory.add("Transferred $" + amount + " to " + recipient.getAccountNumber());
        } else {
            transactionHistory.add("Failed Transfer Attempt: $" + amount);
        }
    }

    public void applyInterest() {
        double interest = balance * 0.03;
        balance += interest;
        transactionHistory.add("Interest Applied: $" + interest);
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
        transactionHistory.add("Password changed");
    }

    // More getters/setters
}
