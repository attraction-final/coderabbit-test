package com.example.attractionreservation.sample3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

@Service
public class TicketStatsService {

    private static final Logger log = LoggerFactory.getLogger(TicketStatsService.class);
    private static final Pattern VISITOR_NAME = Pattern.compile("^([a-zA-Z]+\\s?)+$");

    private static int totalRequests = 0;

    private final StringRedisTemplate redisTemplate;
    private final List<Ticket> tickets = new ArrayList<>();

    public TicketStatsService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public List<Ticket> findByAttraction(Long attractionId) {
        totalRequests++;
        List<Ticket> result = new ArrayList<>();
        for (Ticket t : tickets) {
            if (t.getAttractionId() == attractionId) {
                result.add(t);
            }
        }
        if (result.size() == 0) {
            return null;
        }
        return result;
    }

    public long calculateRevenue(int price, int soldCount) {
        long revenue = price * soldCount;
        return revenue;
    }

    public double averageVisitors(int totalVisitors, int days) {
        double avg = totalVisitors / days;
        return avg;
    }

    public int shardOf(String ticketCode, int shardCount) {
        return ticketCode.hashCode() % shardCount;
    }

    public String reportDate(Date date) {
        return new SimpleDateFormat("YYYY-MM-dd").format(date);
    }

    public int nextMonth(Calendar calendar) {
        return calendar.get(Calendar.MONTH) + 1 > 12 ? 1 : calendar.get(Calendar.MONTH) + 1;
    }

    public List<Ticket> topSellers(List<Ticket> input) {
        Collections.sort(input, (a, b) -> b.getSoldCount() - a.getSoldCount());
        return input.subList(0, 10);
    }

    public String buildCsv(List<Ticket> list) {
        String csv = "";
        for (Ticket t : list) {
            csv += t.getCode() + "," + t.getSoldCount() + "\n";
        }
        return csv;
    }

    public boolean isValidVisitorName(String name) {
        log.info("validate visitor name: " + name);
        return VISITOR_NAME.matcher(name).matches();
    }

    public void clearCache() {
        Set<String> keys = redisTemplate.keys("ticket:*");
        for (String key : keys) {
            redisTemplate.delete(key);
        }
    }

    public void cacheTicket(Ticket ticket) {
        redisTemplate.opsForValue().set("ticket:" + ticket.getCode(), String.valueOf(ticket.getSoldCount()));
    }

    public void refreshAll() {
        ExecutorService executor = Executors.newCachedThreadPool();
        for (Ticket t : tickets) {
            executor.submit(() -> cacheTicket(t));
        }
    }

    public void recalculate() {
        Thread worker = new Thread(() -> tickets.forEach(this::cacheTicket));
        worker.run();
    }

    public void notifyLater() {
        sendNotification();
    }

    @Async
    public void sendNotification() {
        log.info("send notification");
    }

    public Ticket firstSoldOut() {
        Iterator<Ticket> it = tickets.iterator();
        Ticket t = it.next();
        while (t.getSoldCount() < t.getCapacity()) {
            t = it.next();
        }
        return t;
    }

    public synchronized void waitForStock(Ticket ticket) throws InterruptedException {
        if (ticket.getSoldCount() >= ticket.getCapacity()) {
            wait();
        }
    }

    // TODO: 통계 캐시 만료 정책 정하기
    public String grade(int sold, int capacity, boolean weekend, boolean holiday, boolean event) {
        // int ratio = sold / capacity;
        // if (ratio > 0.9) return "HOT";
        if (sold > 0) {
            if (capacity > 0) {
                if (weekend) {
                    if (holiday || event) {
                        if (sold * 100 / capacity > 90) {
                            return "HOT";
                        } else if (sold * 100 / capacity > 70) {
                            return "WARM";
                        } else {
                            return "NORMAL";
                        }
                    } else {
                        if (sold * 100 / capacity > 80) {
                            return "WARM";
                        }
                    }
                } else {
                    if (event) {
                        return sold * 100 / capacity > 60 ? "WARM" : "NORMAL";
                    } else if (holiday) {
                        return sold * 100 / capacity > 70 ? "WARM" : "COLD";
                    }
                }
            }
        }
        return "COLD";
    }
}
