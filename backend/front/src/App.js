import './App.css';
import React from "react";
import {BrowserRouter, Route, Routes, Navigate} from "react-router-dom";
import NavigationBar from "./components/NavigationBar";
import Home from "./components/Home";
import Login from "./components/Login";
import Utils from "./utils/Utils";
import { Provider } from 'react-redux';
import { store } from './utils/Rdx';
import { connect } from 'react-redux';

const ProtectedRoute = ({ user, children }) => {
    return user ? children : <Navigate to={'/login'} />;
};

function mapStateToProps(state) {
    const { msg } = state.alert;
    return { error_message: msg };
}

const ConnectedProtectedRoute = connect(mapStateToProps)(ProtectedRoute);

function App(props) {

    return (
        <div className="App">
            <BrowserRouter>
                <NavigationBar />
                <div className="container-fluid">
                    {props.error_message &&
                        <div className="alert alert-danger m-1">{props.error_message}</div>}
                    <Routes>
                        <Route path="login" element={<Login />}/>
                        <Route path="home" element={<ProtectedRoute><Home/></ProtectedRoute>}/>
                    </Routes>
                </div>
            </BrowserRouter>
        </div>
    );
}

export default connect(mapStateToProps)(App);