package cl.Alchemax.Alchemax.service;

import cl.Alchemax.Alchemax.model.Carrito;
import cl.Alchemax.Alchemax.model.ItemCarrito;
import cl.Alchemax.Alchemax.model.Servicio;
import cl.Alchemax.Alchemax.repository.CarritoRepository;
import cl.Alchemax.Alchemax.repository.ItemCarritoRepository;
import cl.Alchemax.Alchemax.repository.ServicioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemCarritoServiceTest {

    @Mock
    private ItemCarritoRepository itemCarritoRepository;

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private ServicioRepository servicioRepository;

    @InjectMocks
    private ItemCarritoService itemCarritoService;

    private Carrito carrito;
    private Servicio servicio;
    private ItemCarrito item;

    @BeforeEach
    void setUp() {
        carrito = new Carrito();
        carrito.setId(10L);
        carrito.setItems(new ArrayList<>());

        servicio = new Servicio();
        servicio.setId(20L);
        servicio.setNombre("Evaluación Nutricional");
        servicio.setPrecio(25000);

        item = new ItemCarrito(1L, servicio, 2, carrito);
        carrito.getItems().add(item);
    }

    @Test
    void listarYBuscarPorIdYPorCarritoDebenDelegarAlRepositorio() {
        when(itemCarritoRepository.findAll()).thenReturn(List.of(item));
        when(itemCarritoRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemCarritoRepository.findByCarrito_Id(10L)).thenReturn(List.of(item));

        assertEquals(1, itemCarritoService.listar().size());
        assertNotNull(itemCarritoService.buscarPorId(1L));
        assertEquals(1, itemCarritoService.listarPorCarrito(10L).size());
    }

    @Test
    void guardarDebeCrearNuevoItemCuandoNoExisteEnCarrito() {
        when(carritoRepository.findById(10L)).thenReturn(Optional.of(carrito));
        when(servicioRepository.findById(20L)).thenReturn(Optional.of(servicio));
        when(itemCarritoRepository.findByCarrito_IdAndServicio_Id(10L, 20L)).thenReturn(Optional.empty());
        when(itemCarritoRepository.save(any(ItemCarrito.class))).thenAnswer(inv -> inv.getArgument(0));

        ItemCarrito guardado = itemCarritoService.guardar(10L, 20L, 3);

        assertNotNull(guardado);
        assertEquals(3, guardado.getCantidad());
        assertEquals(servicio, guardado.getServicio());
        assertEquals(carrito, guardado.getCarrito());
    }

    @Test
    void guardarDebeIncrementarCantidadCuandoItemYaExisteEnCarrito() {
        when(carritoRepository.findById(10L)).thenReturn(Optional.of(carrito));
        when(servicioRepository.findById(20L)).thenReturn(Optional.of(servicio));
        when(itemCarritoRepository.findByCarrito_IdAndServicio_Id(10L, 20L)).thenReturn(Optional.of(item));
        when(itemCarritoRepository.save(any(ItemCarrito.class))).thenAnswer(inv -> inv.getArgument(0));

        ItemCarrito guardado = itemCarritoService.guardar(10L, 20L, 3);

        assertNotNull(guardado);
        assertEquals(5, guardado.getCantidad());
    }

    @Test
    void guardarDebeRetornarNullSiCarritoOServicioNoExiste() {
        when(carritoRepository.findById(99L)).thenReturn(Optional.empty());
        assertNull(itemCarritoService.guardar(99L, 20L, 1));

        when(carritoRepository.findById(10L)).thenReturn(Optional.of(carrito));
        when(servicioRepository.findById(99L)).thenReturn(Optional.empty());
        assertNull(itemCarritoService.guardar(10L, 99L, 1));
    }

    @Test
    void actualizarDebeModificarCantidadYServicioSiExiste() {
        Servicio nuevoServicio = new Servicio();
        nuevoServicio.setId(30L);
        nuevoServicio.setNombre("Plan Mensual");

        ItemCarrito cambios = new ItemCarrito(null, nuevoServicio, 4, null);
        when(itemCarritoRepository.findById(1L)).thenReturn(Optional.of(item));
        when(servicioRepository.findById(30L)).thenReturn(Optional.of(nuevoServicio));
        when(itemCarritoRepository.save(any(ItemCarrito.class))).thenAnswer(inv -> inv.getArgument(0));

        ItemCarrito actualizado = itemCarritoService.actualizar(cambios, 1L);

        assertNotNull(actualizado);
        assertEquals(4, actualizado.getCantidad());
        assertEquals(30L, actualizado.getServicio().getId());
    }

    @Test
    void actualizarDebeRetornarNullCuandoItemNoExiste() {
        when(itemCarritoRepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(itemCarritoService.actualizar(item, 99L));
    }

    @Test
    void eliminarDebeDesvincularDelCarritoYEliminar() {
        when(itemCarritoRepository.findById(1L)).thenReturn(Optional.of(item));

        boolean eliminado = itemCarritoService.eliminar(1L);

        assertTrue(eliminado);
        assertTrue(carrito.getItems().isEmpty());
        verify(itemCarritoRepository).delete(item);
    }

    @Test
    void eliminarDebeRetornarFalseCuandoNoExiste() {
        when(itemCarritoRepository.findById(99L)).thenReturn(Optional.empty());

        assertFalse(itemCarritoService.eliminar(99L));
        verify(itemCarritoRepository, never()).delete(any());
    }
}
