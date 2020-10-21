package gui;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.entities.Department;
import model.exceptions.ValidationException;
import model.services.DepartmentService;

import javafx.event.ActionEvent;

import java.net.URL;
import java.util.*;

public class DepartmentFormController implements Initializable {

    private Department entity;
    private DepartmentService service;
    private List<DataChangeListener> dataChangeListeners = new ArrayList<>();

    @FXML
    private Label lbError;
    @FXML
    private TextField tfId;
    @FXML
    private TextField tfName;
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

    private Department getFormData() {
        Department obj = new Department();
        ValidationException exception = new ValidationException("Validation error");
        obj.setId(Utils.tryParsetoInt(tfId.getText()));
        if (tfName.getText() == null || tfName.getText().equals("")) exception.addError("name", "Field can't be empty");
        obj.setName(tfName.getText());
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
        Constraints.setTextFieldMaxLength(tfName, 30);
    }

    public void setDepartment(Department entity) {
        this.entity = entity;
    }

    public void setDepartmentService(DepartmentService service) {
        this.service = service;
    }

    public void updateFormData() {
        if (entity == null) throw new IllegalStateException("Entity null");
        tfId.setText(String.valueOf(entity.getId()));
        tfName.setText(entity.getName());

    }

    public void subscribeDataChangeListener(DataChangeListener listener) {
        dataChangeListeners.add(listener);
    }

    private void setErrorMessages(Map<String, String> errors) {
        Set<String> fields = errors.keySet();
        if (fields.contains("name")) lbError.setText(errors.get("name"));
    }

}
