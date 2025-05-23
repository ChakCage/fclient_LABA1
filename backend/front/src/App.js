import './App.css';
import React from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import {createBrowserHistory} from "history";

import NavigationBar from "./components/NavigationBar";
import Home from "./components/Home";
import LoginComponent from "./components/Login";

function App() {
    return (
        <div className="App">
            <BrowserRouter>
                <NavigationBar />
                <div className="container-fluid">
                    <Routes>
                        <Route path="home" element={<Home />}/>
                        <Route path="login" element={<LoginComponent />}/>
                    </Routes>
                </div>
            </BrowserRouter>
        </div>
    );
}

export default App;
