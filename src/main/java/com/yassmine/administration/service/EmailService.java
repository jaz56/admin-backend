package com.yassmine.administration.service;

import com.yassmine.administration.model.User;
import jakarta.mail.internet.MimeMessage; // ← Trés important (Jakarta et non javax)
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // 🟢 NOUVEAU : URL du frontend Angular, configurable via application.properties
    // (distincte de app.base-url qui pointe vers le backend Spring Boot)
    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    public void sendAdminWelcomeEmail(User admin, String rawPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(admin.getEmail());
            helper.setSubject("🔑 Vos accès Administrateur - Plateforme d'Administration");

            // Construction de la liste des permissions au format HTML
            StringBuilder permList = new StringBuilder("<ul style='padding-left:20px; font-family: sans-serif; color: #475569;'>");
            admin.getPermissions().forEach((module, detail) -> {
                if (detail.isRead() || detail.isCreate() || detail.isUpdate()) {
                    permList.append(String.format("<li><strong>%s</strong> : %s%s%s</li>",
                            module.toUpperCase(),
                            detail.isRead() ? "Lecture " : "",
                            detail.isCreate() ? "• Écriture " : "",
                            detail.isUpdate() ? "• Modification" : ""
                    ));
                }
            });
            permList.append("</ul>");

            String expiryText = admin.getPermissionsExpiryDate() != null
                    ? "Vos accès sont valables jusqu'au : <strong>" + admin.getPermissionsExpiryDate() + "</strong>"
                    : "Vos accès ont une <strong>durée indéfinie</strong>.";

            // 🟢 CORRIGÉ : lien de connexion construit dynamiquement à partir de app.frontend-url
            // + le bon chemin de routing Angular (/authentication/login)
            String loginUrl = frontendUrl + "/authentication/login";

            String htmlContent = """
                <div style="font-family: sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 12px;">
                    <h2 style="color: #4f46e5; margin-bottom: 20px;">Bienvenue sur l'Espace Administration</h2>
                    <p style="color: #334155;">Bonjour %s %s,</p>
                    <p style="color: #334155;">Un compte administrateur vient de vous être créé par le Super Administrateur.</p>
                    
                    <div style="background-color: #f8fafc; padding: 15px; border-radius: 8px; margin: 20px 0;">
                        <p style="margin: 5px 0; color: #475569;"><strong>Lien plateforme :</strong> <a href="%s" style="color: #4f46e5;">Accéder à la plateforme</a></p>
                        <p style="margin: 5px 0; color: #475569;"><strong>Identifiant (Email) :</strong> %s</p>
                        <p style="margin: 5px 0; color: #475569;"><strong>Mot de passe temporaire :</strong> <code style="background: #e2e8f0; padding: 2px 6px; border-radius: 4px;">%s</code></p>
                    </div>
                    
                    <h3 style="color: #1e293b;">Vos permissions d'accès :</h3>
                    %s
                    
                    <p style="color: #ef4444; font-size: 13px; margin-top: 20px;">%s</p>
                    <hr style="border: 0; border-top: 1px solid #e2e8f0; margin: 20px 0;" />
                    <p style="font-size: 11px; color: #94a3b8; text-align: center;">Veuillez modifier votre mot de passe dès votre première connexion.</p>
                </div>
                """.formatted(admin.getPrenom(), admin.getNom(), loginUrl, admin.getEmail(), rawPassword, permList.toString(), expiryText);

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (Exception e) {
            // Logger l'erreur pour ne pas bloquer la transaction principale
            System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
        }
    }
}