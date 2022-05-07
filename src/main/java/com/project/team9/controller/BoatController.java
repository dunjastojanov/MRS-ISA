package com.project.team9.controller;

import com.project.team9.dto.BoatCardDTO;
import com.project.team9.dto.BoatDTO;
import com.project.team9.model.resource.Boat;
import com.project.team9.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(path="boat")
@CrossOrigin("*")
public class BoatController {

    private final BoatService service;


    @Autowired
    public BoatController(BoatService service) {
        this.service = service;
    }

    @GetMapping
    public List<Boat> getBoats() {
        return service.getBoats();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Boat getBoat(@PathVariable String id) {
        Long houseId = Long.parseLong(id);
        return service.getBoat(houseId);
    }
    @PostMapping(value = "createBoat")
    public Long addBoatForOwner(BoatDTO boat, @RequestParam("fileImage") MultipartFile[] multipartFiles) throws IOException {
        return service.createBoat(boat, multipartFiles);
    }

//    @PostMapping(value = "updateBoat/{id}")
//    public VacationHouseDTO updateVacationHouse(@PathVariable String id, VacationHouseDTO house, @RequestParam("fileImage") MultipartFile[] multipartFiles) throws IOException {
//        return service.updateHouse(id, house, multipartFiles);
//    }

//    @GetMapping(value = "boatprof/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
//    public VacationHouseDTO getBoatDTO(@PathVariable String id) {
//        Long boatId = Long.parseLong(id);
//        return service.getBoatDTO(boatId);
//    }

    @GetMapping(value = "getownerboats/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<BoatCardDTO> getOwnerHouses(@PathVariable String id) {
        Long owner_id = Long.parseLong(id);
        return service.getOwnerBoats(owner_id);
    }

    @GetMapping(value = "getownerboat/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public BoatCardDTO getOwnerHouse(@PathVariable String id) {
        return service.getBoatCard(Long.parseLong(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteVacationHouse(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.ok().build();
    }
}