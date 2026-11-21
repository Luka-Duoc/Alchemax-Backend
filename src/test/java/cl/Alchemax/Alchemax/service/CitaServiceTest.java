package cl.Alchemax.Alchemax.service;

import cl.Alchemax.Alchemax.exception.HorarioNoDisponibleException;
import cl.Alchemax.Alchemax.model.Cita;
import cl.Alchemax.Alchemax.model.Usuario;
import cl.Alchemax.Alchemax.repository.CitaRepository;
import cl.Alchemax.Alchemax.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CitaServiceTest {

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CitaService citaService;

    private Cita citaEjemplo;

    @BeforeEach
    void setUp() {
        citaEjemplo = new Cita(
                1L,
                LocalDate.of(2026, 11, 25),
                "10:30",
                "evaluacion",
                "Dra. Ana Pérez",
                "Programada"
        );
    }

    @Test
    void listarDebeRetornarTodasLasCitas() {
        when(citaRepository.findAll()).thenReturn(List.of(citaEjemplo));

        List<Cita> resultado = citaService.listar();

        assertEquals(1, resultado.size());
        assertEquals("Dra. Ana Pérez", resultado.get(0).getProfesional());
        verify(citaRepository).findAll();
    }

    @Test
    void buscarPorIdDebeRetornarCitaCuandoExiste() {
        when(citaRepository.findById(1L)).thenReturn(Optional.of(citaEjemplo));

        Cita resultado = citaService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void buscarPorIdDebeRetornarNullCuandoNoExiste() {
        when(citaRepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(citaService.buscarPorId(99L));
    }

    @Test
    void listarPorEstadoProfesionalYFechaDebeDelegarAlRepositorio() {
        LocalDate fecha = LocalDate.of(2026, 11, 25);
        when(citaRepository.findByEstado("Programada")).thenReturn(List.of(citaEjemplo));
        when(citaRepository.findByProfesional("Dra. Ana Pérez")).thenReturn(List.of(citaEjemplo));
        when(citaRepository.findByFecha(fecha)).thenReturn(List.of(citaEjemplo));

        assertEquals(1, citaService.listarPorEstado("Programada").size());
        assertEquals(1, citaService.listarPorProfesional("Dra. Ana Pérez").size());
        assertEquals(1, citaService.listarPorFecha(fecha).size());
    }

    @Test
    void guardarDebeNormalizarHoraYAsignarEstadoProgramadaPorDefecto() {
        Cita nueva = new Cita(null, LocalDate.of(2026, 12, 1), "9:05:00", "control", "Dr. Carlos Soto", null);
        when(citaRepository.existeConflictoHorario("Dr. Carlos Soto", LocalDate.of(2026, 12, 1), "09:05", null))
                .thenReturn(false);
        when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

        Cita guardada = citaService.guardar(nueva);

        assertEquals("Programada", guardada.getEstado());
        assertEquals("09:05", guardada.getHora());
        verify(citaRepository).save(nueva);
    }

    @Test
    void guardarDebeLanzarHorarioNoDisponibleExceptionSiExisteConflicto() {
        LocalDate fecha = LocalDate.of(2026, 11, 25);
        Cita nueva = new Cita(null, fecha, "10:30:00", "evaluacion", "Dra. Ana Pérez", "Programada");
        when(citaRepository.existeConflictoHorario("Dra. Ana Pérez", fecha, "10:30", null))
                .thenReturn(true);

        assertThrows(HorarioNoDisponibleException.class, () -> citaService.guardar(nueva));
        verify(citaRepository, never()).save(any());
    }

    @Test
    void guardarConUsuarioIdDebeSobrescribirPacienteEmailConElDelUsuarioReal() {
        Usuario pacienteReal = new Usuario();
        pacienteReal.setId(2L);
        pacienteReal.setNombre("Camila Soto");
        pacienteReal.setEmail("camila@gmail.com");

        // Incluso si el cliente envía un objeto Usuario con nombre/email falsificados, debe resolver desde BD
        Usuario refFalsificado = new Usuario();
        refFalsificado.setId(2L);
        refFalsificado.setNombre("Nombre Falso");
        refFalsificado.setEmail("falso@gmail.com");

        Cita nueva = new Cita(null, LocalDate.of(2026, 12, 3), "11:00", "control", "Dra. Ana Pérez", "Programada");
        nueva.setUsuario(refFalsificado);
        nueva.setPacienteEmail("suplantado@gmail.com");

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(pacienteReal));
        when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

        Cita guardada = citaService.guardar(nueva);

        assertSame(pacienteReal, guardada.getUsuario());
        assertEquals("camila@gmail.com", guardada.getPacienteEmail());
        assertEquals("Camila Soto", guardada.getPacienteNombre());
    }

    @Test
    void guardarConUsuarioIdInexistenteDebeLimpiarReferenciaUsuarioParaEvitarErrorFK() {
        Usuario refInexistente = new Usuario();
        refInexistente.setId(999L);
        refInexistente.setNombre("Inexistente");
        refInexistente.setEmail("noexiste@gmail.com");

        Cita nueva = new Cita(null, LocalDate.of(2026, 12, 4), "12:00", "control", "Dra. Ana Pérez", "Programada");
        nueva.setUsuario(refInexistente);

        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());
        when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

        Cita guardada = citaService.guardar(nueva);

        assertNull(guardada.getUsuario());
    }

    @Test
    void actualizarExistenteConMismoUsuarioIdDebePreservarUsuarioHidratadoSinSobrescribirConStub() {
        Usuario pacienteHidratado = new Usuario();
        pacienteHidratado.setId(2L);
        pacienteHidratado.setNombre("Camila Soto");
        pacienteHidratado.setEmail("camila@gmail.com");
        citaEjemplo.setUsuario(pacienteHidratado);

        Usuario stubSoloId = new Usuario();
        stubSoloId.setId(2L);

        Cita cambios = new Cita(
                null,
                LocalDate.of(2026, 11, 26),
                "11:30",
                "control",
                "Dra. Ana Pérez",
                "Confirmada"
        );
        cambios.setUsuario(stubSoloId);

        when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

        Cita actualizada = citaService.actualizarExistente(citaEjemplo, cambios);

        assertNotNull(actualizada.getUsuario());
        assertSame(pacienteHidratado, actualizada.getUsuario());
        assertEquals("Camila Soto", actualizada.getUsuario().getNombre());
        assertEquals("camila@gmail.com", actualizada.getUsuario().getEmail());
        verify(usuarioRepository, never()).findById(any());
    }

    @Test
    void actualizarDebeModificarDatosYOmitirConflictoCuandoDestinoEsCancelada() {
        Cita datosCancelacion = new Cita(
                null,
                LocalDate.of(2026, 11, 25),
                "10:30",
                "evaluacion",
                "Dra. Ana Pérez",
                "Cancelada"
        );
        when(citaRepository.findById(1L)).thenReturn(Optional.of(citaEjemplo));
        when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

        Cita actualizada = citaService.actualizar(datosCancelacion, 1L);

        assertNotNull(actualizada);
        assertEquals("Cancelada", actualizada.getEstado());
        // No debe consultar conflicto horario cuando se está cancelando
        verify(citaRepository, never()).existeConflictoHorario(any(), any(), any(), any());
    }

    @Test
    void actualizarDebeRetornarNullAntesDeConsultarConflictoCuandoCitaNoExiste() {
        when(citaRepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(citaService.actualizar(citaEjemplo, 99L));
        verify(citaRepository, never()).existeConflictoHorario(any(), any(), any(), any());
        verify(citaRepository, never()).save(any());
    }

    @Test
    void cambiarEstadoDeCanceladaAActivaDebeVerificarConflictoHorario() {
        Cita citaCancelada = new Cita(
                1L,
                LocalDate.of(2026, 11, 25),
                "10:30",
                "evaluacion",
                "Dra. Ana Pérez",
                "Cancelada"
        );
        when(citaRepository.findById(1L)).thenReturn(Optional.of(citaCancelada));
        when(citaRepository.existeConflictoHorario("Dra. Ana Pérez", LocalDate.of(2026, 11, 25), "10:30", 1L))
                .thenReturn(true);

        assertThrows(HorarioNoDisponibleException.class, () -> citaService.cambiarEstado(1L, "Programada"));
        verify(citaRepository, never()).save(any());
    }

    @Test
    void eliminarDebeRetornarTrueCuandoExisteYFalseCuandoNoExiste() {
        when(citaRepository.existsById(1L)).thenReturn(true);
        when(citaRepository.existsById(99L)).thenReturn(false);

        assertTrue(citaService.eliminar(1L));
        verify(citaRepository).deleteById(1L);

        assertFalse(citaService.eliminar(99L));
        verify(citaRepository, never()).deleteById(99L);
    }
}
