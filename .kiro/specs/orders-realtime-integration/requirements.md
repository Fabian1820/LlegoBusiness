# Requirements Document

## Introduction

Este documento define los requisitos para la integración completa del sistema de pedidos en tiempo real con el backend GraphQL. La aplicación de negocio debe recibir notificaciones push y actualizaciones en tiempo real cuando lleguen nuevos pedidos a cualquier sucursal del negocio, permitiendo cambio rápido de sucursal desde la notificación y gestión completa del ciclo de vida del pedido.

## Glossary

- **Order_System**: Sistema de gestión de pedidos que maneja la obtención, visualización y modificación de pedidos
- **Subscription_Manager**: Componente que gestiona las suscripciones GraphQL para actualizaciones en tiempo real
- **Notification_Service**: Servicio que maneja las notificaciones push y locales para nuevos pedidos
- **Order_Repository**: Repositorio que conecta con el backend GraphQL para operaciones CRUD de pedidos
- **Branch_Context**: Contexto que mantiene la sucursal activa actual y las sucursales del negocio
- **Order_Status**: Estado del pedido según el enum del backend (PENDING_ACCEPTANCE, MODIFIED_BY_STORE, ACCEPTED, PREPARING, READY_FOR_PICKUP, ON_THE_WAY, DELIVERED, CANCELLED)
- **Payment_Status**: Estado del pago (PENDING, VALIDATED, COMPLETED, FAILED)

## Requirements

### Requirement 1: Modelo de Datos Alineado con Backend

**User Story:** Como desarrollador, quiero que el modelo de datos de pedidos esté completamente alineado con el schema GraphQL del backend, para que la app refleje correctamente toda la información disponible.

#### Acceptance Criteria

1. THE Order_System SHALL define un modelo Order que incluya todos los campos del tipo OrderType del backend: id, orderNumber, customerId, branchId, businessId, subtotal, deliveryFee, total, currency, status, paymentMethod, paymentStatus, createdAt, updatedAt, lastStatusAt, deliveryPersonId, estimatedDeliveryTime, paymentId, rating, ratingComment
2. THE Order_System SHALL definir un modelo OrderItem que incluya: productId, name, price, quantity, imageUrl, wasModifiedByStore, lineTotal
3. THE Order_System SHALL definir un modelo DeliveryAddress que incluya: street, city, reference, coordinates
4. THE Order_System SHALL definir un modelo OrderTimeline que incluya: status, timestamp, message, actor
5. THE Order_System SHALL definir un modelo OrderComment que incluya: id, author, message, timestamp
6. THE Order_System SHALL definir un modelo OrderDiscount que incluya: id, title, amount, type
7. THE Order_System SHALL usar el enum OrderStatusEnum del backend: PENDING_ACCEPTANCE, MODIFIED_BY_STORE, ACCEPTED, PREPARING, READY_FOR_PICKUP, ON_THE_WAY, DELIVERED, CANCELLED
8. THE Order_System SHALL usar el enum PaymentStatusEnum del backend: PENDING, VALIDATED, COMPLETED, FAILED
9. THE Order_System SHALL usar el enum OrderActorEnum del backend: CUSTOMER, BUSINESS, SYSTEM, DELIVERY
10. THE Order_System SHALL eliminar todos los datos mock y funciones getMockOrders existentes

### Requirement 2: Obtención de Pedidos desde Backend

**User Story:** Como dueño de negocio, quiero ver los pedidos reales de mi sucursal activa desde el backend, para gestionar mi operación con datos actualizados.

#### Acceptance Criteria

1. WHEN la pantalla de pedidos se carga, THE Order_Repository SHALL ejecutar la query branchOrders con el branchId de la sucursal activa y el JWT del usuario
2. WHEN se solicitan pedidos pendientes, THE Order_Repository SHALL ejecutar la query pendingBranchOrders para obtener solo pedidos que requieren acción inmediata
3. THE Order_Repository SHALL soportar filtrado por status usando el parámetro status de la query branchOrders
4. THE Order_Repository SHALL soportar filtrado por rango de fechas usando los parámetros fromDate y toDate
5. THE Order_Repository SHALL soportar paginación usando los parámetros limit y offset
6. WHEN se obtiene un pedido específico, THE Order_Repository SHALL ejecutar la query order con el orderId correspondiente
7. THE Order_System SHALL mostrar indicador de carga mientras se obtienen los pedidos del backend
8. IF la obtención de pedidos falla, THEN THE Order_System SHALL mostrar mensaje de error y opción de reintentar

### Requirement 3: Suscripciones en Tiempo Real

**User Story:** Como dueño de negocio, quiero recibir actualizaciones en tiempo real de los pedidos, para reaccionar inmediatamente a nuevos pedidos y cambios de estado.

