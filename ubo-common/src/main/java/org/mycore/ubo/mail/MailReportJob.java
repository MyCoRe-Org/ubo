package org.mycore.ubo.mail;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.MCRMailer;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.content.MCRSourceContent;
import org.mycore.mcr.cronjob.MCRCronjob;
import org.mycore.services.i18n.MCRTranslation;
import org.mycore.util.concurrent.MCRFixedUserCallable;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class MailReportJob extends MCRCronjob {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String CONFIG_PREFIX = "UBO.MailReport.";

    private static final String CONFIG_MAILFROM = MCRConfiguration2.getString("UBO.Mail.From").orElse(null);

    private record PreparedEmail(String from, String to, String uri) {}

    @Override
    public String getDescription() {
        return "send configured mail";
    }

    @Override
    public void runJob() {
        try {
            new MCRFixedUserCallable<>(() -> {
                if (CONFIG_MAILFROM == null) {
                    LOGGER.warn("Cannot execute mailing job, configuration UBO.Mail.From not set");
                    return null;
                }

                final String configSubject = MCRTranslation.translate("ubo.mailreport.subject");
                final String configBody = MCRTranslation.translate("ubo.mailreport.body");

                if (configSubject.startsWith("???") || configBody.startsWith("???")) {
                    LOGGER.warn("Cannot execute mailing job, configuration subject or body not properly configured");
                    return null;
                }

                LOGGER.info("Sending emails to configured recipients...");
                List<PreparedEmail> preparedEmails = prepareEmails();

                for (PreparedEmail mail : preparedEmails) {
                    MCRMailer.send(mail.from, mail.to, configSubject, configBody, List.of(mail.uri));
                }

                LOGGER.info("{} emails sent", preparedEmails.size());
                return null;
            }, MCRSystemUserInformation.getSystemUserInstance()).call();
        } catch (Exception e) {
            LOGGER.error("Failed to send emails: {}", e.getMessage());
        }
    }

    private List<PreparedEmail> prepareEmails() throws TransformerException, IOException {
        List<PreparedEmail> preparedEmails = new ArrayList<>();

        Map<String, String> configuredURIs = MCRConfiguration2.getSubPropertiesMap(CONFIG_PREFIX);

        Set<String> uniquePrefixes = configuredURIs.keySet().stream()
            .map(key -> {
                int dotPos = key.indexOf('.');
                return (dotPos > 0) ? key.substring(0, dotPos) : null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        for (String prefix : uniquePrefixes) {
            Map<String, String> configTuple = MCRConfiguration2.getSubPropertiesMap(CONFIG_PREFIX + prefix + ".");
            Set<String> expectedKeys = Set.of("uri", "to", "filetype");
            if (!configTuple.keySet().equals(expectedKeys)) {
                LOGGER.warn("Property '" + CONFIG_PREFIX + prefix + "' is not properly configured. Please make sure "
                    + "that each id has the three values 'uri', 'to' and 'filetype'.");
                continue;
            }
            String attachment = null;
            try {
                attachment = resolveURI(configTuple.get("uri"), configTuple.get("filetype"));
            } catch (Exception e) {
                LOGGER.warn("Couldn't create attachment for uri {}, {}", configTuple.get("uri"), e);
            }
            if (attachment != null) {
                String[] recipients = configTuple.get("to").split(",");
                for (String recipient : recipients) {
                    preparedEmails.add(new PreparedEmail(CONFIG_MAILFROM, recipient, attachment));
                }
            }
        }
        return preparedEmails;
    }

    private String resolveURI(String uri, String filetype) throws IOException, TransformerException {
        MCRSourceContent content = MCRSourceContent.getInstance(uri);

        Path tempFile = Files.createTempFile("attachment_", "." + filetype);
        // TODO send email if resultlist empty?
        try (InputStream inputStream = content.getContentInputStream();
            OutputStream outputStream = Files.newOutputStream(tempFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        return tempFile.toUri().toString();
    }

}
