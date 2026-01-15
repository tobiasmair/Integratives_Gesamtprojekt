package com.gesamtprojekt.application.ui.components.admin;

import com.gesamtprojekt.application.model.Client;
import com.gesamtprojekt.application.service.implementation.BookingService;
import com.gesamtprojekt.application.service.implementation.ClientService;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.shared.Registration;

import java.util.List;

public class UserTableSection extends VerticalLayout {

    private final ClientService clientService;
    private final BookingService bookingService;

    private final Grid<Client> grid = new Grid<>(Client.class, false);
    private final TextField searchField = new TextField();
    private final ComboBox<String> roleFilter = new ComboBox<>("", List.of("All Roles", "ADMIN", "USER", "ROOM"));
    private final Button addClientBtn = new Button("Add User");

    public UserTableSection(ClientService clientService, BookingService bookingService) {
        this.clientService = clientService;
        this.bookingService = bookingService;

        setSizeFull();
        setPadding(false);

        add(buildToolbar(), buildGrid());
        updateList();
    }

    private HorizontalLayout buildToolbar() {
        searchField.setPlaceholder("Search by Name or Email...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("400px");
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> updateList());

        roleFilter.setValue("All Roles");
        roleFilter.addValueChangeListener(e -> updateList());

        //ComboBox<String> deptFilter = new ComboBox<>("", List.of("All Departments", "DIBSE", "MCI 1"));
        //deptFilter.setValue("All Departments");

        addClientBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addClientBtn.addClickListener(e -> addClientDialog());

        HorizontalLayout toolbar = new HorizontalLayout(searchField, roleFilter, addClientBtn);
        toolbar.setWidthFull();
        toolbar.setAlignItems(Alignment.BASELINE);
        toolbar.setFlexGrow(1, searchField);
        return toolbar;
    }

    private Grid<Client> buildGrid() {
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

        // Spalten
        grid.addColumn(Client::getUsername).setHeader("User").setSortable(true);
        grid.addColumn(Client::getEmail).setHeader("Contact"); // Dummy-Email
        grid.addColumn(Client::getRole).setHeader("Role");
        grid.addColumn(Client::getDepartment).setHeader("Department");
        grid.addColumn(client -> {
            // Für Rooms nicht befüllen
            if ("ROOM_SCREEN".equals(client.getUserType())) {
                return "-";
            }
            return bookingService.countByClient_UserIdAndIsActiveTrueAndBookingStatusAndEndTimeAfter(client.getUserId());
        }).setHeader("Bookings");

        // Action Buttons
        grid.addComponentColumn(client -> {
            // Edit Button
            Button edit = new Button(VaadinIcon.EDIT.create());
            // Edit für Rooms deaktivieren
            if ("ROOM_SCREEN".equals(client.getUserType())) {
                edit.setEnabled(false);
                edit.setTooltipText("Technical room users cannot be deleted");
            } else {
                edit.addClickListener(e -> openEditDialog(client));
            }

            // Delete Button
            Button delete = new Button(VaadinIcon.TRASH.create());
            delete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            // Delete für Rooms deaktivieren
            if ("ROOM_SCREEN".equals(client.getUserType())) {
                delete.setEnabled(false);
                delete.setTooltipText("Technical room users cannot be deleted");
            } else {
                delete.addClickListener(e -> openDeleteDialog(client));
            }

            HorizontalLayout actions = new HorizontalLayout(edit, delete);

            return actions;
        }).setHeader("Actions");

        return grid;
    }

    // Liste nach Filter aktualisieren
    private void updateList() {
        grid.setItems(clientService.findAllUsers(searchField.getValue(), roleFilter.getValue()));
    }

    // Dialog Fenster für Edit
    private void openEditDialog(Client client) {
        Dialog dialog = new Dialog();
        RegistrationForm form = new RegistrationForm();

        // Password verdecken und Dummy Werte setzen für isValid Prüfung
        form.password.setVisible(false);
        form.password.setValue("********");
        form.confirmPassword.setVisible(false);
        form.confirmPassword.setValue("********");

        form.setClient(client);

        dialog.setHeaderTitle("Edit User: " + client.getUsername());

        VerticalLayout dialogLayout = new VerticalLayout(form);
        dialog.add(dialogLayout);

        Button saveButton = new Button("Save", event -> {
            if (form.isValid()) {
                client.setUsername(form.username.getValue());
                client.setEmail(form.email.getValue());
                client.setDepartment(form.department.getValue());
                client.setUserType(form.userType.getValue());
                client.setRole(form.role.getValue());

                clientService.updateClient(client);
                updateList();
                fireEvent(new StatsChangedEvent(this)); // Container benachrichtigen
                dialog.close();
                Notification.show("User updated.");
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", event -> dialog.close());

        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    // Dialog Fenster für Delete
    private void openDeleteDialog(Client client) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Delete User: " + client.getUsername());

        Span text = new Span("Are you sure you want to delete User " + client.getUsername() + " ?");
        dialog.add(text);

        Button deleteButton = new Button("Delete", event -> {
            clientService.deleteClient(client);
            updateList();
            fireEvent(new StatsChangedEvent(this)); // Container benachrichtigen
            dialog.close();
            Notification.show("User deleted.");
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Cancel", event -> dialog.close());

        dialog.getFooter().add(cancelButton, deleteButton);
        dialog.open();
    }

    // Dialog Fenster für neuen Client
    private void addClientDialog() {
        Dialog dialog = new Dialog();
        RegistrationForm form = new RegistrationForm();

        dialog.setHeaderTitle("Create new User");

        VerticalLayout dialogLayout = new VerticalLayout(form);
        dialog.add(dialogLayout);

        Button registerButton = new Button("Register");
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", event -> dialog.close());

        // Click Logik für Registrierung
        registerButton.addClickListener(e -> {
            if (form.isValid()) {
                try {
                    clientService.createClient(form.username.getValue(), form.password.getValue(), form.email.getValue(), form.department.getValue(), form.userType.getValue(),form.role.getValue());

                    updateList();
                    fireEvent(new StatsChangedEvent(this)); // Container benachrichtigen
                    dialog.close();
                    Notification.show("Registration success!", 3000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } catch (Exception ex) {
                    Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            }
        });

        dialog.getFooter().add(cancelButton, registerButton);
        dialog.open();
    }

    // View registrieren
    public Registration addStatsChangedListener(ComponentEventListener<StatsChangedEvent> listener) {
        return addListener(StatsChangedEvent.class, listener);
    }
}
