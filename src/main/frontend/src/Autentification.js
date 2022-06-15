import {boatOwner, client, fishingInstructor, frontLink, vacationHouseOwner} from "./Consts";

export function isLoggedIn() {
    return localStorage.getItem('token') !== null && localStorage.getItem('token') !== "";
}

export function isMyPage(role, id) {
    let myPage;

    if (isLoggedIn()) {
        let userRole = localStorage.getItem("userRoleName");
        let userId = localStorage.getItem("userId");
        myPage = userRole === role && userId.toString() === id.toString();
    }
    return myPage;
}

export function isClient() {
    return localStorage.getItem("userRoleName") === client;
}

export function isVacationHouseOwner() {
    return localStorage.getItem("userRoleName") === vacationHouseOwner;
}

export function isBoatOwner() {
    return localStorage.getItem("userRoleName") === boatOwner;
}

export function isFishingInstructor() {
    return localStorage.getItem("userRoleName") === fishingInstructor;
}

export function getProfileLink(){
    if (isLoggedIn()) {
        if (isClient()) {
            return frontLink + "client/" + localStorage.getItem("userId");
        } else if (isVacationHouseOwner()) {
            return frontLink + "houseOwner/" + localStorage.getItem("userId");
        } else if (isBoatOwner()) {
            return frontLink + "boatOwner/" + localStorage.getItem("userId");
        } else if (isFishingInstructor()) {
            return frontLink + "fishingInstructor/" + localStorage.getItem("userId");
        }

    }
}
