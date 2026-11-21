package cl.Alchemax.Alchemax.service;

import cl.Alchemax.Alchemax.model.Cita;
import cl.Alchemax.Alchemax.repository.CitaRepository;
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
    void guardarDebeAsignarEstadoProgramadaPorDefectoSiVieneVacio() {
        Cita nueva = new Cita(null, LocalDate.of(2026, 12, 1), "15:00", "control", "Dr. Carlos Soto", null);
        when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

        Cita guardada = citaService.guardar(nueva);

        assertEquals("Programada", guardada.getEstado());
        verify(citaRepository).save(nueva);
    }

    @Test
    void actualizarDebeModificarDatosCuandoCitaExiste() {
        Cita datosNuevos = new Cita(
                null,
                LocalDate.of(2026, 12, 2),
                "16:00",
                "plan",
                "Dra. María López",
                "Completada"
        );
        when(citaRepository.findById(1L)).thenReturn(Optional.of(citaEjemplo));
        when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

        Cita actualizada = citaService.actualizar(datosNuevos, 1L);

        assertNotNull(actualizada);
        assertEquals(LocalDate.of(2026, 12, 2), actualizada.getFecha());
        assertEquals("16:00", actualizada.getHora());
        assertEquals("plan", actualizada.getTipo());
        assertEquals("Dra. María López", actualizada.getProfesional());
        assertEquals("Completada", actualizada.getEstado());
    }

    @Test
    void actualizarDebeRetornarNullCuandoCitaNoExiste() {
        when(citaRepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(citaService.actualizar(citaEjemplo, 99L));
        verify(citaRepository, never()).save(any());
    }

    @Test
    void cambiarEstadoDebeActualizarSoloElEstado() {
        when(citaRepository.findById(1L)).thenReturn(Optional.of(citaEjemplo));
        when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

        Cita resultado = citaService.cambiarEstado(1L, "Cancelada");

        assertNotNull(resultado);
        assertEquals("Cancelada", resultado.getEstado());
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
