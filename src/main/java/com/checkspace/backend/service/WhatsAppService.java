package com.checkspace.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppService {

    @Value("${meta.whatsapp.phone-id}")
    private String phoneId;

    @Value("${meta.whatsapp.token}")
    private String token;

    private final RestTemplate restTemplate;

    // Generic send method
    @Async
    public void sendMessage(String toPhone, String message) {
        try {
            String url = "https://graph.facebook.com/v19.0/" + phoneId + "/messages";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            Map<String, Object> body = Map.of(
                    "messaging_product", "whatsapp",
                    "to", "91" + toPhone,
                    "type", "text",
                    "text", Map.of("body", message)
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(url, entity, String.class);
            log.info("WhatsApp sent to {}", toPhone);
        } catch (Exception e) {
            log.error("WhatsApp failed to {}: {}", toPhone, e.getMessage());
        }
    }

    // All message templates

    @Async
    public void sendPaymentReceived(String phone, String sellerName, String propertyTitle) {
        sendMessage(phone,
                "Hello " + sellerName + "! 🏠\n\n" +
                        "✅ Payment of ₹352 received for:\n*" + propertyTitle + "*\n\n" +
                        "Our team will call you within 24 hours to schedule the property visit.\n\n" +
                        "Please keep your original documents ready.\n\n" +
                        "— RootPeace Team\nrootpeace.com"
        );
    }

    @Async
    public void sendPropertyApproved(String phone, String sellerName, String propertyTitle) {
        sendMessage(phone,
                "Hello " + sellerName + "! 🎉\n\n" +
                        "Your property is now *LIVE* on RootPeace!\n\n" +
                        "🏠 *" + propertyTitle + "*\n\n" +
                        "Verified buyers can now see and inquire about your property.\n\n" +
                        "View it at: rootpeace.com\n\n" +
                        "— RootPeace Team"
        );
    }

    @Async
    public void sendPropertyRejected(String phone, String sellerName, String reason) {
        sendMessage(phone,
                "Hello " + sellerName + ",\n\n" +
                        "We could not verify your property. ❌\n\n" +
                        "Reason: " + reason + "\n\n" +
                        "Please call us to resolve this and relist.\n" +
                        "Our team will contact you shortly.\n\n" +
                        "— RootPeace Team"
        );
    }

    @Async
    public void sendSiteVisitConfirmed(String phone, String name, String date) {
        sendMessage(phone,
                "Hello " + name + "! 📅\n\n" +
                        "Your site visit is confirmed for *" + date + "*\n\n" +
                        "Our field agent will accompany you.\n" +
                        "Please carry a valid ID proof.\n\n" +
                        "Questions? Reply to this message.\n\n" +
                        "— RootPeace Team"
        );
    }

    @Async
    public void sendTokenReceived(String phone, String sellerName, String propertyTitle) {
        sendMessage(phone,
                "Hello " + sellerName + "! 💰\n\n" +
                        "A buyer has paid the token amount for:\n*" + propertyTitle + "*\n\n" +
                        "Property is now locked for 7 days.\n" +
                        "Our team will connect you with the buyer shortly.\n\n" +
                        "— RootPeace Team"
        );
    }

    @Async
    public void sendDealClosed(String phone, String name, String propertyTitle, String amount) {
        sendMessage(phone,
                "Congratulations " + name + "! 🎊\n\n" +
                        "Deal closed for:\n*" + propertyTitle + "*\n\n" +
                        "Amount: ₹" + amount + "\n\n" +
                        "Commission invoice will be sent to your email.\n" +
                        "Thank you for trusting RootPeace!\n\n" +
                        "— RootPeace Team"
        );
    }
}