package com.project.team9.model.resource;

import com.project.team9.model.buissness.Pricelist;
import com.project.team9.model.Tag;
import com.project.team9.model.reservation.BoatReservation;
import com.project.team9.model.Address;
import com.project.team9.model.user.vendor.BoatOwner;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Boat extends Resource{

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private BoatOwner owner;

    private String type;
    private double length;
    private String engineNumber;
    private double engineStrength;
    private double topSpeed;
    @OneToMany
    private List<Tag> navigationEquipment;
    private int capacity;
    @OneToMany
    private List<BoatReservation> quickReservations;


    public Boat() {
    }


    public Boat(String title, Address address, String description, String rulesAndRegulations, Pricelist pricelist, int cancellationFee, BoatOwner owner, String type, double length, String engineNumber, double engineStrength, double topSpeed, List<Tag> navigationEquipment, int capacity) {
        super(title, address, description, rulesAndRegulations, pricelist, cancellationFee);
        this.owner = owner;
        this.type = type;
        this.length = length;
        this.engineNumber = engineNumber;
        this.engineStrength = engineStrength;
        this.topSpeed = topSpeed;
        this.navigationEquipment = navigationEquipment;
        this.capacity = capacity;
        this.quickReservations = new ArrayList<BoatReservation>();
    }

    public List<BoatReservation> getQuickReservations() {
        return quickReservations;
    }

    public void setQuickReservations(List<BoatReservation> quickReservations) {
        this.quickReservations = quickReservations;
    }

    public BoatOwner getOwner() {
        return owner;
    }

    public void setOwner(BoatOwner owner) {
        this.owner = owner;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public String getEngineNumber() {
        return engineNumber;
    }

    public void setEngineNumber(String engineNumber) {
        this.engineNumber = engineNumber;
    }

    public double getEngineStrength() {
        return engineStrength;
    }

    public void setEngineStrength(double engineStrength) {
        this.engineStrength = engineStrength;
    }

    public double getTopSpeed() {
        return topSpeed;
    }

    public void setTopSpeed(double topSpeed) {
        this.topSpeed = topSpeed;
    }

    public List<Tag> getNavigationEquipment() {
        return navigationEquipment;
    }

    public void setNavigationEquipment(List<Tag> navigationEquipment) {
        this.navigationEquipment = navigationEquipment;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}