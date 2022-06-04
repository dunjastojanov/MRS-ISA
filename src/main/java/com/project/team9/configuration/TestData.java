package com.project.team9.configuration;

import com.project.team9.model.Address;
import com.project.team9.model.Image;
import com.project.team9.model.Tag;
import com.project.team9.model.buissness.Pricelist;
import com.project.team9.model.request.DeleteRequest;
import com.project.team9.model.request.RegistrationRequest;
import com.project.team9.model.reservation.AdventureReservation;
import com.project.team9.model.reservation.Appointment;
import com.project.team9.model.reservation.BoatReservation;
import com.project.team9.model.reservation.VacationHouseReservation;
import com.project.team9.model.resource.Adventure;
import com.project.team9.model.resource.Boat;
import com.project.team9.model.resource.VacationHouse;
import com.project.team9.model.review.ClientReview;
import com.project.team9.model.review.VendorReview;
import com.project.team9.model.user.Administrator;
import com.project.team9.model.user.Client;
import com.project.team9.model.user.Role;
import com.project.team9.model.user.vendor.BoatOwner;
import com.project.team9.model.user.vendor.FishingInstructor;
import com.project.team9.model.user.vendor.VacationHouseOwner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TestData {

    private final BCryptPasswordEncoder passwordEncoder;



    public TestData() {
        this.passwordEncoder = new BCryptPasswordEncoder();


    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    public List<Tag> createTagList(List<String> texts) {
        List<Tag> tags = new ArrayList<Tag>();
        for (String text : texts) {
            tags.add(new Tag(text));
        }
        return tags;
    }

    public List<Image> createImageList(List<String> paths) {
        List<Image> images = new ArrayList<>();
        for (String path : paths) {
            images.add(new Image(path));
        }
        return images;
    }

    public FishingInstructor createFishingInstructor(Long id, String firstName, String lastName, String biography, Address address, Image profilePhoto, Role role) {
        FishingInstructor fishingInstructor = new FishingInstructor(profilePhoto, encodePassword(firstName.toLowerCase() + "123"), firstName, lastName,  firstName.toLowerCase() + "." + lastName.toLowerCase() + "@gmail.com", "0601234567", address, false, "", biography, role, new ArrayList<Adventure>());

        fishingInstructor.setId(id);

        return fishingInstructor;
    }

    public VacationHouseOwner createVacationHouseOwner(Long id, String firstName, String lastName, Address address, Image profilePhoto, Role role) {

        VacationHouseOwner vacationHouseOwner = new VacationHouseOwner(profilePhoto, encodePassword(firstName.toLowerCase() + "123"), firstName, lastName, firstName.toLowerCase() + "." + lastName.toLowerCase() + "@gmail.com", "0601234567", address, false, "", role);
        vacationHouseOwner.setId(id);
        return vacationHouseOwner;
    }

    public BoatOwner createBoatOwner(Long id, String firstName, String lastName, Address address, Image profilePhoto, Role role) {

        BoatOwner boatOwner = new BoatOwner(profilePhoto, encodePassword(firstName.toLowerCase() + "123"), firstName, lastName,  firstName.toLowerCase() + "." + lastName.toLowerCase() + "@gmail.com", "0601234567", address, false, "", new ArrayList<>(), role);
        boatOwner.setId(id);
        return boatOwner;
    }

    public Administrator createAdministrator(Long id, String firstName, String lastName, Address address, Image profilePhoto, Role role, boolean isSuperAdministrator) {
        Administrator administrator = new Administrator(profilePhoto, encodePassword(firstName.toLowerCase() + "123"), firstName, lastName,  firstName.toLowerCase() + "." + lastName.toLowerCase() + "@gmail.com", "0601234567", address, false, role, isSuperAdministrator);
        administrator.setId(id);
        return administrator;
    }

    public Client createClient(Long id, String firstName, String lastName, Address address, Image profilePhoto, Role role) {
        Client client = new Client(profilePhoto, encodePassword(firstName.toLowerCase() + "123"), firstName, lastName, firstName.toLowerCase() + "." + lastName.toLowerCase() + "@gmail.com", "0601234567", address, false, role);
        client.setId(id);
        return client;
    }

    public Adventure createAdventure(Long id, String title, Address address, String description, String rulesAndRegulations, Pricelist pricelist, int cancellationFee, FishingInstructor fishingInstructor, int numberOfClients, List<Image> images, List<Tag> fishingEquipment, List<Tag> additionalServices) {

        Adventure adventure = new Adventure(title, address, description, rulesAndRegulations, pricelist, cancellationFee, fishingInstructor, numberOfClients

        );

        adventure.setImages(images);
        adventure.setFishingEquipment(fishingEquipment);
        adventure.setAdditionalServices(additionalServices);
        adventure.setId(id);

        return adventure;
    }

    public VacationHouse createVacationHouse(Long id, String title, Address address, String description, String rulesAndRegulations, Pricelist pricelist, int cancellationFee, VacationHouseOwner vacationHouseOwner, int numberOfRooms, int numberOfBedroomsPerRoom, List<Image> images, List<Tag> additionalServices) {
        VacationHouse vacationHouse = new VacationHouse(title, address, description, rulesAndRegulations, pricelist, cancellationFee, vacationHouseOwner, numberOfRooms, numberOfBedroomsPerRoom);

        vacationHouse.setImages(images);
        vacationHouse.setAdditionalServices(additionalServices);
        vacationHouse.setId(id);

        return vacationHouse;
    }

    public Boat createBoat(Long id, String title, Address address, String description, String rulesAndRegulations, Pricelist pricelist, int cancellationFee, BoatOwner boatOwner, List<Image> images, List<Tag> additionalServices, List<Tag> navigationEquipment, List<Tag> fishingEquipment, String type, double length, String engineNumber, int engineStrength, double topSpeed, int capacity) {

        Boat boat = new Boat(title, address, description, rulesAndRegulations, pricelist, cancellationFee, boatOwner, type, length, engineNumber, engineStrength, topSpeed, navigationEquipment, fishingEquipment, additionalServices, capacity);

        boat.setImages(images);
        boat.setId(id);
        return boat;

    }

    public AdventureReservation createAdventureReservation(List<Appointment> appointments, int numberOfClients, List<Tag> additionalServices, int price, Client client, Adventure resource, boolean isQuickReservation) {
        return new AdventureReservation(appointments, numberOfClients, additionalServices, price, client, resource, false, isQuickReservation);
    }

    public BoatReservation createBoatReservation(List<Appointment> appointments, int numberOfClients, List<Tag> additionalServices, int price, Client client, Boat resource, boolean isQuickReservation) {
        return new BoatReservation(appointments, numberOfClients, additionalServices, price, client, resource, false, isQuickReservation);
    }

    public VacationHouseReservation createVacationHouseReservation(List<Appointment> appointments, int numberOfClients, List<Tag> additionalServices, int price, Client client, VacationHouse resource, boolean isQuickReservation) {
        return new VacationHouseReservation(appointments, numberOfClients, additionalServices, price, client, resource, false, isQuickReservation);
    }

    public RegistrationRequest createRegistrationRequest(String text, String response, String password, String firstName, String lastName, String email, String phoneNumber, String place, String number, String street, String country, String userRole, String biography, String registrationRationale) {
        return new RegistrationRequest(text, response, encodePassword(password), firstName, lastName, email, phoneNumber, place, number, street, country, userRole, biography, registrationRationale);
    }

    public DeleteRequest createDeleteRequest(String text, String response, String userId, String userType) {
        return new DeleteRequest(text, response, userId, userType);
    }

    public ClientReview createClientReviewForResource(Long resourceId, Long clientId, int rating, String text) {
        return new ClientReview(resourceId, null, rating, text, clientId);
    }

    public ClientReview createClientReviewForVendor(Long vendorId, Long clientId, int rating, String text) {
        return new ClientReview(null, vendorId, rating, text, clientId);
    }

    public VendorReview createVendorReview(Long resourceId, Long vendorId, int rating, String text, Long clientId, boolean penalty, boolean noShow, Long reservationId) {
        return new VendorReview(resourceId, vendorId, rating, text, clientId, penalty, noShow, reservationId);
    }

}