package ru.proton.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.proton.entity.RawData;

public interface RawDataDAO extends JpaRepository<RawData, Long> {
}
