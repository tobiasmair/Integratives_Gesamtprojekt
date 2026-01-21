package com.gesamtprojekt.application.ui.components.calendar;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class RoomDetailsDialog extends Dialog {

    public RoomDetailsDialog(CalendarRoomCardModel room) {
        setWidth("600px");
        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);

        add(createContent(room));
    }

    private VerticalLayout createContent(CalendarRoomCardModel room) {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        H2 header = new H2(room.name() != null ? room.name() : "");
        header.getStyle().set("margin", "0");

        Image img = buildImage(room.name());

        VerticalLayout infoSection = new VerticalLayout();
        infoSection.setPadding(false);
        infoSection.setSpacing(true);
        infoSection.add(infoRow(VaadinIcon.BUILDING, "Building: " + (room.building() != null ? room.building() : "")));
        infoSection.add(infoRow(VaadinIcon.USERS, "Capacity: " + (room.capacity() != null ? room.capacity() : 0) + " people"));
        infoSection.add(infoRow(VaadinIcon.LINES, "Floor: " + (room.floor() != null ? room.floor() : 0)));

        if (room.tags() != null && !room.tags().isEmpty()) {
            Span equipmentLabel = new Span("Equipment:");
            equipmentLabel.getStyle().set("font-weight", "600");
            equipmentLabel.getStyle().set("margin-top", "10px");

            HorizontalLayout tagsLayout = new HorizontalLayout();
            tagsLayout.getStyle().set("flex-wrap", "wrap");
            tagsLayout.getStyle().set("gap", "8px");
            room.tags().forEach(tag -> tagsLayout.add(createTagChip(tag)));

            infoSection.add(equipmentLabel, tagsLayout);
        }

        Button closeButton = new Button("Close", e -> close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        closeButton.getStyle().set("margin-top", "20px");

        content.add(header, img, infoSection, closeButton);
        return content;
    }

    private Image buildImage(String roomName) {
        String src = "/room-images/by-room?roomName=" + (roomName != null ? roomName : "dummypicture");

        Image img = new Image(src, "Room");
        img.setWidthFull();
        img.setHeight("250px");
        img.getStyle().set("object-fit", "cover");
        img.getStyle().set("border-radius", "8px");
        return img;
    }

    private HorizontalLayout infoRow(VaadinIcon icon, String text) {
        Icon i = icon.create();
        i.getStyle().set("color", "var(--lumo-secondary-text-color)");
        i.setSize("18px");

        Span t = new Span(text);
        t.getStyle().set("font-size", "16px");

        HorizontalLayout row = new HorizontalLayout(i, t);
        row.setSpacing(true);
        row.setPadding(false);
        row.setAlignItems(HorizontalLayout.Alignment.CENTER);
        return row;
    }

    private Div createTagChip(String text) {
        Div chip = new Div(new Span(text));
        chip.getStyle().set("font-size", "12px");
        chip.getStyle().set("padding", "6px 12px");
        chip.getStyle().set("border-radius", "4px");
        chip.getStyle().set("background", "var(--lumo-primary-color-10pct)");
        chip.getStyle().set("color", "var(--lumo-primary-text-color)");
        return chip;
    }
}
