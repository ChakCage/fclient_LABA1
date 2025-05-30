package ru.iu3.backend.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.www.NonceExpiredException;
import org.springframework.stereotype.Component;
import ru.iu3.backend.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

@Component
public class AuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {
    @Autowired
    UserRepository userRepository;
    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {

    }

    @Override
    protected UserDetails retrieveUser(String userName, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {

        Object token = usernamePasswordAuthenticationToken.getCredentials();
        ru.iu3.backend.models.User user = userRepository.findByToken(String.valueOf(token));

        if (user == null)
            throw new UsernameNotFoundException("user is not found");


        boolean timeout = true;
        Date currentTime = new Date();
        if (user.getActivity() != null) {

            Calendar cal = Calendar.getInstance();
            cal.setTime(user.getActivity());
            cal.add(Calendar.MINUTE, 10);
            Date sessionExpiryTime = cal.getTime();


            if (currentTime.before(sessionExpiryTime))
                timeout = false;
        }

        if (timeout) {

            user.setToken(null);
            userRepository.save(user);
            throw new CredentialsExpiredException("session is expired");
        } else {

            user.setActivity(currentTime);
            userRepository.save(user);
        }

        return new User(user.getLogin(), user.getPassword(),
                true,
                true,
                true,
                true,
                AuthorityUtils.createAuthorityList("USER"));
    }
}
