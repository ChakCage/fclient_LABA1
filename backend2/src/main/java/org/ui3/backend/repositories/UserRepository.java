package org.ui3.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.ui3.backend.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>
{

}
