import re

# Read the file
with open('src/main/java/pe/unmsm/crm/marketing/campanas/telefonicas/infra/jpa/JpaCampaignDataProvider.java', 'r', encoding='utf-8') as f:
    content = f.read()

# Define the old code to replace
old_code = '''                        contacto.setEstadoEnCola("COMPLETADO");
                        contacto.setIdAgenteActual(null); // Liberar asignación
                        colaRepo.save(contacto);

                        log.info("Estado de contacto actualizado a COMPLETADO [contactoId={}]",
                                        request.getIdContactoCola());'''

# Define the new code
new_code = '''                        // Verificar si hay reagendamiento
                        if (request.getFechaReagendamiento() != null) {
                                // REAGENDAR: Volver a PENDIENTE con fecha programada
                                contacto.setEstadoEnCola("PENDIENTE");
                                contacto.setFechaProgramada(request.getFechaReagendamiento());
                                contacto.setPrioridadCola("ALTA"); // Alta prioridad para reagendados
                                contacto.setIdAgenteActual(null); // Liberar al pool
                                
                                log.info("Contacto reagendado para {} [contactoId={}]",
                                                request.getFechaReagendamiento(), request.getIdContactoCola());
                        } else {
                                // COMPLETAR: Marcar como completado
                                contacto.setEstadoEnCola("COMPLETADO");
                                contacto.setIdAgenteActual(null); // Liberar asignación
                                
                                log.info("Estado de contacto actualizado a COMPLETADO [contactoId={}]",
                                                request.getIdContactoCola());
                        }
                        
                        colaRepo.save(contacto);'''

# Replace
content = content.replace(old_code, new_code)

# Write back
with open('src/main/java/pe/unmsm/crm/marketing/campanas/telefonicas/infra/jpa/JpaCampaignDataProvider.java', 'w', encoding='utf-8') as f:
    f.write(content)

print("File updated successfully!")
