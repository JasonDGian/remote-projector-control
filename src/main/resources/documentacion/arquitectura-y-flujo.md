<div align="center">
  <table border="1" cellpadding="10" cellspacing="0">
    <tr>
      <td colspan="9" align="center"><strong>📚 Navegación del Proyecto</strong></td>
    </tr>
    <tr>
      <td align="center"><a href="../../../../README.md">🏠<br><strong>Inicio</strong></a></td>
      <td align="center"><a href="./acerca-del-proyecto.md">ℹ️<br><strong>Acerca de</strong></a></td>
      <!-- <td align="center"><a href="./arquitectura-y-flujo.md">🛠️<br><strong>Arquitectura</strong></a></td> -->
      <td align="center"><a href="./esquema-y-tablas.md">🛢️<br><strong>Esquema BBDD</strong></a></td>
      <td align="center"><a href="./interfaz-grafica-y-roles.md">🎨<br><strong>Interfaz</strong></a></td>
      <td align="center"><a href="./api-rest.md">📡<br><strong>API REST</strong></a></td>
      <td align="center"><a href="./hardware-especial.md">🧰<br><strong>Hardware</strong></a></td>
      <td align="center"><a href="./codificacion.md">📟<br><strong>Codificación</strong></a></td>
      <td align="center"><a href="./instrucciones-de-uso.md">📄<br><strong>Instrucciones</strong></a></td>
    </tr>
  </table>

</div>


   
<a name="detalles-tecnicos-y-funcionales"></a>   
# 🛠️ Arquitectura y flujo de operaciones logicas.
## Funcionamiento del sistema.
La arquitectura utilizada en el desarrollo del proyecto sigue un modelo cliente-servidor, que para fines ilustrativos se puede dividir en dos partes o `lados´:

1. **Lado usuario:** Aquí se presenta la interfaz gráfica web, donde los usuarios pueden seleccionar la unidad física a la que desean enviar una tarea (un proyector específico ubicado en un aula) y definir la tarea a ejecutar. Una vez registrada, la tarea se almacena en la base de datos del servidor junto con información relevante, como la fecha de solicitud, el usuario que la registró, la instrucción deseada y el modelo del proyector en cuestión.

2. **Lado agente remoto (microcontrolador):** Periódicamente, el agente remoto asociado a cada proyector consulta la API del microservicio para verificar si hay tareas pendientes para él en la cola de ejecución. Si existen tareas asignadas, el servidor responde enviando la instrucción correspondiente según el modelo del proyector. El microcontrolador recibe la instrucción, la transmite al proyector a través de su interfaz de comunicación, y éste la ejecuta.

Además, el sistema incluye un mecanismo de actualización del estado de las tareas, que no se detalla en esta explicación y será abordado más adelante.
   
![427159308-c8482c0b-a148-4e50-bc02-ba7a3e1c8846](https://github.com/user-attachments/assets/03fdd701-d07f-4abc-bb46-d69f2002cc04)
   
---

## Orden de operaciones.
En el siguiente diagrama se representa el orden de operaciones en el flujo de trabajo de los clientes, servidor y agente remoto.
    
1. Un usuario autenticado en el sistema envía una solicitud para ejecutar una acción en un proyector específico.
2. El servidor almacena la solicitud en la base de datos, dentro de la tabla "Servidor Evento".
3. Periódicamente, un agente remoto consulta al servidor en busca de nuevas tareas.
4. El servidor verifica la base de datos y, si existen eventos pendientes para el proyector en cuestión, recupera los datos de la instrucción.
5. Luego, el servidor formatea la instrucción y la envía al agente remoto.
6. El agente remoto recibe la instrucción y la convierte a un formato compatible con el proyector, enviándosela.
7. Según la respuesta del proyector, el agente remoto envía una actualización de estado al servidor.
8. Finalmente, el servidor actualiza el estado del evento en la tabla "Servidor Evento".    
   

![449336603-b9ad96ac-6823-4789-9111-cc401183c552](https://github.com/user-attachments/assets/ae568f38-e5e1-4ec1-8d97-7da6bb63b599)

    
    
--- 
   
<div align="center">
  <table border="1" cellpadding="10" cellspacing="0">
    <tr>
      <td colspan="9" align="center"><strong>📚 Navegación del Proyecto</strong></td>
    </tr>
    <tr>
      <td align="center"><a href="../../../../README.md">🏠<br><strong>Inicio</strong></a></td>
      <td align="center"><a href="./acerca-del-proyecto.md">ℹ️<br><strong>Acerca de</strong></a></td>
      <!-- <td align="center"><a href="./arquitectura-y-flujo.md">🛠️<br><strong>Arquitectura</strong></a></td> -->
      <td align="center"><a href="./esquema-y-tablas.md">🛢️<br><strong>Esquema BBDD</strong></a></td>
      <td align="center"><a href="./interfaz-grafica-y-roles.md">🎨<br><strong>Interfaz</strong></a></td>
      <td align="center"><a href="./api-rest.md">📡<br><strong>API REST</strong></a></td>
      <td align="center"><a href="./hardware-especial.md">🧰<br><strong>Hardware</strong></a></td>
      <td align="center"><a href="./codificacion.md">📟<br><strong>Codificación</strong></a></td>
      <td align="center"><a href="./instrucciones-de-uso.md">📄<br><strong>Instrucciones</strong></a></td>
    </tr>
  </table>

</div>


