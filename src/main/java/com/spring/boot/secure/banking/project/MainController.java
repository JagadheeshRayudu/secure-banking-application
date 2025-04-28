package com.spring.boot.secure.banking.project;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MainController {

    private final AccountService accountService;
    private final String ADMIN_PASSWORD = "admin123";

    private Account loggedInAccount = null;
    private final AccountRepository accountRepository;


    public MainController(AccountService accountService, AccountRepository accountRepository) {
        this.accountService = accountService;
        this.accountRepository = accountRepository;
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/create-account")
    public String createAccountForm(Model model) {
        model.addAttribute("account", new Account());
        return "create-account";
    }

    @PostMapping("/create-account")
    public String createAccount(@ModelAttribute Account account, Model model) {
        if (accountService.getAccount(account.getAccountNumber()).isPresent()) {
            model.addAttribute("error", "Account already exists!");
            return "create-account";
        }
        accountService.createAccount(account);
        return "redirect:/";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String accountNumber, @RequestParam String password, Model model) {
        return accountService.getAccount(accountNumber)
                .filter(acc -> acc.authenticate(password))
                .map(acc -> {
                    loggedInAccount = acc;
                    model.addAttribute("account", acc);
                    return "account-menu";
                })
                .orElseGet(() -> {
                    model.addAttribute("error", "Invalid Credentials!");
                    return "login";
                });
    }

    @GetMapping("/admin")
    public String adminForm() {
        return "admin-login";
    }

    @PostMapping("/admin")
    public String adminLogin(@RequestParam String password, Model model) {
        if (ADMIN_PASSWORD.equals(password)) {
            model.addAttribute("accounts", accountService.getAllAccounts());
            return "admin-dashboard";
        } else {
            model.addAttribute("error", "Incorrect Admin Password");
            return "admin-login";
        }
    }

    @GetMapping("/account-menu")
    public String accountMenu(Model model) {
        model.addAttribute("account", loggedInAccount);
        return "account-menu";
    }

    @GetMapping("/deposit")
    public String depositForm() {
        return "deposit";
    }

    @PostMapping("/deposit")
    public String deposit(@RequestParam double amount, Model model) {
        loggedInAccount.deposit(amount);
        accountService.createAccount(loggedInAccount); // Save update
        model.addAttribute("account", loggedInAccount);
        return "account-menu";
    }

    @GetMapping("/withdraw")
    public String withdrawForm() {
        return "withdraw";
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestParam double amount, Model model) {
        loggedInAccount.withdraw(amount);
        accountService.createAccount(loggedInAccount); // Save update
        model.addAttribute("account", loggedInAccount);
        return "account-menu";
    }

    @GetMapping("/change-password")
    public String changePasswordForm() {
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String newPassword, Model model) {
        loggedInAccount.changePassword(newPassword);
        accountService.createAccount(loggedInAccount); // Save update
        model.addAttribute("account", loggedInAccount);
        return "account-menu";
    }

    @GetMapping("/transaction-history")
    public String transactionHistory(Model model) {
        model.addAttribute("transactions", loggedInAccount.getTransactionHistory());
        return "transaction-history";
    }

    @GetMapping("/transfer")
    public String showTransferPage(Model model) {
        model.addAttribute("recipientAccountNumber", "");
        model.addAttribute("amount", 0);
        return "transfer";
    }

    @PostMapping("/transfer")
    public String transferMoney(
            @RequestParam("recipientAccountNumber") String recipientAccountNumber,
            @RequestParam("amount") double amount,
            Model model
    ) {
        // Assuming currentAccount is fetched from session or login
        Account currentAccount = getCurrentLoggedInAccount();

        Account recipient = accountRepository.findByAccountNumber(recipientAccountNumber);
        if (recipient == null) {
            model.addAttribute("error", "Recipient account not found!");
            return "transfer";
        }

        if (currentAccount.getBalance() >= amount) {
            currentAccount.transfer(recipient, amount);
            accountRepository.save(currentAccount);
            accountRepository.save(recipient);
            model.addAttribute("success", "Transfer successful!");
        } else {
            model.addAttribute("error", "Insufficient balance!");
        }

        return "transfer";
    }

    @GetMapping("/apply-interest")
    public String applyInterest(Model model) {
        Account currentAccount = getCurrentLoggedInAccount();

        currentAccount.applyInterest();
        accountRepository.save(currentAccount);

        model.addAttribute("message", "Interest applied successfully!");
        return "apply-interest";
    }

    // Helper method (pseudo) - you should have an actual login mechanism
    private Account getCurrentLoggedInAccount() {
        // Fetch logged-in account from session or context
        return loggedInAccount;
    }

}


