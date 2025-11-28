# Guía de Recursos Compartidos (Shared Resources)

Esta guía documenta los recursos compartidos disponibles en el frontend y backend del proyecto Marketing CRM. El objetivo es promover la reutilización de código y mantener la consistencia en toda la aplicación.

---

## 🎨 Frontend Shared Resources (`src/shared`)

### 1. UI Components (`src/shared/components/ui`)

Componentes reutilizables estilizados con Tailwind CSS.

#### **DataTable**
Tabla de datos con soporte para paginación y estados de carga.

```tsx
import { DataTable } from '../../shared/components/ui/DataTable';

const columns = [
    { header: 'Nombre', accessor: 'nombre' },
    { header: 'Email', accessor: 'email' },
    { 
        header: 'Estado', 
        accessor: (item) => <Badge>{item.estado}</Badge> 
    }
];

<DataTable
    columns={columns}
    data={data}
    loading={isLoading}
    pagination={{
        currentPage: 0,
        totalPages: 10,
        totalElements: 100,
        onPageChange: (page) => loadPage(page)
    }}
/>
```

#### **Modal**
Ventana modal para confirmaciones o formularios.

```tsx
import { Modal } from '../../shared/components/ui/Modal';

<Modal
    isOpen={isOpen}
    title="Confirmar Acción"
    onClose={() => setIsOpen(false)}
    onConfirm={handleConfirm}
    variant="primary" // o 'danger'
    confirmText="Guardar"
>
    <p>¿Estás seguro de realizar esta acción?</p>
</Modal>
```

#### **Otros Componentes Útiles**
- **Button:** Botones con variantes (`primary`, `secondary`, `danger`, `ghost`) y estado de carga.
- **Input / Select / TextArea:** Campos de formulario estandarizados.
- **LoadingSpinner:** Indicador de carga.
- **Tabs:** Navegación por pestañas.
- **FileDropzone:** Zona de arrastrar y soltar para subida de archivos.

### 2. Hooks (`src/shared/hooks`)

#### **useDebounce**
Retrasa la actualización de un valor hasta que el usuario deja de escribir. Ideal para búsquedas.

```tsx
import { useDebounce } from '../../shared/hooks/useDebounce';

const [searchTerm, setSearchTerm] = useState('');
const debouncedSearch = useDebounce(searchTerm, 500);

useEffect(() => {
    // Se ejecuta solo cuando el usuario deja de escribir por 500ms
    searchApi(debouncedSearch);
}, [debouncedSearch]);
```

#### **useLocalStorage**
Persiste el estado en el almacenamiento local del navegador.

```tsx
const [theme, setTheme] = useLocalStorage('theme', 'light');
```

### 3. Utilities (`src/shared/utils`)

#### **exportUtils**
Utilidades para exportar datos y descargar archivos.

**Exportar a Excel desde API:**
```typescript
import { exportToExcel } from '../../shared/utils/exportUtils';

const handleExport = async () => {
    await exportToExcel(
        '/api/leads/export', // Endpoint
        { estado: 'NUEVO' }, // Parámetros Query
        'GET',               // Método
        null,                // Body (para POST)
        'reporte_leads.xlsx' // Nombre de archivo (opcional)
    );
};
```

**Descargar Blob:**
```typescript
import { downloadBlob } from '../../shared/utils/exportUtils';
downloadBlob(myBlob, 'archivo.pdf');
```

---

## ☕ Backend Shared Resources (`pe.unmsm.crm.marketing.shared`)

### 1. Services (`application/service`)

#### **ExcelExportService**
Servicio genérico para generar archivos Excel (.xlsx) a partir de cualquier lista de objetos.

**Uso:**
1. Definir la configuración de columnas (`ExcelConfig`).
2. Llamar a `exportToExcel`.

```java
@Service
@RequiredArgsConstructor
public class LeadExportService {

    private final ExcelExportService excelService;

    public byte[] exportarLeads(List<Lead> leads) {
        // 1. Definir columnas
        List<ColumnConfig<Lead>> columns = List.of(
            new ColumnConfig<>("ID", Lead::getId),
            new ColumnConfig<>("Nombre", Lead::getNombre),
            new ColumnConfig<>("Email", Lead::getEmail),
            new ColumnConfig<>("Fecha", lead -> lead.getFechaCreacion().toString())
        );

        // 2. Crear configuración
        ExcelConfig<Lead> config = new ExcelConfig<>("Reporte de Leads", columns);

        // 3. Generar Excel
        return excelService.exportToExcel(leads, config);
    }
}
```

### 2. Utilities (`utils`)

#### **DateTimeUtils**
Utilidades para manejo de fechas y horas.

#### **PaginationUtils**
Ayudas para calcular índices y metadatos de paginación.

#### **ResponseUtils**
Estandarización de respuestas API.

---

## 🚀 Buenas Prácticas

1. **DRY (Don't Repeat Yourself):** Antes de crear un nuevo componente o utilidad, revisa si ya existe en `shared`.
2. **Desacoplamiento:** Los recursos compartidos no deben depender de módulos específicos (ej. `leads`, `campaigns`). Deben ser genéricos.
3. **Tipado Fuerte:** Usa Interfaces y Genéricos en TypeScript y Java para mantener la seguridad de tipos.
