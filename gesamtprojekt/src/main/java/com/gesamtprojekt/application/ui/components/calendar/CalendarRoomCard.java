package com.gesamtprojekt.application.ui.components.calendar;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class CalendarRoomCard extends Div {

    public CalendarRoomCard(CalendarRoomCardModel room) {
        addClassName("calendar-room-card");
        add(buildCard(room));
    }

    private VerticalLayout buildCard(CalendarRoomCardModel r) {
        Image img = buildImage(r.imagePath());
        Span title = buildTitle(n(r.name()));

        VerticalLayout info = buildInfo(r);
        HorizontalLayout tags = buildTags(r);
        Button book = buildButton();

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

    private Span buildTitle(String text) {
        Span title = new Span(text);
        title.getStyle().set("font-size", "18px");
        title.getStyle().set("font-weight", "700");
        title.getStyle().set("padding", "8px 12px 8px 12px");
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

    private HorizontalLayout buildTags(CalendarRoomCardModel r) {
        HorizontalLayout tags = new HorizontalLayout();
        tags.setSpacing(true);
        tags.setPadding(false);
        tags.getStyle().set("padding", "0 12px 10px 12px");
        tags.getStyle().set("flex-wrap", "wrap");
        tags.getStyle().set("gap", "8px");

        r.tags().forEach(t -> tags.add(tagChip(t)));
        return tags;
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

    private Button buildButton() {
        Button b = new Button("Book Room");
        b.getStyle().set("margin", "0 12px 12px 12px");
        b.getStyle().set("width", "calc(100% - 24px)");
        return b;
    }

    private String n(String v) {
        return v == null ? "" : v;
    }

    private int nz(Integer v) {
        return v == null ? 0 : v;
    }
}
