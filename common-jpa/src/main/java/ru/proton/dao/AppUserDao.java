package ru.proton.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.proton.entity.AppUser;

public interface AppUserDao extends JpaRepository<AppUser, Long> {

AppUser findAppUsersByTelegramUserId(Long id);
}
