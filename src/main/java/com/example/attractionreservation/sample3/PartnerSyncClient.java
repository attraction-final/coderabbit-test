package com.example.attractionreservation.sample3;

import org.springframework.stereotype.Component;

import javax.net.ssl.*;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.X509Certificate;

@Component
public class PartnerSyncClient {

    private static final String PARTNER_HOST = "192.168.10.25";
    private static final String PARTNER_API = "http://partner.example.com/api/tickets";

    public String fetchTickets() throws Exception {
        TrustManager[] trustAll = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };
        SSLContext context = SSLContext.getInstance("TLSv1");
        context.init(null, trustAll, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

        InputStream in = new URL(PARTNER_API).openStream();
        return new String(in.readAllBytes());
    }

    public String partnerHost() {
        return PARTNER_HOST;
    }
}
