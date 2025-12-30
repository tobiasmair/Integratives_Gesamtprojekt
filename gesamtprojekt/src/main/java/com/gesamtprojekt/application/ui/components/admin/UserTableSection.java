package com.gesamtprojekt.application.ui.components.admin;

import com.gesamtprojekt.application.model.Client;
import com.gesamtprojekt.application.service.implementation.ClientService;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.util.List;

public class UserTableSection extends VerticalLayout {

    private final ClientService clientService;
    private final Grid<Client> grid = new Grid<>(Client.class, false);
    private final TextField searchField = new TextField();
    private final ComboBox<String> roleFilter = new ComboBox<>("", List.of("All Roles", "ADMIN", "USER"));

    public UserTableSection(ClientService clientService) {
        this.clientService = clientService;
        setSizeFull();
        setPadding(false);

        add(buildToolbar(), buildGrid());
        updateList();
    }

    private HorizontalLayout buildToolbar() {
        searchField.setPlaceholder("Suche nach Name...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("400px");
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> updateList());

        roleFilter.setValue("All Roles");
        roleFilter.addValueChangeListener(e -> updateList());

        //ComboBox<String> deptFilter = new ComboBox<>("", List.of("All Departments", "DIBSE", "MCI 1"));
        //deptFilter.setValue("All Departments");

        Button addUserBtn = new Button("Add User");
        addUserBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(searchField, roleFilter, addUserBtn);
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
        grid.addColumn(c -> "DUMMY").setHeader("Contact"); // Dummy-Email
        grid.addColumn(Client::getRole).setHeader("Role");
        grid.addColumn(c -> "DUMMY").setHeader("Department");
        grid.addColumn(c -> "DUMMY").setHeader("Bookings");

        // Action Buttons
        grid.addComponentColumn(client -> {
            // Edit Button
            Button edit = new Button(VaadinIcon.EDIT.create());
            edit.addClickListener(e -> {
                openEditDialog(client);
            });

            // Delete Button
            Button delete = new Button(VaadinIcon.TRASH.create());
            delete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            delete.addClickListener(e -> {
                ConfirmDialog dialog = new ConfirmDialog();
                dialog.setHeader("User löschen?");
                dialog.setHeader("Willst du den Benutzer " + client.getUsername() + " wirklich löschen?");
                dialog.setCancelable(true);
                dialog.setConfirmText("Löschen");
                dialog.setConfirmButtonTheme("error primary");

                dialog.addConfirmListener(event -> {
                    clientService.deleteClient(client);
                    updateList();
                    Notification.show("Benutzer gelöscht.");
                });
                dialog.open();
            });

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
        dialog.setHeaderTitle("User bearbeiten: " + client.getUsername());

        TextField usernameField = new TextField("Benutzername");
        usernameField.setValue(client.getUsername());

        ComboBox<String> roleField = new ComboBox<>("Rolle", List.of("ADMIN", "USER"));
        roleField.setValue(client.getRole());

        VerticalLayout dialogLayout = new VerticalLayout(usernameField, roleField);
        dialog.add(dialogLayout);

        Button saveButton = new Button("Speichern", event -> {
            client.setUsername(usernameField.getValue());
            client.setRole(roleField.getValue());
            clientService.updateClient(client);
            updateList();
            dialog.close();
            Notification.show("Benutzer aktualisiert.");
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Abbrechen", event -> dialog.close());

        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }
}
