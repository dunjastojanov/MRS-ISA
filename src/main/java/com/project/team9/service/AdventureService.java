package com.project.team9.service;

import com.project.team9.dto.*;
import com.project.team9.exceptions.ReservationNotAvailableException;
import com.project.team9.model.Address;
import com.project.team9.model.Image;
import com.project.team9.model.Tag;
import com.project.team9.model.buissness.Pricelist;
import com.project.team9.model.reservation.AdventureReservation;
import com.project.team9.model.reservation.Appointment;
import com.project.team9.model.resource.Adventure;
import com.project.team9.model.user.Client;
import com.project.team9.model.user.vendor.FishingInstructor;
import com.project.team9.repo.AdventureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sun.swing.BakedArrayList;
import sun.util.resources.LocaleData;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdventureService {
    private final AdventureRepository repository;
    private final FishingInstructorService fishingInstructorService;
    private final TagService tagService;
    private final AddressService addressService;
    private final PricelistService pricelistService;
    private final ImageService imageService;
    private final AppointmentService appointmentService;
    private final ClientService clientService;
    private final AdventureReservationService adventureReservationService;
    private final ReservationService reservationService;
    private final ReviewService reviewService;
    private final ReviewRequestService reviewRequestService;

    final String IMAGES_PATH = "/images/adventures/";

    @Autowired
    public AdventureService(AdventureRepository adventureRepository, FishingInstructorService fishingInstructorService, TagService tagService, AddressService addressService, PricelistService pricelistService, ImageService imageService, AppointmentService appointmentService, ClientService clientService, AdventureReservationService adventureReservationService, ReservationService reservationService, ReviewService reviewService, ReviewRequestService reviewRequestService) {
        this.repository = adventureRepository;
        this.fishingInstructorService = fishingInstructorService;
        this.tagService = tagService;
        this.addressService = addressService;
        this.pricelistService = pricelistService;
        this.imageService = imageService;
        this.appointmentService = appointmentService;
        this.clientService = clientService;
        this.adventureReservationService = adventureReservationService;
        this.reservationService = reservationService;
        this.reviewService = reviewService;
        this.reviewRequestService = reviewRequestService;
    }

    public List<AdventureQuickReservationDTO> getQuickReservations(String id) {
        Adventure adv = this.getById(id);
        return getQuickReservations(adv);
    }

    private List<AdventureQuickReservationDTO> getQuickReservations(Adventure adv) {
        List<AdventureQuickReservationDTO> quickReservations = new ArrayList<AdventureQuickReservationDTO>();
        for (AdventureReservation reservation : adv.getQuickReservations()) {
            if (reservation.isQuickReservation())
                quickReservations.add(createAdventureReservationDTO(adv.getPricelist().getPrice(), reservation));
        }
        return quickReservations;
    }

    public Boolean addQuickReservation(String id, AdventureQuickReservationDTO quickReservationDTO) {
        Adventure adventure = this.getById(id);
        AdventureReservation reservation = getReservationFromDTO(quickReservationDTO);
        reservation.setResource(adventure);
        adventureReservationService.save(reservation);
        adventure.addQuickReservations(reservation);
        this.addAdventure(adventure);
        return true;
    }

    private AdventureReservation getReservationFromDTO(AdventureQuickReservationDTO dto) {
        List<Appointment> appointments = new ArrayList<Appointment>();
        String[] splitDate = dto.getStartDate().split(" ");
        String[] splitTime = splitDate[3].split(":");
        Appointment startDateAppointment = Appointment.getBoatAppointment(Integer.parseInt(splitDate[2]), Integer.parseInt(splitDate[1]), Integer.parseInt(splitDate[0]), Integer.parseInt(splitTime[0]), Integer.parseInt(splitTime[1]));
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
        AdventureReservation reservation = new AdventureReservation(dto.getNumberOfPeople(), dto.getPrice());
        reservation.setClient(null);
        reservation.setAdditionalServices(tags);
        reservation.setAppointments(appointments);
        reservation.setQuickReservation(true);
        return reservation;
    }

    public Boolean updateQuickReservation(String id, AdventureQuickReservationDTO quickReservationDTO) {
        Adventure adventure = this.getById(id);
        AdventureReservation newReservation = getReservationFromDTO(quickReservationDTO);
        AdventureReservation originalReservation = adventureReservationService.getById(quickReservationDTO.getReservationID());
        updateQuickReservation(originalReservation, newReservation);
        adventureReservationService.save(originalReservation);
        this.addAdventure(adventure);
        return true;
    }

    private void updateQuickReservation(AdventureReservation originalReservation, AdventureReservation newReservation) {
        originalReservation.setAppointments(newReservation.getAppointments());
        originalReservation.setAdditionalServices(newReservation.getAdditionalServices());
        originalReservation.setNumberOfClients(newReservation.getNumberOfClients());
        originalReservation.setPrice(newReservation.getPrice());
    }

    private AdventureQuickReservationDTO createAdventureReservationDTO(int boatPrice, AdventureReservation reservation) {
        Appointment firstAppointment = getFirstAppointment(reservation.getAppointments());
        LocalDateTime startDate = firstAppointment.getStartTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm'h'");
        String strDate = startDate.format(formatter);
        int numberOfPeople = reservation.getNumberOfClients();
        List<Tag> additionalServices = reservation.getAdditionalServices();
        int duration = reservation.getAppointments().size();
        int price = reservation.getPrice();
        int discount = 100 - (100 * price / boatPrice);
        return new AdventureQuickReservationDTO(reservation.getId(), strDate, numberOfPeople, additionalServices, duration, price, discount);
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


    public List<Adventure> getAdventures() {
        return repository.findAll();
    }

    public void addAdventure(Adventure adventure) {
        repository.save(adventure);
    }

    public Adventure getById(String id) {
        return repository.getById(Long.parseLong(id));
    }

    public AdventureDTO getDTOById(String id) {
        return new AdventureDTO(repository.getById(Long.parseLong(id)));
    }

    public void deleteById(String id) {
        repository.deleteById(Long.parseLong(id));
    }

    private Adventure updateAdventure(Adventure oldAdventure, Adventure newAdventure) {
        oldAdventure.setTitle(newAdventure.getTitle());
        oldAdventure.setAddress(newAdventure.getAddress());
        oldAdventure.setDescription(newAdventure.getDescription());
        oldAdventure.setImages(newAdventure.getImages());
        oldAdventure.setAdditionalServices(newAdventure.getAdditionalServices());
        oldAdventure.setRulesAndRegulations(newAdventure.getRulesAndRegulations());
        oldAdventure.setPricelist(newAdventure.getPricelist());
        oldAdventure.setCancellationFee(newAdventure.getCancellationFee());
        oldAdventure.setOwner(newAdventure.getOwner());
        oldAdventure.setNumberOfClients(newAdventure.getNumberOfClients());
        oldAdventure.setFishingEquipment(newAdventure.getFishingEquipment());
        return oldAdventure;
    }

    public List<Adventure> findAdventuresWithOwner(String ownerId) {
        return repository.findAdventuresWithOwner(Long.parseLong(ownerId));
    }

    public Long createAdventure(AdventureDTO adventure, MultipartFile[] multipartFiles) throws IOException {

        Adventure newAdventure = createAdventureFromDTO(adventure);
        repository.save(newAdventure);
        addImagesToAdventure(multipartFiles, newAdventure);
        return newAdventure.getId();
    }

    private Adventure createAdventureFromDTO(AdventureDTO dto) {
        Pricelist pricelist = new Pricelist(dto.getPrice(), new Date());
        pricelistService.addPriceList(pricelist);

        Address address = new Address(dto.getPlace(), dto.getNumber(), dto.getStreet(), dto.getCountry());
        addressService.addAddress(address);

        FishingInstructor owner = fishingInstructorService.getById(dto.getOwnerId().toString());

        Adventure adventure = new Adventure(
                dto.getTitle(),
                address,
                dto.getDescription(),
                dto.getRulesAndRegulations(),
                pricelist,
                dto.getCancellationFee(),
                owner,
                dto.getNumberOfClients()
        );

        for (String text : dto.getAdditionalServicesText()) {
            Tag tag = new Tag(text);
            tagService.addTag(tag);
            adventure.addAdditionalService(tag);
        }

        for (String text : dto.getFishingEquipmentText()) {
            Tag tag = new Tag(text);
            tagService.addTag(tag);
            adventure.addFishingEquipment(tag);
        }

        List<Image> images = getExistingImages(dto);
        adventure.setImages(images);

        return adventure;
    }

    private List<Image> getExistingImages(AdventureDTO dto) {
        List<Image> images = new ArrayList<Image>();
        if (dto.getImagePaths() != null) {
            for (String path : dto.getImagePaths()) {
                Optional<Image> optImage = imageService.getImageByPath(path);
                optImage.ifPresent(images::add);
            }
        }
        return images;
    }

    public Adventure editAdventure(String id, AdventureDTO dto, MultipartFile[] multipartFiles) throws IOException {
        Adventure adventure = createAdventureFromDTO(dto);
        adventure.setId(Long.parseLong(id));
        addImagesToAdventure(multipartFiles, adventure);
        repository.save(adventure);

        System.out.println(adventure);
        return adventure;
    }

    private void addImagesToAdventure(MultipartFile[] multipartFiles, Adventure adventure) throws IOException {
        List<String> paths = imageService.saveImages(adventure.getId(), multipartFiles, IMAGES_PATH, adventure.getImages());
        List<Image> images = imageService.getImages(paths);
        adventure.setImages(images);
        repository.save(adventure);
    }

    public List<ReservationDTO> getReservationsForAdventure(Long id) {
        List<ReservationDTO> reservations = new ArrayList<ReservationDTO>();

        for (AdventureReservation ar : adventureReservationService.getStandardReservations()) {
            if (Objects.equals(ar.getResource().getId(), id)) {
                reservations.add(createDTOFromReservation(ar));
            }
        }
        return reservations;
    }

    private ReservationDTO createDTOFromReservation(AdventureReservation reservation) {
        return new ReservationDTO(reservation.getAppointments(), reservation.getNumberOfClients(), reservation.getAdditionalServices(), reservation.getPrice(), reservation.getClient(), reservation.getResource().getTitle(), reservation.isBusyPeriod(), reservation.isQuickReservation());
    }

    public List<ReservationDTO> getReservationsForFishingInstructor(Long id) {
        List<ReservationDTO> reservations = new ArrayList<ReservationDTO>();

        for (Adventure a : this.findAdventuresWithOwner(id.toString())) {
            for (AdventureReservation ar : adventureReservationService.getStandardReservations()) {
                if (Objects.equals(ar.getResource().getId(), a.getId())) {
                    reservations.add(createDTOFromReservation(ar));
                }
            }
        }

        return reservations;
    }

    public List<ReservationDTO> getReservationsForClient(Long id) {

        List<ReservationDTO> reservations = new ArrayList<ReservationDTO>();

        for (AdventureReservation ar : adventureReservationService.getStandardReservations()) {
            if (Objects.equals(ar.getClient().getId(), id)) {
                reservations.add(createDTOFromReservation(ar));
            }
        }

        return reservations;
    }

    public Long createReservation(NewReservationDTO dto) throws ReservationNotAvailableException {
        AdventureReservation reservation = createFromDTO(dto);

        List<AdventureReservation> reservations = adventureReservationService.getPossibleCollisionReservations(reservation.getResource().getId(), reservation.getResource().getOwner().getId());
        for (AdventureReservation r : reservations) {
            for (Appointment a : r.getAppointments()) {
                for (Appointment newAppointment : reservation.getAppointments()) {
                    reservationService.checkAppointmentCollision(a, newAppointment);
                }
            }
        }

        adventureReservationService.save(reservation);
        return reservation.getId();
    }

    private AdventureReservation createFromDTO(NewReservationDTO dto) {

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
        String id = dto.getResourceId().toString();
        Adventure adventure = this.getById(id);

        int price = adventure.getPricelist().getPrice() * appointments.size();

        List<Tag> tags = new ArrayList<Tag>();
        for (String text : dto.getAdditionalServicesStrings()) {
            Tag tag = new Tag(text);
            tags.add(tag);
        }

        tagService.saveAll(tags);

        return new AdventureReservation(
                appointments,
                dto.getNumberOfClients(),
                tags,
                price,
                client,
                adventure,
                dto.isBusyPeriod(), dto.isQuickReservation());
    }

    public List<ReservationDTO> getBusyPeriodsForAdventure(Long id) {
        List<ReservationDTO> periods = new ArrayList<ReservationDTO>();

        Adventure adventure = getById(id.toString());

        for (AdventureReservation ar : adventureReservationService.getBusyPeriodsForAdventure(id, adventure.getOwner().getId())) {
            periods.add(createDTOFromReservation(ar));
        }

        return periods;
    }

    public List<ReservationDTO> getBusyPeriodsForFishingInstructor(Long id) {
        List<ReservationDTO> periods = new ArrayList<ReservationDTO>();

        for (AdventureReservation ar : adventureReservationService.getBusyPeriodsForFishingInstructor(id)) {
            periods.add(createDTOFromReservation(ar));
        }

        return periods;
    }

    public Long createBusyPeriod(NewBusyPeriodDTO dto) throws ReservationNotAvailableException {

        AdventureReservation reservation = createBusyPeriodReservationFromDTO(dto);

        List<AdventureReservation> reservations = adventureReservationService.getPossibleCollisionReservations(reservation.getResource().getId(), reservation.getResource().getOwner().getId());
        for (AdventureReservation r : reservations) {
            for (Appointment a : r.getAppointments()) {
                for (Appointment newAppointment : reservation.getAppointments()) {
                    reservationService.checkAppointmentCollision(a, newAppointment);
                    reservationService.checkAppointmentCollision(newAppointment, a);
                }
            }
        }

        adventureReservationService.save(reservation);
        return reservation.getId();
    }

    private AdventureReservation createBusyPeriodReservationFromDTO(NewBusyPeriodDTO dto) {
        List<Appointment> appointments = new ArrayList<Appointment>();

        LocalDateTime startTime = LocalDateTime.of(dto.getStartYear(), Month.of(dto.getStartMonth()), dto.getStartDay(), 0, 0);
        LocalDateTime endTime = startTime.plusDays(1);

        while (startTime.isBefore(LocalDateTime.of(dto.getEndYear(), Month.of(dto.getEndMonth()), dto.getEndDay(), 23, 59))) {
            appointments.add(new Appointment(startTime, endTime));
            startTime = endTime;
            endTime = startTime.plusHours(1);
        }
        appointmentService.saveAll(appointments);

        String id = dto.getResourceId().toString();
        Adventure adventure = this.getById(id);

        return new AdventureReservation(
                appointments,
                0,
                null,
                0,
                null,
                adventure,
                true,
                false

        );
    }

    public boolean clientCanReview(Long resourceId, Long clientId) {
        return hasReservations(resourceId, clientId) &&
                !reviewService.clientHasReview(resourceId, clientId) &&
                !reviewRequestService.hasReviewRequests(resourceId, clientId);
    }

    public boolean hasReservations(Long resourceId, Long clientId) {
        return adventureReservationService.clientHasReservations(resourceId, clientId);
    }

    public List<Adventure> getFilteredAdventures(AdventureFilterDTO filterDTO) {
        if (filterDTO.isAdventuresChecked()) {
            ArrayList<Adventure> adventures = new ArrayList<>();//treba da prodjes i kroz brze rezervacije
            for (Adventure adventure : repository.findAll()) {
                if (checkNumberOfClient(filterDTO, adventure) &&
                        checkInstructorName(filterDTO, adventure) &&
                        checkReviewRating(filterDTO, adventure) &&
                        checkLocation(filterDTO, adventure) &&
                        checkCancellationFee(filterDTO, adventure)
                )
                    adventures.add(adventure);
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm");
            String datetime = filterDTO.getStartDate() + " " + filterDTO.getStartTime();
            LocalDateTime startDateTime = LocalDateTime.parse(datetime, formatter);//ovde puca
            datetime = filterDTO.getEndDate() + " " + filterDTO.getEndTime();
            LocalDateTime endDateTime = LocalDateTime.parse(datetime, formatter);

            int numberOfDays = (int) ChronoUnit.DAYS.between(startDateTime.toLocalDate(), endDateTime.toLocalDate()) == 0 ? 1 : (int) ChronoUnit.DAYS.between(startDateTime.toLocalDate(), endDateTime.toLocalDate());
            int numberOfHours = (int) ChronoUnit.HOURS.between(startDateTime.toLocalTime(), endDateTime.toLocalTime());
            if (numberOfHours < 0) {
                numberOfHours += 24;
            }
            HashMap<LocalDateTime, Integer> listOfDatesBusyness = new HashMap<>();
            boolean remove = true;
            for (Adventure adventure : adventures) {
                if (!checkPrice(filterDTO, adventure.getPricelist().getPrice() * numberOfHours))  //of days ce da bude za vikendice
                    adventures.remove(adventure); //cenu za sve dane sto ostaje
                for (int i = 0; i < numberOfDays; i++) {
                    for (int j = 0; j < numberOfHours; j++) {
                        listOfDatesBusyness.put(startDateTime.plusHours(j).plusDays(i), 0);
                    }
                }
                for (AdventureReservation adventureReservation : adventureReservationService.getReservationsByAdventureId(adventure.getId())) {
                    LocalDateTime startAppointment = adventureReservation.getAppointments().get(0).getStartTime();
                    LocalDateTime endAppointment = adventureReservation.getAppointments().get(adventureReservation.getAppointments().size()-1).getEndTime();
                    for (LocalDateTime time:listOfDatesBusyness.keySet()) {
                        if((startAppointment.isBefore(time) && endAppointment.isAfter(time)) || (startAppointment.isBefore(time.plusHours(1)) && endAppointment.isAfter(time.plusHours(1))))
                            listOfDatesBusyness.replace(time,1);
                    }
                }
                for (int i : listOfDatesBusyness.values()) {
                    if (i == 0) {
                        remove = false;
                        break;
                    }
                }
                if (remove)
                    adventures.remove(adventure);
                listOfDatesBusyness.clear();

            }
            //TODO filtriraj one koje nisu slobodne
            return adventures;
        } else {
            return new ArrayList<>();
        }
    }

    private boolean checkPrice(AdventureFilterDTO filterDTO, int price) {
        return filterDTO.getPriceRange().isEmpty() || (filterDTO.getPriceRange().get(0) <= price && price <= filterDTO.getPriceRange().get(1));
    }

    private boolean checkCancellationFee(AdventureFilterDTO filterDTO, Adventure adventure) {
        if (filterDTO.isCancellationFee() && adventure.getCancellationFee() == 0)
            return true;
        else if (!filterDTO.isCancellationFee() && adventure.getCancellationFee() != 0)
            return true;
        return false;
    }

    private boolean checkLocation(AdventureFilterDTO filterDTO, Adventure adventure) {
        if (filterDTO.getLocation().isEmpty())
            return true;
        Address location = new Address(filterDTO.getLocation());
        return adventure.getAddress().getStreet().equals(location.getStreet()) &&
                adventure.getAddress().getPlace().equals(location.getPlace()) &&
                adventure.getAddress().getNumber().equals(location.getNumber()) &&
                adventure.getAddress().getCountry().equals(location.getCountry());
    }

    private boolean checkReviewRating(AdventureFilterDTO filterDTO, Adventure adventure) {
        List<ClientReviewDTO> list = reviewService.getReviews(adventure.getId());
        if (list.isEmpty() && (filterDTO.getReviewRating().isEmpty() || filterDTO.getReviewRating().equals("0")))
            return true;
        double score = list.stream().mapToDouble(ClientReviewDTO::getRating).sum() / list.size();
        return (filterDTO.getReviewRating().isEmpty() || Double.parseDouble(filterDTO.getReviewRating()) <= score);
    }

    private boolean checkInstructorName(AdventureFilterDTO filterDTO, Adventure adventure) {
        return ((adventure.getOwner().getFirstName() + " " + adventure.getOwner().getLastName()).equals(filterDTO.getFishingInstructorName()) || filterDTO.getFishingInstructorName().isEmpty());
    }

    private boolean checkNumberOfClient(AdventureFilterDTO filterDTO, Adventure adventure) {
        return (filterDTO.getNumberOfClients().isEmpty() || Integer.parseInt(filterDTO.getNumberOfClients()) == adventure.getNumberOfClients());
    }


    public List<String> getAdventureAddress() {
        List<String> address = new ArrayList<>();
        String fullName = "";
        for (Adventure adventure :
                repository.findAll()) {
            fullName = adventure.getAddress().getFullAddressName();
            if (!address.contains(fullName)) {
                address.add(fullName);
            }
        }
        return address;
    }
}
