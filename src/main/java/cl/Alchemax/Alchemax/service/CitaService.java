package cl.Alchemax.Alchemax.service;

import cl.Alchemax.Alchemax.exception.HorarioNoDisponibleException;
import cl.Alchemax.Alchemax.model.Cita;
import cl.Alchemax.Alchemax.model.Usuario;
import cl.Alchemax.Alchemax.repository.CitaRepository;
import cl.Alchemax.Alchemax.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
public class CitaService {

    private static final String MENSAJE_CONFLICTO_HORARIO =
            "El profesional seleccionado ya cuenta con una cita activa en esa fecha y hora.";

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

    // Validar disponibilidad de horario en la base de datos
    public boolean tieneConflictoHorario(String profesional, LocalDate fecha, String hora, Long excluirCitaId) {
        if (profesional == null || profesional.isBlank() || fecha == null || hora == null || hora.isBlank()) {
            return false;
        }
        String profNormalizado = profesional.trim();
        String horaNormalizada = Cita.normalizarHora(hora);
        return citaRepository.existeConflictoHorario(profNormalizado, fecha, horaNormalizada, excluirCitaId);
    }

    // Guardar
    @Transactional
    public Cita guardar(Cita c) {
        c.setId(null);
        normalizarCampos(c);
        if (c.getEstado() == null || c.getEstado().isBlank()) {
            c.setEstado(Cita.ESTADO_PROGRAMADA);
        }
        c.sincronizarSlotActivo();

        if (!Cita.ESTADO_CANCELADA.equalsIgnoreCase(c.getEstado())
                && tieneConflictoHorario(c.getProfesional(), c.getFecha(), c.getHora(), null)) {
            throw new HorarioNoDisponibleException(MENSAJE_CONFLICTO_HORARIO);
        }

        vincularUsuario(c);
        return citaRepository.save(c);
    }

    // Actualizar (recibiendo ID)
    @Transactional
    public Cita actualizar(Cita c, Long id) {
        Cita citaExistente = citaRepository.findById(id).orElse(null);
        if (citaExistente == null) {
            return null;
        }
        return actualizarExistente(citaExistente, c);
    }

    // Actualizar (recibiendo entidad ya cargada para evitar doble SELECT)
    @Transactional
    public Cita actualizarExistente(Cita citaExistente, Cita c) {
        normalizarCampos(c);
        String estadoDestino = (c.getEstado() != null && !c.getEstado().isBlank())
                ? c.getEstado()
                : citaExistente.getEstado();

        if (!Cita.ESTADO_CANCELADA.equalsIgnoreCase(estadoDestino)
                && tieneConflictoHorario(c.getProfesional(), c.getFecha(), c.getHora(), citaExistente.getId())) {
            throw new HorarioNoDisponibleException(MENSAJE_CONFLICTO_HORARIO);
        }

        citaExistente.setFecha(c.getFecha());
        citaExistente.setHora(c.getHora());
        citaExistente.setTipo(c.getTipo());
        citaExistente.setProfesional(c.getProfesional());
        citaExistente.setEstado(estadoDestino);

        if (c.getPacienteNombre() != null) {
            citaExistente.setPacienteNombre(c.getPacienteNombre());
        }
        if (c.getPacienteEmail() != null) {
            citaExistente.setPacienteEmail(c.getPacienteEmail());
        }
        if (c.getUsuario() != null && c.getUsuario().getId() != null) {
            boolean mismoUsuarioYaHidratado = citaExistente.getUsuario() != null
                    && citaExistente.getUsuario().getId() != null
                    && citaExistente.getUsuario().getId().equals(c.getUsuario().getId())
                    && citaExistente.getUsuario().getEmail() != null;
            if (mismoUsuarioYaHidratado) {
                citaExistente.setPacienteNombre(citaExistente.getUsuario().getNombre());
                citaExistente.setPacienteEmail(citaExistente.getUsuario().getEmail());
            } else {
                citaExistente.setUsuario(c.getUsuario());
                vincularUsuario(citaExistente);
            }
        } else if (c.getUsuario() == null && c.getPacienteEmail() != null && !c.getPacienteEmail().isBlank()) {
            vincularUsuario(citaExistente);
        }

        citaExistente.sincronizarSlotActivo();
        return citaRepository.save(citaExistente);
    }

    // Cambiar estado (por ID)
    @Transactional
    public Cita cambiarEstado(Long id, String estado) {
        Cita citaExistente = citaRepository.findById(id).orElse(null);
        if (citaExistente == null) {
            return null;
        }
        return cambiarEstadoExistente(citaExistente, estado);
    }

    // Cambiar estado (con entidad ya cargada para evitar doble SELECT)
    @Transactional
    public Cita cambiarEstadoExistente(Cita citaExistente, String estado) {
        if (Cita.ESTADO_CANCELADA.equalsIgnoreCase(citaExistente.getEstado())
                && !Cita.ESTADO_CANCELADA.equalsIgnoreCase(estado)
                && tieneConflictoHorario(
                        citaExistente.getProfesional(),
                        citaExistente.getFecha(),
                        citaExistente.getHora(),
                        citaExistente.getId()
                )) {
            throw new HorarioNoDisponibleException(MENSAJE_CONFLICTO_HORARIO);
        }

        citaExistente.setEstado(estado);
        citaExistente.sincronizarSlotActivo();
        return citaRepository.save(citaExistente);
    }

    // Eliminar (por ID)
    @Transactional
    public boolean eliminar(Long id) {
        if (citaRepository.existsById(id)) {
            citaRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Eliminar (con entidad ya cargada, 1 query en vez de 3)
    @Transactional
    public boolean eliminarExistente(Cita citaExistente) {
        if (citaExistente == null) {
            return false;
        }
        citaRepository.delete(citaExistente);
        return true;
    }

    private void normalizarCampos(Cita cita) {
        if (cita.getHora() != null) {
            cita.setHora(Cita.normalizarHora(cita.getHora()));
        }
        if (cita.getProfesional() != null) {
            cita.setProfesional(cita.getProfesional().trim());
        }
        if (cita.getTipo() != null) {
            cita.setTipo(cita.getTipo().trim());
        }
    }

    private void vincularUsuario(Cita cita) {
        if (usuarioRepository == null) return;

        Usuario resolved = null;
        boolean buscadoPorId = false;

        if (cita.getUsuario() != null && cita.getUsuario().getId() != null) {
            buscadoPorId = true;
            resolved = usuarioRepository.findById(cita.getUsuario().getId()).orElse(null);
            if (resolved == null) {
                cita.setUsuario(null);
            }
        } else if (cita.getPacienteEmail() != null && !cita.getPacienteEmail().isBlank()) {
            resolved = usuarioRepository
                    .findByEmailIgnoreCase(cita.getPacienteEmail().trim().toLowerCase(Locale.ROOT))
                    .orElse(null);
        }

        if (resolved != null) {
            cita.setUsuario(resolved);
            if (buscadoPorId || cita.getPacienteNombre() == null || cita.getPacienteNombre().isBlank()) {
                cita.setPacienteNombre(resolved.getNombre());
            }
            if (buscadoPorId || cita.getPacienteEmail() == null || cita.getPacienteEmail().isBlank()) {
                cita.setPacienteEmail(resolved.getEmail());
            }
        }
    }
}
