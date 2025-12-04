# Audit Plan - Telephone Campaign Lifecycle

This plan outlines the steps to verify the integration between the Campaign Manager (Gestor) and the Telephone Campaign module, ensuring all lifecycle actions function correctly and affect the system as intended.

## Objectives
- Verify that actions in the **Campaign Manager** correctly propagate to the **Telephone Module**.
- Verify that **status changes** (Pause, Cancel, Finalize) correctly **block** call distribution.
- Verify that **Reprogramming** updates campaign dates.

## Audit Steps

### 1. Setup & Creation
1.  Navigate to **Campaign Manager** (`/marketing/campanas/gestor`).
2.  Create a new **Telephone Campaign** (Canal: Llamadas).
    -   Name: `Audit Test Campaign [Timestamp]`
    -   Segment: Select any available segment.
    -   Dates: Future dates initially.
3.  **Verify:** Navigate to **Telephone Campaigns** (`/marketing/campanas/telefonicas`).
    -   Confirm the campaign appears in the list with status **BORRADOR** (or Programada if created directly).

### 2. Program & Activate
1.  In **Gestor**, "Programar" the campaign (if in Draft).
2.  In **Gestor**, "Activar" the campaign.
3.  **Verify:**
    -   In **Telephone Module**, status should be **VIGENTE**.
    -   (Optional) Check if agents can see contacts (requires agent login simulation).

### 3. Pause & Call Blocking (Critical)
1.  In **Gestor**, click **Pausar**.
2.  **Verify:**
    -   In **Telephone Module**, status should be **PAUSADA**.
    -   **Critical:** Verify that no calls can be initiated (simulated by checking if "Start Calling" button is disabled or if API returns no contacts).

### 4. Resume
1.  In **Gestor**, click **Reanudar**.
2.  **Verify:**
    -   In **Telephone Module**, status should be **VIGENTE**.

### 5. Reprogram
1.  In **Gestor**, click **Reprogramar**.
2.  Change Start/End dates.
3.  **Verify:**
    -   In **Telephone Module**, verify the dates have been updated.

### 6. Finalize (New Feature)
1.  In **Gestor**, click **Finalizar** (or move to Finalized state).
2.  **Verify:**
    -   In **Telephone Module**, status should be **FINALIZADA**.
    -   Verify campaign is read-only or archived.

### 7. Cancel
1.  Create a dummy campaign for this test.
2.  In **Gestor**, click **Cancelar**.
3.  **Verify:**
    -   In **Telephone Module**, status should be **CANCELADA**.

## Success Criteria
-   All status changes in Gestor are reflected in Telephone Module within < 5 seconds.
-   "Pausar" and "Finalizar" effectively stop the operational flow (no new contacts served).
