/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gadroves.gsisinve.controller;

import com.gadroves.gsisinve.UI.controller.ControlledScreen;
import com.gadroves.gsisinve.UI.controller.ScreensController;
import com.gadroves.gsisinve.utils.CustomDate;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

import java.net.URL;
import java.util.ResourceBundle;
/**
 * FXML Controller class
 *
 * @author Aaron
 */
public class FacturarController implements Initializable, ControlledScreen {

    @FXML
    Label fecha;
    @FXML
    Label hora;
    @FXML
    RadioButton entregasi;
    @FXML
    RadioButton entregano;
    
    private ScreensController myController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        CustomDate customdate = new CustomDate(fecha.textProperty(),hora.textProperty());
        customdate.start();
        ToggleGroup group = new ToggleGroup();
        this.entregasi.setToggleGroup(group);
        this.entregano.setToggleGroup(group);
    }

    @Override
    public void setScreenParent(ScreensController screenPage) {
        this.myController=screenPage;
    }

}
