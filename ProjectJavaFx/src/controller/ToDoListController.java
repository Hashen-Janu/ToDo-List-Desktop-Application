package controller;

import db.DBConnection;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import tm.ToDoTM;

import java.io.IOException;
import java.sql.*;
import java.util.Optional;

public class ToDoListController {
    public Label lblId;
    public Label lblTitle;
    public AnchorPane root;
    public Pane subRoot;
    public TextField txtDescription;
    public ListView<ToDoTM> lstToDo;

    public Button btnUpdate;
    public Button btnDelete;
    public TextField txtSelectedToDo;

    public String selectedId = null;


    public void initialize(){
        lblTitle.setText("Hi " + LoginFormController.loginUserName + " Well come to To-Do List");
        lblId.setText(LoginFormController.loginUserId);

        subRoot.setVisible(false);
        loadList();

        setDisableCommon(true);

         lstToDo.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ToDoTM>() {
             @Override
             public void changed(ObservableValue<? extends ToDoTM> observable, ToDoTM oldValue, ToDoTM newValue) {

                 ToDoTM selectedItem =lstToDo.getSelectionModel().getSelectedItem();
                 if(selectedItem == null){
                     return;

                 }
                setDisableCommon(false);

                subRoot.setVisible(false);

                txtSelectedToDo.setText(lstToDo.getSelectionModel().getSelectedItem().getDescription());
                selectedId = lstToDo.getSelectionModel().getSelectedItem().getId();
             }
         });
    }

    public void setDisableCommon(boolean isDisable){
        txtSelectedToDo.setDisable(isDisable);
        btnDelete.setDisable(isDisable);
        btnUpdate.setDisable(isDisable);
    }

    public void btnLogOutOnAction(ActionEvent actionEvent) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,"Do you want to Log Out...?", ButtonType.YES,ButtonType.NO);
        Optional<ButtonType> buttonType = alert.showAndWait();

        if (buttonType.get().equals(ButtonType.YES)){



            Parent parent = FXMLLoader.load(this.getClass().getResource("../view/LoginForm.fxml"));
            Scene scene = new Scene(parent);

            Stage stage = (Stage) root.getScene().getWindow();

            stage.setScene(scene);
            stage.setTitle("Login");
            stage.centerOnScreen();
        }
    }

    public void btnAddNewToDoOnAction(ActionEvent actionEvent) {

        subRoot.setVisible(true);
        txtDescription.requestFocus();

        setDisableCommon(true);
        txtSelectedToDo.clear();

    }

    public void addToListOnAction(ActionEvent actionEvent) {
        String id = autoGenerateID();
        String description = txtDescription.getText();
        String user_id = lblId.getText();

        Connection connection = DBConnection.getInstance().getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("insert into todo values (?,?,?)");
            preparedStatement.setObject(1,id);
            preparedStatement.setObject(2,description);
            preparedStatement.setObject(3,user_id);

            int i = preparedStatement.executeUpdate();

            txtDescription.clear();
            subRoot.setVisible(false);

            loadList();


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public String autoGenerateID(){

        String id = null;


        try {
            Connection connection = DBConnection.getInstance().getConnection();

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select id from todo order by id desc limit 1");
            boolean isExist = resultSet.next();

            if (isExist){
                String todoId = resultSet.getString(1);

                todoId = todoId.substring(1, todoId.length());

                int intId = Integer.parseInt(todoId);

                intId++;
                if (intId < 10){
                    id = "T00" + intId;
                }else if(intId<100){
                    id = "T0" + intId;
                }else {
                    id = "T" + intId;
                }


            }else {
                id = "T001";

            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return (id);
    }

    public void loadList(){

        ObservableList<ToDoTM> items = lstToDo.getItems();
        items.clear();

        Connection connection = DBConnection.getInstance().getConnection();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("select  * from todo where user_id = ?");
            preparedStatement.setObject(1,LoginFormController.loginUserId);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()){
                String id = resultSet.getString(1);
                String description = resultSet.getString(2);
                String user_id = resultSet.getString(3);

                items.add(new ToDoTM(id,description,user_id));

            }
            lstToDo.refresh();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public void btnUpdateOnAction(ActionEvent actionEvent) {
        String description = txtSelectedToDo.getText();
        Connection connection = DBConnection.getInstance().getConnection();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("update todo set description = ? where id = ?");

            preparedStatement.setObject(1,description);
            preparedStatement.setObject(2,selectedId);

            preparedStatement.executeUpdate();

            loadList();
           txtSelectedToDo.clear();

            setDisableCommon(true);


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void btnDeleteOnAction(ActionEvent actionEvent) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,"Do you want to Delete this todo...?",ButtonType.YES,ButtonType.NO);
        Optional<ButtonType> buttonType = alert.showAndWait();

        if (buttonType.get().equals(ButtonType.YES)){
            Connection connection = DBConnection.getInstance().getConnection();

            try {
                PreparedStatement preparedStatement = connection.prepareStatement("delete from todo where id = ?");
                preparedStatement.setObject(1,selectedId);

                preparedStatement.executeUpdate();

                loadList();
                txtSelectedToDo.clear();
                setDisableCommon(true);

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        }



    }
}
