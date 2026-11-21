package cl.Alchemax.Alchemax.service;

import cl.Alchemax.Alchemax.exception.SolicitudInvalidaException;
import cl.Alchemax.Alchemax.model.Compra;
import cl.Alchemax.Alchemax.model.Servicio;
import cl.Alchemax.Alchemax.model.Usuario;
import cl.Alchemax.Alchemax.repository.CompraRepository;
import cl.Alchemax.Alchemax.repository.ServicioRepository;
import cl.Alchemax.Alchemax.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CompraService {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired(required = false)
    private ServicioRepository servicioRepository;

    // Listar todas las compras ordenadas por fecha e ID descendente en SQL
    public List<Compra> listarTodas() {
        return compraRepository.findAllByOrderByFechaCompraDescIdDesc();
    }

    // Listar compras con paginación en SQL
    public List<Compra> listarTodasPaginado(int page, int size) {
        return compraRepository
                .findAllByOrderByFechaCompraDescIdDesc(
                        PageRequest.of(Math.max(0, page), Math.max(1, size))
                )
                .getContent();
    }

    // Buscar por ID
    public Compra buscarPorId(Long id) {
        return compraRepository.findById(id).orElse(null);
    }

    // Listar por usuario ordenado en SQL
    public List<Compra> listarPorUsuario(Long usuarioId) {
        return compraRepository.findByUsuario_IdOrderByFechaCompraDescIdDesc(usuarioId);
    }

    // Guardar validando usuario y resolviendo precio/nombre reales desde el catálogo
    @Transactional
    public Compra guardar(Long usuarioId, Compra compra) {
        Usuario user = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new SolicitudInvalidaException("Usuario no encontrado."));

        if (compra.getServicio() <= 0) {
            throw new SolicitudInvalidaException("El identificador del servicio debe ser positivo.");
        }

        if (servicioRepository != null) {
            Servicio servicioCatalogo = servicioRepository.findById((long) compra.getServicio())
                    .orElseThrow(() -> new SolicitudInvalidaException(
                            "El servicio indicado no existe en el catálogo."
                    ));
            compra.setNombre(servicioCatalogo.getNombre());
            compra.setMonto(servicioCatalogo.getPrecio());
        }

        compra.setId(null);
        compra.setUsuario(user);

        if (compra.getFechaCompra() == null) {
            compra.setFechaCompra(LocalDate.now());
        }
        if (compra.getEstado() == null || compra.getEstado().isBlank()) {
            compra.setEstado(Compra.ESTADO_ACTIVO);
        } else if (!Compra.ESTADOS_VALIDOS.contains(compra.getEstado())) {
            throw new SolicitudInvalidaException("El estado debe ser Activo, Completado o Cancelado.");
        }

        return compraRepository.save(compra);
    }

    // Cambiar estado por ID
    @Transactional
    public Compra cambiarEstado(Long id, String nuevoEstado) {
        validarEstadoDestino(nuevoEstado);
        Compra existente = compraRepository.findById(id).orElse(null);
        if (existente == null) {
            return null;
        }
        return cambiarEstadoExistente(existente, nuevoEstado);
    }

    // Cambiar estado reutilizando entidad cargada
    @Transactional
    public Compra cambiarEstadoExistente(Compra existente, String nuevoEstado) {
        if (existente == null) {
            return null;
        }
        validarEstadoDestino(nuevoEstado);
        if (Compra.ESTADO_CANCELADO.equals(existente.getEstado())
                && !Compra.ESTADO_CANCELADO.equals(nuevoEstado)) {
            throw new SolicitudInvalidaException(
                    "No se puede reactivar ni completar una compra que ya fue cancelada."
            );
        }
        existente.setEstado(nuevoEstado);
        return compraRepository.save(existente);
    }

    // Eliminar (1 SELECT + 1 DELETE)
    @Transactional
    public boolean eliminar(Long id) {
        Compra existente = compraRepository.findById(id).orElse(null);
        if (existente == null) {
            return false;
        }
        compraRepository.delete(existente);
        return true;
    }

    private void validarEstadoDestino(String nuevoEstado) {
        if (nuevoEstado == null || !Compra.ESTADOS_VALIDOS.contains(nuevoEstado)) {
            throw new SolicitudInvalidaException(
                    "El estado debe ser Activo, Completado o Cancelado."
            );
        }
    }
}
