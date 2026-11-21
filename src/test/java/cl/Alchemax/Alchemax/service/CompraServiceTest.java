package cl.Alchemax.Alchemax.service;

import cl.Alchemax.Alchemax.exception.SolicitudInvalidaException;
import cl.Alchemax.Alchemax.model.Compra;
import cl.Alchemax.Alchemax.model.Servicio;
import cl.Alchemax.Alchemax.model.Usuario;
import cl.Alchemax.Alchemax.repository.CompraRepository;
import cl.Alchemax.Alchemax.repository.ServicioRepository;
import cl.Alchemax.Alchemax.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompraServiceTest {

    @Mock
    private CompraRepository compraRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ServicioRepository servicioRepository;

    @InjectMocks
    private CompraService compraService;

    private Usuario paciente;
    private Servicio servicioDeportivo;

    @BeforeEach
    void setUp() {
        paciente = new Usuario();
        paciente.setId(2L);
        paciente.setNombre("Camila Soto");
        paciente.setEmail("camila.soto@gmail.com");

        servicioDeportivo = new Servicio();
        servicioDeportivo.setId(2L);
        servicioDeportivo.setNombre("Plan Nutricional Deportivo");
        servicioDeportivo.setPrecio(120000);
    }

    @Test
    void guardarDebeSobrescribirMontoYNombreDesdeCatalogoYRechazarServicioInexistente() {
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(paciente));
        when(servicioRepository.findById(2L)).thenReturn(Optional.of(servicioDeportivo));
        when(servicioRepository.findById(999L)).thenReturn(Optional.empty());
        when(compraRepository.save(any(Compra.class))).thenAnswer(inv -> inv.getArgument(0));

        // Cliente intenta pagar monto=0 y nombre falso para servicio=2
        Compra intentoManipulado = new Compra(
                null,
                2,
                "Nombre Falso",
                null,
                0,
                null,
                null
        );

        Compra guardada = compraService.guardar(2L, intentoManipulado);

        assertEquals("Plan Nutricional Deportivo", guardada.getNombre());
        assertEquals(120000, guardada.getMonto());
        assertEquals(Compra.ESTADO_ACTIVO, guardada.getEstado());
        assertEquals(LocalDate.now(), guardada.getFechaCompra());

        // Servicio inexistente -> SolicitudInvalidaException
        Compra servicioInvalido = new Compra(null, 999, "Inexistente", LocalDate.now(), 1000, "Activo", null);
        assertThrows(SolicitudInvalidaException.class, () -> compraService.guardar(2L, servicioInvalido));
    }

    @Test
    void cambiarEstadoExistenteDebeValidarEstadosYProhibirReactivarCompraCancelada() {
        Compra compra = new Compra(1L, 2, "Plan Nutricional Deportivo", LocalDate.now(), 120000, Compra.ESTADO_ACTIVO, paciente);
        when(compraRepository.save(any(Compra.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThrows(SolicitudInvalidaException.class, () -> compraService.cambiarEstadoExistente(compra, "EstadoInvalido"));
        assertThrows(SolicitudInvalidaException.class, () -> compraService.cambiarEstadoExistente(compra, null));

        Compra cancelada = compraService.cambiarEstadoExistente(compra, Compra.ESTADO_CANCELADO);
        assertEquals(Compra.ESTADO_CANCELADO, cancelada.getEstado());

        // No se puede pasar de Cancelado a Completado ni Activo
        assertThrows(SolicitudInvalidaException.class, () -> compraService.cambiarEstadoExistente(cancelada, Compra.ESTADO_COMPLETADO));
    }

    @Test
    void eliminarDebeRetornarTrueCuandoExisteYFalseCuandoNoExiste() {
        Compra compra = new Compra(1L, 2, "Plan Nutricional Deportivo", LocalDate.now(), 120000, Compra.ESTADO_ACTIVO, paciente);
        when(compraRepository.findById(1L)).thenReturn(Optional.of(compra));
        when(compraRepository.findById(99L)).thenReturn(Optional.empty());

        assertTrue(compraService.eliminar(1L));
        verify(compraRepository).delete(compra);

        assertFalse(compraService.eliminar(99L));
    }
}
