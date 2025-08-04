package com.touchvirtual.config;

import com.touchvirtual.model.UserSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuração para UserSettings
 *
 * @author TouchVirtual Team
 * @version 1.0.0
 */
@Configuration
public class UserSettingsConfig {

    private static final Logger logger = LoggerFactory.getLogger(UserSettingsConfig.class);

    @Bean
    @Primary
    UserSettings userSettings() {
        UserSettings settings = new UserSettings();

        // Configurações padrão
        settings.setUserId("default");
        settings.setProfileName("Default Profile");
        settings.setSensitivity(1.0);
        settings.setDeadband(0.05);
        settings.setLeftHanded(false);
        settings.setEnableSound(true);
        settings.setEnableVibration(false);

        logger.info("✅ UserSettings configurado com sucesso");

        return settings;
    }
}
