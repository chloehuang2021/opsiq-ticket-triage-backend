package com.chloe.opsiq_backend.controller;

import com.chloe.opsiq_backend.dto.AiAnalysisResponse;
import com.chloe.opsiq_backend.model.Ticket;
import com.chloe.opsiq_backend.repository.TicketRepository;
import com.chloe.opsiq_backend.service.OpenRouterService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
//Local & production frontend
@CrossOrigin(origins = {
        "http://localhost:4200",
        "https://opsiq-ticket-triage.onrender.com"
})
public class TicketController {

    private final TicketRepository ticketRepository;
    private final OpenRouterService openRouterService;

    public TicketController(TicketRepository ticketRepository,
                            OpenRouterService openRouterService) {
        this.ticketRepository = ticketRepository;
        this.openRouterService = openRouterService;
    }

    @PostMapping
    public Ticket createTicket(@RequestBody Ticket ticket) {

        try {
            AiAnalysisResponse aiResult = openRouterService.analyzeTicket(
                    ticket.getTitle(),
                    ticket.getDescription()
            );

            ticket.setCategory(aiResult.category());
            ticket.setPriority(aiResult.priority());
            ticket.setSummary(aiResult.summary());
            ticket.setSuggestedSteps(String.join("\n", aiResult.suggestedSteps()));

        } catch (Exception exception) {
            applyManualReviewFallback(ticket);
        }

        return ticketRepository.save(ticket);
    }

    private void applyManualReviewFallback(Ticket ticket) {
        ticket.setCategory("Needs Review");
        ticket.setPriority("Medium");
        ticket.setSummary("AI analysis was unavailable. This ticket should be reviewed manually.");
        ticket.setSuggestedSteps("""
            Review the ticket details manually.
            Confirm the affected system or department.
            Assign the ticket to the appropriate support team.
            """);
    }

    @GetMapping
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    @GetMapping("/{id}")
    public Ticket getTicketById(@PathVariable Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
    }

    @PatchMapping("/{id}/status")
    public Ticket updateTicketStatus(@PathVariable Long id, @RequestBody Ticket updatedTicket) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        ticket.setStatus(updatedTicket.getStatus());

        return ticketRepository.save(ticket);
    }

    @DeleteMapping("/{id}")
    public String deleteTicket(@PathVariable Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        ticketRepository.delete(ticket);

        return "Ticket deleted successfully";
    }
}