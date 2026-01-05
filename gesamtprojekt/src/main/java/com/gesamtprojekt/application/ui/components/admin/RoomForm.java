package com.gesamtprojekt.application.ui.components.admin;

import com.gesamtprojekt.application.model.Equipment;
import com.gesamtprojekt.application.model.MeetingRoom;
import com.gesamtprojekt.application.service.implementation.EquipmentService;
import com.gesamtprojekt.application.service.implementation.RoomImageStorageService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import java.util.Comparator;
import com.vaadin.flow.component.upload.Upload;

import com.vaadin.flow.component.html.Image;
import java.util.Base64;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.component.UI;

import java.io.ByteArrayInputStream;

import java.util.LinkedHashSet;
import java.util.List;

public class RoomForm extends FormLayout {

    private final EquipmentService equipmentService;
    private final RoomImageStorageService imageStorage;

    private final Upload imageUpload;
    private final Image imagePreview = new Image();

    private String stagedImagePath;
    private String stagedImageMime;
    private String stagedImageOriginalName;
    private String stagedImageDataUrl;

    public final TextField name = new TextField("Room Name *");
    public final IntegerField capacity = new IntegerField("Capacity *");
    public final ComboBox<String> building = new ComboBox<>("Building *");
    public final IntegerField floor = new IntegerField("Floor");

    public final ComboBox<String> status = new ComboBox<>("Status");

    private final CheckboxGroup<Equipment> equipmentGroup = new CheckboxGroup<>();

    public RoomForm(EquipmentService equipmentService, RoomImageStorageService imageStorage) {
        this.equipmentService = equipmentService;
        this.imageStorage = imageStorage;
        this.imageUpload = buildImageUpload();

        setResponsiveSteps(new ResponsiveStep("0", 2), new ResponsiveStep("900px", 4));
        setupFields();

        setColspan(name, 2);
        setColspan(capacity, 1);
        setColspan(building, 1);

        setColspan(floor, 1);
        setColspan(status, 1);

        add(name, capacity, building, floor, status);
        add(new Hr(), 2);
        add(roomImageSection(), 2);
        add(equipmentSection(), 2);
    }

    public void setRoom(MeetingRoom r) {
        if (r == null) return;

        name.setValue(n(r.getName()));
        capacity.setValue(r.getCapacity());
        building.setValue(n(r.getLocation()));
        status.setValue(n(r.getStatus()));
        floor.setValue(r.getFloor());

        setSelectedEquipmentFromRoom(r);

        stagedImagePath = r.getImagePath();
        stagedImageMime = r.getImageMime();
        stagedImageOriginalName = r.getImageOriginalName();

        if (stagedImagePath != null && !stagedImagePath.isBlank()) {
            imagePreview.setVisible(false);
        }
    }



    public void apply(MeetingRoom r) {
        r.setName(name.getValue());
        r.setCapacity(capacity.getValue());
        r.setLocation(building.getValue());
        r.setFloor(floor.getValue());
        r.setStatus(status.getValue());
        r.setEquipment(new java.util.LinkedHashSet<>(equipmentGroup.getValue()));
        r.setImagePath(stagedImagePath);
        r.setImageMime(stagedImageMime);
        r.setImageOriginalName(stagedImageOriginalName);

        }

    public boolean isValid() {
        return !name.isEmpty() && capacity.getValue() != null && !building.isEmpty();
    }

    private Component equipmentSection() {
        Span title = new Span("Amenities & Equipment");
        title.getStyle().set("font-weight", "600");

        equipmentGroup.setItemLabelGenerator(Equipment::getDescription);
        reloadEquipmentSorted();

        equipmentGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        equipmentGroup.getStyle().set("columns", "2"); // 2 columns left

        var left = new VerticalLayout(title, equipmentGroup);
        left.setPadding(false);
        left.setSpacing(true);
        left.setWidthFull();

        TextField newEquipment = new TextField("New equipment");
        newEquipment.setPlaceholder("e.g. Projector");
        newEquipment.setWidthFull();

        Button addBtn = new Button("Add equipment");
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.setWidthFull();
        addBtn.addClickListener(e -> addEquipment(newEquipment));

        var right = new VerticalLayout(newEquipment, addBtn);
        right.setPadding(false);
        right.setSpacing(true);
        right.setWidth("260px");

        HorizontalLayout wrap = new HorizontalLayout(left, right);
        wrap.setWidthFull();
        wrap.setAlignItems(FlexComponent.Alignment.START);
        wrap.setFlexGrow(1, left);

        return wrap;
    }


