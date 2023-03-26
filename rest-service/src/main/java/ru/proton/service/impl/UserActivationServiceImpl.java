package ru.proton.service.impl;

import org.springframework.stereotype.Service;
import ru.proton.dao.AppUserDao;
import ru.proton.entity.AppUser;
import ru.proton.service.UserActivationService;
import ru.proton.utils.CryptoTool;

import java.util.Optional;

@Service
public class UserActivationServiceImpl implements UserActivationService {
    private final AppUserDao appUserDAO;
    private final CryptoTool cryptoTool;

    public UserActivationServiceImpl(AppUserDao appUserDAO, CryptoTool cryptoTool) {
        this.appUserDAO = appUserDAO;
        this.cryptoTool = cryptoTool;
    }

    @Override
    public boolean activation(String cryptoUserId) {
        Long userId = cryptoTool.ofId(cryptoUserId);
        Optional<AppUser> optional = appUserDAO.findById(userId);
        if (optional.isPresent()) {
            AppUser user = optional.get();
            user.setIsActive(true);
            appUserDAO.save(user);
            return true;
        }
        return false;
    }
}
