package com.gesamtprojekt.application.ui.components.dashboard;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.timepicker.TimePicker;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

public class QuickBookingContainer extends Div {

    private final Div roomsList = new Div();


    public QuickBookingContainer() {
        addClassName("quick-booking-container");
        add(createContent());
    }

    private VerticalLayout createContent() {
        var content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        content.setWidthFull();

        content.add(createHeader());
        content.add(createDateTimeRow());

        return content;
    }

    private H3 createHeader() {
        var title = new H3("Quick book");
        title.getStyle().set("margin", "0");
        return title;
    }

    private HorizontalLayout createDateTimeRow() {
        var datePicker = new DatePicker("Date");
        var start = createStartTimePicker();
        var end = createEndTimePicker();
        return new HorizontalLayout(datePicker, start, end);
    }

    private TimePicker createStartTimePicker() {
        TimePicker timePicker = new TimePicker();
        timePicker.setLabel("Start");
        timePicker.setStep(Duration.ofMinutes(30));
        timePicker.setValue(LocalTime.of(12, 30));
        //dd(timePicker);
        return timePicker;
    }

    private TimePicker createEndTimePicker() {
        TimePicker timePicker = new TimePicker();
        timePicker.setLabel("End");
        timePicker.setStep(Duration.ofMinutes(30));
        timePicker.setValue(LocalTime.of(12, 30));
        //add(timePicker);
        return timePicker;
    }


}
