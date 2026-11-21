package cl.Alchemax.Alchemax.config;

import cl.Alchemax.Alchemax.model.Categoria;
import cl.Alchemax.Alchemax.model.Cita;
import cl.Alchemax.Alchemax.model.Rol;
import cl.Alchemax.Alchemax.model.Servicio;
import cl.Alchemax.Alchemax.model.Usuario;
import cl.Alchemax.Alchemax.repository.CategoriaRepository;
import cl.Alchemax.Alchemax.repository.CitaRepository;
import cl.Alchemax.Alchemax.repository.ServicioRepository;
import cl.Alchemax.Alchemax.repository.UsuarioRepository;
import cl.Alchemax.Alchemax.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final CategoriaRepository categoriaRepository;
    private final ServicioRepository servicioRepository;
    private final CitaRepository citaRepository;

    @Override
    public void run(String... args) {
        seedUsuarios();
        seedCategoriasYServicios();
        seedCitas();
    }

    private void seedUsuarios() {
        if (usuarioRepository.count() > 0) return;

        Usuario admin = new Usuario();
        admin.setNombre("Administrador");
        admin.setEmail("admin@gmail.com");
        admin.setPassword("admin123");
        admin.setGenero("otro");
        admin.setFechaNacimiento("1990-01-01");
        admin.setRol(Rol.admin);
        usuarioService.guardar(admin);

        Usuario paciente = new Usuario();
        paciente.setNombre("Camila Andrea Soto");
        paciente.setEmail("camila.soto@gmail.com");
        paciente.setPassword("user123");
        paciente.setGenero("femenino");
        paciente.setFechaNacimiento("1994-05-12");
        paciente.setRol(Rol.usuario);
        usuarioService.guardar(paciente);

        Usuario nutricionista = new Usuario();
        nutricionista.setNombre("Nut. Andrea Valdés");
        nutricionista.setEmail("michael@gmail.com");
        nutricionista.setPassword("nutri123");
        nutricionista.setGenero("masculino");
        nutricionista.setFechaNacimiento("1985-08-29");
        nutricionista.setRol(Rol.nutricionista);
        usuarioService.guardar(nutricionista);
    }

    private void seedCategoriasYServicios() {
        if (categoriaRepository.count() > 0) return;

        Categoria catEval = categoriaRepository.save(
                new Categoria(null, "Evaluación Biométrica", "Medición y pesajes presenciales", 1));
        Categoria catPlanes = categoriaRepository.save(
                new Categoria(null, "Planes Especializados", "Planes personalizados y deportivos", 1));
        Categoria catTele = categoriaRepository.save(
                new Categoria(null, "Telemedicina y Controles", "Consultas y seguimiento virtual", 1));
        Categoria catClinica = categoriaRepository.save(
                new Categoria(null, "Nutrición Clínica", "Tratamiento de patologías crónicas y metabólicas", 1));

        if (servicioRepository.count() > 0) return;

        servicioRepository.save(new Servicio(
                null,
                "Evaluación Biométrica Inicial",
                "Análisis InBody, revisión de exámenes y diagnóstico metabólico presencial.",
                35000,
                "evaluacion",
                "⚖️",
                catEval,
                catEval.getNombre(),
                true
        ));
        servicioRepository.save(new Servicio(
                null,
                "Plan Nutricional Deportivo",
                "Pauta estructurada para recomposición corporal y rendimiento físico. Incluye 2 controles.",
                120000,
                "plan",
                "🏋️",
                catPlanes,
                catPlanes.getNombre(),
                true
        ));
        servicioRepository.save(new Servicio(
                null,
                "Control Telemedicina",
                "Ajuste de pauta y seguimiento de progreso mediante videollamada de 30 minutos.",
                25000,
                "control",
                "💻",
                catTele,
                catTele.getNombre(),
                true
        ));
        servicioRepository.save(new Servicio(
                null,
                "Plan Nutricional Clínico",
                "Diseñado para patologías específicas (Resistencia a la insulina, hipertensión, celiaquía).",
                85000,
                "plan",
                "🩺",
                catClinica,
                catClinica.getNombre(),
                true
        ));
    }

    private void seedCitas() {
        if (citaRepository.count() > 0) return;

        Usuario paciente = usuarioRepository.findByEmail("camila.soto@gmail.com").orElse(null);

        Cita c1 = new Cita(
                null,
                LocalDate.of(2026, 11, 28),
                "10:30",
                "Consulta Presencial",
                "Nut. Andrea Valdés",
                "Programada"
        );
        if (paciente != null) {
            c1.setUsuario(paciente);
            c1.setPacienteNombre(paciente.getNombre());
            c1.setPacienteEmail(paciente.getEmail());
        }
        citaRepository.save(c1);

        Cita c2 = new Cita(
                null,
                LocalDate.of(2026, 10, 15),
                "11:00",
                "Control Telemedicina",
                "Nut. Andrea Valdés",
                "Completada"
        );
        if (paciente != null) {
            c2.setUsuario(paciente);
            c2.setPacienteNombre(paciente.getNombre());
            c2.setPacienteEmail(paciente.getEmail());
        }
        citaRepository.save(c2);
    }
}
