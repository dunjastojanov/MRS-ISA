package com.project.team9.service;

import com.project.team9.dto.*;
import com.project.team9.exceptions.ReservationNotAvailableException;
import com.project.team9.model.Address;
import com.project.team9.model.Image;
import com.project.team9.model.Tag;
import com.project.team9.model.buissness.Pricelist;
import com.project.team9.model.reservation.Appointment;
import com.project.team9.model.reservation.BoatReservation;
import com.project.team9.model.resource.Adventure;
import com.project.team9.model.resource.Boat;
import com.project.team9.model.resource.VacationHouse;
import com.project.team9.model.user.Client;
import com.project.team9.model.user.vendor.BoatOwner;
import com.project.team9.repo.BoatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BoatService {
    final String STATIC_PATH = "src/main/resources/static/";
    final String STATIC_PATH_TARGET = "target/classes/static/";
    final String IMAGES_PATH = "/images/boats/";

    private final BoatRepository repository;
    private final AddressService addressService;
    private final PricelistService pricelistService;
    private final TagService tagService;
    private final ImageService imageService;
    private final BoatReservationService boatReservationService;
    private final AppointmentService appointmentService;
    private final ClientService clientService;
    private final ReservationService reservationService;
    private final ClientReviewService clientReviewService;
    private final EmailService emailService;


    @Autowired
    public BoatService(BoatRepository repository, AddressService addressService, PricelistService pricelistService, TagService tagService, ImageService imageService, BoatReservationService boatReservationService, AppointmentService appointmentService, ClientService clientService, ReservationService reservationService, ClientReviewService clientReviewService, EmailService emailService) {
        this.repository = repository;
        this.addressService = addressService;
        this.pricelistService = pricelistService;
        this.tagService = tagService;
        this.imageService = imageService;
        this.boatReservationService = boatReservationService;
        this.appointmentService = appointmentService;
        this.clientService = clientService;
        this.reservationService = reservationService;
        this.clientReviewService = clientReviewService;
        this.emailService = emailService;
    }

    public List<Boat> getBoats() {
        return repository.findAll().stream().filter(boat -> !boat.getDeleted()).collect(Collectors.toCollection(ArrayList::new));
    }

    public BoatCardDTO getBoatCard(Long id) {
        Boat boat = getBoat(id);
        String address = boat.getAddress().getStreet() + " " + boat.getAddress().getNumber() + ", " + boat.getAddress().getPlace() + ", " + boat.getAddress().getCountry();
        return new BoatCardDTO(boat.getId(), boat.getImages().get(0).getPath(), boat.getTitle(), boat.getDescription(), address);
    }

    public Boolean addQuickReservation(Long id, BoatQuickReservationDTO quickReservationDTO) {
        Boat boat = this.getBoat(id);
        BoatReservation reservation = getReservationFromDTO(quickReservationDTO, true);
        reservation.setResource(boat);
        boatReservationService.addReservation(reservation);
        boat.addReservation(reservation);
        this.addBoat(boat);
        //TODO proveri da li radi
        for (Long userId : boat.getSubClientUsernames()) {
            Client client = clientService.getById(String.valueOf(userId));
            String fullResponse = "Napravljena je akcija na koji ste se preplatili\n " +
                    "Avanture na brod kоšta " + reservation.getPrice() + "\n" +
                    "Zakazani period je od " + reservation.getAppointments().get(0).getStartTime().toString() + " do " +
                    reservation.getAppointments().get(reservation.getAppointments().size() - 1).getEndTime().toString();
            String additionalText = "<a href=\"" + "http://localhost:3000" + "\">Prijavite se i rezervišite je</a>";
            String emailForSubbedUser = emailService.buildHTMLEmail(client.getName(), fullResponse, additionalText, "Notifikacija o pretplacenim akcijama");
            emailService.send(client.getEmail(), emailForSubbedUser, "Notifikacija o pretplacenim akcijama");
        }
        return true;
    }

    private BoatReservation getReservationFromDTO(BoatQuickReservationDTO dto, Boolean isQuick) {
        List<Appointment> appointments = new ArrayList<Appointment>();
        String[] splitDate = dto.getStartDate().split(" ");
        String[] splitTime = splitDate[3].split(":");
        Appointment startDateAppointment = Appointment.getHourAppointment(Integer.parseInt(splitDate[2]), Integer.parseInt(splitDate[1]), Integer.parseInt(splitDate[0]), Integer.parseInt(splitTime[0]), Integer.parseInt(splitTime[1]));
        appointments.add(startDateAppointment);
        appointmentService.save(startDateAppointment);
        Appointment currApp = startDateAppointment;
        for (int i = 0; i < dto.getDuration() - 1; i++) {
            LocalDateTime startDate = currApp.getEndTime();
            LocalDateTime endDate = startDate.plusDays(1);
            currApp = new Appointment(startDate, endDate);
            appointmentService.save(currApp);
            appointments.add(currApp);
        }
        List<Tag> tags = new ArrayList<Tag>();
        for (String tagText : dto.getTagsText()) {
            Tag tag = new Tag(tagText);
            tagService.addTag(tag);
            tags.add(tag);
        }
        BoatReservation reservation = new BoatReservation(dto.getNumberOfPeople(), dto.getPrice());
        reservation.setClient(null);
        reservation.setAdditionalServices(tags);
        reservation.setAppointments(appointments);
        reservation.setQuickReservation(isQuick);
        return reservation;
    }

    public Boolean updateQuickReservation(Long id, BoatQuickReservationDTO quickReservationDTO) {
        Boat boat = this.getBoat(id);
        BoatReservation newReservation = getReservationFromDTO(quickReservationDTO, true);
        BoatReservation originalReservation = boatReservationService.getBoatReservation(quickReservationDTO.getReservationID());
        updateQuickReservation(originalReservation, newReservation);
        boatReservationService.addReservation(originalReservation);
        this.addBoat(boat);
        return true;
    }

    private void updateQuickReservation(BoatReservation originalReservation, BoatReservation newReservation) {
        originalReservation.setAppointments(newReservation.getAppointments());
        originalReservation.setAdditionalServices(newReservation.getAdditionalServices());
        originalReservation.setNumberOfClients(newReservation.getNumberOfClients());
        originalReservation.setPrice(newReservation.getPrice());
    }

    public List<ReservationDTO> getReservations(Long id) {
        Boat boat = this.getBoat(id);
        List<ReservationDTO> reservations = new ArrayList<ReservationDTO>();

        for (BoatReservation boatReservation : boat.getReservations()) {
            if (!boatReservation.isQuickReservation() && !boatReservation.isBusyPeriod()) {
                reservations.add(createDTOFromReservation(boatReservation));
            }
        }
        return reservations;
    }

    public Boolean deleteQuickReservation(Long id, BoatQuickReservationDTO quickReservationDTO) {
        Boat boat = this.getBoat(id);
        boatReservationService.deleteById(quickReservationDTO.getReservationID());
        //izbaci se reservation iz house
        this.addBoat(boat);
        return true;
    }

    public Long reserveQuickReservation(BoatQuickReservationDTO dto) {
        BoatReservation quickReservation = boatReservationService.getBoatReservation(dto.getReservationID());
        Client client = clientService.getById(dto.getClientID().toString());
        quickReservation.getResource().removeQuickReservation(quickReservation);
        quickReservation.setClient(client);
        quickReservation.setQuickReservation(false);
        //TODO napravi potvrdu o rezervaciji na akciju
        return boatReservationService.save(quickReservation);
    }

    public List<Boat> getOwnersBoats(Long owner_id) {
        return repository.findByOwnerId(owner_id);
    }

    public List<BoatReservation> getBoatReservations(Long boat_id) {
        return boatReservationService.getReservationsByBoatId(boat_id);
    }

    public List<BoatCardDTO> getOwnerBoats(Long owner_id) {
        List<Boat> boats = repository.findByOwnerId(owner_id);
        List<BoatCardDTO> boatCards = new ArrayList<BoatCardDTO>();
        for (Boat boat : boats) {
            if (boat.getDeleted()){
                continue;
            }
            String address = boat.getAddress().getStreet() + " " + boat.getAddress().getNumber() + ", " + boat.getAddress().getPlace() + ", " + boat.getAddress().getCountry();
            String thumbnail = "./images/housenotext.png";
            if (boat.getImages().size() > 0) {
                thumbnail = boat.getImages().get(0).getPath();
            }
            boatCards.add(new BoatCardDTO(boat.getId(), thumbnail, boat.getTitle(), boat.getDescription(), address));
        }
        return boatCards;
    }

    public ResourceOwnerDTO getOwner(Long id) {
        Boat boat = this.getBoat(id);
        BoatOwner owner = boat.getOwner();
        return new ResourceOwnerDTO(owner.getId(), owner.getName(), owner.getProfileImg());
    }

    public Boat getBoat(Long id) {
        return repository.getById(id);
    }

    public BoatDTO getBoatDTO(Long id) {
        Boat bt = repository.getById(id);
        if (bt.getDeleted())
            return null;
        String address = bt.getAddress().getStreet() + " " + bt.getAddress().getNumber() + ", " + bt.getAddress().getPlace() + ", " + bt.getAddress().getCountry();
        List<String> images = new ArrayList<String>();
        for (Image img : bt.getImages()) {
            images.add(img.getPath());
        }
        List<BoatQuickReservationDTO> quickReservations = getQuickReservations(bt);
        return new BoatDTO(bt.getId(), bt.getTitle(), address, bt.getAddress().getNumber(), bt.getAddress().getStreet(), bt.getAddress().getPlace(), bt.getAddress().getCountry(), bt.getDescription(), bt.getType(), images, bt.getRulesAndRegulations(), bt.getEngineNumber(), bt.getEngineStrength(), bt.getTopSpeed(), bt.getLength(), bt.getNavigationEquipment(), bt.getFishingEquipment(), bt.getAdditionalServices(), bt.getPricelist().getPrice(), bt.getCancellationFee(), bt.getCapacity(), quickReservations);
    }

    private List<BoatQuickReservationDTO> getQuickReservations(Boat bt) {
        List<BoatQuickReservationDTO> quickReservations = new ArrayList<BoatQuickReservationDTO>();
        for (BoatReservation reservation : bt.getReservations()) {
            if (reservation.getPrice() < bt.getPricelist().getPrice() && reservation.getClient() == null && reservation.isQuickReservation())
                quickReservations.add(createBoatReservationDTO(bt.getPricelist().getPrice(), reservation));
        }
        return quickReservations;
    }

    private BoatQuickReservationDTO createBoatReservationDTO(int boatPrice, BoatReservation reservation) {
        Appointment firstAppointment = getFirstAppointment(reservation.getAppointments());
        LocalDateTime startDate = firstAppointment.getStartTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm'h'");
        String strDate = startDate.format(formatter);
        int numberOfPeople = reservation.getNumberOfClients();
        List<Tag> additionalServices = reservation.getAdditionalServices();
        int duration = reservation.getAppointments().size();
        int price = reservation.getPrice();
        int discount = 100 - (100 * price / boatPrice);
        return new BoatQuickReservationDTO(reservation.getId(), strDate, numberOfPeople, additionalServices, duration, price, discount);
    }

    private Appointment getFirstAppointment(List<Appointment> appointments) {
        List<Appointment> sortedAppointments = getSortedAppointments(appointments);
        return sortedAppointments.get(0);
    }

    private List<Appointment> getSortedAppointments(List<Appointment> appointments) {
        Collections.sort(appointments, new Comparator<Appointment>() {
            @Override
            public int compare(Appointment a1, Appointment a2) {
                return a1.getStartTime().compareTo(a2.getStartTime());
            }
        });
        return appointments;
    }

    public void addBoat(Boat boat) {
        repository.save(boat);
    }

    public void deleteById(Long id) {
        Boat boat = this.getBoat(id);
        boat.setDeleted(true);
        this.addBoat(boat);
    }

    public Long createBoat(BoatDTO boat, MultipartFile[] multipartFiles) throws IOException {
        Boat bt = getBoatFromDTO(boat);
        this.addBoat(bt);
        List<String> paths = saveImages(bt, multipartFiles);
        List<Image> images = getImages(paths);
        bt.setImages(images);
        this.addBoat(bt);
        return bt.getId();
    }


    public BoatDTO updateBoat(String id, BoatDTO boatDTO, MultipartFile[] multipartFiles) throws IOException {
        Boat originalBoat = this.getBoat(Long.parseLong(id));
        Boat newBoat = getBoatFromDTO(boatDTO);
        updateBoatFromNew(originalBoat, newBoat);
        this.addBoat(originalBoat);
        List<String> paths = saveImages(originalBoat, multipartFiles);
        List<Image> images = getImages(paths);
        originalBoat.setImages(images);
        this.addBoat(originalBoat);
        return this.getBoatDTO(originalBoat.getId());
    }

    private List<Image> getImages(List<String> paths) {
        List<Image> images = new ArrayList<Image>();
        for (String path : paths) {
            Optional<Image> optImg = imageService.getImageByPath(path);
            Image img;
            img = optImg.orElseGet(() -> new Image(path));
            imageService.save(img);
            images.add(img);
        }
        return images;
    }

    private void updateBoatFromNew(Boat originalBoat, Boat newBoat) {
        originalBoat.setTitle(newBoat.getTitle());
        originalBoat.setPricelist(newBoat.getPricelist());
        originalBoat.setDescription(newBoat.getDescription());
        originalBoat.setType(newBoat.getType());
        originalBoat.setLength(newBoat.getLength());
        originalBoat.setTopSpeed(newBoat.getTopSpeed());
        originalBoat.setEngineNumber(newBoat.getEngineNumber());
        originalBoat.setEngineStrength(newBoat.getEngineStrength());
        originalBoat.setRulesAndRegulations(newBoat.getRulesAndRegulations());
        originalBoat.setAddress(newBoat.getAddress());
        originalBoat.setNavigationEquipment(newBoat.getNavigationEquipment());
        originalBoat.setFishingEquipment(newBoat.getFishingEquipment());
        originalBoat.setAdditionalServices(newBoat.getAdditionalServices());
        originalBoat.setCancellationFee(newBoat.getCancellationFee());
        originalBoat.setCapacity(newBoat.getCapacity());
        originalBoat.setImages(newBoat.getImages());
    }

    private List<String> saveImages(Boat boat, MultipartFile[] multipartFiles) throws IOException {
        List<String> paths = new ArrayList<>();
        if (multipartFiles == null) {
            return paths;
        }
        Path path = Paths.get(STATIC_PATH + IMAGES_PATH + boat.getId());
        Path path_target = Paths.get(STATIC_PATH_TARGET + IMAGES_PATH + boat.getId());
        savePicturesOnPath(boat, multipartFiles, paths, path);
        savePicturesOnPath(boat, multipartFiles, paths, path_target);
        if (boat.getImages() != null && boat.getImages().size() > 0) {
            for (Image image : boat.getImages()) {
                paths.add(image.getPath());
            }
        }
        return paths.stream().distinct().collect(Collectors.toList());
    }

    private void savePicturesOnPath(Boat boat, MultipartFile[] multipartFiles, List<String> paths, Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        for (MultipartFile mpf : multipartFiles) {
            String fileName = mpf.getOriginalFilename();
            try (InputStream inputStream = mpf.getInputStream()) {
                Path filePath = path.resolve(fileName);
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                paths.add(IMAGES_PATH + boat.getId() + "/" + fileName);
            } catch (DirectoryNotEmptyException dnee) {
                continue;
            } catch (IOException ioe) {
                throw new IOException("Could not save image file: " + fileName, ioe);
            }
        }
    }

    private Boat getBoatFromDTO(BoatDTO boatDTO) {
        Pricelist pl = new Pricelist(boatDTO.getPrice(), new Date());
        pricelistService.addPriceList(pl);
        Address adr = new Address(boatDTO.getCity(), boatDTO.getNumber(), boatDTO.getStreet(), boatDTO.getCountry());
        addressService.addAddress(adr);
        Boat boat = new Boat(boatDTO.getName(), adr, boatDTO.getDescription(), boatDTO.getRulesAndRegulations(), pl, boatDTO.getCancellationFee(), null, boatDTO.getType(), boatDTO.getLength(), boatDTO.getEngineNumber(), boatDTO.getEngineStrength(), boatDTO.getTopSpeed(), boatDTO.getCapacity());

        List<Tag> tags = new ArrayList<Tag>();
        for (String tagText : boatDTO.getTagsText()) {
            Tag tag = new Tag(tagText);
            tagService.addTag(tag);
            tags.add(tag);
        }
        boat.setNavigationEquipment(tags);

        List<Tag> tagsFishingEquip = new ArrayList<Tag>();
        for (String tagText : boatDTO.getTagsFishingEquipText()) {
            Tag tag = new Tag(tagText);
            tagService.addTag(tag);
            tagsFishingEquip.add(tag);
        }
        boat.setFishingEquipment(tagsFishingEquip);

        List<Tag> tagsAdditionalServices = new ArrayList<Tag>();
        for (String tagText : boatDTO.getTagsAdditionalServicesText()) {
            Tag tag = new Tag(tagText);
            tagService.addTag(tag);
            tagsAdditionalServices.add(tag);
        }
        boat.setAdditionalServices(tagsAdditionalServices);

        List<Image> images = new ArrayList<Image>();
        if (boatDTO.getImagePaths() != null) {
            for (String path : boatDTO.getImagePaths()) {
                Optional<Image> optImage = imageService.getImageByPath(path);
                optImage.ifPresent(images::add);
            }
        }
        boat.setImages(images);
        return boat;
    }

    public List<ReservationDTO> getReservationsForClient(Long id) {
        List<ReservationDTO> reservations = new ArrayList<ReservationDTO>();

        for (BoatReservation br : boatReservationService.getStandardReservations()) {
            if (Objects.equals(br.getClient().getId(), id) && !br.isBusyPeriod()) {
                reservations.add(createDTOFromReservation(br));
            }
        }
        return reservations;

    }

    public List<ReservationDTO> getReservationsForBoat(Long id) {
        List<ReservationDTO> reservations = new ArrayList<ReservationDTO>();

        for (BoatReservation br : boatReservationService.getAll()) {
            if (Objects.equals(br.getResource().getId(), id) && !br.isQuickReservation() && !br.isBusyPeriod()) {
                reservations.add(createDTOFromReservation(br));
            }
        }
        return reservations;

    }

    public List<ReservationDTO> getReservationsForOwner(Long id) {
        List<ReservationDTO> reservations = new ArrayList<ReservationDTO>();

        for (BoatReservation br : boatReservationService.getAll()) {
            if (Objects.equals(br.getResource().getOwner().getId(), id) && !br.isQuickReservation() && !br.isBusyPeriod()) {
                reservations.add(createDTOFromReservation(br));
            }
        }
        return reservations;

    }

    public Long createReservation(NewReservationDTO dto) throws ReservationNotAvailableException {
        BoatReservation reservation = createFromDTO(dto);

        List<BoatReservation> reservations = boatReservationService.getPossibleCollisionReservations(reservation.getResource().getId());
        for (BoatReservation r : reservations) {
            for (Appointment a : r.getAppointments()) {
                for (Appointment newAppointment : reservation.getAppointments()) {
                    reservationService.checkAppointmentCollision(a, newAppointment);
                }
            }
        }
        //TODO napravi potvrdu o rezervaciji na akciju
        boatReservationService.save(reservation);
        return reservation.getId();
    }


    private BoatReservation createFromDTO(NewReservationDTO dto) {

        List<Appointment> appointments = new ArrayList<Appointment>();

        LocalDateTime startTime = LocalDateTime.of(dto.getStartYear(), Month.of(dto.getStartMonth()), dto.getStartDay(), dto.getStartHour(), dto.getStartMinute());
        LocalDateTime endTime = startTime.plusHours(1);

        while (startTime.isBefore(LocalDateTime.of(dto.getEndYear(), Month.of(dto.getEndMonth()), dto.getEndDay(), dto.getEndHour(), dto.getEndMinute()))) {
            appointments.add(new Appointment(startTime, endTime));
            startTime = endTime;
            endTime = startTime.plusHours(1);
        }
        appointmentService.saveAll(appointments);

        Client client = clientService.getById(dto.getClientId().toString());
        Long id = dto.getResourceId();
        Boat boat = this.getBoat(id);

        int price = boat.getPricelist().getPrice() * appointments.size();

        List<Tag> tags = new ArrayList<Tag>();
        for (String text : dto.getAdditionalServicesStrings()) {
            Tag tag = new Tag(text);
            tags.add(tag);
        }

        tagService.saveAll(tags);

        return new BoatReservation(
                appointments,
                dto.getNumberOfClients(),
                tags,
                price,
                client,
                boat,
                dto.isBusyPeriod(), dto.isQuickReservation());
    }

    public Long createBusyPeriod(NewBusyPeriodDTO dto) {
        BoatReservation reservation = createBusyPeriodReservationFromDTO(dto);

        List<BoatReservation> reservations = boatReservationService.getPossibleCollisionReservations(reservation.getResource().getId());
        for (BoatReservation r : reservations) {
            for (Appointment a : r.getAppointments()) {
                for (Appointment newAppointment : reservation.getAppointments()) {
                    reservationService.checkAppointmentCollision(a, newAppointment);
                }
            }
        }
        boatReservationService.save(reservation);
        return reservation.getId();
    }

    private ReservationDTO createDTOFromReservation(BoatReservation r) {
        return new ReservationDTO(
                r.getAppointments(),
                r.getNumberOfClients(),
                r.getAdditionalServices(),
                r.getPrice(),
                r.getClient(),
                r.getResource().getTitle(),
                r.isBusyPeriod(),
                r.isQuickReservation(),
                r.getResource().getId(),
                r.getId()
        );
    }

    private BoatReservation createBusyPeriodReservationFromDTO(NewBusyPeriodDTO dto) {

        List<Appointment> appointments = new ArrayList<Appointment>();

        LocalDateTime startTime = LocalDateTime.of(dto.getStartYear(), Month.of(dto.getStartMonth()), dto.getStartDay(), 0, 0);
        LocalDateTime endTime = startTime.plusDays(1);


        while (startTime.isBefore(LocalDateTime.of(dto.getEndYear(), Month.of(dto.getEndMonth()), dto.getEndDay(), 23, 59))) {
            appointments.add(new Appointment(startTime, endTime));
            startTime = endTime;
            endTime = startTime.plusDays(1);
        }
        appointmentService.saveAll(appointments);

        Long id = dto.getResourceId();
        Boat boat = this.getBoat(id);

        return new BoatReservation(
                appointments,
                0,
                null,
                0,
                null,
                boat,
                true,
                false

        );
    }

    public List<ReservationDTO> getBusyPeriodForBoat(Long id) {
        List<ReservationDTO> periods = new ArrayList<ReservationDTO>();

        for (BoatReservation ar : boatReservationService.getBusyPeriodForBoat(id)) {
            periods.add(createDTOFromReservation(ar));
        }

        return periods;
    }

    public boolean clientCanReview(Long resourceId, Long clientId) {

        return hasReservations(resourceId, clientId);

    }

    private boolean hasReservations(Long resourceId, Long clientId) {
        return boatReservationService.hasReservations(resourceId, clientId);
    }

    public List<ReservationDTO> getReservationsForReview(Long id) {
        List<ReservationDTO> reservations = new ArrayList<ReservationDTO>();
        for (BoatReservation r : boatReservationService.getStandardReservations()) {
            if (!clientReviewService.reservationHasReview(r.getId())) {
                if (Objects.equals(r.getResource().getOwner().getId(), id)) {
                    int index = r.getAppointments().size() - 1;
                    LocalDateTime time = r.getAppointments().get(index).getEndTime();
                    if (time.isBefore(LocalDateTime.now())) {
                        reservations.add(createDTOFromReservation(r));
                    }

                }
            }
        }
        return reservations;
    }

    public List<String> getBoatTypes() {
        List<String> types = new ArrayList<>();
        for (Boat boat :
                getBoats()) {
            if (!types.contains(boat.getType()))
                types.add(boat.getType());
        }
        return types;
    }

    public boolean clientCanReviewVendor(Long vendorId, Long clientId) {
        return boatReservationService.clientCanReviewVendor(vendorId, clientId);
    }

    public List<String> getBoatAddress() {
        List<String> address = new ArrayList<>();
        String fullName = "";
        for (Boat boat :
                getBoats()) {
            fullName = boat.getAddress().getFullAddressName();
            if (!address.contains(fullName)) {
                address.add(fullName);
            }
        }
        return address;
    }

    public List<Boat> getFilteredBoats(BoatFilterDTO boatFilterDTO) {
        if (boatFilterDTO.isBoatsChecked()) {
            ArrayList<Boat> boats = new ArrayList<>();
            for (Boat boat : getBoats()) {
                if (checkBoatType(boatFilterDTO, boat) &&
                        checkOwnerName(boatFilterDTO, boat) &&
                        checkBoatEnginePower(boatFilterDTO, boat) &&
                        checkEngineNum(boatFilterDTO, boat) &&
                        checkBoatMaxSpeed(boatFilterDTO, boat) &&
                        checkBoatCapacity(boatFilterDTO, boat) &&
                        checkReviewRating(boatFilterDTO, boat) &&
                        checkLocation(boatFilterDTO, boat) &&
                        checkCancellationFee(boatFilterDTO, boat)
                )
                    boats.add(boat);
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm");
            String datetime = boatFilterDTO.getStartDate() + " " + boatFilterDTO.getStartTime();
            LocalDateTime startDateTime = LocalDateTime.parse(datetime, formatter);//ovde puca
            datetime = boatFilterDTO.getEndDate() + " " + boatFilterDTO.getEndTime();
            LocalDateTime endDateTime = LocalDateTime.parse(datetime, formatter);

            int numberOfDays = (int) ChronoUnit.DAYS.between(startDateTime.toLocalDate(), endDateTime.toLocalDate()) == 0 ? 1 : (int) ChronoUnit.DAYS.between(startDateTime.toLocalDate(), endDateTime.toLocalDate());
            int numberOfHours = (int) ChronoUnit.HOURS.between(startDateTime.toLocalTime(), endDateTime.toLocalTime());
            if (numberOfHours < 0) {
                numberOfHours += 24;
            }
            HashMap<LocalDateTime, Integer> listOfDatesBusyness = new HashMap<>();
            boolean remove = true;
            ArrayList<Boat> boatsToDelete = new ArrayList<>();
            for (Boat boat : boats) {
                if (!checkPrice(boatFilterDTO, boat.getPricelist().getPrice() * numberOfHours))
                    boats.remove(boat);
                for (int i = 0; i < numberOfDays; i++) {
                    for (int j = 0; j < numberOfHours; j++) {
                        listOfDatesBusyness.put(startDateTime.plusHours(j).plusDays(i), 0);
                    }
                }
                for (BoatReservation boatReservation : boatReservationService.getReservationsByBoatId(boat.getId())) {
                    LocalDateTime startAppointment = boatReservation.getAppointments().get(0).getStartTime();
                    LocalDateTime endAppointment = boatReservation.getAppointments().get(boatReservation.getAppointments().size() - 1).getEndTime();
                    for (LocalDateTime time : listOfDatesBusyness.keySet()) {
                        if ((startAppointment.isBefore(time) && endAppointment.isAfter(time)))
                            listOfDatesBusyness.replace(time, 1);
                    }
                }
                for (int i : listOfDatesBusyness.values()) {
                    if (i == 0) {
                        remove = false;
                        break;
                    }
                }
                if (remove)
                    boatsToDelete.add(boat);
                listOfDatesBusyness.clear();

            }
            for (Boat boat :
                    boatsToDelete) {
                boats.remove(boat);
            }
            return boats;
        } else {
            return new ArrayList<>();
        }
    }

    private boolean checkCancellationFee(BoatFilterDTO boatFilterDTO, Boat boat) {
        if (boatFilterDTO.isCancellationFee() && boat.getCancellationFee() == 0)
            return true;
        else if (!boatFilterDTO.isCancellationFee() && boat.getCancellationFee() != 0)
            return true;
        return false;
    }

    private boolean checkLocation(BoatFilterDTO boatFilterDTO, Boat boat) {
        if (boatFilterDTO.getLocation().isEmpty())
            return true;
        Address location = new Address(boatFilterDTO.getLocation());
        return boat.getAddress().getStreet().equals(location.getStreet()) &&
                boat.getAddress().getPlace().equals(location.getPlace()) &&
                boat.getAddress().getNumber().equals(location.getNumber()) &&
                boat.getAddress().getCountry().equals(location.getCountry());
    }

    public List<ResourceReportDTO> getOwnerResources(Long owner_id) {
        List<Boat> boats = repository.findByOwnerId(owner_id);
        List<ResourceReportDTO> resources = new ArrayList<ResourceReportDTO>();
        for (Boat resource : boats) {
            Image img = resource.getImages().get(0);
            resources.add(new ResourceReportDTO(resource.getId(), resource.getTitle(), img, clientReviewService.getRating(resource.getId(), "resource")));
        }
        return resources;
    }

    private boolean checkReviewRating(BoatFilterDTO boatFilterDTO, Boat boat) {
        List<ClientReviewDTO> list = clientReviewService.getResourceReviews(boat.getId());
        if (list.isEmpty() && (boatFilterDTO.getReviewRating().isEmpty() || boatFilterDTO.getReviewRating().equals("0")))
            return true;
        double score = list.stream().mapToDouble(ClientReviewDTO::getRating).sum() / list.size();
        return (boatFilterDTO.getReviewRating().isEmpty() || Double.parseDouble(boatFilterDTO.getReviewRating()) <= score);
    }

    private boolean checkBoatCapacity(BoatFilterDTO boatFilterDTO, Boat boat) {
        return boatFilterDTO.getBoatCapacity().isEmpty() || Integer.parseInt(boatFilterDTO.getBoatCapacity()) == boat.getCapacity();
    }

    private boolean checkBoatMaxSpeed(BoatFilterDTO boatFilterDTO, Boat boat) {
        return boatFilterDTO.getBoatMaxSpeed().isEmpty() || Double.parseDouble(boatFilterDTO.getBoatMaxSpeed()) == boat.getTopSpeed();
    }

    private boolean checkEngineNum(BoatFilterDTO boatFilterDTO, Boat boat) {
        return boatFilterDTO.getBoatEngineNum().isEmpty() || boatFilterDTO.getBoatEngineNum().equals(boat.getEngineNumber());
    }

    private boolean checkBoatEnginePower(BoatFilterDTO boatFilterDTO, Boat boat) {
        return (boatFilterDTO.getBoatEnginePower().isEmpty() || Double.parseDouble(boatFilterDTO.getBoatEnginePower()) == boat.getEngineStrength());
    }

    private boolean checkOwnerName(BoatFilterDTO boatFilterDTO, Boat boat) {
        return ((boat.getOwner().getFirstName() + " " + boat.getOwner().getLastName()).equals(boatFilterDTO.getBoatOwnerName()) || boatFilterDTO.getBoatOwnerName().isEmpty());
    }

    private boolean checkBoatType(BoatFilterDTO boatFilterDTO, Boat boat) {
        return boatFilterDTO.getBoatType().isEmpty() || boatFilterDTO.getBoatType().equals(boat.getType());
    }

    private boolean checkPrice(BoatFilterDTO boatFilterDTO, int price) {
        return boatFilterDTO.getPriceRange().isEmpty() || (boatFilterDTO.getPriceRange().get(0) <= price && price <= boatFilterDTO.getPriceRange().get(1));
    }

    public String subscribeBoatUserOnBoat(SubscribeDTO subscribeDTO) {
        Boat boat = getBoat(subscribeDTO.getEntityId());
        boat.getSubClientUsernames().add(subscribeDTO.getUserId());
        return "Uspešno ste prijavljeni na akcije ovog broda";
    }

    public Boolean isUserSubscribedToBoat(SubscribeDTO subscribeDTO) {
        Boat boat = getBoat(subscribeDTO.getEntityId());
        return boat.getSubClientUsernames().contains(subscribeDTO.getUserId());
    }

    public double getBoarRating(Long id) {
        List<ClientReviewDTO> list = clientReviewService.getResourceReviews(id);
        return list.isEmpty() ? 0 : list.stream().mapToDouble(ClientReviewDTO::getRating).sum() / list.size();
    }

    public String unsubscribeBoatUserOnBoat(SubscribeDTO subscribeDTO) {
        Boat boat = getBoat(subscribeDTO.getEntityId());
        boat.getSubClientUsernames().remove(subscribeDTO.getUserId());
        return "Uspešno ste se odjavili na akcije ovog broda";
    }

    public List<EntitySubbedDTO> getClientsSubscribedBoats() {
        List<EntitySubbedDTO> entities=new ArrayList<>();
        for(Boat boat :getBoats()){
            entities.add(new EntitySubbedDTO(
                    boat.getTitle(),
                    "boat",
                    boat.getImages().get(0),
                    getBoarRating(boat.getId()),
                    boat.getId(),
                    boat.getAddress(),
                    boat.getPricelist().getPrice()
            ));
        }
        return entities;
    }
}