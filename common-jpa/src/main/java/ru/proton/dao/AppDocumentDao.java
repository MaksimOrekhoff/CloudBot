package ru.proton.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.proton.entity.AppDocument;

public interface AppDocumentDao extends JpaRepository<AppDocument, Long> {
}
