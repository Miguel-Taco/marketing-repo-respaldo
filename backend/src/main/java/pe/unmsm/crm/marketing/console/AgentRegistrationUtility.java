package pe.unmsm.crm.marketing.console;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity.AgenteMarketingEntity;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.repository.AgenteMarketingRepository;
import pe.unmsm.crm.marketing.security.domain.RolEntity;
import pe.unmsm.crm.marketing.security.domain.UsuarioEntity;
import pe.unmsm.crm.marketing.security.repository.RolRepository;
import pe.unmsm.crm.marketing.security.repository.UsuarioRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

@Component
@Profile("console")
public class AgentRegistrationUtility implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final AgenteMarketingRepository agenteMarketingRepository;
    private final PasswordEncoder passwordEncoder;

    public AgentRegistrationUtility(UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            AgenteMarketingRepository agenteMarketingRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.agenteMarketingRepository = agenteMarketingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n=================================================");
        System.out.println("   UTILIDAD DE REGISTRO DE AGENTES DE MARKETING   ");
        System.out.println("=================================================\n");

        Scanner scanner = new Scanner(System.in);
        boolean continuar = true;

        while (continuar) {
            System.out.print("¿Desea registrar un nuevo agente? (S/N): ");
            String response = scanner.nextLine().trim();

            if (!response.equalsIgnoreCase("S")) {
                System.out.println("\nSaliendo de la utilidad de registro...");
                continuar = false;
            } else {
                try {
                    registerAgent(scanner);
                    System.out.println("\n");
                } catch (Exception e) {
                    System.err.println("Error durante el registro: " + e.getMessage());
                    e.printStackTrace();
                    System.out.println("\n");
                }
            }
        }

        scanner.close();
        System.out.println("¡Hasta pronto!");
        System.exit(0);
    }

    @Transactional
    private void registerAgent(Scanner scanner) {
        // 1. Personal Details
        System.out.println("\n--- Datos Personales ---");
        System.out.print("Nombre completo: ");
        String nombre = scanner.nextLine().trim();

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Teléfono: ");
        String telefono = scanner.nextLine().trim();

        // 2. Credentials
        System.out.println("\n--- Credenciales de Acceso ---");
        String username;
        while (true) {
            System.out.print("Username: ");
            username = scanner.nextLine().trim();
            if (username.isEmpty()) {
                System.out.println("El username no puede estar vacío.");
                continue;
            }
            if (usuarioRepository.existsByUsername(username)) {
                System.out.println("El username ya existe. Por favor elija otro.");
            } else {
                break;
            }
        }

        String password;
        while (true) {
            System.out.print("Password: ");
            password = scanner.nextLine().trim();
            if (!password.isEmpty())
                break;
        }

        // 3. Roles
        System.out.println("\n--- Selección de Roles ---");
        List<RolEntity> roles = rolRepository.findAll();
        if (roles.isEmpty()) {
            System.out.println("ERROR: No existen roles en la base de datos.");
            return;
        }

        for (RolEntity rol : roles) {
            System.out.printf("ID: %d | Nombre: %s | Descripción: %s%n", rol.getIdRol(), rol.getNombre(),
                    rol.getDescripcion());
        }

        System.out.print("Ingrese el ID del rol a asignar: ");
        long roleId;
        try {
            roleId = Long.parseLong(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("ID de rol inválido.");
            return;
        }

        long finalRoleId = roleId;
        RolEntity selectedRole = roles.stream()
                .filter(r -> r.getIdRol() == finalRoleId)
                .findFirst()
                .orElse(null);

        if (selectedRole == null) {
            System.out.println("Rol no válido.");
            return;
        }

        // 4. Additional Agent Info
        System.out.println("\n--- Información Adicional del Agente ---");
        System.out.print("ID Trabajador RRHH (Obligatorio): ");
        long idTrabajadorRrhh;
        try {
            idTrabajadorRrhh = Long.parseLong(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("ID de trabajador inválido. Usando 0 por defecto.");
            idTrabajadorRrhh = 0L;
        }

        // 5. Create and Save
        System.out.println("\nGuardando información...");

        // Create Usuario
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setUsername(username);

        usuario.setPasswordHash(passwordEncoder.encode(password));
        usuario.setActivo(true);

        Set<RolEntity> usuarioRoles = new HashSet<>();
        usuarioRoles.add(selectedRole);
        usuario.setRoles(usuarioRoles);

        UsuarioEntity savedUsuario = usuarioRepository.save(usuario);
        System.out.println("Usuario creado con ID: " + savedUsuario.getIdUsuario());

        // Create Agente
        AgenteMarketingEntity agente = new AgenteMarketingEntity();
        agente.setNombre(nombre);
        agente.setEmail(email);
        agente.setTelefono(telefono);
        agente.setIdUsuario(savedUsuario.getIdUsuario());
        agente.setIdTrabajadorRrhh(idTrabajadorRrhh);
        agente.setHabilitadoLlamadas(true); // Default
        agente.setActivo(true); // Default
        agente.setMaxLlamadasDia(100); // Default value
        agente.setCanalPrincipal("Llamadas"); // Default

        AgenteMarketingEntity savedAgente = agenteMarketingRepository.save(agente);
        System.out.println("Agente de marketing creado con ID: " + savedAgente.getIdAgente());
        System.out.println("\n✓ Registro completado exitosamente.");
        System.out.println("=================================================\n");
    }
}
