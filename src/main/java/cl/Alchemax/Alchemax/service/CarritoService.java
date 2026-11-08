package cl.Alchemax.Alchemax.service;

import cl.Alchemax.Alchemax.model.Carrito;
import cl.Alchemax.Alchemax.model.ItemCarrito;
import cl.Alchemax.Alchemax.model.Servicio;
import cl.Alchemax.Alchemax.model.Usuario;
import cl.Alchemax.Alchemax.repository.CarritoRepository;
import cl.Alchemax.Alchemax.repository.ServicioRepository;
import cl.Alchemax.Alchemax.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CarritoService {
    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    // Listar
    public Carrito obtenerPorUsuario(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId).orElseGet(() -> {
            Usuario user = usuarioRepository.findById(usuarioId).orElse(null);
            if (user == null) return null;

            Carrito nuevoCarrito = new Carrito();
            nuevoCarrito.setUsuario(user);
            return carritoRepository.save(nuevoCarrito);
        });
    }

    // Agregar
    public Carrito agregarItem(Long usuarioId, Long servicioId, int cantidad) {
        Carrito carrito = obtenerPorUsuario(usuarioId);
        if (carrito == null) return null;

        Servicio servicio = servicioRepository.findById(servicioId).orElse(null);
        if (servicio == null) return null;

        // Comprobar si el servicio ya estaba en el carrito
        Optional<ItemCarrito> itemExistente = carrito.getItems().stream()
                .filter(item -> item.getServicio().getId().equals(servicioId))
                .findFirst();

        if (itemExistente.isPresent()) {

            itemExistente.get().setCantidad(itemExistente.get().getCantidad() + cantidad);
        } else {
            ItemCarrito nuevoItem = new ItemCarrito();
            nuevoItem.setServicio(servicio);
            nuevoItem.setCantidad(cantidad > 0 ? cantidad : 1);
            nuevoItem.setCarrito(carrito);
            carrito.getItems().add(nuevoItem);
        }

        return carritoRepository.save(carrito);
    }

    // Eliminar
    public Carrito removerItem(Long usuarioId, Long servicioId) {
        Carrito carrito = obtenerPorUsuario(usuarioId);
        if (carrito == null) return null;

        carrito.getItems().removeIf(item -> item.getServicio().getId().equals(servicioId));
        return carritoRepository.save(carrito);
    }

    // Vaciar el carrito (Al terminar el pago)
    public Carrito vaciarCarrito(Long usuarioId) {
        Carrito carrito = obtenerPorUsuario(usuarioId);
        if (carrito == null) return null;

        carrito.getItems().clear();
        return carritoRepository.save(carrito);
    }
}

