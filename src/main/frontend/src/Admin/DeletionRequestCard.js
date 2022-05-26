import {Badge, Button, Card, Form, Modal} from "react-bootstrap";
import React, {useEffect, useState} from "react";
import axios from "axios";
import {backLink} from "../Consts";

export function DeletionRequestCard({request}) {
    const [show, setShow] = useState(false);
    const [user, setUser] = useState();
    const [text, setComment] = useState("");

    const handleDeletionRequest = (type) => {
        let dto = {
            username: user.username,
            comment: text,
            requestId: request.id.toString(),
            type: type
        }
        console.log(dto)
        console.log(request.id)
        axios.post(backLink + "/deletionRequests/validateDeletion", dto).then(
            response => {
                console.log(response.data)
                setShow(false)
            }
        )
        window.location.reload();

    }

    const fetchUserData = () => {
        switch (request.userType) {
            case "CLIENT":
                axios.get(backLink + "/client/" + request.userDeletionIdentification).then(
                    response => {
                        console.log(response.data)
                        setUser(response.data)
                    }
                )
                break
            case "FISHING_INSTRUCTOR":
                axios.get(backLink + "/fishinginstructor/" + request.userDeletionIdentification).then(
                    response => {
                        console.log(response.data)
                        setUser(response.data)
                    }
                )
                break
            case "VACATION_HOUSE_OWNER":
                axios.get(backLink + "/houseowner/" + request.userDeletionIdentification).then(
                    response => {
                        console.log(response.data)
                        setUser(response.data)
                    }
                )
                break
            case "BOAT_OWNER":
                axios.get(backLink + "/boatowner/" + request.userDeletionIdentification).then(
                    response => {
                        console.log(response.data)
                        setUser(response.data)
                    }
                )
                break
        }
    }
    useEffect(() => {
        fetchUserData()
    }, [])

    let html = ""
    if (user) {
        html = <>
            <div>
                <Card className="m-3">
                    <Card.Header className="d-flex align-items-center">
                        {user.firstName + " " + user.lastName}
                        <Badge className="ms-auto" bg="primary">{request.userType}</Badge>
                    </Card.Header>
                    <Card.Body className="d-flex align-items-center">
                        <ul>
                            <li>Email: {user.email}</li>
                            <li>Broj telefona: {user.phoneNumber}</li>
                            <li>Adresa: {user.address.street + " " + user.address.number + ", " + user.address.place + ", " + user.address.country}</li>
                        </ul>

                        <Button onClick={() => setShow(true)} variant="outline-primary"
                                className="ms-auto m-1">Pregledaj</Button>
                        <Button variant="outline-success" className="m-1"
                                onClick={()=>handleDeletionRequest("approve")}
                        >Odobri</Button>
                        <Button variant="outline-danger" className="m-1"
                                onClick={()=>handleDeletionRequest("deny")}
                        >Poništi</Button>
                    </Card.Body>
                </Card>

                <Modal show={show} onHide={() => setShow(false)}>
                    <Modal.Header closeButton>
                        <Modal.Title>{request.name}</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        {request.text}

                        <hr className="mb-3 mt-3"/>

                        <Form.Group>
                            <Form.Label>Komentar</Form.Label>
                            <Form.Control as="textarea" rows={3} name="comment"
                                          onChange={e => setComment(e.target.value)}/>
                        </Form.Group>

                    </Modal.Body>
                    <Modal.Footer>
                        <Button onClick={() => setShow(false)} variant="outline-primary"
                                className="ms-auto m-1">Otkaži</Button>
                        <Button
                            onClick={()=>handleDeletionRequest("approve")}
                            variant="outline-success" className="m-1"
                        >Odobri</Button>
                        <Button
                            onClick={()=>handleDeletionRequest("deny")}
                                variant="outline-danger"
                                className="m-1">Poništi</Button>
                    </Modal.Footer>
                </Modal>
            </div>
        </>;
    }
    return html;
}