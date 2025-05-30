// BackendService.js
import axios from 'axios'
import Utils from "../utils/Utils";
import {alertActions, store, userConstants} from "../utils/Rdx";

const API_URL = 'http://localhost:8081/api/v1'
const AUTH_URL = 'http://localhost:8081/auth'

function showError(msg) {
    store.dispatch(alertActions.error(msg))
}


axios.interceptors.request.use(
    config => {
        store.dispatch(alertActions.clear())
        let token = Utils.getToken();
        console.log('Using token for request:', token);
        if (token)
            config.headers.Authorization = `Bearer ${token}`; // Добавьте префикс "Bearer"
        console.log('Request headers:', config.headers);
        return config;
    },
    error => {
        showError(error.message)
        return Promise.reject(error);
    }
)

// Добавьте перехватчик ответов:
axios.interceptors.response.use(
    response => {
        return response;
    },
    error => {
        const requestUrl = error.config ? error.config.url : '';
        console.log('Error response:', error.response);

        // Проверяем именно коды 401/403 для выхода из системы
        if (error.response && error.response.status &&
            [401, 403].indexOf(error.response.status) !== -1) {
            showError("Ошибка авторизации");

            // Выходим из системы только если это не запрос к countries
            if (!requestUrl.includes('/countries') && !requestUrl.includes('/deletecountries')) {
                store.dispatch({ type: userConstants.LOGOUT });
            }
        }
        // Другие ошибки просто показываем, но не выходим из системы
        else if (error.response && error.response.data && error.response.data.message) {
            showError(error.response.data.message);
        }
        else {
            showError(error.message);
        }

        return Promise.reject(error);
    }
);

class BackendService {
    login(login, password) {
        return axios.post(`${AUTH_URL}/login`, {login, password})
            .then(response => {
                console.log('Token received:', response.data.token);
                return response;
            });
    }

    logout() {
        return axios.get(`${AUTH_URL}/logout`);
    }
    /* Artists */
    retrieveAllArtistsDetailed() {
        return axios.get(`${API_URL}/artistsdetailed`);
    }

    retrieveArtist(id) {
        return axios.get(`${API_URL}/artists/${id}`);
    }

    createArtist(artist) {
        console.log("BackendService.createArtist:", artist);
        let url = `${API_URL}/artist/create?name=${encodeURIComponent(artist.name)}`;

        if (artist.age) {
            url += `&age=${encodeURIComponent(artist.age)}`;
        }

        if (artist.country && artist.country.id) {
            url += `&countryId=${artist.country.id}`;
        }

        return axios.post(url);
    }

    updateArtist(artist) {
        console.log("BackendService.updateArtist:", artist);
        let url = `${API_URL}/artist/update/${artist.id}?name=${encodeURIComponent(artist.name)}`;

        if (artist.age) {
            url += `&age=${encodeURIComponent(artist.age)}`;
        }

        if (artist.country && artist.country.id) {
            url += `&countryId=${artist.country.id}`;
        }

        return axios.put(url);
    }

    deleteArtists(artists) {
        const artistIds = artists.map(artist => artist.id);
        const params = new URLSearchParams();
        artistIds.forEach(id => {
            params.append('ids', id);
        });

        return axios.delete(`${API_URL}/artist/delete-by-id?${params.toString()}`);
    }

    /* Museums */
    retrieveAllMuseumsDetailed() {
        return axios.get(`${API_URL}/museumsdetailed`);
    }

    retrieveMuseum(id) {
        return axios.get(`${API_URL}/museums/${id}`);
    }

    createMuseum(museum) {
        console.log("BackendService.createMuseum:", museum);
        let url = `${API_URL}/museum/create?name=${encodeURIComponent(museum.name)}`;

        if (museum.location) {
            url += `&location=${encodeURIComponent(museum.location)}`;
        }

        return axios.post(url);
    }

    updateMuseum(museum) {
        console.log("BackendService.updateMuseum:", museum);
        let url = `${API_URL}/museum/update/${museum.id}?name=${encodeURIComponent(museum.name)}`;

        if (museum.location) {
            url += `&location=${encodeURIComponent(museum.location)}`;
        }

        return axios.put(url);
    }

    deleteMuseums(museums) {
        const museumIds = museums.map(museum => museum.id);
        const params = new URLSearchParams();
        museumIds.forEach(id => {
            params.append('ids', id);
        });

        return axios.delete(`${API_URL}/museum/delete-by-id?${params.toString()}`);
    }

