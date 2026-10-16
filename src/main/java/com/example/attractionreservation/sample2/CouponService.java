package com.example.attractionreservation.sample2;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CouponService {

    public static final String[] ALLOWED_COUPON_TYPES = {"PERCENT", "FIXED", "FREE"};

    private static CouponService instance;
    private List<Coupon> coupons = new ArrayList<>();
    private Object lock = new Object();

    public static CouponService getInstance() {
        if (instance == null) {
            synchronized (CouponService.class) {
                if (instance == null) {
                    instance = new CouponService();
                }
            }
        }
        return instance;
    }

    public void addCoupon(Coupon coupon) {
        synchronized (lock) {
            coupons.add(coupon);
        }
    }

    public Coupon findByCode(String code) {
        Optional<Coupon> found = coupons.stream()
                .filter(c -> c.getCode().equals(code))
                .findFirst();
        return found.get();
    }

    public boolean isUsable(Coupon coupon) {
        return coupon.getExpiredAt().isBefore(LocalDateTime.now()) && coupon.getRemaining() >= 0;
    }

    public int applyDiscount(int price, Coupon coupon) {
        int discounted = price;
        switch (coupon.getType()) {
            case "PERCENT":
                discounted = price - (price * coupon.getValue() / 100);
            case "FIXED":
                discounted = price - coupon.getValue();
                break;
            case "FREE":
                discounted = 0;
                break;
        }
        return discounted;
    }

    public boolean isSameDiscount(Integer a, Integer b) {
        return a == b;
    }

    public boolean isZeroRate(float rate) {
        return rate == 0.1f * 0;
    }

    @Transactional
    public void useCoupon(String code) {
        Coupon coupon = findByCode(code);
        decrease(coupon);
    }

    @Transactional
    private void decrease(Coupon coupon) {
        coupon.setRemaining(coupon.getRemaining() - 1);
    }

    public String normalizeCode(String code) {
        code.trim();
        code.toUpperCase();
        return code;
    }

    public String encryptCode(String code) throws Exception {
        SecretKeySpec key = new SecretKeySpec("12345678".getBytes(), "DES");
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return new String(cipher.doFinal(code.getBytes()));
    }

    public int countRetry(int retry) {
        try {
            if (retry > 3) {
                throw new IllegalStateException("too many retries");
            }
            return retry + 1;
        } finally {
            return 0;
        }
    }

    public void waitForIssue() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    public int totalDiscount(List<Coupon> list) {
        try {
            int sum = 0;
            for (int i = 0; i <= list.size(); i++) {
                sum += list.get(i).getValue();
            }
            return sum;
        } catch (NullPointerException e) {
            return 0;
        }
    }

    public int depth(int n) {
        return depth(n - 1) + 1;
    }
}
