package com.mateojohan.sistemahotel.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mateojohan.sistemahotel.dto.GuestContactDTO;
import com.mateojohan.sistemahotel.dto.GuestRequestDTO;
import com.mateojohan.sistemahotel.dto.GuestResponseDTO;
import com.mateojohan.sistemahotel.dto.GuestUpdateDTO;
import com.mateojohan.sistemahotel.enums.ReservationStatus;
import com.mateojohan.sistemahotel.exceptions.BusinessRuleException;
import com.mateojohan.sistemahotel.exceptions.InvalidDataException;
import com.mateojohan.sistemahotel.exceptions.ResourceNotFoundException;
import com.mateojohan.sistemahotel.models.Guest;
import com.mateojohan.sistemahotel.repositories.GuestRepository;
import com.mateojohan.sistemahotel.repositories.InvoiceRepository;
import com.mateojohan.sistemahotel.repositories.ReservationRepository;

/** Épica 2: Huéspedes (HU-05 a HU-08). */
@Service
public class GuestService {

    private final GuestRepository guestRepository;
    private final ReservationRepository reservationRepository;
    private final InvoiceRepository invoiceRepository;

    public GuestService(GuestRepository guestRepository, ReservationRepository reservationRepository,
                        InvoiceRepository invoiceRepository) {
        this.guestRepository = guestRepository;
        this.reservationRepository = reservationRepository;
        this.invoiceRepository = invoiceRepository;
    }

    /** HU-05 / RN-03: documento único. */
    @Transactional
    public GuestResponseDTO create(GuestRequestDTO request) {
        if (guestRepository.existsByDocument(request.document().trim())) {
            throw new BusinessRuleException("RN-03", "El huésped ya existe en el sistema.");
        }
        Guest guest = new Guest(request.document(), request.fullName(), request.email(), request.phone());
        return GuestResponseDTO.from(guestRepository.save(guest));
    }

    @Transactional(readOnly = true)
    public List<GuestResponseDTO> findAll() {
        return guestRepository.findByActiveTrueOrderByFullNameAsc().stream().map(GuestResponseDTO::from).toList();
    }

    /** HU-06: si el documento no existe, informa que el huésped no fue encontrado. */
    @Transactional(readOnly = true)
    public GuestResponseDTO findByDocument(String document) {
        return GuestResponseDTO.from(getActiveGuest(document));
    }

    /** HU-08: búsqueda por nombre, apellido o documento. */
    @Transactional(readOnly = true)
    public List<GuestResponseDTO> search(String text) {
        if (text == null || text.isBlank()) {
            throw new InvalidDataException("q", "Ingrese un nombre, apellido o documento para buscar.");
        }
        return guestRepository.search(text.trim()).stream().map(GuestResponseDTO::from).toList();
    }

    /** HU-06: datos personales y de contacto. */
    @Transactional
    public GuestResponseDTO update(String document, GuestUpdateDTO request) {
        Guest guest = getActiveGuest(document);
        guest.updatePersonalInfo(request.fullName(), request.email(), request.phone());
        return GuestResponseDTO.from(guest);
    }

    /** HU-06: solo datos de contacto. */
    @Transactional
    public GuestResponseDTO updateContact(String document, GuestContactDTO request) {
        Guest guest = getActiveGuest(document);
        guest.updateContactInfo(request.email(), request.phone());
        return GuestResponseDTO.from(guest);
    }

    /** HU-07 / RN-04: baja lógica sin hospedaje activo, reservas vigentes ni facturas sin pagar. */
    @Transactional
    public void delete(String document) {
        Guest guest = getActiveGuest(document);
        if (reservationRepository.existsByGuestAndStatusIn(guest, ReservationStatus.BLOCKING)
                || invoiceRepository.existsUnpaidByGuest(guest)) {
            throw new BusinessRuleException("RN-04",
                    "No se puede eliminar: el huésped tiene hospedaje activo, reservas vigentes o facturas sin pagar.");
        }
        guest.deactivate();
    }

    /** Uso interno de otros servicios (ReservationService). */
    Guest getActiveGuest(String document) {
        String doc = document == null ? "" : document.trim();
        return guestRepository.findByDocumentAndActiveTrue(doc)
                .orElseThrow(() -> new ResourceNotFoundException("El huésped con documento " + doc + " no fue encontrado."));
    }
}
