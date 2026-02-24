package com.marketplace.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Charge automatiquement le fichier .env situé à la racine du projet dans
 * l'environnement Spring, avant que les beans soient créés.
 * Les valeurs du .env n'écrasent PAS les variables d'environnement système
 * ni les propriétés déjà définies (priorité la plus basse).
 */
public class DotEnvEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final Logger log = LoggerFactory.getLogger(DotEnvEnvironmentPostProcessor.class);
    private static final String PROPERTY_SOURCE_NAME = "dotenvFile";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Path dotEnvPath = resolveEnvFile();
        if (dotEnvPath == null) {
            return;
        }

        Map<String, Object> properties = new LinkedHashMap<>();
        try {
            Files.lines(dotEnvPath).forEach(line -> {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    return;
                }
                int idx = trimmed.indexOf('=');
                if (idx <= 0) {
                    return;
                }
                String key   = trimmed.substring(0, idx).trim();
                String value = trimmed.substring(idx + 1).trim();
                // Retirer les guillemets optionnels
                if ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }
                properties.put(key, value);
            });
        } catch (IOException e) {
            log.warn(".env trouvé mais illisible : {}", e.getMessage());
            return;
        }

        if (!properties.isEmpty()) {
            // Priorité la plus basse : ne remplace pas les vraies variables d'environnement
            environment.getPropertySources().addLast(
                    new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
            log.info(".env chargé depuis {} ({} variable(s))", dotEnvPath, properties.size());
        }
    }

    private Path resolveEnvFile() {
        // 1. Répertoire courant (cas mvn spring-boot:run ou IDE lancé depuis la racine)
        Path candidate = Paths.get(".env").toAbsolutePath();
        if (Files.isReadable(candidate)) {
            return candidate;
        }
        // 2. Deux niveaux au-dessus (cas où le working dir est marketplace-infrastructure/)
        candidate = Paths.get("../../.env").toAbsolutePath().normalize();
        if (Files.isReadable(candidate)) {
            return candidate;
        }
        log.debug(".env introuvable — variables d'environnement système utilisées.");
        return null;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
