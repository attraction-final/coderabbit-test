package com.example.attractionreservation.sample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/sample")
public class SampleReservationController {

    @Autowired
    private SampleReservationService sampleReservationService;

    @GetMapping("/attractions")
    public List<Map<String, Object>> search(@RequestParam String name, @RequestParam(defaultValue = "id") String sort) {
        return sampleReservationService.searchAttractions(name, sort);
    }

    @PostMapping("/reserve")
    public String reserve(@RequestParam Long slotId, @RequestParam int quantity, @RequestParam int capacity) {
        boolean result = sampleReservationService.reserve(slotId, quantity, capacity);
        if (result == true) {
            return "OK";
        } else {
            return "FAIL";
        }
    }

    @GetMapping("/blacklist")
    public String blacklist(@RequestParam String path) {
        return sampleReservationService.readBlacklist(path);
    }

    @GetMapping("/price")
    public double price(@RequestParam double unitPrice, @RequestParam int quantity, @RequestParam(required = false) String grade) {
        return sampleReservationService.calculatePrice(unitPrice, quantity, grade);
    }
}
