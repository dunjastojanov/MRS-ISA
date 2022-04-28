import React, {useState} from 'react'
import 'bootstrap/dist/css/bootstrap.css';
import {Form} from "react-bootstrap";
import axios from "axios";
import LoginPopUp from "./LoginPopUp";

export default function Login() {
    function handleValidateLogIn({email, password}) {
        let loginDto={
            email:email,
            password:password
        }
        axios.post("http://localhost:4444/login/validate",loginDto).then(res => {
            console.log(res.data)
            if (res.data!==null && res.data > 0) {
                localStorage.setItem('user', res.data)
                setText("Uspesno ste se ulogovali")
                handleShow()
            } else {
                setText("Ne postoji ovaj klijent u bazi")
                handleShow()
            }
        })
    }

    const [text, setText] = useState("")
    const [show, setShow] = useState(false)

    const handleClose = () => setShow(false)
    const handleShow = () => setShow(true)


    const [form, setForm] = useState({})
    const [errors, setErrors] = useState({})

    const emailRegExp = new RegExp("[A-Za-z0-9]+@[a-z]+.(com)")
    const passwordExp = new RegExp(".[^ ]+")

    const findFormErrors = () => {
        const {email, password} = form
        const newErrors = {}

        if (!password || !passwordExp.test(password)) newErrors.password = 'cannot be blank!'

        if (!email || !emailRegExp.test(email)) newErrors.email = 'cannot be blank! must have @ and .com'

        return newErrors
    }

    const handleSubmit = e => {
        e.preventDefault()
        // get our new errors
        const newErrors = findFormErrors()
        // Conditional logic:
        if (Object.keys(newErrors).length > 0) {
            setErrors(newErrors)
        } else {
            handleValidateLogIn(form)
        }
    }
    const setField = (field, value) => {
        setForm({
            ...form,
            [field]: value
        })
        if (!!errors[field]) setErrors({
            ...errors,
            [field]: null
        })
    }
    return (
        <div className="mt-4 d-flex justify-content-center ">
            <img className="border rounded-start border-end-0 border-2 border-dark" src={require("./images/login.jpg")}
                 alt="login"/>
            <div className="py-5 px-4 border rounded-end border-2 border-start-0 border-dark d-flex flex-column"
                 style={{width: "335px"}}>
                <h1 className="mb-5">Login</h1>
                <Form.Group className="mb-3" controlId="formEmail">
                    <Form.Label>Email</Form.Label>
                    <Form.Control type="text"
                                  onChange={e => setField('email', e.target.value)}
                                  isInvalid={!!errors.email}/>
                    <Form.Control.Feedback type='invalid'>
                        {errors.email}
                    </Form.Control.Feedback>
                </Form.Group>
                <Form.Group className="mb-3" controlId="formPassword">
                    <Form.Label>Password</Form.Label>
                    <Form.Control type="password"
                                  onChange={e => setField('password', e.target.value)}
                                  isInvalid={!!errors.password}/>
                    <Form.Control.Feedback type='invalid'>
                        {errors.password}
                    </Form.Control.Feedback>
                </Form.Group>
                <button onClick={handleSubmit} type="submit" className="btn btn-primary btn-block mt-2">Submit
                </button>
                <div className="d-flex flex-column mt-5 pt-lg-5">
                    <label>Don't have an account?</label>
                    <button className="btn btn-primary btn-block mt-2">Register</button>
                    <label>Want to explore?</label>
                    <button className="btn btn-primary btn-block mt-2">Log in as a guest</button>
                </div>
            </div>
            <LoginPopUp text={text} show={show} handleClose={handleClose}/>
        </div>
    )
}