/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gadroves.gsisinve.model.beans;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 *
 * @author Juan
 */
public class Linea_Factura_Compra {
    
    public Linea_Factura_Compra(Integer numero,Integer codigo_articulo,Integer cantidad,Double precio,Double impuesto,Double descuento){
        this.numero = new SimpleIntegerProperty(numero);
        this.codigo_articulo = new SimpleIntegerProperty(codigo_articulo);
        this.cantidad = new SimpleIntegerProperty(cantidad);
        this.precio = new SimpleDoubleProperty(precio);
        this.impuesto = new SimpleDoubleProperty(impuesto);
        this.descuento = new SimpleDoubleProperty(descuento);
    }
    
    
    
     public IntegerProperty numeroProperty(){
        return this.numero;
    }
     
    public IntegerProperty codigo_articuloProperty(){
        return this.codigo_articulo;
    }
    
    public IntegerProperty cantidadProperty(){
        return this.cantidad;
    }
    
    public DoubleProperty precioProperty(){
        return this.precio;
    }
    
    public DoubleProperty impuestoProperty(){
        return this.impuesto;
    }
    
    public DoubleProperty descuentoProperty(){
        return this.descuento;
    }
    
    

    public Integer getNumero() {
        return numero.get();
    }

    public void setNumero(Integer numero) {
        this.numero.set(numero);
    }

    public Integer getCodigo_articulo() {
        return codigo_articulo.get();
    }

    public void setCodigo_articulo(Integer codigo_articulo) {
        this.codigo_articulo.set(codigo_articulo);
    }

    public Integer getCantidad() {
        return cantidad.get();
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad.set(cantidad);
    }

    public Double getPrecio() {
        return precio.get();
    }

    public void setPrecio(Double precio) {
        this.precio.set(precio);
    }

    public Double getImpuesto() {
        return impuesto.get();
    }

    public void setImpuesto(Double impuesto) {
        this.impuesto.set(impuesto);
    }

    public Double getDescuento() {
        return descuento.get();
    }

    public void setDescuento(Double descuento) {
        this.descuento.set(descuento);
    }
    
   
    
    private IntegerProperty numero;
    private IntegerProperty codigo_articulo;
    private IntegerProperty cantidad;
    private DoubleProperty precio;
    private DoubleProperty impuesto;
    private DoubleProperty descuento;
    
    
}
