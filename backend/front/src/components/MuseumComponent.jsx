import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import BackendService from '../services/BackendService';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faChevronLeft } from '@fortawesome/free-solid-svg-icons';
import axios from 'axios';

// Определяем API_URL или импортируем из BackendService
const API_URL = 'http://localhost:8089/api/v1';

const MuseumComponent = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [name, setName] = useState('');
    const [location, setLocation] = useState('');
    const [submitted, setSubmitted] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');

    useEffect(() => {
        // Если id не -1, загружаем данные музея для редактирования
        if (id !== '-1') {
            BackendService.retrieveMuseum(id)
                .then(response => {
                    const museum = response.data;
                    console.log("Загруженный музей:", museum);
                    setName(museum.name);
                    setLocation(museum.location || '');
                })
                .catch(error => {
                    console.error("Ошибка загрузки музея:", error);
                    setErrorMessage("Ошибка при загрузке данных музея");
                });
        }
    }, [id]);

    const handleSubmit = (event) => {
        event.preventDefault();
        setSubmitted(true);
        setErrorMessage('');

        if (name) {
            let url = `${API_URL}/museum/create?name=${encodeURIComponent(name)}`;

            if (location) {
                url += `&location=${encodeURIComponent(location)}`;
            }

            if (id === '-1') {
                // Создание нового музея
                axios.post(url)
                    .then(() => {
                        navigate('/museums');
                    })
                    .catch(error => {
                        console.error("Ошибка создания музея:", error);
                        setErrorMessage("Ошибка при создании музея: " + (error.response?.data?.message || error.message));
                    });
            } else {
                // Обновление существующего музея
                url = url.replace("/museum/create", `/museum/update/${id}`);
                axios.put(url)
                    .then(() => {
                        navigate('/museums');
                    })
                    .catch(error => {
                        console.error("Ошибка обновления музея:", error);
                        setErrorMessage("Ошибка при обновлении музея: " + (error.response?.data?.message || error.message));
                    });
            }
        }
    };

    const handleCancel = () => {
        navigate('/museums');
    };

    return (
        <div className="container">
            <div className="row">
                <div className="col">
                    <h2 className="text-center">{id === '-1' ? 'Добавление музея' : 'Редактирование музея'}</h2>
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
                            <label htmlFor="name">Название</label>
                            <input
                                type="text"
                                id="name"
                                className={`form-control ${submitted && !name ? 'is-invalid' : ''}`}
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                            />
                            {submitted && !name && <div className="invalid-feedback">Введите название музея</div>}
                        </div>

                        <div className="form-group mb-3">
                            <label htmlFor="location">Местоположение</label>
                            <input
                                type="text"
                                id="location"
                                className="form-control"
                                value={location}
                                onChange={(e) => setLocation(e.target.value)}
                            />
                        </div>

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

export default MuseumComponent;