#### Acceptance Criteria

1. WHEN el usuario tiene una sucursal activa, THE Subscription_Manager SHALL establecer una suscripción GraphQL newBranchOrder para esa sucursal
2. WHEN el usuario tiene una sucursal activa, THE Subscription_Manager SHALL establecer una suscripción GraphQL branchOrderUpdated para esa sucursal
3. WHEN llega un nuevo pedido via suscripción, THE Order_System SHALL agregar el pedido a la lista sin necesidad de refresh manual
4. WHEN un pedido existente se actualiza via suscripción, THE Order_System SHALL actualizar el pedido en la lista automáticamente
5. WHEN el usuario cambia de sucursal activa, THE Subscription_Manager SHALL cancelar las suscripciones anteriores y establecer nuevas para la nueva sucursal
6. IF la conexión de suscripción se pierde, THEN THE Subscription_Manager SHALL intentar reconectar automáticamente
7. THE Subscription_Manager SHALL mantener suscripciones activas para TODAS las sucursales del negocio del usuario para recibir notificaciones de pedidos en cualquier sucursal

### Requirement 4: Notificaciones de Nuevos Pedidos

**User Story:** Como dueño de negocio, quiero recibir notificaciones cuando llegue un pedido a cualquiera de mis sucursales, para no perder ninguna venta.

#### Acceptance Criteria

1. WHEN llega un nuevo pedido a la sucursal activa, THE Notification_Service SHALL mostrar una notificación local con el número de pedido y total
2. WHEN llega un nuevo pedido a una sucursal diferente a la activa, THE Notification_Service SHALL mostrar una notificación con botón "Cambiar para ver"
3. WHEN el usuario toca "Cambiar para ver" en la notificación, THE Order_System SHALL cambiar automáticamente a esa sucursal y mostrar el pedido
4. THE Notification_Service SHALL reproducir un sonido distintivo cuando llegue un nuevo pedido
5. WHEN la app está en segundo plano, THE Notification_Service SHALL mostrar notificación push del sistema
6. THE Notification_Service SHALL mostrar badge con contador de pedidos pendientes en el ícono de la app

### Requirement 5: Gestión de Estados del Pedido

**User Story:** Como dueño de negocio, quiero poder cambiar el estado de los pedidos según su progreso, para mantener informados a los clientes.

#### Acceptance Criteria

1. WHEN un pedido está en PENDING_ACCEPTANCE, THE Order_System SHALL permitir ejecutar la mutation acceptOrder con estimatedMinutes
2. WHEN un pedido está en PENDING_ACCEPTANCE, THE Order_System SHALL permitir ejecutar la mutation rejectOrder con reason
3. WHEN un pedido está en ACCEPTED o PREPARING, THE Order_System SHALL permitir ejecutar la mutation updateOrderStatus para cambiar a PREPARING
4. WHEN un pedido está en PREPARING, THE Order_System SHALL permitir ejecutar la mutation markOrderReady para cambiar a READY_FOR_PICKUP
5. THE Order_System SHALL mostrar los botones de acción apropiados según el estado actual del pedido
6. WHEN se ejecuta una acción sobre un pedido, THE Order_System SHALL mostrar indicador de carga y deshabilitar botones hasta completar
7. IF una acción sobre pedido falla, THEN THE Order_System SHALL mostrar mensaje de error descriptivo
8. THE Order_System SHALL actualizar la UI inmediatamente después de una acción exitosa sin esperar la suscripción

### Requirement 6: Modificación de Items del Pedido

**User Story:** Como dueño de negocio, quiero poder modificar los items de un pedido cuando sea necesario, para ajustar disponibilidad o corregir errores.

#### Acceptance Criteria

1. WHEN un pedido permite edición (isEditable = true), THE Order_System SHALL mostrar botón de editar items
2. WHEN el usuario entra en modo edición, THE Order_System SHALL cargar los productos de la sucursal para agregar nuevos items
3. THE Order_System SHALL permitir modificar la cantidad de items existentes
4. THE Order_System SHALL permitir eliminar items del pedido
5. THE Order_System SHALL permitir agregar nuevos items desde el catálogo de productos
6. WHEN el usuario confirma modificaciones, THE Order_System SHALL ejecutar la mutation modifyOrderItems con los items actualizados y reason
7. THE Order_System SHALL recalcular y mostrar el nuevo total en tiempo real durante la edición
8. THE Order_System SHALL marcar visualmente los items que fueron modificados (wasModifiedByStore = true)

### Requirement 7: Comentarios en Pedidos

**User Story:** Como dueño de negocio, quiero poder agregar comentarios a los pedidos, para comunicarme con el cliente sobre el estado o problemas.

