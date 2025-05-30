import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import BackendService from '../services/BackendService';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faChevronLeft } from '@fortawesome/free-solid-svg-icons';

const PaintingComponent = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [name, setName] = useState('');
    const [year, setYear] = useState('');
    const [artistId, setArtistId] = useState('');
    const [museumId, setMuseumId] = useState('');
    const [artists, setArtists] = useState([]);
    const [museums, setMuseums] = useState([]);
    const [submitted, setSubmitted] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');

    useEffect(() => {
        // Загрузка списка художников
        BackendService.retrieveAllArtistsDetailed()
            .then(response => {
                console.log("Ответ API художников:", response.data);
                setArtists(response.data);
            })
            .catch(error => {
                console.error("Ошибка загрузки художников:", error);
                setArtists([]);
                setErrorMessage("Ошибка при загрузке списка художников");
            });

        // Загрузка списка музеев
        BackendService.retrieveAllMuseumsDetailed()
            .then(response => {
                console.log("Ответ API музеев:", response.data);
                setMuseums(response.data);
            })
            .catch(error => {
                console.error("Ошибка загрузки музеев:", error);
                setMuseums([]);
                setErrorMessage("Ошибка при загрузке списка музеев");
            });

        // Если id не -1, загружаем данные картины для редактирования
        if (id !== '-1') {
            BackendService.retrievePainting(id)
                .then(response => {
                    const painting = response.data;
                    console.log("Загруженная картина:", painting);
                    setName(painting.name);
                    setYear(painting.year ? painting.year.toString() : '');
                    if (painting.artist && painting.artist.id) {
                        setArtistId(painting.artist.id);
                    }
                    if (painting.museum && painting.museum.id) {
                        setMuseumId(painting.museum.id);
                    }
                })
                .catch(error => {
                    console.error("Ошибка загрузки картины:", error);
                    setErrorMessage("Ошибка при загрузке данных картины");
                });
        }
    }, [id]);

    const handleSubmit = (event) => {
        event.preventDefault();
        setSubmitted(true);
        setErrorMessage('');

        if (name) {
            if (id === '-1') {
                // Создание новой картины
                const paintingData = {
                    name: name,
                    year: year ? parseInt(year) : null,
                    artist: artistId ? { id: parseInt(artistId) } : null,
                    museum: museumId ? { id: parseInt(museumId) } : null
                };

                BackendService.createPainting(paintingData)
                    .then(() => {
                        navigate('/paintings');
                    })
                    .catch(error => {
                        console.error("Ошибка создания картины:", error);
                        setErrorMessage("Ошибка при создании картины: " + (error.response?.data?.message || error.message));
                    });
            } else {
                // Обновление существующей картины
                const paintingData = {
                    id: parseInt(id),
                    name: name,
                    year: year ? parseInt(year) : null,
                    artist: artistId ? { id: parseInt(artistId) } : null,
                    museum: museumId ? { id: parseInt(museumId) } : null
                };

                BackendService.updatePainting(paintingData)
                    .then(() => {
                        navigate('/paintings');
                    })
                    .catch(error => {
                        console.error("Ошибка обновления картины:", error);
                        setErrorMessage("Ошибка при обновлении картины: " + (error.response?.data?.message || error.message));
                    });
            }
        }
    };

    const handleCancel = () => {
        navigate('/paintings');
    };

    return (
        <div className="container">
            <div className="row">
                <div className="col">
                    <h2 className="text-center">{id === '-1' ? 'Добавление картины' : 'Редактирование картины'}</h2>
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
                            {submitted && !name && <div className="invalid-feedback">Введите название картины</div>}
                        </div>

                        <div className="form-group mb-3">
                            <label htmlFor="artist">Художник</label>
                            <select
                                id="artist"
                                className="form-control"
                                value={artistId}
                                onChange={(e) => setArtistId(e.target.value)}
                            >
                                <option value="">Выберите художника</option>
                                {artists && artists.length > 0 ? (
                                    artists.map(artist => (
                                        <option key={artist.id} value={artist.id}>
                                            {artist.name}
                                        </option>
                                    ))
                                ) : (
                                    <option value="" disabled>Загрузка художников...</option>
                                )}
                            </select>
                        </div>

                        <div className="form-group mb-3">
                            <label htmlFor="museum">Музей</label>
                            <select
                                id="museum"
                                className="form-control"
                                value={museumId}
                                onChange={(e) => setMuseumId(e.target.value)}
                            >
                                <option value="">Выберите музей</option>
                                {museums && museums.length > 0 ? (
                                    museums.map(museum => (
                                        <option key={museum.id} value={museum.id}>
                                            {museum.name}
                                        </option>
                                    ))
                                ) : (
                                    <option value="" disabled>Загрузка музеев...</option>
                                )}
                            </select>
                        </div>

                        <div className="form-group mb-3">
                            <label htmlFor="year">Год</label>
                            <input
                                type="number"
                                id="year"
                                className="form-control"
                                value={year}
                                onChange={(e) => setYear(e.target.value)}
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

export default PaintingComponent;