    private List<Equipment> loadEquipment() {
        return equipmentService.findAll();
    }

    private void setupFields() {
        name.setPlaceholder("Enter room name");
        name.setRequiredIndicatorVisible(true);
        name.setWidthFull();
        capacity.setMin(1);
        capacity.setRequiredIndicatorVisible(true);
        capacity.setWidthFull();
        building.setItems("MCI I", "MCI II", "MCI III");
        building.setRequiredIndicatorVisible(true);
        building.setWidthFull();
        status.setItems("ACTIVE", "INACTIVE");
        status.setValue("ACTIVE");
        status.setWidthFull();
        floor.setStepButtonsVisible(true);
        floor.setMin(-10);
        floor.setMax(50);
        floor.setHelperText(null);
        floor.setWidthFull();

    }

    private void reloadEquipmentSorted() {
        List<Equipment> items = equipmentService.findAll();
        items.sort(Comparator.comparing(e -> n(e.getDescription()).toLowerCase()));
        equipmentGroup.setItems(items);
    }

    private void addEquipment(TextField field) {
        String val = field.getValue() == null ? "" : field.getValue().trim();
        if (val.isEmpty()) return;

        Equipment eq = new Equipment();
        eq.setDescription(val);

        Equipment created = equipmentService.create(eq);

        field.clear();
        reloadEquipmentSorted();

        var selected = new LinkedHashSet<>(equipmentGroup.getValue());
        selected.add(created);
        equipmentGroup.setValue(selected);
    }

    private void setSelectedEquipmentFromRoom(MeetingRoom room) {
        List<Equipment> all = equipmentService.findAll();
        all.sort(Comparator.comparing(e -> n(e.getDescription()).toLowerCase()));
        equipmentGroup.setItems(all);

        var selectedIds = room.getEquipment().stream()
                .map(Equipment::getEquipmentId)
                .filter(id -> id != null)
                .collect(java.util.stream.Collectors.toSet());

        var selected = all.stream()
                .filter(e -> e.getEquipmentId() != null && selectedIds.contains(e.getEquipmentId()))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        equipmentGroup.setValue(selected);
    }

    private Component roomImageSection() {
        Span title = new Span("Room Image");
        title.getStyle().set("font-weight", "600");

        setupPreview();

        VerticalLayout wrap = new VerticalLayout(title, imageUpload, imagePreview);
        wrap.setPadding(false);
        wrap.setSpacing(true);
        wrap.setWidthFull();
        return wrap;
    }

    private Upload buildImageUpload() {
        InMemoryUploadHandler handler = UploadHandler.inMemory((meta, data) -> {
            var stored = imageStorage.save(
                    new ByteArrayInputStream(data),
                    meta.fileName(),
                    meta.contentType()
            );

            String mime = meta.contentType() == null ? "image/png" : meta.contentType();
            String b64 = Base64.getEncoder().encodeToString(data);
            String dataUrl = "data:" + mime + ";base64," + b64;

            UI ui = UI.getCurrent();
            if (ui != null) ui.access(() -> applyStoredImage(stored, dataUrl));
            else applyStoredImage(stored, dataUrl);
        });

        Upload upload = new Upload(handler);
        upload.setMaxFiles(1);
        upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/jpg", "image/webp");
        upload.addFileRejectedListener(e -> clearStagedImage());
        return upload;
    }


    private void applyStoredImage(RoomImageStorageService.StoredImage stored, String dataUrl) {
        stagedImagePath = stored.path();
        stagedImageMime = stored.mime();
        stagedImageOriginalName = stored.originalName();
        stagedImageDataUrl = dataUrl;

        imagePreview.setSrc(stagedImageDataUrl);
        imagePreview.setVisible(true);
    }

    private void clearStagedImage() {
        stagedImagePath = null;
        stagedImageMime = null;
        stagedImageOriginalName = null;
        imagePreview.setVisible(false);
    }

    private void setupPreview() {
        imagePreview.setVisible(false);
        imagePreview.setMaxWidth("420px");
        imagePreview.getStyle().set("border-radius", "10px");
    }


    private String n(String v) {
        return v == null ? "" : v;
    }
}
