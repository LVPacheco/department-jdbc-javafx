package gui;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Callback;
import model.entities.Department;
import model.entities.Seller;
import model.exceptions.ValidationException;
import model.services.DepartmentService;
import model.services.SellerService;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class SellerFormController implements Initializable {

    private Seller entity;
    private SellerService service;
    private DepartmentService dpService;
    private List<DataChangeListener> dataChangeListeners = new ArrayList<>();
    private ObservableList<Department> obsList;

    @FXML
    private Label lbErrorName;
    @FXML
    private Label lbErrorEmail;
    @FXML
    private Label lbErrorBirthDate;
    @FXML
    private Label lbErrorBaseSalary;
    @FXML
    private TextField tfId;
    @FXML
    private TextField tfName;
    @FXML
    private TextField tfEmail;
    @FXML
    private ComboBox<Department> comboBoxDepartment;
    @FXML
    private DatePicker dpBirthDate;
    @FXML
    private TextField tfBaseSalary;
    @FXML
    private Button btSave;
    @FXML
    private Button btCancel;

    @FXML
    public void onBtSaveAction(ActionEvent event) {
        if (entity == null) throw new IllegalStateException("Entity was null");
        if (service == null) throw new IllegalStateException("Service was null");
        try {
            entity = getFormData();
            service.saveOrUpdate(entity);
            notifyDataChangeListeners();
            Utils.currentStage(event).close();
        } catch (DbException e) {
            Alerts.showAlert("Error saving object", null, e.getMessage(), Alert.AlertType.ERROR);
        } catch (ValidationException e) {
            setErrorMessages(e.getErrors());
        }

    }

    private void notifyDataChangeListeners() {
        dataChangeListeners.forEach(DataChangeListener::onDataChanged);
    }

    private Seller getFormData() {
        Seller obj = new Seller();
        ValidationException exception = new ValidationException("Validation error");
        obj.setId(Utils.tryParsetoInt(tfId.getText()));
        if (tfName.getText() == null || tfName.getText().equals("")) exception.addError("name", "Field can't be empty");
        obj.setName(tfName.getText());
        if (tfEmail.getText() == null || tfEmail.getText().equals(""))
            exception.addError("email", "Field can't be empty");
        obj.setEmail(tfEmail.getText());
        if (dpBirthDate.getValue() == null) exception.addError("birthDate", "Field can't be empty");
        else {
            Instant instant = Instant.from(dpBirthDate.getValue().atStartOfDay(ZoneId.systemDefault()));
            obj.setBirthDate(Date.from(instant));
        }
        if (tfBaseSalary.getText() == null || tfBaseSalary.getText().equals(""))
            exception.addError("baseSalary", "Field can't be empty");
        obj.setBaseSalary(Utils.tryParsetoDouble(tfBaseSalary.getText()));

        obj.setDepartment(comboBoxDepartment.getValue());
        if (exception.getErrors().size() > 0) throw exception;
        return obj;
    }

    @FXML
    public void onBtCancelAction(ActionEvent event) {
        Utils.currentStage(event).close();
    }


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initializeNodes();
    }

    private void initializeNodes() {
        Constraints.setTextFieldInteger(tfId);
        Constraints.setTextFieldMaxLength(tfName, 70);
        Constraints.setTextFieldDouble(tfBaseSalary);
        Constraints.setTextFieldMaxLength(tfEmail, 50);
        Utils.formatDatePicker(dpBirthDate, "dd/MM/yyyy");
        initializeComboBoxDepartment();
    }

    public void setSeller(Seller entity) {
        this.entity = entity;
    }

    public void setServices(SellerService service, DepartmentService dpService) {
        this.service = service;
        this.dpService = dpService;
    }

    public void updateFormData() {
        if (entity == null) throw new IllegalStateException("Entity null");
        tfId.setText(String.valueOf(entity.getId()));
        tfName.setText(entity.getName());
        tfEmail.setText(entity.getEmail());
        Locale.setDefault(Locale.US);
        tfBaseSalary.setText(String.format("%.2f", entity.getBaseSalary()));
        if (entity.getBirthDate() != null)
            dpBirthDate.setValue(LocalDate.ofInstant(entity.getBirthDate().toInstant(), ZoneId.systemDefault()));

        if (entity.getDepartment() == null) comboBoxDepartment.getSelectionModel().selectFirst();
        else comboBoxDepartment.setValue(entity.getDepartment());
    }

    public void subscribeDataChangeListener(DataChangeListener listener) {
        dataChangeListeners.add(listener);
    }

    private void setErrorMessages(Map<String, String> errors) {
        Set<String> fields = errors.keySet();

        lbErrorName.setText(fields.contains("name") ? errors.get("name") : "");
        lbErrorEmail.setText(fields.contains("email") ? errors.get("email") : "");
        lbErrorBirthDate.setText(fields.contains("birthDate") ? errors.get("birthDate") : "");
        lbErrorBaseSalary.setText(fields.contains("baseSalary") ? errors.get("baseSalary") : "");

    }

    public void loadAssociatedObjects() {
        if (dpService == null) throw new IllegalStateException("DpService was null");
        List<Department> list = dpService.findAll();
        obsList = FXCollections.observableArrayList(list);
        comboBoxDepartment.setItems(obsList);
    }

    private void initializeComboBoxDepartment() {
        Callback<ListView<Department>, ListCell<Department>> factory = lv -> new ListCell<Department>() {
            @Override
            protected void updateItem(Department item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName());
            }
        };
        comboBoxDepartment.setCellFactory(factory);
        comboBoxDepartment.setButtonCell(factory.call(null));

    }
}
