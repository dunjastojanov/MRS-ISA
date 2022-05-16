import {React, useState, useRef} from 'react';
import axios from "axios";  
import { useParams } from "react-router-dom";
import {Modal, Button, Form, Row, Col, InputGroup} from 'react-bootstrap'
import { TagInfo } from '../Info';

function AddHouseQuickReservation({showModal, closeModal}) {
    const statePlaceHolder = {startDate:'', price:'', discount:'', numberOfPeople:'', duration:'', additionalServices:[{id:0, text:''}]};
    let [state, setState] = useState(statePlaceHolder)
    const [tagText, setTagText] = useState('');
    const [validated, setValidated] = useState(false);
    const form = useRef();
    const {id} = useParams();

    const submit = e => {
        e.preventDefault()
    
        if (form.current.checkValidity() === false) {
          e.stopPropagation();
          setValidated(true);
        }
        else {   
            var data = new FormData(form.current);   
            state.tagsText = []
            for (let i=0; i < state.additionalServices.length; i++){
                if (state.additionalServices[i].text !== ''){
                    state.tagsText.push(state.additionalServices[i].text)
                }
            } 
            data.append("tagsText", state.tagsText)
            axios
            .post("http://localhost:4444/house/addQuickReservation/" + id, data)
            .then(res => {
                window.location.reload();
            });
            close();
        }
      
      }
      function close(){
        Reset();
        closeModal();
      }
      const Reset = () => {
        setValidated(false);
        setState(statePlaceHolder);
      }

    const setStartDate = (val) => {
        setState( prevState => {
            return {...prevState, startDate:val}
        })
    }
    const setDuration = (value) => {
        setState( prevState => {
           return {...prevState, duration:value}
        })
    }
    const setNumberOfPeople = (value) => {
        setState( prevState => {
           return {...prevState, numberOfPeople:value}
        })
    }
    
    const setPrice = (value) => {
        setState( prevState => {
           return {...prevState, price:value}
        })
    }

    function addButton() {
        if (tagText !== ''){
            setState( prevState => {
                return {...prevState, additionalServices:[...prevState.additionalServices, {id:prevState.additionalServices.at(-1).id+1, text:tagText}]}
            })
            setTagText('')
        }
    }

    return (
        <Modal show={showModal} onHide={close} >
        <Form ref={form} noValidate validated={validated}>
            <Modal.Header>
                <Modal.Title>Dodavanje akcije</Modal.Title>
            </Modal.Header>
            <Modal.Body>
          
                <Form.Group className="mb-3">
                            <Form.Label>Početak važenja akcije</Form.Label>
                            <Form.Control required type="date" name="startDate" onChange={e => setStartDate(e.target.value)}/>
                            <Form.Control.Feedback type="invalid">Molimo Vas unesite datum.</Form.Control.Feedback>               
                </Form.Group>

                <Row className="mb-3">

                    <Form.Group as={Col} >
                        <Form.Label>Broj dana za koje akcija važi</Form.Label>
                        <Form.Control required type="number" min={1} name="duration" onChange={e => setDuration(e.target.value)}/>
                        <Form.Control.Feedback type="invalid">Molimo Vas unesite broj dana za koje akcija važi.</Form.Control.Feedback>
                    </Form.Group>

                    <Form.Group as={Col}>
                        <Form.Label>Maksimalan broj osoba</Form.Label>
                        <Form.Control required type="number" min={1} name="numberOfPeople" onChange={e => setNumberOfPeople(e.target.value)}/>
                        <Form.Control.Feedback type="invalid">Molimo Vas unesite broj ljudi za koje važi akcija..</Form.Control.Feedback>
                    </Form.Group>
                </Row>

             
                <Form.Group as={Col} >
                    <Form.Label>Cena po noćenju</Form.Label>
                    <InputGroup>
                        <Form.Control required type="number" min={1} name="price" onChange={e => setPrice(e.target.value)}/>
                        <InputGroup.Text>€</InputGroup.Text>
                        <Form.Control.Feedback type="invalid">Molimo Vas unesite grad.</Form.Control.Feedback>
                    </InputGroup>
                </Form.Group>

                <Form.Group as={Col} controlId="formGridServices">
                <Form.Label>Dodatne usluge</Form.Label>
                    <div className='d-flex justify-content-start'>
                        <TagInfo tagList={state.additionalServices} edit={true} setState={setState} entity="additionalServices"/>
                        <InputGroup className="p-0 mt-2" style={{maxWidth:"17vh", minWidth:"17vh"}}>
                            <Form.Control style={{height:"4vh"}} aria-describedby="basic-addon2" placeholder='Dodaj tag' value={tagText} onChange={e => setTagText(e.target.value)}/>        
                            <Button className="p-0 pe-2 ps-2" style={{height:"4vh"}} variant="primary" id="button-addon2" onClick={addButton}> + </Button>
                        </InputGroup>
                    </div>
                </Form.Group>

            </Modal.Body>
            <Modal.Footer>
                <Button variant="secondary" onClick={close}>Zatvori</Button>
                <Button variant="primary" onClick={submit}>Dodaj</Button>
            </Modal.Footer>
        </Form>
      </Modal>
    );
}

export default AddHouseQuickReservation;