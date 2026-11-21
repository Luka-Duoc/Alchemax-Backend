package cl.Alchemax.Alchemax.service;

import cl.Alchemax.Alchemax.model.Cita;
import cl.Alchemax.Alchemax.repository.CitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;

    // Listar
    public List<Cita> listar() {
        return citaRepository.findAll();
    }

    // Buscar por ID
    public Cita buscarPorId(Long id) {
        return citaRepository.findById(id).orElse(null);
    }

    // Listar por estado
    public List<Cita> listarPorEstado(String estado) {
        return citaRepository.findByEstado(estado);
    }

    // Listar por profesional
    public List<Cita> listarPorProfesional(String profesional) {
        return citaRepository.findByProfesional(profesional);
    }

    // Listar por fecha
    public List<Cita> listarPorFecha(LocalDate fecha) {
        return citaRepository.findByFecha(fecha);
    }

    // Guardar
    public Cita guardar(Cita c) {
        if (c.getEstado() == null || c.getEstado().isBlank()) {
            c.setEstado("Programada");
        }
        return citaRepository.save(c);
    }

    // Actualizar
    public Cita actualizar(Cita c, Long id) {
        Cita citaExistente = citaRepository.findById(id).orElse(null);
        if (citaExistente == null) {
            return null;
        }

        citaExistente.setFecha(c.getFecha());
        citaExistente.setHora(c.getHora());
        citaExistente.setTipo(c.getTipo());
        citaExistente.setProfesional(c.getProfesional());
        if (c.getEstado() != null && !c.getEstado().isBlank()) {
            citaExistente.setEstado(c.getEstado());
        }

        return citaRepository.save(citaExistente);
    }

    // Cambiar estado
    public Cita cambiarEstado(Long id, String estado) {
        Cita citaExistente = citaRepository.findById(id).orElse(null);
        if (citaExistente == null) {
            return null;
        }

        citaExistente.setEstado(estado);
        return citaRepository.save(citaExistente);
    }

    // Eliminar
    public boolean eliminar(Long id) {
        if (citaRepository.existsById(id)) {
            citaRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
