package com.example.attractionreservation.sample3;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TicketStatsController {

    private final TicketStatsService ticketStatsService;
    private final PartnerSyncClient partnerSyncClient;

    public TicketStatsController(TicketStatsService ticketStatsService, PartnerSyncClient partnerSyncClient) {
        this.ticketStatsService = ticketStatsService;
        this.partnerSyncClient = partnerSyncClient;
    }

    @RequestMapping("/api/sample3/cache/clear")
    public String clearCache() {
        ticketStatsService.clearCache();
        return "cleared";
    }

    @GetMapping("/api/sample3/tickets")
    public List<Ticket> tickets() {
        return ticketStatsService.getTickets();
    }

    @GetMapping("/api/sample3/sync")
    public String sync() {
        try {
            return partnerSyncClient.fetchTickets();
        } catch (Exception e) {
            return "sync failed: " + e;
        }
    }

    @GetMapping("/api/sample3/visitor")
    public boolean visitor(@RequestParam String name) {
        return ticketStatsService.isValidVisitorName(name);
    }
}
