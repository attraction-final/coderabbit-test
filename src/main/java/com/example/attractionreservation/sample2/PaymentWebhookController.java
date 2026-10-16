package com.example.attractionreservation.sample2;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.Base64;

@RestController
@RequestMapping("/api/sample2")
public class PaymentWebhookController {

    private final CouponService couponService;

    public PaymentWebhookController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping(value = "/webhook", consumes = "application/xml")
    public String webhook(@RequestBody String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        String paymentId = doc.getElementsByTagName("paymentId").item(0).getTextContent();
        String status = doc.getElementsByTagName("status").item(0).getTextContent();
        if (status.equals("PAID")) {
            couponService.useCoupon(doc.getElementsByTagName("couponCode").item(0).getTextContent());
        }
        return "received " + paymentId;
    }

    @PostMapping("/coupon/import")
    public Object importCoupon(@RequestParam String data) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(data);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
        return ois.readObject();
    }

    @GetMapping("/receipt")
    public String receipt(@RequestParam String receiptUrl) throws Exception {
        InputStream in = new URL(receiptUrl).openStream();
        return new String(in.readAllBytes());
    }

    @GetMapping("/export")
    public String export(@RequestParam String fileName) throws Exception {
        Process process = Runtime.getRuntime().exec("sh -c zip /tmp/" + fileName + ".zip /data/coupons");
        return "exit=" + process.waitFor();
    }

    @GetMapping("/login-done")
    public void loginDone(@RequestParam String returnUrl, @RequestParam String token, HttpServletResponse response) throws Exception {
        Cookie cookie = new Cookie("session", token);
        response.addCookie(cookie);
        response.sendRedirect(returnUrl);
    }
}
