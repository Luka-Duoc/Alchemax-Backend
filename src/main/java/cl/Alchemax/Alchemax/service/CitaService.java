package cl.Alchemax.Alchemax.service;

import cl.Alchemax.Alchemax.model.Cita;
import cl.Alchemax.Alchemax.model.Usuario;
import cl.Alchemax.Alchemax.repository.CitaRepository;
import cl.Alchemax.Alchemax.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired(required = false)
    private UsuarioRepository usuarioRepository;

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

    // Listar por usuario
    public List<Cita> listarPorUsuario(Long usuarioId) {
        return citaRepository.findByUsuario_Id(usuarioId);
    }

    // Guardar
    public Cita guardar(Cita c) {
        if (c.getEstado() == null || c.getEstado().isBlank()) {
            c.setEstado("Programada");
        }
        vincularUsuario(c);
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
        if (c.getPacienteNombre() != null) {
            citaExistente.setPacienteNombre(c.getPacienteNombre());
        }
        if (c.getPacienteEmail() != null) {
            citaExistente.setPacienteEmail(c.getPacienteEmail());
        }
        if (c.getUsuario() != null) {
            citaExistente.setUsuario(c.getUsuario());
            vincularUsuario(citaExistente);
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

    private void vincularUsuario(Cita cita) {
        if (usuarioRepository == null) return;

        Usuario resolved = null;
        if (cita.getUsuario() != null && cita.getUsuario().getId() != null) {
            resolved = usuarioRepository.findById(cita.getUsuario().getId()).orElse(null);
        } else if (cita.getPacienteEmail() != null && !cita.getPacienteEmail().isBlank()) {
            resolved = usuarioRepository.findByEmail(cita.getPacienteEmail().trim().toLowerCase()).orElse(null);
        }

        if (resolved != null) {
            cita.setUsuario(resolved);
            if (cita.getPacienteNombre() == null || cita.getPacienteNombre().isBlank()) {
                cita.setPacienteNombre(resolved.getNombre());
            }
            if (cita.getPacienteEmail() == null || cita.getPacienteEmail().isBlank()) {
                cita.setPacienteEmail(resolved.getEmail());
            }
        }
    }
}
