import React, {useState} from 'react';
import Home from "./Home";
import {useNavigate} from "react-router-dom";
import BackendService from "../services/BackendService";
import Utils from "../utils/Utils";

class Login extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            username: '',
            password: '',
            loggingIn: false,
            submitted: false,
            error_message: null,
        };

        this.handleChange = this.handleChange.bind(this)
        this.handleSubmit = this.handleSubmit.bind(this)
    }

    handleChange(e) {
        const { name, value } = e.target;
        this.setState({ [name]: value });
    }

    handleSubmit(e) {
        e.preventDefault();
        this.setState({ submitted: true, loggingIn: true, error_message: null });
        const { username, password } = this.state;
        BackendService.login(username, password)
            .then ( resp => {
                console.log(resp.data);
                Utils.saveUser(resp.data);
            })
            .catch( err => {
                if (err.response && err.response.status === 401)
                    this.setState( { error_message : "Ошибка авторизации", loggingIn: false } );
                else
                    this.setState( { error_message : err.message, loggingIn: false  } );
            })
            .finally(()=> this.setState({ loggingIn : false }));
    }

    render() {
        console.log("render");
        let { submitted, username, password, loggingIn } = this.state;
        return (
            <div className="col-md-6 me-0">
                {this.state.error_message &&
                    <div className="alert alert-danger mt-1 me-0 ms-0">{this.state.error_message}</div>}
            <h2>Вход</h2>
            <form name="form" onSubmit={this.handleSubmit}>
                <div className="form-group">
                    <label htmlFor="username">Логин</label>
                    <input type="text" className={'form-control' + (submitted && !username ? ' is-invalid' : '' )}
                            name="username" value={username}
                            onChange={this.handleChange} />
                    {submitted && !username && <div className="help-block text-danger">Введите имя пользователя</div>}
                </div>
                <div className="form-group">
                    <label htmlFor="password">Пароль</label>
                    <input type="password" className={'form-control' + (submitted && !password ? ' is-invalid' : '' )}
                            name="password" value={password}
                            onChange={this.handleChange} />
                    {submitted && !password &&
                    <div className="help-block text-danger">Введите пароль</div>
                    }
                </div>
                <div className="form-group mt-2">
                    <button className="btn btn-primary">
                        {loggingIn && <span className="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>}
                        Вход
                    </button>
                    </div>
                </form>
            </div>
        );
    }

    logout() {
        BackendService.logout().then(() => {
            Utils.removeUser();
            this.goHome()
        });
    }
}

// const LoginComponent = props => {
//     const navigate = useNavigate()
//
//     return <Login navigate={navigate} {...props} />
//
// }

export default function  LoginComponent() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [loggingIn, setLoggingIn] = useState(false);
    const [submitted, setSubmitted] = useState(false);
    const [error_message, setErrorMessage] = useState(null);
    const nav = useNavigate();

    function handleChangeLogin(e) {
        setUsername(e.target.value);
    }

    function handleChangePassword(e) {
        setPassword(e.target.value);
    }

    function handleSubmit(e) {
        e.preventDefault();
        setSubmitted(true);
        setErrorMessage(null);
        setLoggingIn(true);
        BackendService.login(username, password)
            .then ( resp => {
                console.log(resp.data);
                Utils.saveUser(resp.data);
                setLoggingIn(false);
                nav("/home");
            })
            .catch( err => {
                if (err.response && err.response.status === 401)
                    setErrorMessage("Ошибка авторизации");
                else
                    setErrorMessage(err.message);
                setLoggingIn(false);
            })
        }

        return  (
            <div className="col-md-6 me-0">
            {error_message &&
            <div className="alert alert-danger mt-1 me-0 ms-0">{error_message}</div>}
            <h2>Вход</h2>
            <form name="form" onSubmit={handleSubmit}>
                <div className="form-group">
                    <label htmlFor="username">Логин</label>
                    <input type="text" className={'form-control' + (submitted && !username ? ' is-invalid' : '' )}
                            name="username" value={username}
                            onChange={handleChangeLogin} />
                    {submitted && !username && <div className="help-block text-danger">Введите имя пользователя</div>}
                </div>
                <div className="form-group">
                    <label htmlFor="password">Пароль</label>
                    <input type="password" className={'form-control' + (submitted && !password ? ' is-invalid' : '' )}
                            name="password" value={password}
                            onChange={handleChangePassword} />
                    {submitted && !password &&
                    <div className="help-block text-danger">Введите пароль</div>
                    }
                </div>
                <div className="form-group mt-2">
                    <button className="btn btn-primary">
                        {loggingIn && <span className="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>}
                        Вход
                    </button>
                </div>
            </form>
        </div>
        );
}