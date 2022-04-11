package com.project.team9.model.resource;

import com.project.team9.model.Tag;
import com.project.team9.model.buissness.Pricelist;
import com.project.team9.model.reservation.AdventureReservation;
import com.project.team9.model.Address;
import com.project.team9.model.user.vendor.FishingInstructor;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Adventure extends Resource{
    @ManyToOne
    @Cascade(CascadeType.SAVE_UPDATE)

    private FishingInstructor owner;
    private int numberOfClients;
    @OneToMany
    private List<Tag> fishingEquipment;
    @OneToMany(cascade= javax.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<AdventureReservation> quickReservations;


    public Adventure() {
    }

    public Adventure(String title,
                     Address address,
                     String description,
                     String rulesAndRegulations,
                     Pricelist pricelist,
                     int cancellationFee,
                     FishingInstructor owner,
                     int numberOfClients,
                     List<Tag> fishingEquipment) {
        super(title, address, description, rulesAndRegulations, pricelist, cancellationFee);
        this.owner = owner;
        this.numberOfClients = numberOfClients;
        this.fishingEquipment = fishingEquipment;
        this.quickReservations = new ArrayList<AdventureReservation>();

    }

    public FishingInstructor getOwner() {
        return owner;
    }

    public void setOwner(FishingInstructor owner) {
        this.owner = owner;
    }

    public int getNumberOfClients() {
        return numberOfClients;
    }

    public void setNumberOfClients(int numberOfClients) {
        this.numberOfClients = numberOfClients;
    }

    public List<Tag> getFishingEquipment() {
        return fishingEquipment;
    }

    public void setFishingEquipment(List<Tag> fishingEquipment) {
        this.fishingEquipment = fishingEquipment;
    }

    public void addQuickReservation(AdventureReservation adventureReservation) {
        quickReservations.add(adventureReservation);
    }
}
