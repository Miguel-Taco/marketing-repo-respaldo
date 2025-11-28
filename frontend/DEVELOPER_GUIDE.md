# Frontend Developer Guide

This guide documents the shared components, hooks, and utilities available in `frontend/src/shared`. Please use these resources to ensure consistency and speed up development.

## 1. UI Components

### Core Components (`shared/components/ui`)

#### `Input`, `Select`, `TextArea`
Standard form controls with built-in label and error handling.

```tsx
<Input
  label="Email"
  type="email"
  placeholder="user@example.com"
  error={errors.email}
  {...register('email')}
/>
```

#### `Card`
Container for grouping content.

```tsx
<Card title="Lead Details">
  <p>Content here...</p>
</Card>
```

#### `LoadingSpinner`
Use for loading states.

```tsx
<LoadingSpinner size="lg" />
```

### Complex Components

#### `DataTable`
A powerful table component with pagination support.

```tsx
<DataTable
  columns={[
    { header: 'Name', accessor: 'name' },
    { header: 'Status', accessor: (item) => <Badge>{item.status}</Badge> }
  ]}
  data={leads}
  loading={loading}
  pagination={{
    currentPage: 0,
    totalPages: 10,
    totalElements: 100,
    onPageChange: (page) => setPage(page)
  }}
/>
```

#### `PageHeader`
Standard header for pages.

```tsx
<PageHeader
  title="Leads"
  subtitle="Manage your potential customers"
  action={<Button>Create Lead</Button>}
/>
```

#### `Toast` (Notification)
Use `useToast` to show global notifications.

```tsx
const { showToast } = useToast();
showToast('Lead created successfully', 'success');
```

## 2. Hooks (`shared/hooks`)

#### `useDebounce`
Delays the update of a value (useful for search).

```tsx
const debouncedSearch = useDebounce(searchTerm, 500);
```

#### `useLocalStorage`
Persist state in localStorage.

```tsx
const [theme, setTheme] = useLocalStorage('theme', 'light');
```

## 3. Utilities (`shared/utils`)

#### `formatters.ts`
- `formatDate(date)`: "22/11/2025"
- `formatCurrency(amount)`: "S/ 100.00"
- `formatPhone(phone)`: "(+51) 999 999 999"

#### `validators.ts`
- `isValidEmail(email)`
- `isValidDNI(dni)`
- `isValidPhone(phone)`

#### `constants.ts`
- `APP_CONSTANTS.PAGINATION.DEFAULT_PAGE_SIZE`
- `STATUS_COLORS`
