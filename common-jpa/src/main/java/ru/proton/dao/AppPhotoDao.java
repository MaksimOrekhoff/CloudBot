package ru.proton.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.proton.entity.AppPhoto;

public interface AppPhotoDao extends JpaRepository<AppPhoto, Long> {
}
