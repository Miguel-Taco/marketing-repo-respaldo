package pe.unmsm.crm.marketing;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePasswordHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

        String adminPassword = "admin123";
        String agentePassword = "agente123";

        String adminHash = encoder.encode(adminPassword);
        String agenteHash = encoder.encode(agentePassword);

        System.out.println("Hash para 'admin123':");
        System.out.println(adminHash);
        System.out.println();
        System.out.println("Hash para 'agente123':");
        System.out.println(agenteHash);
        System.out.println();
        System.out.println("SQL INSERT statements:");
        System.out.println("INSERT INTO `usuarios` VALUES (2,'admin','" + adminHash
                + "',_binary '','2025-12-01 08:45:49','2025-12-01 08:45:49');");
        System.out.println("INSERT INTO `usuarios` VALUES (3,'agente1','" + agenteHash
                + "',_binary '','2025-12-01 08:45:52','2025-12-01 08:45:52');");
    }
}
