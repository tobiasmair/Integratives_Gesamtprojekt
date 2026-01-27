package com.gesamtprojekt.application;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Theme(value = "gesamtprojekt")
@PWA(name = "MCI Meeting Booker", shortName = "MCI Booker", iconPath = "icons/mci_logo.png")
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void configurePage(AppShellSettings settings) {
        // Icon für Brwoser Tab
        settings.addFavIcon("icon", "icons/mci_logo.png", "32x32");

        // Globaler Seitentitel im Tab
        settings.setPageTitle("MCI Meeting Booker");
    }
}

