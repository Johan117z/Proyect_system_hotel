package com.mycompany.system_hotel;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

/**
 * Épica 2 — Huéspedes (HU-05 a HU-08): reglas para registrar, actualizar, eliminar y consultar huéspedes.
 */
public class GuestService {

    private final HotelStore store;

    public GuestService(HotelStore store) {
        this.store = store;
    }

    /** HU-05 / RN-03: el documento es único. */
    public void register(Guest guest) {
        if (guest == null) {
            throw new IllegalArgumentException("El huésped es obligatorio.");
        }
        if (store.documentExists(guest.getDocument())) {
            throw new BusinessRuleException("RN-03", "El huésped ya existe en el sistema.");
        }
        store.addGuest(guest);
    }

    /** HU-06: si el documento no existe, informa que el huésped no fue encontrado. */
    public Guest find(String document) {
        return store.findGuest(document);
    }

    /** HU-06: solo datos de contacto. */
    public void updateContact(String document, String email, String phone) {
        store.findGuest(document).updateContactInfo(email, phone);
    }

    /** HU-06: datos personales y de contacto. */
    public void update(String document, String fullName, String email, String phone) {
        store.findGuest(document).updatePersonalInfo(fullName, email, phone);
    }

    /**
     * HU-07 / RN-04: baja lógica, solo si el huésped no tiene hospedaje activo, reservas vigentes
     * ni facturas sin pagar. Conserva su historial de reservas y facturas.
     */
    public void delete(String document) {
        Guest guest = store.findGuest(document);
        boolean hasLiveReservations = store.getReservations().stream()
                .anyMatch(r -> r.getGuest() == guest && r.getStatus().blocksRoom());
        boolean hasUnpaidInvoices = store.getInvoices().stream()
                .anyMatch(i -> i.getReservation().getGuest() == guest && !store.isPaid(i));
        if (hasLiveReservations || hasUnpaidInvoices) {
            throw new BusinessRuleException("RN-04",
                    "No se puede eliminar: el huésped tiene hospedaje activo, reservas vigentes o facturas sin pagar.");
        }
        store.retireGuest(guest);
    }

    /** HU-08: busca por nombre, apellido o documento (sin distinguir mayúsculas ni tildes). */
    public List<Guest> search(String text) {
        String query = normalize(text);
        if (query.isEmpty()) {
            throw new IllegalArgumentException("Ingrese un nombre, apellido o documento para buscar.");
        }
        return store.getActiveGuests().stream()
                .filter(g -> normalize(g.getFullName()).contains(query) || g.getDocument().contains(query))
                .toList();
    }

    public List<Guest> getAll() {
        return store.getActiveGuests();
    }

    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String withoutAccents = Normalizer.normalize(text.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase(Locale.ROOT);
    }
}
