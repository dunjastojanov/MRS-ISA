import {useEffect, useState} from "react";
import axios from "axios";
import {backLink} from "../Consts";

export function GetAllFishingInstructors() {
    const [fishingInstructors, setFishingInstructors] = useState([]);

    const fetchFishingInstructors = () => {
        axios.get(backLink + "/fishinginstructor").then(res => {
            console.log(res.data);
            setFishingInstructors(res.data);
        });
    };

    useEffect(() => {
        fetchFishingInstructors();
    }, []);

    return fishingInstructors;
}