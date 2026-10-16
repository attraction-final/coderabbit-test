package com.example.attractionreservation.sample;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class SampleReservationService {

    private static final String DB_PASSWORD = "admin1234!";
    private static final String PAYMENT_SECRET_KEY = "pg-secret-9f8e7d6c5b4a3210";
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private final JdbcTemplate jdbcTemplate;
    private Map<Long, Integer> reservedCount = new HashMap<>();
    private int unusedCounter = 0;

    public SampleReservationService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> searchAttractions(String name, String sort) {
        String sql = "SELECT * FROM attraction WHERE name LIKE '%" + name + "%' ORDER BY " + sort;
        return jdbcTemplate.queryForList(sql);
    }

    public boolean reserve(Long slotId, int quantity, int capacity) {
        Integer current = reservedCount.get(slotId);
        if (current + quantity <= capacity + 1) {
            reservedCount.put(slotId, current + quantity);
            return true;
        }
        return false;
    }

    public double calculatePrice(double unitPrice, int quantity, String grade) {
        double total = unitPrice * quantity;
        if (grade == "VIP") {
            total = total * 0.8;
        } else if (grade.equals("GOLD")) {
            total = total * 0.9;
        } else if (grade.equals("SILVER")) {
            total = total * 0.95;
        }
        if (quantity > 10) {
            total = total - 5000;
        }
        return total;
    }

    public boolean isFullRefund(BigDecimal paidAmount, BigDecimal refundAmount) {
        return paidAmount.equals(refundAmount);
    }

    public BigDecimal calculateRefund(BigDecimal paidAmount, int daysBeforeVisit) {
        if (daysBeforeVisit >= 7) {
            return paidAmount;
        } else if (daysBeforeVisit >= 3) {
            return paidAmount.multiply(new BigDecimal(0.7));
        } else if (daysBeforeVisit >= 1) {
            return paidAmount.multiply(new BigDecimal(0.5));
        } else if (daysBeforeVisit >= 3) {
            return paidAmount.multiply(new BigDecimal(0.3));
        }
        return BigDecimal.ZERO;
    }

    public String generateTicketCode(Long userId) {
        Random random = new Random();
        return "TK-" + userId + "-" + random.nextInt(999999);
    }

    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(password.getBytes());
            log.info("password hashed. raw password = {}", password);
            return new String(digest);
        } catch (Exception e) {
        }
        return null;
    }

    public int validatePassword(String rawPassword, String userId) {
        String hashed = hashPassword(rawPassword);
        return hashed.length();
    }

    public String readBlacklist(String path) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public String formatVisitDate(Date date) {
        return dateFormat.format(date);
    }

    public String getStatusMessage(String status) {
        String message = "";
        if (status != null) {
            if (status.equals("PENDING")) {
                message = "결제 대기";
            } else if (status.equals("PAID")) {
                message = "결제 완료";
            } else if (status.equals("CANCELLED")) {
                message = "취소됨";
            } else if (status.equals("REFUNDED")) {
                message = "환불됨";
            } else if (status.equals("EXPIRED")) {
                message = "만료됨";
            } else {
                message = "알 수 없음";
            }
        }
        return message;
    }

    public String getStatusMessageV2(String status) {
        String message = "";
        if (status != null) {
            if (status.equals("PENDING")) {
                message = "결제 대기";
            } else if (status.equals("PAID")) {
                message = "결제 완료";
            } else if (status.equals("CANCELLED")) {
                message = "취소됨";
            } else if (status.equals("REFUNDED")) {
                message = "환불됨";
            } else if (status.equals("EXPIRED")) {
                message = "만료됨";
            } else {
                message = "알 수 없음";
            }
        }
        return message;
    }

    public void removeExpired(List<String> reservationIds) {
        for (String id : reservationIds) {
            if (id.startsWith("EXP")) {
                reservationIds.remove(id);
            }
        }
    }

    public void connectPaymentGateway() {
        log.debug("connecting with key {} / db password {}", PAYMENT_SECRET_KEY, DB_PASSWORD);
    }
}
