package com.checkspace.backend.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final InvoiceService invoiceService;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Async
    public void sendPaymentInvoice(String toEmail, String sellerName,
                                   String phone, String propertyTitle, String invoiceNumber) {
        try {
            byte[] pdfBytes = invoiceService.generateVerificationInvoice(
                    sellerName, phone, propertyTitle, invoiceNumber);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Payment Receipt — RootPeace | " + invoiceNumber);
            helper.setText(buildPaymentEmailHtml(sellerName, propertyTitle, invoiceNumber), true);
            helper.addAttachment("RootPeace_Invoice_" + invoiceNumber + ".pdf",
                    () -> new java.io.ByteArrayInputStream(pdfBytes), "application/pdf");

            mailSender.send(message);
            log.info("Invoice email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Email failed to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendPropertyApproved(String toEmail, String sellerName, String propertyTitle) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Your property is LIVE on RootPeace! 🎉");
            helper.setText(buildApprovedEmailHtml(sellerName, propertyTitle), true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Approved email failed: {}", e.getMessage());
        }
    }

    @Async
    public void sendAdminEmail(String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            // TODO: Replace this with your actual admin email address
            helper.setTo("rootpeaceofficial@gmail.com");
            helper.setSubject(subject);

            // Convert the plain text stats into a nice RootPeace HTML email
            helper.setText(buildAdminEmailHtml(subject, body), true);

            mailSender.send(message);
            log.info("Admin nightly stats email sent successfully.");
        } catch (Exception e) {
            log.error("Admin stats email failed: {}", e.getMessage());
        }
    }

    private String buildAdminEmailHtml(String subject, String bodyText) {
        // Convert plain text newlines to HTML line breaks
        String formattedBody = bodyText.replace("\n", "<br>");

        return """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:24px">
              <div style="background:#111827;padding:24px;border-radius:12px;text-align:center;margin-bottom:24px">
                <h1 style="color:#fff;margin:0;font-size:24px">Root<span style="color:#FBBF24">Peace</span></h1>
                <p style="color:rgba(255,255,255,0.5);margin:8px 0 0;font-size:13px">Admin Dashboard</p>
              </div>
              <h2 style="color:#111827">%s</h2>
              <div style="color:#111827;font-size:15px;line-height:1.8;background:#F9FAFB;padding:20px;border-radius:12px;border:1px solid #E5E7EB;">
                %s
              </div>
              <p style="color:#6B7280;font-size:12px;text-align:center;margin-top:24px">
                Auto-generated nightly report · rootpeace.com
              </p>
            </div>
            """.formatted(subject, formattedBody);
    }

    private String buildPaymentEmailHtml(String name, String property, String invoice) {
        return """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:24px">
              <div style="background:#111827;padding:24px;border-radius:12px;text-align:center;margin-bottom:24px">
                <h1 style="color:#fff;margin:0;font-size:24px">Root<span style="color:#FBBF24">Peace</span></h1>
                <p style="color:rgba(255,255,255,0.5);margin:8px 0 0;font-size:13px">India's Verified Property Marketplace</p>
              </div>

              <h2 style="color:#111827">Payment Received ✅</h2>
              <p style="color:#6B7280">Hello <strong>%s</strong>,</p>
              <p style="color:#6B7280">Thank you for your payment. Your property verification request has been received.</p>

              <div style="background:#F9FAFB;border:1px solid #E5E7EB;border-radius:12px;padding:20px;margin:20px 0">
                <table style="width:100%%">
                  <tr><td style="color:#6B7280;font-size:13px">Invoice No</td><td style="text-align:right;font-weight:bold;color:#111827">%s</td></tr>
                  <tr><td style="color:#6B7280;font-size:13px;padding-top:8px">Property</td><td style="text-align:right;font-weight:bold;color:#111827;padding-top:8px">%s</td></tr>
                  <tr><td style="color:#6B7280;font-size:13px;padding-top:8px">Verification Fee</td><td style="text-align:right;padding-top:8px">₹299.00</td></tr>
                  <tr><td style="color:#6B7280;font-size:13px;padding-top:8px">GST (18%%)</td><td style="text-align:right;padding-top:8px">₹53.82</td></tr>
                  <tr style="border-top:1px solid #E5E7EB">
                    <td style="font-weight:bold;padding-top:12px;color:#111827">Total Paid</td>
                    <td style="text-align:right;font-weight:bold;color:#FBBF24;font-size:18px;padding-top:12px">₹352.82</td>
                  </tr>
                </table>
              </div>

              <div style="background:#ECFDF5;border:1px solid #D1FAE5;border-radius:12px;padding:16px;margin:20px 0">
                <p style="color:#065F46;font-weight:bold;margin:0 0 8px">What happens next?</p>
                <ol style="color:#047857;font-size:13px;margin:0;padding-left:16px">
                  <li>Our team will call you within 24 hours</li>
                  <li>Field agent visits your property</li>
                  <li>Verification in 48-72 hours</li>
                  <li>Property goes live for verified buyers</li>
                </ol>
              </div>

              <p style="color:#6B7280;font-size:12px;text-align:center;margin-top:24px">
                Invoice attached as PDF · rootpeace.com<br>
                © 2025 RootPeace Technologies Pvt. Ltd.
              </p>
            </div>
            """.formatted(name, invoice, property);
    }

    private String buildApprovedEmailHtml(String name, String property) {
        return """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:24px">
              <div style="background:#111827;padding:24px;border-radius:12px;text-align:center;margin-bottom:24px">
                <h1 style="color:#fff;margin:0">Root<span style="color:#FBBF24">Peace</span></h1>
              </div>
              <h2 style="color:#111827">Your property is LIVE! 🎉</h2>
              <p style="color:#6B7280">Hello <strong>%s</strong>,</p>
              <p style="color:#6B7280">Great news! <strong>%s</strong> has been verified and is now live on RootPeace.</p>
              <p style="color:#6B7280">Verified buyers can now view and inquire about your property.</p>
              <div style="text-align:center;margin:24px 0">
                <a href="https://rootpeace.com/buy" style="background:#111827;color:#fff;padding:12px 32px;border-radius:8px;text-decoration:none;font-weight:bold">View on RootPeace →</a>
              </div>
              <p style="color:#6B7280;font-size:12px;text-align:center">rootpeace.com · © 2025 RootPeace Technologies Pvt. Ltd.</p>
            </div>
            """.formatted(name, property);
    }
}