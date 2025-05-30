import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import BackendService from '../services/BackendService';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faChevronLeft } from '@fortawesome/free-solid-svg-icons';
import axios from 'axios';

// Определяем API_URL или импортируем из BackendService
const API_URL = 'http://localhost:8081/api/v1';

const ArtistComponent = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [name, setName] = useState('');
    const [age, setAge] = useState('');
    const [countryId, setCountryId] = useState('');
    const [countries, setCountries] = useState([]);
    const [submitted, setSubmitted] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');

    useEffect(() => {
        // Прямой запрос к API для получения стран
        axios.get(`${API_URL}/allcountries`)
            .then(response => {
                console.log("Ответ API стран:", response.data);
                // Обрабатываем разные форматы ответа
                let countriesList = response.data;
                if (response.data.content) {
                    countriesList = response.data.content;
                }

                console.log("Список стран для отображения:", countriesList);
                setCountries(countriesList);
            })
            .catch(error => {
                console.error("Ошибка загрузки стран:", error);
                setCountries([]);
            });

        // Если id не -1, загружаем данные художника для редактирования
        if (id !== '-1') {
            BackendService.retrieveArtist(id)
                .then(response => {
                    const artist = response.data;
                    console.log("Загруженный художник:", artist);
                    setName(artist.name);
                    setAge(artist.age || '');
                    if (artist.country && artist.country.id) {
                        setCountryId(artist.country.id);
                    }
                })
                .catch(error => {
                    console.error("Ошибка загрузки художника:", error);
                    setErrorMessage("Ошибка при загрузке данных художника");
                });
        }
    }, [id]);

    const handleSubmit = (event) => {
        event.preventDefault();
        setSubmitted(true);
        setErrorMessage('');

        if (name) {
            let url = `${API_URL}/artist/create?name=${encodeURIComponent(name)}`;

            if (age) {
                url += `&age=${encodeURIComponent(age)}`;
            }

            if (countryId) {
                url += `&countryId=${countryId}`;
            }

            if (id === '-1') {
                // Создание нового художника
                axios.post(url)
                    .then(() => {
                        navigate('/artists');
                    })
                    .catch(error => {
                        console.error("Ошибка создания художника:", error);
                        setErrorMessage("Ошибка при создании художника: " + (error.response?.data?.message || error.message));
                    });
            } else {
                // Обновление существующего художника
                url = url.replace("/artist/create", `/artist/update/${id}`);
                axios.put(url)
                    .then(() => {
                        navigate('/artists');
                    })
                    .catch(error => {
                        console.error("Ошибка обновления художника:", error);
                        setErrorMessage("Ошибка при обновлении художника: " + (error.response?.data?.message || error.message));
                    });
            }
        }
    };

    const handleCancel = () => {
        navigate('/artists');
    };

    return (
        <div className="container">
            <div className="row">
                <div className="col">
                    <h2 className="text-center">{id === '-1' ? 'Добавление художника' : 'Редактирование художника'}</h2>
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
                            <label htmlFor="name">Имя</label>
                            <input
                                type="text"
                                id="name"
                                className={`form-control ${submitted && !name ? 'is-invalid' : ''}`}
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                            />
                            {submitted && !name && <div className="invalid-feedback">Введите имя художника</div>}
                        </div>

                        <div className="form-group mb-3">
                            <label htmlFor="country">Страна</label>
                            <select
                                id="country"
                                className="form-control"
                                value={countryId}
                                onChange={(e) => setCountryId(e.target.value)}
                            >
                                <option value="">Выберите страну</option>
                                {countries && countries.length > 0 ? (
                                    countries.map(country => (
                                        <option key={country.id} value={country.id}>
                                            {country.name}
                                        </option>
                                    ))
                                ) : (
                                    <option value="" disabled>Загрузка стран...</option>
                                )}
                            </select>
                            {countries && countries.length === 0 && (
                                <div className="text-danger mt-1">Нет доступных стран</div>
                            )}
                        </div>

                        <div className="form-group mb-3">
                            <label htmlFor="age">Возраст/Годы жизни</label>
                            <input
                                type="text"
                                id="age"
                                className="form-control"
                                value={age}
                                onChange={(e) => setAge(e.target.value)}
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

export default ArtistComponent;