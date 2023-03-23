package ru.proton.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.proton.entity.BinaryContent;

public interface BinaryContentDao extends JpaRepository<BinaryContent, Long> {
}