    /* Paintings */
    retrieveAllPaintingsDetailed() {
        return axios.get(`${API_URL}/paintingsdetailed`);
    }

    retrievePainting(id) {
        return axios.get(`${API_URL}/paintings/${id}`);
    }

    createPainting(painting) {
        console.log("BackendService.createPainting:", painting);
        let url = `${API_URL}/painting/create?name=${encodeURIComponent(painting.name)}`;

        if (painting.year) {
            url += `&year=${painting.year}`;
        }

        if (painting.artist && painting.artist.id) {
            url += `&artistId=${painting.artist.id}`;
        }

        if (painting.museum && painting.museum.id) {
            url += `&museumId=${painting.museum.id}`;
        }

        return axios.post(url);
    }

    updatePainting(painting) {
        console.log("BackendService.updatePainting:", painting);
        let url = `${API_URL}/painting/update/${painting.id}?name=${encodeURIComponent(painting.name)}`;

        if (painting.year) {
            url += `&year=${painting.year}`;
        }

        if (painting.artist && painting.artist.id) {
            url += `&artistId=${painting.artist.id}`;
        }

        if (painting.museum && painting.museum.id) {
            url += `&museumId=${painting.museum.id}`;
        }

        return axios.put(url);
    }

    deletePaintings(paintings) {
        const paintingIds = paintings.map(painting => painting.id);
        const params = new URLSearchParams();
        paintingIds.forEach(id => {
            params.append('ids', id);
        });

        return axios.delete(`${API_URL}/painting/delete-by-id?${params.toString()}`);
    }

    /* Users */
    retrieveAllUsersDetailed() {
        return axios.get(`${API_URL}/usersdetailed`);
    }

    retrieveUser(id) {
        return axios.get(`${API_URL}/users/${id}`);
    }

    createUser(user) {
        console.log("BackendService.createUser:", user);
        let url = `${API_URL}/user/create`;

        // Используем URLSearchParams для передачи параметров
        const params = new URLSearchParams();
        params.append('login', user.login);
        params.append('email', user.email);
        params.append('password', user.password);

        return axios.post(url, params);
    }

    updateUser(user) {
        console.log("BackendService.updateUser:", user);
        let url = `${API_URL}/user/update/${user.id}`;

        // Используем URLSearchParams для передачи параметров
        const params = new URLSearchParams();
        params.append('login', user.login);
        params.append('email', user.email);

        // Если есть новый пароль, добавляем его в запрос
        if (user.password) {
            params.append('password', user.password);
        }

        return axios.put(url, params);
    }

    deleteUsers(users) {
        const userIds = users.map(user => user.id);
        const params = new URLSearchParams();
        userIds.forEach(id => {
            params.append('ids', id);
        });

        return axios.delete(`${API_URL}/user/delete-by-id?${params.toString()}`);
    }

// Методы для управления связью пользователь-музей
    addMuseumsToUser(userId, museumIds) {
        const params = new URLSearchParams();
        museumIds.forEach(id => {
            params.append('museumIds', id);
        });

        return axios.post(`${API_URL}/user/${userId}/addmuseums`, params);
    }

    removeMuseumsFromUser(userId, museumIds) {
        const params = new URLSearchParams();
        museumIds.forEach(id => {
            params.append('museumIds', id);
        });

        return axios.post(`${API_URL}/user/${userId}/removemuseums`, params);
    }
    /* Countries */

    retrieveAllCountries(page, limit) {
        return axios.get(`${API_URL}/countries?page=${page}&limit=${limit}`);
    }



    retrieveCountry(id) {
        return axios.get(`${API_URL}/countries/${id}`);
    }
    createCountry(country) {
        console.log("BackendService.createCountry:", country);
        // Используем GET запрос с параметрами в URL
        return axios.post(`${API_URL}/country/create?name=${encodeURIComponent(country.name)}`);
    }

    updateCountry(country) {
        console.log("BackendService.updateCountry:", country);
        // Используем GET запрос с параметрами в URL
        return axios.put(`${API_URL}/country/update/${country.id}?name=${encodeURIComponent(country.name)}`);
    }

    deleteCountries(countries) {
        const countryIds = countries.map(country => country.id);
        const params = new URLSearchParams();
        countryIds.forEach(id => {
            params.append('ids', id);
        });

        return axios.delete(`${API_URL}/country/delete-by-id?${params.toString()}`);
    }
}

export default new BackendService()