#### Acceptance Criteria

1. THE Order_System SHALL mostrar el historial de comentarios del pedido ordenados por timestamp
2. THE Order_System SHALL mostrar el actor de cada comentario (CUSTOMER, BUSINESS, SYSTEM, DELIVERY) con icono distintivo
3. WHEN el usuario escribe un comentario, THE Order_System SHALL ejecutar la mutation addOrderComment
4. WHEN se agrega un comentario exitosamente, THE Order_System SHALL actualizar la lista de comentarios inmediatamente

### Requirement 8: Timeline del Pedido

**User Story:** Como dueño de negocio, quiero ver el historial completo de estados del pedido, para entender su progreso y resolver disputas.

#### Acceptance Criteria

1. THE Order_System SHALL mostrar el timeline completo del pedido con todos los cambios de estado
2. THE Order_System SHALL mostrar para cada entrada del timeline: status, timestamp formateado, message y actor
3. THE Order_System SHALL ordenar el timeline cronológicamente del más reciente al más antiguo
4. THE Order_System SHALL usar iconos distintivos para cada tipo de actor en el timeline

### Requirement 9: Filtrado de Pedidos Alineado con Backend

**User Story:** Como dueño de negocio, quiero filtrar pedidos por estado y fecha usando los filtros del backend, para encontrar rápidamente lo que busco.

#### Acceptance Criteria

1. THE Order_System SHALL mostrar filtros por estado usando los valores del enum OrderStatusEnum del backend
2. THE Order_System SHALL mostrar filtros por rango de fecha que se traduzcan a parámetros fromDate y toDate
3. WHEN el usuario selecciona un filtro de estado, THE Order_System SHALL ejecutar nueva query con el parámetro status
4. WHEN el usuario selecciona un rango de fecha, THE Order_System SHALL ejecutar nueva query con fromDate y toDate
5. THE Order_System SHALL mostrar el nombre localizado de cada estado: PENDING_ACCEPTANCE="Pendiente", MODIFIED_BY_STORE="Modificado", ACCEPTED="Aceptado", PREPARING="Preparando", READY_FOR_PICKUP="Listo", ON_THE_WAY="En camino", DELIVERED="Entregado", CANCELLED="Cancelado"

### Requirement 10: Información del Cliente y Entrega

**User Story:** Como dueño de negocio, quiero ver la información completa del cliente y dirección de entrega, para coordinar la entrega correctamente.

#### Acceptance Criteria

1. THE Order_System SHALL mostrar la información del cliente obtenida del campo customer del pedido: name, phone, avatar
2. THE Order_System SHALL mostrar la dirección de entrega completa: street, city, reference
3. THE Order_System SHALL mostrar las coordenadas de entrega en un mapa cuando estén disponibles
4. WHEN hay un repartidor asignado, THE Order_System SHALL mostrar información del deliveryPerson: name, phone, vehicleType, rating
5. THE Order_System SHALL permitir llamar al cliente tocando el número de teléfono
6. THE Order_System SHALL mostrar el tiempo estimado de entrega (estimatedDeliveryTime) cuando esté disponible

### Requirement 11: Estadísticas de Pedidos

**User Story:** Como dueño de negocio, quiero ver estadísticas de mis pedidos, para entender el rendimiento de mi negocio.

#### Acceptance Criteria

1. THE Order_System SHALL ejecutar la query orderStats con fromDate, toDate y branchId opcional
2. THE Order_System SHALL mostrar: totalOrders, completedOrders, cancelledOrders, totalRevenue, averageOrderValue, averageDeliveryTime
3. THE Order_System SHALL permitir filtrar estadísticas por rango de fechas
4. THE Order_System SHALL actualizar estadísticas cuando se complete o cancele un pedido

### Requirement 12: Cambio Rápido de Sucursal desde Notificación

**User Story:** Como dueño de negocio con múltiples sucursales, quiero poder cambiar rápidamente a otra sucursal cuando llegue un pedido ahí, sin navegar por menús.

#### Acceptance Criteria

1. WHEN llega un pedido a una sucursal diferente a la activa, THE Notification_Service SHALL incluir el branchId y orderId en la notificación
2. WHEN el usuario toca la acción "Cambiar para ver", THE Branch_Context SHALL cambiar la sucursal activa al branchId de la notificación
3. WHEN se cambia de sucursal desde notificación, THE Order_System SHALL navegar automáticamente a la pantalla de detalle del pedido
4. THE Order_System SHALL mostrar confirmación visual del cambio de sucursal
5. THE Subscription_Manager SHALL actualizar las suscripciones de tiempo real para la nueva sucursal activa

