package com.gesamtprojekt.application.ui.components.calendar;

import com.gesamtprojekt.application.security.SecurityService;
import com.gesamtprojekt.application.service.implementation.BookingService;
import com.gesamtprojekt.application.service.implementation.MeetingRoomService;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;

import java.time.LocalDateTime;

public class CalendarRoomCard extends Div {

    private final BookingService bookingService;
    private final MeetingRoomService meetingRoomService;
    private final SecurityService securityService;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    public CalendarRoomCard(CalendarRoomCardModel room, BookingService bookingService,
                            MeetingRoomService meetingRoomService, SecurityService securityService) {
        this.bookingService = bookingService;
        this.meetingRoomService = meetingRoomService;
        this.securityService = securityService;

        addClassName("calendar-room-card");
        add(buildCard(room));
    }

    public void setFilterDateTime(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    private VerticalLayout buildCard(CalendarRoomCardModel r) {
        Image img = buildImage(r.imagePath());
        Span title = buildTitle(n(r.name()), r);

        VerticalLayout info = buildInfo(r);
        Div tags = buildTags(r);
        Button book = buildButton(r);

        VerticalLayout box = new VerticalLayout(img, title, info, tags, book);
        box.setPadding(false);
        box.setSpacing(false);
        box.setWidthFull();
        return box;
    }

    private Image buildImage(String imagePath) {
        String src = (imagePath == null || imagePath.isBlank())
                ? "https://picsum.photos/600/350"
                : "/room-images?path=" + imagePath;

        Image img = new Image(src, "Room");
        img.setWidthFull();
        img.setHeight("160px");
        img.getStyle().set("object-fit", "cover");
        img.getStyle().set("border-radius", "12px 12px 0 0");
        return img;
    }

    private Span buildTitle(String text, CalendarRoomCardModel room) {
        Span title = new Span(text);
        title.getStyle().set("font-size", "18px");
        title.getStyle().set("font-weight", "700");
        title.getStyle().set("padding", "8px 12px 8px 12px");
        title.getStyle().set("cursor", "pointer");
        title.getStyle().set("color", "var(--lumo-primary-text-color)");

        title.addClickListener(e -> {
            RoomDetailsDialog dialog = new RoomDetailsDialog(room);
            dialog.open();
        });

        return title;
    }

    private VerticalLayout buildInfo(CalendarRoomCardModel r) {
        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);
        info.getStyle().set("padding", "0 12px 10px 12px");

        info.add(infoRow(VaadinIcon.BUILDING, n(r.building())));
        info.add(infoRow(VaadinIcon.USERS, "Capacity: " + nz(r.capacity()) + " people"));
        info.add(infoRow(VaadinIcon.LINES, "Floor " + nz(r.floor())));
        return info;
    }

    private HorizontalLayout infoRow(VaadinIcon icon, String text) {
        Icon i = icon.create();
        i.getStyle().set("color", "var(--lumo-secondary-text-color)");
        i.setSize("16px");

        Span t = new Span(text);
        t.getStyle().set("color", "var(--lumo-secondary-text-color)");
        t.getStyle().set("font-size", "14px");

        HorizontalLayout row = new HorizontalLayout(i, t);
        row.setSpacing(true);
        row.setPadding(false);
        return row;
    }

    private Div buildTags(CalendarRoomCardModel r) {
        Div tagsContainer = new Div();
        tagsContainer.getStyle().set("padding", "0 12px 10px 12px");
        tagsContainer.getStyle().set("height", "80px");
        tagsContainer.getStyle().set("overflow-y", "auto");
        tagsContainer.getStyle().set("overflow-x", "hidden");

        HorizontalLayout tags = new HorizontalLayout();
        tags.setSpacing(true);
        tags.setPadding(false);
        tags.getStyle().set("flex-wrap", "wrap");
        tags.getStyle().set("gap", "8px");

        r.tags().forEach(t -> tags.add(tagChip(t)));

        tagsContainer.add(tags);
        return tagsContainer;
    }

    private Div tagChip(String text) {
        Div chip = new Div(new Span(text));
        chip.getStyle().set("font-size", "12px");
        chip.getStyle().set("padding", "4px 6px");
        chip.getStyle().set("border-radius", "4px");
        chip.getStyle().set("background", "var(--lumo-primary-color-10pct)");
        chip.getStyle().set("color", "var(--lumo-primary-text-color)");
        return chip;
    }

    private Button buildButton(CalendarRoomCardModel room) {
        Button b = new Button("Book Room");
        b.getStyle().set("margin", "0 12px 12px 12px");
        b.getStyle().set("width", "calc(100% - 24px)");

        b.addClickListener(e -> {
            RoomBookingDialog dialog = new RoomBookingDialog(
                    room.roomId(),
                    room.name(),
                    bookingService,
                    meetingRoomService,
                    securityService,
                    startDateTime,
                    endDateTime
            );

            dialog.addBookingCreatedListener(event -> {
                fireEvent(new BookingCreatedEvent(this));
            });

            dialog.open();
        });

        return b;
    }

    public static class BookingCreatedEvent extends ComponentEvent<CalendarRoomCard> {
        public BookingCreatedEvent(CalendarRoomCard source) {
            super(source, false);
        }
    }

    public Registration addBookingCreatedListener(ComponentEventListener<BookingCreatedEvent> listener) {
        return addListener(BookingCreatedEvent.class, listener);
    }

    private String n(String v) {
        return v == null ? "" : v;
    }

    private int nz(Integer v) {
        return v == null ? 0 : v;
    }
}
