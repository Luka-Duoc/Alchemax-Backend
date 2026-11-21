package cl.Alchemax.Alchemax.service;

import cl.Alchemax.Alchemax.model.Carrito;
import cl.Alchemax.Alchemax.model.ItemCarrito;
import cl.Alchemax.Alchemax.model.Servicio;
import cl.Alchemax.Alchemax.repository.CarritoRepository;
import cl.Alchemax.Alchemax.repository.ItemCarritoRepository;
import cl.Alchemax.Alchemax.repository.ServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ItemCarritoService {

    @Autowired
    private ItemCarritoRepository itemCarritoRepository;

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    // Listar
    public List<ItemCarrito> listar() {
        return itemCarritoRepository.findAll();
    }

    // Buscar por ID
    public ItemCarrito buscarPorId(Long id) {
        return itemCarritoRepository.findById(id).orElse(null);
    }

    // Buscar carrito por ID para verificación de propiedad
    public Carrito obtenerCarrito(Long carritoId) {
        return carritoRepository.findById(carritoId).orElse(null);
    }

    // Listar por carrito
    public List<ItemCarrito> listarPorCarrito(Long carritoId) {
        return itemCarritoRepository.findByCarrito_Id(carritoId);
    }

    // Guardar recibiendo carritoId
    public ItemCarrito guardar(Long carritoId, Long servicioId, int cantidad) {
        Carrito carrito = carritoRepository.findById(carritoId).orElse(null);
        return guardarEnCarrito(carrito, servicioId, cantidad);
    }

    // Guardar reutilizando Carrito ya cargado para evitar doble SELECT
    public ItemCarrito guardarEnCarrito(Carrito carrito, Long servicioId, int cantidad) {
        if (carrito == null || carrito.getId() == null) {
            return null;
        }

        Servicio servicio = servicioRepository.findById(servicioId).orElse(null);
        if (servicio == null) {
            return null;
        }

        int cantidadValida = cantidad > 0 ? cantidad : 1;

        Optional<ItemCarrito> itemExistente = itemCarritoRepository.findByCarrito_IdAndServicio_Id(
                carrito.getId(),
                servicioId
        );
        if (itemExistente.isPresent()) {
            ItemCarrito existente = itemExistente.get();
            existente.setCantidad(existente.getCantidad() + cantidadValida);
            return itemCarritoRepository.save(existente);
        }

        ItemCarrito nuevoItem = new ItemCarrito();
        nuevoItem.setCarrito(carrito);
        nuevoItem.setServicio(servicio);
        nuevoItem.setCantidad(cantidadValida);

        return itemCarritoRepository.save(nuevoItem);
    }

    // Actualizar
    public ItemCarrito actualizar(ItemCarrito item, Long id) {
        ItemCarrito existente = itemCarritoRepository.findById(id).orElse(null);
        if (existente == null) {
            return null;
        }
        return actualizarExistente(existente, item);
    }

    public ItemCarrito actualizarExistente(ItemCarrito existente, ItemCarrito item) {
        if (existente == null || item == null || item.getCantidad() <= 0) {
            return null;
        }

        existente.setCantidad(item.getCantidad());

        if (item.getServicio() != null && item.getServicio().getId() != null) {
            Long nuevoServicioId = item.getServicio().getId();
            Servicio servicio = servicioRepository.findById(nuevoServicioId).orElse(null);
            if (servicio == null) {
                return null;
            }

            if (existente.getCarrito() != null && existente.getCarrito().getId() != null) {
                Optional<ItemCarrito> duplicadoEnCarrito = itemCarritoRepository
                        .findByCarrito_IdAndServicio_Id(existente.getCarrito().getId(), nuevoServicioId);
                if (duplicadoEnCarrito.isPresent()
                        && !duplicadoEnCarrito.get().getId().equals(existente.getId())) {
                    ItemCarrito destino = duplicadoEnCarrito.get();
                    destino.setCantidad(destino.getCantidad() + existente.getCantidad());
                    itemCarritoRepository.delete(existente);
                    return itemCarritoRepository.save(destino);
                }
            }

            existente.setServicio(servicio);
        }

        return itemCarritoRepository.save(existente);
    }

    // Eliminar
    public boolean eliminar(Long id) {
        ItemCarrito item = itemCarritoRepository.findById(id).orElse(null);
        if (item == null) {
            return false;
        }
        return eliminarExistente(item);
    }

    public boolean eliminarExistente(ItemCarrito item) {
        if (item == null) {
            return false;
        }

        if (item.getCarrito() != null && item.getCarrito().getItems() != null) {
            item.getCarrito().getItems().remove(item);
        }

        itemCarritoRepository.delete(item);
        return true;
    }
}
