package com.chloe.opsiq_backend.repository;

import com.chloe.opsiq_backend.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
}