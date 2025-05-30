import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import BackendService from '../services/BackendService';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faChevronLeft } from '@fortawesome/free-solid-svg-icons';
import axios from 'axios';

// Определяем API_URL или импортируем из BackendService
const API_URL = 'http://localhost:8081/api/v1';

const UserComponent = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [login, setLogin] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [museums, setMuseums] = useState([]);
    const [selectedMuseums, setSelectedMuseums] = useState([]);
    const [submitted, setSubmitted] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');

    useEffect(() => {
        // Загрузка списка музеев
        axios.get(`${API_URL}/museumsdetailed`)
            .then(response => {
                console.log("Ответ API музеев:", response.data);
                setMuseums(response.data);
            })
            .catch(error => {
                console.error("Ошибка загрузки музеев:", error);
                setMuseums([]);
            });

        // Если id не -1, загружаем данные пользователя для редактирования
        if (id !== '-1') {
            BackendService.retrieveUser(id)
                .then(response => {
                    const user = response.data;
                    console.log("Загруженный пользователь:", user);
                    setLogin(user.login);
                    setEmail(user.email);

                    // Если у пользователя есть музеи, сохраняем их ID
                    if (user.museums && user.museums.length > 0) {
                        setSelectedMuseums(user.museums.map(museum => museum.id));
                    }
                })
                .catch(error => {
                    console.error("Ошибка загрузки пользователя:", error);
                    setErrorMessage("Ошибка при загрузке данных пользователя");
                });
        }
    }, [id]);

    const handleSubmit = (event) => {
        event.preventDefault();
        setSubmitted(true);
        setErrorMessage('');

        if (login && email) {
            // Для нового пользователя пароль обязателен
            if (id === '-1' && !password) {
                setErrorMessage("Пароль обязателен для нового пользователя");
                return;
            }

            const user = {
                login,
                email,
                password
            };

            if (id === '-1') {
                // Создание нового пользователя
                BackendService.createUser(user)
                    .then(response => {
                        const newUser = response.data;

                        // Если выбраны музеи, добавляем их к пользователю
                        if (selectedMuseums.length > 0) {
                            return BackendService.addMuseumsToUser(newUser.id, selectedMuseums)
                                .then(() => navigate('/users'));
                        }

                        navigate('/users');
                    })
                    .catch(error => {
                        console.error("Ошибка создания пользователя:", error);
                        setErrorMessage("Ошибка при создании пользователя: " + (error.response?.data?.message || error.message));
                    });
            } else {
                // Обновление существующего пользователя
                user.id = parseInt(id, 10);
                BackendService.updateUser(user)
                    .then(() => {
                        // Обновляем связанные музеи
                        return BackendService.addMuseumsToUser(parseInt(id, 10), selectedMuseums)
                            .then(() => navigate('/users'));
                    })
                    .catch(error => {
                        console.error("Ошибка обновления пользователя:", error);
                        setErrorMessage("Ошибка при обновлении пользователя: " + (error.response?.data?.message || error.message));
                    });
            }
        }
    };

    const handleCancel = () => {
        navigate('/users');
    };

    // Обработка выбора музеев
    const handleMuseumChange = (museumId) => {
        if (selectedMuseums.includes(museumId)) {
            setSelectedMuseums(selectedMuseums.filter(id => id !== museumId));
        } else {
            setSelectedMuseums([...selectedMuseums, museumId]);
        }
    };

    return (
        <div className="container">
            <div className="row">
                <div className="col">
                    <h2 className="text-center">{id === '-1' ? 'Добавление пользователя' : 'Редактирование пользователя'}</h2>
                    <div className="text-end">
                        <button className="btn btn-outline-secondary" onClick={handleCancel}>
                            <FontAwesomeIcon icon={faChevronLeft}/>{' '}Назад
                        </button>
                    </div>
                </div>
            </div>

            {errorMessage && <div className="alert alert-danger">{errorMessage}</div>}

            <div className="row">
                <div className="col-md-6 offset-md-3">
                    <form onSubmit={handleSubmit}>
                        <div className="form-group mb-3">
                            <label htmlFor="login">Логин</label>
                            <input
                                type="text"
                                id="login"
                                className={`form-control ${submitted && !login ? 'is-invalid' : ''}`}
                                value={login}
                                onChange={(e) => setLogin(e.target.value)}
                            />
                            {submitted && !login && <div className="invalid-feedback">Введите логин</div>}
                        </div>

                        <div className="form-group mb-3">
                            <label htmlFor="email">Email</label>
                            <input
                                type="email"
                                id="email"
                                className={`form-control ${submitted && !email ? 'is-invalid' : ''}`}
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                            />
                            {submitted && !email && <div className="invalid-feedback">Введите email</div>}
                        </div>

                        <div className="form-group mb-3">
                            <label htmlFor="password">
                                {id === '-1' ? 'Пароль' : 'Новый пароль (оставьте пустым, чтобы не менять)'}
                            </label>
                            <input
                                type="password"
                                id="password"
                                className={`form-control ${submitted && id === '-1' && !password ? 'is-invalid' : ''}`}
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                            />
                            {submitted && id === '-1' && !password &&
                                <div className="invalid-feedback">Введите пароль</div>}
                        </div>

                        {museums.length > 0 && (
                            <div className="form-group mb-3">
                                <label>Доступные музеи</label>
                                <div className="border p-2 rounded">
                                    {museums.map(museum => (
                                        <div key={museum.id} className="form-check">
                                            <input
                                                className="form-check-input"
                                                type="checkbox"
                                                id={`museum-${museum.id}`}
                                                checked={selectedMuseums.includes(museum.id)}
                                                onChange={() => handleMuseumChange(museum.id)}
                                            />
                                            <label className="form-check-label" htmlFor={`museum-${museum.id}`}>
                                                {museum.name}
                                            </label>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        )}

                        <div className="form-group">
                            <button type="submit" className="btn btn-primary me-2">Сохранить</button>
                            <button type="button" className="btn btn-secondary" onClick={handleCancel}>Отмена</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default UserComponent;