package controller;

import db.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import sun.security.pkcs11.Secmod;

import java.io.IOException;
import java.sql.*;

public class CreateAccountController {
    public PasswordField txtNewPassword;
    public PasswordField txtConfirmPassword;
    public Label lblPasswordNotMatch1;
    public Label lblPasswordNotMatch2;
    public Label lblUserId;
    public TextField txtUserName;
    public TextField txtEmail;
    public Button btnRegister;
    public AnchorPane root;

    Connection connection = DBConnection.getInstance().getConnection();

    public void initialize(){
        setLblVisibility(false);

        setDisableCommon(true);
    }

    public void btnRegisterOnAction(ActionEvent actionEvent) {
        register();

    }

    public void setBorderColor(String color){
        txtNewPassword.setStyle("-fx-border-color:" + color);
        txtConfirmPassword.setStyle("-fx-border-color:" + color);
    }
    public void setLblVisibility(boolean isVisible){
        lblPasswordNotMatch1.setVisible(isVisible);
        lblPasswordNotMatch2.setVisible(isVisible);
    }

    public void btnAddNewUserOnAction(ActionEvent actionEvent) {
        autoGenerateID();

        setDisableCommon(false);

        txtUserName.requestFocus();

    }
    public void setDisableCommon(boolean isDisable){
        txtUserName.setDisable(isDisable);
        txtEmail.setDisable(isDisable);
        txtNewPassword.setDisable(isDisable);
        txtConfirmPassword.setDisable(isDisable);
        btnRegister.setDisable(isDisable);

    }
    public void autoGenerateID(){



        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select user_id from user order by user_id desc limit 1");
            boolean isExist = resultSet.next();

            if (isExist){
                String usreId = resultSet.getString(1);

                usreId = usreId.substring(1, usreId.length());

                int intId = Integer.parseInt(usreId);

                intId++;
                if (intId < 10){
                    lblUserId.setText("U00"+intId);
                }else if(intId<100){
                    lblUserId.setText("U0"+ intId);
                }else {
                    lblUserId.setText("U"+ intId);
                }


            }else {
                lblUserId.setText(" ");

            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void txtConfirmPasswordOnAction(ActionEvent actionEvent) {
        register();

    }

    public void register(){
        String newPassword = txtNewPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        if(newPassword.equals(confirmPassword)){
            setBorderColor("transparent");
            setLblVisibility(false);

            String id = lblUserId.getText();
            String userName = txtUserName.getText();
            String email = txtEmail.getText();

            try {
                PreparedStatement preparedStatement = connection.prepareStatement("insert into user values (?,?,?,?)");
                preparedStatement.setObject(1,id);
                preparedStatement.setObject(2,userName);
                preparedStatement.setObject(3,email);
                preparedStatement.setObject(4,confirmPassword);

                int i = preparedStatement.executeUpdate();
                if (i>0 ){
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION,"Success....");
                    alert.showAndWait();

                    Parent parent = FXMLLoader.load(this.getClass().getResource("../view/LoginForm.fxml"));
                    Scene scene = new Scene(parent);
                    Stage stage = (Stage) root.getScene().getWindow();
                    stage.setScene(scene);
                    stage.setTitle("Login");
                    stage.centerOnScreen();



                }

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }else{
            setBorderColor("red");
            txtNewPassword.requestFocus();
            setLblVisibility(true);
        }
    }
}

