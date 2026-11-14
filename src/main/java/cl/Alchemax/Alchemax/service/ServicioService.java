package cl.Alchemax.Alchemax.service;

import cl.Alchemax.Alchemax.model.Servicio;
import cl.Alchemax.Alchemax.repository.ServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicioService {

    @Autowired
    private ServicioRepository servicioRepository;

    // Listar
    public List<Servicio> listar() {
        return servicioRepository.findAll();
    }

    //  Guardar
    public Servicio guardar(Servicio s) {
        return servicioRepository.save(s);
    }

    // Actualizar
    public Servicio actualizar(Servicio s, Long id) {
        Servicio ser = servicioRepository.findById(id).orElse(null);
        if (ser == null) return null;

        ser.setNombre(s.getNombre());
        ser.setPrecio(s.getPrecio());
        ser.setTipo(s.getTipo());
        ser.setDescripcion(s.getDescripcion());
        ser.setIcono(s.getIcono());
        ser.setCategoriaId(s.getCategoriaId());
        ser.setCategoriaNombre(s.getCategoriaNombre());
        if (s.getEstado() != null) {
            ser.setEstado(s.getEstado());
        }

        return servicioRepository.save(ser);
    }

    //Cambiar estado
    public Servicio cambiarEstado(Long id) {
        Servicio ser = servicioRepository.findById(id).orElse(null);
        if (ser == null) {
            return null;
        }
       
        boolean estadoActual = Boolean.TRUE.equals(ser.getEstado());
        ser.setEstado(!estadoActual);

        return servicioRepository.save(ser);
    }

    //Buscar por nombre
    public List<Servicio> buscarServicio(String nombre) {
        return servicioRepository.findByNombre(nombre);
    }
}
