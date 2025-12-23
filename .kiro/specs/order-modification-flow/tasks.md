# Implementation Plan: Order Modification Flow

## Overview

Este plan implementa el flujo mejorado de gestión de pedidos en la app de negocios. Las tareas están organizadas para construir incrementalmente: primero los modelos de datos, luego la lógica de estado, después los componentes UI, y finalmente la integración con el chat.

## Tasks

- [-] 1. Crear modelos de datos para modificación de pedidos
  - [x] 1.1 Crear ModificationType enum y ModifiedOrderItem data class en Order.kt
    - Agregar enum ModificationType con valores: UNCHANGED, QUANTITY_CHANGED, INSTRUCTIONS_CHANGED, ADDED, REMOVED
    - Agregar data class ModifiedOrderItem con orderItem, modificationType, originalQuantity, originalInstructions
    - _Requirements: 2.3, 2.4, 2.5, 2.6, 3.4_

  - [ ] 1.2 Crear OrderModificationState data class
    - Crear nuevo archivo OrderModificationState.kt en data/model/
    - Incluir: originalItems, modifiedItems, isEditMode, hasChanges, originalTotal, newTotal
    - Agregar computed properties: totalDifference, hasPriceChange
    - _Requirements: 3.1, 3.2, 3.3, 6.3_

  - [ ] 1.3 Extender MessageType enum en Chat.kt
    - Agregar ORDER_MODIFIED al enum MessageType
    - _Requirements: 5.1, 5.2, 5.3_

- [ ] 2. Implementar lógica de modificación en OrdersViewModel
  - [ ] 2.1 Agregar estado de modificación al ViewModel
    - Agregar MutableStateFlow<OrderModificationState?> para el estado de modificación
    - Agregar función enterEditMode(order: Order)
    - Agregar función exitEditMode()
    - _Requirements: 2.2, 7.1_

  - [ ] 2.2 Implementar operaciones de modificación de items
    - Agregar función modifyItemQuantity(itemId: String, newQuantity: Int)
    - Agregar función removeItem(itemId: String)
    - Agregar función addItem(menuItem: MenuItem, quantity: Int, instructions: String?)
    - Agregar función modifyItemInstructions(itemId: String, instructions: String?)
    - Actualizar hasChanges automáticamente al comparar con originalItems
    - _Requirements: 2.3, 2.4, 2.5, 2.6_

  - [ ] 2.3 Implementar cálculo automático de totales
    - Agregar función privada recalculateTotals()
    - Calcular subtotal de cada item como price * quantity
    - Calcular newTotal como suma de subtotales
    - Llamar recalculateTotals() después de cada modificación
    - _Requirements: 6.1, 6.2_

  - [ ]* 2.4 Write property test for total calculation correctness
    - **Property 8: Total Calculation Correctness**
    - **Validates: Requirements 6.1, 6.2**

  - [ ] 2.5 Implementar cancelación de edición
    - Agregar función cancelEdit() que restaure modifiedItems = originalItems
    - Resetear hasChanges a false
    - Resetear isEditMode a false
    - _Requirements: 7.2, 7.3_

  - [ ]* 2.6 Write property test for cancel edit restoration
    - **Property 9: Cancel Edit Restoration (Round-Trip)**
    - **Validates: Requirements 7.2, 7.3**

- [ ] 3. Checkpoint - Verificar lógica de modificación
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 4. Simplificar flujo de aceptación de pedido
  - [ ] 4.1 Modificar OrderActionsSection para aceptación directa
    - Eliminar showAcceptConfirmation state y su AlertDialog
    - Cambiar onClick del botón "Aceptar" para llamar onUpdateStatus(OrderStatus.PREPARING) directamente
    - Eliminar acceptNotes state ya que no se usa
    - _Requirements: 1.1, 1.2_

  - [ ]* 4.2 Write property test for accept order state transition
    - **Property 1: Accept Order State Transition**
    - **Validates: Requirements 1.1, 1.2**

