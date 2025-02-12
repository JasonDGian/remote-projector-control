# Dia 1 - 08/02/25
- Creado repositorio.
- Recogidos datos sobre la placa ESP32 y el interfaz RS232
- Configurado el entorno de desarrollo -> Depuirados problemas.
- Primeras pruebas de conexión a wifi.    
**TIEMPO DEDICADO: 4 horas.**

# Dia 2 - 09/02/25
- Averiguado pinout adaptador SR232
- Creada aplicacion base con dependencias de proyecto necesarias.
- Revisión y normalización de modelo relacional.
- Creado esquema de base de datos en aplicacion base.
- Documentadas entidades Java con comentarios Javadoc.

![imagen](https://github.com/user-attachments/assets/58db3fd5-9e9a-4899-8846-53eda922b04a)

**TIEMPO DEDICADO: 5 horas**

# Dia 3 - 10/02/25
- Finalizado el mecanismo de parseo de comandos.
- Creados los logs del mecanismo de parseo de comandos.
- Redactadas las primeras tareas o "roadmap".
- Documentación de clases de parseo.
- Finalizado el mecanismo de parseo de unidades fisicas proyectores.
- Añadido mecanismo de parseo de modelos de proyector.
- Añadidos endpoints de testeo para los mecanismos de parseo.

**TIEMPO DEDICADO: 5 horas**

# Dia 4 - 11/02/25
- Mejora de mecanismo de parseo implementando manejo de errores.
- Mejora de puntos de ataque implementando manejo de errores.
- Modulariza la validacion de los ficheros CSV de los puntos de ataque.
- Arregla configuración de registro de eventos (log).
- Añade documento de referencia para excepciones personalizadas (exceptions-document.txt).
- Añadidos comentarios y javadoc en controlador REST y actualizados comentarios de implementaciones parseadores con nuevas caracteristicas.
- Creado primer draft del mecanismo de creación de eventos en BBDD.

**TIEMPO DEDICADO: 4 horas**

# Dia 5 - 12/02/25
- Se añaden métodos toString() personalizados en todas las entidades para evitar llamadas recurrentes y excepciones de puntero nulo.
- Se corrige el ID en la entidad ServerEvent para usar el tipo envolvente Long con valor autogenerado.
- Se corrigen los comentarios Javadoc en la clase CommandId para mayor consistencia y claridad.
- Se actualiza el archivo exceptions-document.txt con las nuevas excepciones definidas.
- Se reactiva el repositorio ServerEventRepository, que estaba previamente desactivado debido a todas las líneas comentadas.
- Se reorganizan los métodos de los endpoints en el controlador de Projector para mejorar la claridad y estructura.
- Se eliminan comentarios en español innecesarios y obsoletos para mejorar la legibilidad.
- Se introduce el primer borrador del método createServerEvent en el endpoint /server-events.
- Se redactan dos endpoints adicionales para las acciones del MicroController con comentarios bosquejo.
- Se introducen nuevos constantes como ejemplo temporal para los estados de los eventos. (por definir de manera final).

**TIEMPO DEDICADO: 3 horas**


# Dia 6 - 13/02/25
- Redacción de la introducción para el anteproyecto.
- Elaboración de esquemas y material explicativo complementario para el documento del proyecto.
- Redacción preliminar del análisis del proyecto.
- Definición inicial de los objetivos del proyecto.
- Recopilación de datos sobre las conexiones del dispositivo.
- Investigación del mecanismo de comunicación entre UART y RS232.
- Análisis del módulo transceptor y del chip MAX232.
- Estudio de las funcionalidades y el pinout de la placa ESP32.
- Inicio de configuración para la comunicación en el sketch arduino ESP32.

**TIEMPO DEDICADO: 6 horas**
