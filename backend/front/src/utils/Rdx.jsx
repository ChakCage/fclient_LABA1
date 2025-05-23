import Utils from "./Utils";
import { createStore, combineReducers, applyMiddleware } from 'redux';
import { createLogger } from 'redux-logger';

// Константы для типов действий
export const userConstants = {
    LOGIN: 'USER_LOGIN',
    LOGOUT: 'USER_LOGOUT',
};

// Константы для типов оповещений
export const alertConstants = {
    SUCCESS: 'ALERT_SUCCESS',
    ERROR: 'ALERT_ERROR',
    CLEAR: 'ALERT_CLEAR'
};

// Генераторы действий (Action Creators)
export const userActions = {
    login(user) {
        Utils.saveUser(user);
        return { type: userConstants.LOGIN, user };
    },
    logout() {
        Utils.removeUser();
        return { type: userConstants.LOGOUT };
    }
};

export const alertActions = {
    success(message) {
        return { type: alertConstants.SUCCESS, message };
    },
    error(message) {
        return { type: alertConstants.ERROR, message };
    },
    clear() {
        return { type: alertConstants.CLEAR };
    }
};

// Начальное состояние для аутентификации
let user = Utils.getUser();
const initialState = user ? { user } : {};

// Редюсер для аутентификации
function authentication(state = initialState, action) {
    console.log("authentication");
    switch (action.type) {
        case userConstants.LOGIN:
            return { user: action.user };
        case userConstants.LOGOUT:
            return { };
        default:
            return state;
    }
}

// Редюсер для обработки оповещений
function alert(state = {}, action) {
    switch (action.type) {
        case alertConstants.SUCCESS:
            return {
                type: 'success',
                message: action.message
            };
        case alertConstants.ERROR:
            return {
                type: 'error',
                message: action.message
            };
        case alertConstants.CLEAR:
            return {};
        default:
            return state;
    }
}

// Комбинирование редюсеров
const rootReducer = combineReducers({
    authentication,
    alert
});

// Middleware для логирования действий
const loggerMiddleware = createLogger();

// Создание хранилища с несколькими редюсерами и middleware
export const store = createStore(
    rootReducer,
    applyMiddleware(loggerMiddleware)
);