- [ ] 5. Transformar "Rechazar" en "Modificar Pedido"
  - [ ] 5.1 Actualizar botón y texto en OrderActionsSection
    - Cambiar texto de "Rechazar" a "Modificar Pedido"
    - Cambiar icono de Cancel a Edit
    - Cambiar color de error a secondary/neutral
    - Cambiar onClick para llamar onEnterEditMode() en lugar de showCancelConfirmation
    - _Requirements: 2.1_

  - [ ] 5.2 Agregar UI de modo edición en OrderItemsSection
    - Crear nuevo composable EditableOrderItemsSection
    - Mostrar controles de cantidad (+/-) para cada item
    - Mostrar botón de eliminar para cada item
    - Mostrar campo editable para instrucciones especiales
    - Resaltar visualmente items modificados (diferente color de fondo)
    - _Requirements: 2.3, 2.4, 2.6, 3.4_

  - [ ] 5.3 Agregar botón para agregar nuevos items
    - Crear composable AddItemButton que abra un selector de menú
    - Mostrar lista de items disponibles del menú
    - Permitir seleccionar cantidad e instrucciones al agregar
    - _Requirements: 2.5_

- [ ] 6. Implementar control dinámico de botones
  - [ ] 6.1 Actualizar OrderActionsSection con estados de botones
    - Recibir modificationState como parámetro
    - Deshabilitar "Aceptar" cuando hasChanges es true
    - Mostrar "Confirmar Modificación" solo cuando isEditMode es true
    - Habilitar "Confirmar Modificación" solo cuando hasChanges es true
    - Mostrar "Cancelar Edición" cuando isEditMode es true
    - _Requirements: 3.1, 3.2, 3.3, 7.1_

  - [ ]* 6.2 Write property test for button state consistency
    - **Property 5: Button State Consistency**
    - **Validates: Requirements 3.1, 3.2, 3.3**

- [ ] 7. Implementar diálogo de nota obligatoria
  - [ ] 7.1 Crear ModificationNoteDialog composable
    - Crear nuevo composable en OrderDetailSections.kt
    - Mostrar campo de texto para la nota
    - Mostrar contador de caracteres (mínimo 10)
    - Deshabilitar botón de confirmación si nota < 10 caracteres
    - Incluir botón de cancelar que cierre el diálogo
    - _Requirements: 4.1, 4.2, 4.3_

  - [ ]* 7.2 Write property test for note validation
    - **Property 6: Modification Note Validation**
    - **Validates: Requirements 4.2, 4.3**

- [ ] 8. Checkpoint - Verificar UI de modificación
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 9. Integrar con ChatsViewModel
  - [ ] 9.1 Agregar método createModificationMessage en ChatsViewModel
    - Crear función que reciba orderId, orderNumber, customerName, note, modifiedItems, totals
    - Crear ChatMessage con messageType = ORDER_MODIFIED
    - Formatear mensaje con nota y resumen de cambios
    - Agregar mensaje al chat existente o crear nuevo chat
    - _Requirements: 5.1, 5.2, 5.3_

  - [ ]* 9.2 Write property test for chat message content
    - **Property 7: Chat Message Content Completeness**
    - **Validates: Requirements 5.1, 5.2, 5.3**

  - [ ] 9.3 Implementar navegación al chat después de confirmar
    - Llamar createModificationMessage desde OrderActionsSection
    - Navegar a ChatDetail screen con el orderId
    - _Requirements: 5.4_

- [ ] 10. Actualizar datos de prueba
  - [ ] 10.1 Agregar pedidos PENDING con múltiples items variados
    - Asegurar que getMockOrders() tenga al menos 2 pedidos PENDING
    - Incluir pedidos con 3+ items para probar modificaciones
    - Incluir items con y sin instrucciones especiales
    - _Requirements: 8.1, 8.4_

  - [ ] 10.2 Verificar items de menú disponibles
    - Asegurar que getMockProducts() tenga items variados
    - Incluir items de diferentes categorías
    - _Requirements: 8.2_

  - [ ] 10.3 Agregar chats asociados a pedidos de prueba
    - Asegurar que mockChatsData tenga chats para los pedidos PENDING
    - Incluir historial de mensajes previos
    - _Requirements: 8.3, 8.5_

- [ ] 11. Mostrar comparación de totales
  - [ ] 11.1 Agregar sección de comparación de precios en modo edición
    - Mostrar total original
    - Mostrar nuevo total
    - Resaltar diferencia si hay cambio de precio (verde si menor, rojo si mayor)
    - _Requirements: 6.3, 6.4_

- [ ] 12. Final checkpoint - Verificar flujo completo
  - Ensure all tests pass, ask the user if questions arise.
  - Probar flujo: Ver pedido → Modificar items → Agregar nota → Confirmar → Ver chat

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties
- Unit tests validate specific examples and edge cases
