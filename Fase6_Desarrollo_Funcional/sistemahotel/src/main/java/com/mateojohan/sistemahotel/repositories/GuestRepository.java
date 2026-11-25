package com.mateojohan.sistemahotel.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mateojohan.sistemahotel.models.Guest;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Integer> {

    /** HU-06: busca un huésped activo por documento. */
    Optional<Guest> findByDocumentAndActiveTrue(String document);

    /** RN-03: el documento es único incluso frente a huéspedes dados de baja. */
    boolean existsByDocument(String document);

    List<Guest> findByActiveTrueOrderByFullNameAsc();

    /**
     * HU-08: busca por nombre, apellido o documento. La colación utf8mb4_0900_ai_ci de la BD
     * hace que LIKE ignore mayúsculas y tildes ("perez" encuentra "Pérez").
     */
    @Query("""
            SELECT g FROM Guest g
             WHERE g.active = true
               AND (g.fullName LIKE CONCAT('%', :text, '%') OR g.document LIKE CONCAT('%', :text, '%'))
             ORDER BY g.fullName
            """)
    List<Guest> search(@Param("text") String text);
}
