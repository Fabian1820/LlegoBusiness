# Requirements Document

## Introduction

Este documento define los requisitos para mejorar el flujo de gestión de pedidos en la aplicación de negocios (restaurante). Los cambios principales incluyen simplificar la aceptación de pedidos, transformar el "rechazo" en un flujo interactivo de "modificación de pedido", y enviar la nota de modificación al chat del pedido. La interfaz principal será el `OrderDetailScreen` existente.

**Nota de contexto:** Existe una app de cliente separada que recibirá las modificaciones vía chat, pero esta especificación se enfoca únicamente en la app de negocios.

## Glossary

- **Order_Detail_Screen**: Pantalla principal que muestra los detalles de un pedido y permite gestionarlo
- **Order_Modification_UI**: Interfaz interactiva dentro del OrderDetailScreen que permite modificar items del pedido
- **Order_Item**: Elemento individual dentro de un pedido que contiene producto, cantidad e instrucciones especiales
- **Modification_Note**: Nota obligatoria que explica los cambios realizados a un pedido
- **Modified_Order**: Pedido con cambios pendientes de confirmación que será enviado al cliente
- **Chat_Message**: Mensaje enviado al chat del pedido con la nota y resumen de modificaciones
- **Test_Data**: Datos de prueba locales para validar el flujo completo

## Requirements

### Requirement 1: Simplificación de Aceptación de Pedido

**User Story:** Como restaurante, quiero aceptar pedidos con un solo clic, para que el proceso sea más rápido y eficiente.

#### Acceptance Criteria

1. WHEN el restaurante presiona el botón "Aceptar" THEN THE Order_Detail_Screen SHALL cambiar el estado del pedido a "PREPARING" inmediatamente
2. WHEN el restaurante acepta un pedido THEN THE Order_Detail_Screen SHALL NOT mostrar ningún diálogo de confirmación ni solicitar notas adicionales
3. THE Order_Detail_Screen SHALL mantener el botón "Aceptar" visible solo para pedidos en estado "PENDING"

### Requirement 2: Transformación de Rechazo a Modificación de Pedido

**User Story:** Como restaurante, quiero poder modificar un pedido en lugar de rechazarlo, para que pueda ofrecer alternativas al cliente cuando no tengo disponibilidad de ciertos productos.

#### Acceptance Criteria

1. THE Order_Detail_Screen SHALL mostrar el botón como "Modificar Pedido" en lugar de "Rechazar"
2. WHEN el restaurante presiona "Modificar Pedido" THEN THE Order_Modification_UI SHALL habilitar el modo de edición interactiva de los items del pedido
3. THE Order_Modification_UI SHALL permitir cambiar la cantidad de cualquier item existente
4. THE Order_Modification_UI SHALL permitir eliminar items del pedido
5. THE Order_Modification_UI SHALL permitir agregar nuevos items al pedido desde el menú disponible
6. THE Order_Modification_UI SHALL permitir editar las instrucciones especiales de cada item

### Requirement 3: Control de Botones Durante Modificación

**User Story:** Como restaurante, quiero que los botones se habiliten/deshabiliten según mis acciones, para que el flujo sea claro y evite errores.

#### Acceptance Criteria

1. WHILE no se han realizado modificaciones al pedido THEN THE Order_Modification_UI SHALL mantener el botón "Confirmar Modificación" deshabilitado
2. WHEN se realiza cualquier cambio a un item del pedido THEN THE Order_Modification_UI SHALL habilitar el botón "Confirmar Modificación"
3. WHEN se realiza cualquier cambio a un item del pedido THEN THE Order_Modification_UI SHALL deshabilitar el botón "Aceptar"
4. THE Order_Modification_UI SHALL mostrar visualmente qué items han sido modificados respecto al pedido original

### Requirement 4: Nota Obligatoria de Modificación

**User Story:** Como restaurante, quiero agregar una nota explicativa obligatoria cuando modifico un pedido, para que el cliente entienda los cambios realizados.

#### Acceptance Criteria

1. WHEN el restaurante presiona el botón "Confirmar Modificación" habilitado THEN THE Order_Modification_UI SHALL mostrar un diálogo solicitando una nota explicativa
2. THE Order_Modification_UI SHALL requerir que la nota tenga al menos 10 caracteres
3. WHILE la nota no cumpla con el mínimo de caracteres THEN THE Order_Modification_UI SHALL mantener deshabilitado el botón de confirmación final
4. THE Modification_Note SHALL servir como resumen de los cambios realizados para el cliente

### Requirement 5: Envío de Modificación al Chat del Pedido

**User Story:** Como restaurante, quiero que la nota de modificación se envíe automáticamente al chat del pedido, para que el cliente pueda revisar los cambios propuestos y comunicarse conmigo.

#### Acceptance Criteria

1. WHEN el restaurante confirma la modificación con nota válida THEN THE Order_Detail_Screen SHALL enviar la nota como mensaje al chat del pedido
2. THE Chat_Message SHALL incluir la nota explicativa como mensaje del restaurante
3. THE Chat_Message SHALL incluir un resumen de los items modificados (agregados, eliminados, cambiados)
4. WHEN la modificación se confirma THEN THE Order_Detail_Screen SHALL navegar automáticamente al chat del pedido
5. THE Order_Detail_Screen SHALL mostrar un indicador visual de que el pedido está pendiente de respuesta del cliente

### Requirement 6: Recálculo de Totales

**User Story:** Como restaurante, quiero que los totales se recalculen automáticamente cuando modifico items, para que el precio final sea correcto.

#### Acceptance Criteria

1. WHEN se modifica la cantidad de un item THEN THE Order_Modification_UI SHALL recalcular el subtotal de ese item
2. WHEN se agrega o elimina un item THEN THE Order_Modification_UI SHALL recalcular el total del pedido
3. THE Order_Modification_UI SHALL mostrar el total original y el nuevo total para comparación
4. IF el nuevo total es diferente al original THEN THE Order_Modification_UI SHALL resaltar la diferencia de precio

### Requirement 7: Cancelar Modo Edición

**User Story:** Como restaurante, quiero poder cancelar la edición y volver al estado original del pedido, para que pueda deshacer cambios no deseados.

#### Acceptance Criteria

1. WHILE el modo de edición está activo THEN THE Order_Modification_UI SHALL mostrar un botón "Cancelar Edición"
2. WHEN el restaurante presiona "Cancelar Edición" THEN THE Order_Modification_UI SHALL restaurar todos los items a su estado original
3. WHEN se cancela la edición THEN THE Order_Modification_UI SHALL rehabilitar el botón "Aceptar" y deshabilitar "Confirmar Modificación"

### Requirement 8: Datos de Prueba Locales

**User Story:** Como desarrollador, quiero tener datos de prueba locales completos, para poder validar todo el flujo de modificación de pedidos.

#### Acceptance Criteria

1. THE Test_Data SHALL incluir pedidos en estado PENDING con múltiples items para probar modificaciones
2. THE Test_Data SHALL incluir items de menú disponibles para agregar a pedidos
3. THE Test_Data SHALL incluir datos de chat asociados a cada pedido para probar el envío de mensajes
4. THE Test_Data SHALL incluir clientes con información completa (nombre, teléfono, dirección)
5. THE Test_Data SHALL permitir simular el flujo completo: ver pedido → modificar items → agregar nota → enviar al chat
