import React, { useState, useEffect } from 'react';
import axios from "axios";    
import Banner from '../Banner';
import BeginButton from '../BeginButton';

import BoatOwnerForm from './BoatOwnerForm'
import OwnerInfo from '../OwnerInfo';
import OwnerBoats from './OwnerBoats';
import AddBoat from './AddBoat';
import { useParams } from "react-router-dom";
import Navigation from "../Navigation/Navigation";
import { profilePicturePlaceholder, backLink } from '../Consts';
import {backLink, profilePicturePlaceholder} from '../Consts';
import {Calendar} from "../Calendar/Calendar";
import {ReservationCardGrid} from "../Calendar/ReservationCardGrid";
import {Collapse} from "react-bootstrap";
import {ReservationsTable} from "../Calendar/ReservationsTable";

const  UpdateOwner = ({show, setShow, owner}) => {
    if (typeof owner.firstName !== "undefined"){
        if (owner.profileImg !== null) {
            var profileImg = backLink + owner.profileImg.path;
        }
        else {
            var profileImg = profilePicturePlaceholder;
        }
        return <BoatOwnerForm show={show} setShow={setShow} owner={owner} profileImg={profileImg}/>
    }
    else {
        return <></>
    }
}

function BoatOwnerPage() {
    const {id} = useParams();

    const [boatOwner, setboatOwner] = useState({address:'', profileImg:{path:""}});
    let [ownerBoats, setOwnerBoats] = useState([]);

    const [show, setShow] = useState(false);
    const handleShow = () => setShow(true);

    const [reservations, setReservations] = useState([]);
    const [open, setOpen] = useState(false);

    const fetchReservations = () => {
        axios.get(backLink+ "/boat/reservation/boatOwner/" + id).then(res => {
            setReservations(res.data);
        })
    }


    const HOST = "http://localhost:4444";
    const fetchOwnerBoats = () => {
      axios
      .get("http://localhost:4444/boat/getownerboats/" + id)
      .then(res => {
          var boats = res.data;
          for (let boat of boats){
              if (!boat.thumbnailPath.includes(HOST)){
                boat.thumbnailPath = HOST + boat.thumbnailPath;
              }
          }
          setOwnerBoats(res.data);
        });
    };
    const fetchboatOwner = () => {
        axios
        .get("http://localhost:4444/boatowner/" + id)
        .then(res => {
            setboatOwner(res.data);
        });
    };
    useEffect(() => {
        fetchboatOwner();
        fetchOwnerBoats();
        fetchReservations();
    }, [ownerBoats]);
    return (
        <>
            <Banner caption={boatOwner.firstName + " " + boatOwner.lastName}/>
            <Navigation buttons={
                [
                    {text: "Osnovne informacije", path: "#info"},
                    {text: "Brodovi", path: "#boats"},
                    {text: "Rezervacije", path: "#sales"},
                    {text: "Izveštaji", path: "#reports"}
                ]}
                        editable={true} editFunction={handleShow} searchable={true} showProfile={true}/>
            <AddBoat/>
            <UpdateOwner show={show} setShow={setShow} owner={boatOwner}/>
            

                <div className='p-5 pt-0'>
                    { boatOwner.profileImg !== null ?
                            <OwnerInfo bio = {boatOwner.registrationRationale}
                                name={boatOwner.firstName + " " + boatOwner.lastName}
                                rate = {4.5}
                                email={boatOwner.email}
                                phoneNum={boatOwner.phoneNumber}
                                address={boatOwner.address}
                                profileImg = {HOST + boatOwner.profileImg.path}
                                />
                        :
                    
                            <OwnerInfo bio = {boatOwner.registrationRationale}
                                name={boatOwner.firstName + " " + boatOwner.lastName}
                                rate = {4.5}
                                email={boatOwner.email}
                                phoneNum={boatOwner.phoneNumber}
                                address={boatOwner.address}
                                profileImg = {profilePicturePlaceholder}
                                />
                    }

                <hr/>
                <OwnerBoats boats={ownerBoats}/>
                <hr/>
               
            </div>

            <hr className="me-5 ms-5"/>
            <Calendar reservations={reservations} reservable={false}/>

            <h2 className="me-5 ms-5 mt-5" id="reservations">Predstojaće rezervacije</h2>
            <hr className="me-5 ms-5"/>

            <ReservationCardGrid reservations={reservations}/>

            <h2 className="me-5 ms-5 mt-5" onClick={() => setOpen(!open)}
                aria-controls="reservationsTable"
                aria-expanded={open}
                style = {{cursor: "pointer"}}
            >Istorija rezervacija</h2>

            <hr className="me-5 ms-5"/>
            <Collapse in={open}>
                <div id="reservationsTable">
                    <ReservationsTable  reservations={reservations} showResource={false}/>
                </div>
            </Collapse>

        <BeginButton/>
        </>
    );
}

export default BoatOwnerPage;