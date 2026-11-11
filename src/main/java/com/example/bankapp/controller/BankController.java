
package com.example.bankapp.controller;

import com.example.bankapp.dto.TransferRequest;
import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.model.Customer;
import com.example.bankapp.service.BankService;
import com.example.bankapp.service.TransactionAuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;
    private final TransactionAuditService transactionAuditService;

    @GetMapping("/")
    public String dashboard(
            @RequestParam(required = false) String keyword,
            Model model
    ) {
        prepareDashboard(model, keyword, new TransferRequest(), new Customer());
        return "bank";
    }

    @PostMapping("/transfer")
    public String transfer(
            @Valid @ModelAttribute("transferRequest") TransferRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepareDashboard(model, null, request, new Customer());
            return "bank";
        }

        try {
            bankService.transfer(
                    request.getFromAccount(),
                    request.getToAccount(),
                    request.getAmount(),
                    request.isSimulateFailure()
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Transfer completed successfully"
            );

        } catch (Exception exception) {

    redirectAttributes.addFlashAttribute(
            "error",
            exception.getMessage()
    );
}

        return "redirect:/";
    }

    @PostMapping("/customers")
    public String createCustomer(
            @ModelAttribute("customer") Customer customer,
            RedirectAttributes redirectAttributes
    ) {
        try {
            bankService.createCustomer(customer);
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Customer added successfully"
            );
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    exception.getMessage()
            );
        }

        return "redirect:/";
    }

    @GetMapping("/accounts/{no}/history")
    public String accountHistory(
            @PathVariable("no") String accountNumber,
            Model model
    ) {
        Customer customer = bankService.getCustomer(accountNumber);
        List<BankTransaction> transactions =
                bankService.getTransactionHistory(accountNumber);

        BigDecimal totalSent = transactions.stream()
                .filter(t -> t.getFromAccount().equals(accountNumber))
                .filter(t -> t.getStatus().name().equals("SUCCESS"))
                .map(BankTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("customer", customer);
        model.addAttribute("transactions", transactions);
        model.addAttribute("totalSent", totalSent);

        return "history";
    }

    private void prepareDashboard(
            Model model,
            String keyword,
            TransferRequest transferRequest,
            Customer customer
    ) {
        List<Customer> allCustomers = bankService.getAllCustomers();

        List<Customer> customers;

        if (keyword == null || keyword.isBlank()) {
            customers = allCustomers;
        } else {
            customers = bankService.searchCustomers(keyword);
        }

        model.addAttribute("allCustomers", allCustomers);
        model.addAttribute("customers", customers);
        model.addAttribute("transactions", bankService.getLatestTransactions());
        model.addAttribute("totalCustomers", allCustomers.size());
        model.addAttribute("totalBalance", bankService.getTotalBalance());
        model.addAttribute("successCount", bankService.getSuccessCount());
        model.addAttribute("failedCount", bankService.getFailedCount());
        model.addAttribute("transferRequest", transferRequest);
        model.addAttribute("customer", customer);
        model.addAttribute("keyword", keyword);
    }
}
