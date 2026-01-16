package com.gesamtprojekt.application.ui.components.buttons;

import com.gesamtprojekt.application.model.Client;
import com.gesamtprojekt.application.model.Notification;
import com.gesamtprojekt.application.service.implementation.NotificationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NotificationBell extends Div {

    private final Span badge;
    private final Popover popover;
    private final VerticalLayout notificationList;
    private final NotificationService notificationService;
    private final Client client;

    public NotificationBell(NotificationService notificationService, Client currentClient) {
        this.notificationService = notificationService;
        this.client = currentClient;

        addClassName("notification-bell-container");
        getStyle().set("position", "relative");

        // Glocken-Button
        Button bellButton = new Button(VaadinIcon.BELL.create());
        bellButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        bellButton.getStyle().set("font-size", "1.3rem");

        badge = new Span("0");
        badge.getElement().getThemeList().add("badge error pill");
        badge.getStyle()
                .set("position", "absolute")
                .set("top", "5px")
                .set("right", "5px")
                .set("font-size", "0.7rem");
        badge.setVisible(false);

        popover = new Popover();
        popover.setTarget(bellButton);

        notificationList = new VerticalLayout();
        notificationList.setPadding(false);
        notificationList.setSpacing(false);
        notificationList.setWidth("300px");
        //notificationList.add(new Span("No new notifications"));

        popover.add(notificationList);

        add(bellButton, badge);

        bellButton.addClickListener(e -> refreshNotifications());

        updateBadge();
    }

    public void updateBadge() {
        long count = notificationService.countUnread(client);
        setUnreadCount(count);
    }

    private void refreshNotifications() {
        notificationList.removeAll();
        var notifications = notificationService.findAllByUser(client);

        if (notifications.isEmpty()) {
            Span emptyText = new Span("All caught up!");
            emptyText.getStyle().set("padding", "20px").set("color", "var(--lumo-secondary-text-color)");
            notificationList.add(emptyText);
        } else {
            notifications.forEach(n -> {
                // Container pro Benachrichtigung
                VerticalLayout item = new VerticalLayout();
                item.setSpacing(false);
                item.setPadding(true);
                item.getStyle()
                        .set("border-bottom", "1px solid var(--lumo-contrast-10pct)")
                        .set("cursor", "pointer");

                // Header: Typ-Label + Zeit
                HorizontalLayout header = new HorizontalLayout();
                header.setWidthFull();
                header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

                Span typeLabel = new Span(n.getType().name().replace("_", " "));
                typeLabel.getStyle()
                        .set("font-size", "var(--lumo-font-size-xxs)")
                        .set("text-transform", "uppercase")
                        .set("letter-spacing", "0.05em")
                        .set("color", "var(--lumo-primary-color)");

                Span time = new Span(getRelativeTime(n.getCreatedAt()));
                time.getStyle()
                        .set("font-size", "var(--lumo-font-size-xs)")
                        .set("color", "var(--lumo-secondary-text-color)");

                header.add(typeLabel, time);

                // Content
                String formattedText = n.getType().format(n.getMeetingRoom().getName());
                Span message = new Span(formattedText);
                message.getStyle().set("font-size", "var(--lumo-font-size-s)");

                if (!n.isRead()) {
                    item.getStyle().set("background-color", "var(--lumo-primary-color-10pct)");
                    message.getStyle().set("font-weight", "600");
                }

                item.add(header, message);
                notificationList.add(item);
            });

            notificationService.markAllAsRead(client);
            updateBadge();
        }
    }

    public void setUnreadCount(long count) {
        badge.setText(String.valueOf(count));
        badge.setVisible(count > 0);
    }

    // Timestamp umrechnen
    private String getRelativeTime(LocalDateTime dateTime) {
        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        if (duration.toMinutes() < 1) return "Just now";
        if (duration.toMinutes() < 60) return duration.toMinutes() + "m ago";
        if (duration.toHours() < 24) return duration.toHours() + "h ago";
        return dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }
}