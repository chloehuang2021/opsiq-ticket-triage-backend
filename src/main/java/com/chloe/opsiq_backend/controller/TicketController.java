package com.chloe.opsiq_backend.controller;

import com.chloe.opsiq_backend.model.Ticket;
import com.chloe.opsiq_backend.repository.TicketRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "http://localhost:4200")
public class TicketController {

    private final TicketRepository ticketRepository;

    public TicketController(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @PostMapping
    public Ticket createTicket(@RequestBody Ticket ticket) {
        return ticketRepository.save(ticket);
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

    @PostMapping("/analyze")
    public Ticket analyzeTicket(@RequestBody Ticket ticket) {
        String text = (ticket.getTitle() + " " + ticket.getDescription() + " " + ticket.getDepartment()).toLowerCase();

        if (text.contains("database") || text.contains("query") || text.contains("migration")) {
            ticket.setCategory("Database");
            ticket.setPriority("HIGH");
            ticket.setSummary("Potential database performance or migration-related issue.");
            ticket.setSuggestedSteps("Review indexes, check execution plans, and validate recent migration changes.");
        } else if (text.contains("login") || text.contains("access") || text.contains("permission")) {
            ticket.setCategory("Access");
            ticket.setPriority("MEDIUM");
            ticket.setSummary("Potential user access or permission issue.");
            ticket.setSuggestedSteps("Verify user permissions, role mappings, and authentication logs.");
        } else if (text.contains("report") || text.contains("dashboard") || text.contains("etl")) {
            ticket.setCategory("Data Pipeline");
            ticket.setPriority("HIGH");
            ticket.setSummary("Potential reporting or data pipeline issue.");
            ticket.setSuggestedSteps("Check ETL logs, refresh status, and downstream reporting tables.");
        } else {
            ticket.setCategory("General Support");
            ticket.setPriority("LOW");
            ticket.setSummary("General IT support request requiring review.");
            ticket.setSuggestedSteps("Gather more details, reproduce the issue, and route to the appropriate team.");
        }

        return ticket;
    }

}