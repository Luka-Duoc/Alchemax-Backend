package cl.Alchemax.Alchemax.service;

import cl.Alchemax.Alchemax.model.Carrito;
import cl.Alchemax.Alchemax.model.ItemCarrito;
import cl.Alchemax.Alchemax.model.Servicio;
import cl.Alchemax.Alchemax.repository.CarritoRepository;
import cl.Alchemax.Alchemax.repository.ItemCarritoRepository;
import cl.Alchemax.Alchemax.repository.ServicioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Test
    void actualizarExistenteCambiandoAServicioYaPresenteEnCarritoDebeFusionarCantidadesYEliminarDuplicado() {
        Carrito carrito = new Carrito();
        carrito.setId(10L);

        Servicio s1 = new Servicio();
        s1.setId(20L);
        s1.setNombre("Evaluación");

        Servicio s2 = new Servicio();
        s2.setId(30L);
        s2.setNombre("Plan Deportivo");

        ItemCarrito item1 = new ItemCarrito(1L, s1, 2, carrito);
        ItemCarrito item2Existente = new ItemCarrito(2L, s2, 1, carrito);

        ItemCarrito cambios = new ItemCarrito();
        cambios.setCantidad(3);
        cambios.setServicio(s2);

        when(servicioRepository.findById(30L)).thenReturn(Optional.of(s2));
        when(itemCarritoRepository.findByCarrito_IdAndServicio_Id(10L, 30L))
                .thenReturn(Optional.of(item2Existente));
        when(itemCarritoRepository.save(any(ItemCarrito.class))).thenAnswer(inv -> inv.getArgument(0));

        ItemCarrito resultado = itemCarritoService.actualizarExistente(item1, cambios);

        assertNotNull(resultado);
        assertEquals(2L, resultado.getId());
        assertEquals(4, resultado.getCantidad()); // 1 existente + 3 del item actualizado
        verify(itemCarritoRepository).delete(item1);
        verify(itemCarritoRepository).save(item2Existente);
    }
}
