package com.project.team9.dto;

import com.project.team9.model.Tag;
import com.project.team9.model.reservation.BoatReservation;

import java.util.List;


public class BoatDTO {
    private Long id;
    private String name;
    private String address;
    private String number;
    private String street;
    private String city;
    private String country;
    private String description;
    private String type;
    private List<String> imagePaths;
    private String rulesAndRegulations;
    private String engineNumber;
    private double engineStrength;
    private double topSpeed;
    private double length;
    private List<Tag> navigationEquipment;
    private List<String> tagsText;
    private int price;
    private int cancellationFee;
    private int capacity;
    private List<BoatReservation> quickReservations;

    public BoatDTO() {
    }

    public BoatDTO(Long id, String name, String address, String number, String street, String city, String country, String description, String type, List<String> imagePaths, String rulesAndRegulations, String engineNumber, double engineStrength, double topSpeed, double length, List<Tag> navigationEquipment, List<String> tagsText, int price, int cancellationFee, int capacity, List<BoatReservation> quickReservations) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.number = number;
        this.street = street;
        this.city = city;
        this.country = country;
        this.description = description;
        this.price = price;
        this.rulesAndRegulations = rulesAndRegulations;
        this.capacity = capacity;
        this.type = type;
        this.engineNumber = engineNumber;
        this.engineStrength = engineStrength;
        this.topSpeed = topSpeed;
        this.length = length;
        this.navigationEquipment = navigationEquipment;
        this.cancellationFee = cancellationFee;
        this.tagsText = tagsText;
        this.imagePaths = imagePaths;
        this.quickReservations = quickReservations;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getImagePaths() {
        return imagePaths;
    }

    public void setImagePaths(List<String> imagePaths) {
        this.imagePaths = imagePaths;
    }

    public String getRulesAndRegulations() {
        return rulesAndRegulations;
    }

    public void setRulesAndRegulations(String rulesAndRegulations) {
        this.rulesAndRegulations = rulesAndRegulations;
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

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public List<Tag> getNavigationEquipment() {
        return navigationEquipment;
    }

    public void setNavigationEquipment(List<Tag> navigationEquipment) {
        this.navigationEquipment = navigationEquipment;
    }

    public List<String> getTagsText() {
        return tagsText;
    }

    public void setTagsText(List<String> tagsText) {
        this.tagsText = tagsText;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getCancellationFee() {
        return cancellationFee;
    }

    public void setCancellationFee(int cancellationFee) {
        this.cancellationFee = cancellationFee;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public List<BoatReservation> getQuickReservations() {
        return quickReservations;
    }

    public void setQuickReservations(List<BoatReservation> quickReservations) {
        this.quickReservations = quickReservations;
    }
}
