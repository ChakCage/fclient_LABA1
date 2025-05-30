import React, { useState, useEffect } from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faTrash, faEdit, faPlus } from '@fortawesome/free-solid-svg-icons'
import Alert from './Alert'
import BackendService from "../services/BackendService";
import { useNavigate } from 'react-router-dom';

const UserListComponent = props => {

    const [message, setMessage] = useState();
    const [users, setUsers] = useState([]);
    const [selectedUsers, setSelectedUsers] = useState([]);
    const [show_alert, setShowAlert] = useState(false);
    const [checkedItems, setCheckedItems] = useState([]);
    const [hidden, setHidden] = useState(false);
    const navigate = useNavigate();

    const setChecked = v =>  {
        setCheckedItems(Array(users.length).fill(v));
    }

    const handleCheckChange = e => {
        const idx = e.target.name;
        const isChecked = e.target.checked;

        let checkedCopy = [...checkedItems];
        checkedCopy[idx] = isChecked;
        setCheckedItems(checkedCopy);
    }

    const handleGroupCheckChange = e => {
        const isChecked = e.target.checked;
        setChecked(isChecked);
    }

    const deleteUsersClicked = () => {
        let x = [];
        users.map ((t, idx) => {
            if (checkedItems[idx]) {
                x.push(t)
            }
            return 0
        });
        if (x.length > 0) {
            var msg;
            if (x.length > 1) {
                msg = "Пожалуйста подтвердите удаление " + x.length + " пользователей";
            }
            else  {
                msg = "Пожалуйста подтвердите удаление пользователя " + x[0].login;
            }
            setShowAlert(true);
            setSelectedUsers(x);
            setMessage(msg);
        }
    }

    const refreshUsers = () => {
        BackendService.retrieveAllUsersDetailed()
            .then(
                resp => {
                    setUsers(resp.data);
                    setHidden(false);
                }
            )
            .catch((error)=> {
                console.error("Ошибка загрузки пользователей:", error);
                setHidden(true);
            })
            .finally(()=> setChecked(false))
    }

    useEffect(() => {
        refreshUsers();
    }, [])

    const updateUserClicked = id => {
        navigate(`/users/${id}`)
    }

    const onDelete = () =>  {
        BackendService.deleteUsers(selectedUsers)
            .then(() => refreshUsers())
            .catch(()=>{})
    }

    const closeAlert = () => {
        setShowAlert(false)
    }

    const addUserClicked = () => {
        navigate(`/users/-1`)
    }

    // Форматирование даты активности
    const formatDate = (dateString) => {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toLocaleString();
    }

    if (hidden)
        return null;
    return (
        <div className="m-4">
            <div className="row my-2">
                <h3>Пользователи</h3>
                <div className="btn-toolbar">
                    <div className="btn-group ms-auto">
                        <button className="btn btn-outline-secondary"
                                onClick={addUserClicked}>
                            <FontAwesomeIcon icon={faPlus} />{' '}Добавить
                        </button>
                    </div>
                    <div className="btn-group ms-2">
                        <button className="btn btn-outline-secondary"
                                onClick={deleteUsersClicked}>
                            <FontAwesomeIcon icon={faTrash} />{' '}Удалить
                        </button>
                    </div>
                </div>
            </div>
            <div className="row my-2 me-0">
                <table className="table table-sm">
                    <thead className="thead-light">
                    <tr>
                        <th>Логин</th>
                        <th>Email</th>
                        <th>Последняя активность</th>
                        <th>
                            <div className="btn-toolbar pb-1">
                                <div className="btn-group ms-auto">
                                    <input type="checkbox" onChange={handleGroupCheckChange} />
                                </div>
                            </div>
                        </th>
                    </tr>
                    </thead>
                    <tbody>
                    {
                        users && users.map((user, index) =>
                            <tr key={user.id}>
                                <td>{user.login}</td>
                                <td>{user.email}</td>
                                <td>{formatDate(user.activity)}</td>
                                <td>
                                    <div className="btn-toolbar">
                                        <div className="btn-group ms-auto">
                                            <button className="btn btn-outline-secondary btn-sm btn-toolbar"
                                                    onClick={() =>
                                                        updateUserClicked(user.id)}>
                                                <FontAwesomeIcon icon={faEdit} fixedWidth />
                                            </button>
                                        </div>
                                        <div className="btn-group ms-2 mt-1">
                                            <input type="checkbox" name={index}
                                                   checked={checkedItems.length > index ? checkedItems[index] : false}
                                                   onChange={handleCheckChange}/>
                                        </div>
                                    </div>
                                </td>
                            </tr>
                        )
                    }
                    </tbody>
                </table>
            </div>
            <Alert title="Удаление"
                   message={message}
                   ok={onDelete}
                   close={closeAlert}
                   modal={show_alert}
                   cancelButton={true} />
        </div>
    )
}

export default UserListComponent;