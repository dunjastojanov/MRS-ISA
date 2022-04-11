package com.project.team9.configuration;

import com.project.team9.model.Address;
import com.project.team9.model.Tag;
import com.project.team9.model.buissness.Pricelist;
import com.project.team9.model.reservation.AdventureReservation;
import com.project.team9.model.reservation.Appointment;
import com.project.team9.model.resource.Adventure;
import com.project.team9.model.user.vendor.FishingInstructor;
import com.project.team9.model.user.vendor.RegistrationType;
import com.project.team9.repo.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Configuration
public class Config {
    @Bean
    CommandLineRunner configureTestData(
            AdventureRepository adventureRepository,
            FishingInstructorRepository fishingInstructorRepository,
            PricelistRepository pricelistRepository,
            AddressRepository addressRepository,
            TagRepository tagRepository,
            AdventureReservationRepository adventureReservationRepository,
            AppointmentRepository appointmentRepository
    ) {
        return args -> {
            Address fishingInstructorAddress = new Address("Novi Sad", "23", "Bulevar Cara Lazara", "Srbija");
            addressRepository.save(fishingInstructorAddress);
            FishingInstructor fishingInstructor = new FishingInstructor(
                    "petar.jovanovic@email.com",
                    "peraribar",
                    "Petar",
                    "Jovanovic",
                    "0601233215",
                    fishingInstructorAddress,
                    RegistrationType.FishingInstructor,
                    "Imam zavrseni pecaroski fakultet.",
                    "Jos sam bio savim mlad, neke barske ptice sam lovio tad, kad je dosla da se kupa lepa protina kci."
            );


            Address adventureAddress = new Address("Novi Sad", "52a", "Dunav", "Srbija");
            addressRepository.save(adventureAddress);

            Pricelist adventurePricelist = new Pricelist(75, new Date());
            pricelistRepository.save(adventurePricelist);
            
            ArrayList<Tag> fishingEquipment =new ArrayList<Tag>();
            fishingEquipment.add(new Tag("Stap za pecanje marke BestFishing rod"));
            fishingEquipment.add(new Tag("250g crva"));
            fishingEquipment.add(new Tag("3 udice"));

            tagRepository.saveAll(fishingEquipment);

            Adventure bigAdventure = new Adventure(
                    "Velika ribarska avantura",
                    adventureAddress,
                    "Ovo je jedna zahtevna i izazovna avanutura u kojoj pecamo samo najvece sarane i somove.",
                    "Obavezno se pojavite na vreme, a vreme je uvek rano.",
                    adventurePricelist,
                    10,
                    fishingInstructor,
                    2,
                    fishingEquipment
            );


            fishingInstructor.addAdventure(bigAdventure);
            fishingInstructorRepository.save(fishingInstructor);
            adventureRepository.save(bigAdventure);

        };
    }
}