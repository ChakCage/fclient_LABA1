import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import BackendService from '../services/BackendService';

const CountryComponent = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [name, setName] = useState('');
    const [submitted, setSubmitted] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');

    useEffect(() => {
        // Если id не -1, загружаем данные страны для редактирования
        if (id !== '-1') {
            BackendService.retrieveCountry(id)
                .then(response => {
                    console.log("Загруженные данные страны:", response.data);
                    setName(response.data.name);
                })
                .catch(error => {
                    console.error("Ошибка загрузки страны:", error);
                    setErrorMessage("Ошибка при загрузке данных страны");
                });
        }
    }, [id]);

    const handleSubmit = (event) => {
        event.preventDefault();
        setSubmitted(true);
        setErrorMessage('');

        if (name) {
            if (id === '-1') {
                // Создание новой страны
                const newCountry = { name };
                BackendService.createCountry(newCountry)
                    .then(response => {
                        console.log("Страна успешно создана:", response.data);
                        navigate('/countries');
                    })
                    .catch(error => {
                        console.error("Ошибка создания страны:", error);
                        if (error.response && error.response.data && error.response.data.message) {
                            setErrorMessage(error.response.data.message);
                        } else {
                            setErrorMessage("Ошибка при создании страны");
                        }
                    });
            } else {
                // Обновление существующей страны
                const updatedCountry = {
                    id: id,
                    name
                };

                BackendService.updateCountry(updatedCountry)
                    .then(response => {
                        console.log("Страна успешно обновлена:", response.data);
                        navigate('/countries');
                    })
                    .catch(error => {
                        console.error("Ошибка обновления страны:", error);
                        if (error.response && error.response.data && error.response.data.message) {
                            setErrorMessage(error.response.data.message);
                        } else {
                            setErrorMessage("Ошибка при обновлении страны");
                        }
                    });
            }
        }
    };

    const handleCancel = () => {
        navigate('/countries');
    };

    return (
        <div className="container">
            <div className="row">
                <div className="col-md-6 offset-md-3">
                    <h2>{id === '-1' ? 'Добавление страны' : 'Редактирование страны'}</h2>
                    {errorMessage && <div className="alert alert-danger">{errorMessage}</div>}
                    <form onSubmit={handleSubmit}>
                        <div className="form-group mb-3">
                            <label htmlFor="name">Название</label>
                            <input
                                id="name"
                                type="text"
                                className={`form-control ${submitted && !name ? 'is-invalid' : ''}`}
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                            />
                            {submitted && !name && <div className="invalid-feedback">Введите название страны</div>}
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

export default CountryComponent;