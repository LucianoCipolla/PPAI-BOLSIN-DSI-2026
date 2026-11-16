# Cambios para que el código y los diagramas UML coincidan

Rama: `coincidencia-diagramas-codigo`.

Objetivo: que cada método, atributo y mensaje de los diagramas de `docs/diagramas` corresponda exactamente a lo que hace
el código del CU 36, y al revés.

## Punto de partida

Antes de estos cambios ya coincidían exactamente:

- **Patrón Adapter:** `InterfazGPSTracker`, `IAdaptadorUbicacionBolsin`, los 3 adaptadores y los 3 servicios.
- **Entidades:** `Sesion`, `Usuario`, `Empleado`, `Rol`, `CM`, `Bolsin`, `CambioEstadoBolsin` y `Estado`.
- **Firmas de `GestorConsultarBolsines`:** nombres, visibilidad, parámetros y tipos de retorno.

Las diferencias estaban en el comportamiento interno del gestor, en cómo se representaba la pantalla y en elementos del
código que los diagramas no mostraban.

## Decisiones tomadas

| # | Tema | Decisión |
|---|---|---|
| 1 | Quién le pide el nombre a la CM del usuario (paso 2) | El **gestor**, como dice la secuencia. La pantalla recibe un `String` y deja de depender de `CM`. |
| 2 | Mensajes del gestor a la pantalla | Se dibujan como **llamadas de la pantalla a sí misma**, porque el gestor le devuelve los datos. |
| 3 | Repositorios | **No se modelan.** Se aclara con una nota visible en el diagrama de clases. |

---

## Cambios en el código

### C1. `GestorConsultarBolsines.buscarDatosLocBolsines()`: un solo `getNumeroBolsin()` por bolsín

`src/main/java/com/g5/bolsines/control/GestorConsultarBolsines.java`

**Antes:** se llamaba dos veces a `getNumeroBolsin()`, una para consultar el GPS y otra para guardar el resultado. La
secuencia muestra una sola llamada.

**Después:** cada dato se guarda en una variable local con el mismo nombre que el argumento del mensaje
`buscarUbicacionBolsin(nroBolsin, codCMOrigen, codCMDestino, modeloGPS)`. El orden de las llamadas es el de la secuencia:

```java
int nroBolsin = bolsin.getNumeroBolsin();
String codCMOrigen = bolsin.getCMOrigen().getCodigo();
String codCMDestino = bolsin.getCMDestino().getCodigo();
String modeloGPS = bolsin.getModeloGPS();
Object[] datos = interfazGPSTracker.buscarUbicacionBolsin(nroBolsin, codCMOrigen, codCMDestino, modeloGPS);
```

### C2. `GestorConsultarBolsines.consultarUbBolsines()`: un solo `getNumeroBolsin()` en el loop del paso 6

Mismo problema que C1. Ahora el loop empieza con `int nroBolsin = bolsin.getNumeroBolsin();` y usa esa variable en
`datosLocBolsines.get(...)` y en `ubicacion.put("nroBolsin", ...)`.

### C3. `GestorConsultarBolsines.llamarCUNotificarUbicacionDeBolsines()`: un solo `getNumeroBolsin()`

Llamaba dos veces a `bolsinSeleccionado.getNumeroBolsin()`. Ahora lo guarda en `nroBolsin` y lo usa en el `get` y en
`notificarUbicacion(...)`.

### C4. El nombre de la CM lo obtiene el gestor (decisión 1)

**Antes:**

- En la secuencia, el gestor le mandaba `getNombre()` a la CM.
- En el código, la pantalla recibía la `CM` y le pedía el nombre en `mostrarCM`. Eso además hacía que una boundary
  accediera directo a una entidad.

**Después:**

| Archivo | Cambio |
|---|---|
| `control/GestorConsultarBolsines.java` | Nuevo atributo `private String nombreCMUsuarioLogueado`. |
| | `buscarCMUsuarioLogueado(sesion)` lo carga con `cmUsuarioLogueado.getNombre()`. |
| | `consultarUbBolsines` devuelve `Object[]{nombreCMUsuarioLogueado, ubicaciones}` en lugar de `{cmUsuarioLogueado, ubicaciones}`. |
| | `finCU()` lo pone en `null`. |
| `boundary/PantallaConsultarBolsines.java` | `mostrarCM(CM cm, Model model)` pasa a `mostrarCM(String nombreCM, Model model)`. |
| | Se borra `import com.g5.bolsines.entity.CM`: la pantalla ya no conoce ninguna entidad. |

### C5. JS de la pantalla: `seleccionarBolsin` pasa a llamarse `tomarSelecBolsinMapa`

`src/main/resources/static/js/pantallaConsultarBolsines.js`

En la secuencia, el EB le manda `tomarSelecBolsinMapa(nroBolsin)` a la pantalla. El punto de entrada real del lado del
navegador, que llaman el clic en el marcador y el clic en la lista, se llamaba `seleccionarBolsin`. Se renombró en sus 3
usos: la definición, el callback que recibe `InterfazGoogleMaps` y el listener de la lista.

### C6. Comentario corregido en `Empleado.getNombreCompleto()`

Decía `// CU36 paso 1`, pero solo lo usa `PantallaInicio`, que está fuera del CU 36. Ahora dice
`// CU36 sin paso: lo usa PantallaInicio`.

### Verificación

`mvn test`: 30 tests, 0 fallos.

| Clase de test | Tests |
|---|---|
| `AdaptadorUbicacionGeoPulseMTR900Test` | 4 |
| `AdaptadorUbicacionNavTrackQX7ATest` | 4 |
| `AdaptadorUbicacionXTR4500LTest` | 3 |
| `InterfazGPSTrackerTest` | 6 |
| `PantallaConsultarBolsinesTest` | 13 |

`PantallaConsultarBolsinesTest` recorre de punta a punta el flujo básico, A1 y A3, y verifica `lblCMUsuario` en el modelo.

---

## Cambios en los diagramas

### `diagrama-secuencia-cu36.puml`

| # | Cambio | Por qué |
|---|---|---|
| D1 | `mostrarCM`, `mostrarMapaConUbicaciones`, `solicitarSelecBolsin`, `solicitarEnvioCorreo` e `informarEjecucionExitosa` pasan de `gestor -> pantalla` a `pantalla -> pantalla`, después de que vuelve la llamada al gestor. | Decisión 2. El gestor no tiene referencia a la pantalla: le devuelve los datos. |
| D1 | Los mensajes de la pantalla llevan los mismos parámetros que el diagrama de clases: `registrarOpcConsultasUbBolsines(httpSession, model)`, `habilitarVentana(model)`, `mostrarCM(nombreCM, model)`, `mostrarMapaConUbicaciones(ubicaciones, model)`, `solicitarSelecBolsin(model)`, `tomarConfirmEnvioCorreo(redirect)`, `informarEjecucionExitosa(redirect)`. | Antes la secuencia y el diagrama de clases tenían firmas distintas. |
| D1 | `finCU()` queda antes de `informarEjecucionExitosa(redirect)`. | Es el orden real: el gestor termina el CU y después la pantalla informa. |
| D1 | `pantalla -> mapa : mostrarMapaConUbicaciones(ubicaciones)` va después de `solicitarSelecBolsin(model)`. | El JS dibuja el mapa cuando la página ya está armada. |
| D1 | Se borra el comentario "ver CLAUDE.md, regla 4" y se explica la convención en la cabecera. | `CLAUDE.md` no forma parte del repositorio. |
| D2 | Dentro de `esTuCMOrigen(cmUsuarioLogueado)`: `bolsin -> origen:CM : getCodigo()` y `bolsin -> log:CM : getCodigo()`. | `Bolsin.esTuCMOrigen` compara códigos. No se puede comparar con `==`, porque la CM de la sesión es otro objeto Java distinto del que carga `findAll()`. |
| D2 | Dentro de `esTuCM(cmDestino)`: `empleado -> :CM : getCodigo()` y `empleado -> destino:CM : getCodigo()`. | Mismo motivo, en `Empleado.esTuCM`. |
| D7 | Lifelines de CM: `log:CM` (la del usuario), `origen:CM` (la de origen de cada bolsín), `destino:CM` y `:CM` (la asignada a cada empleado). En los pasos 4 y 5, `getCodigo()` va a `origen:CM`. | Antes el código de origen se le pedía a la CM del usuario. Ahora coincide con `diagrama-secuencia-xtr4500l`. |
| C5 | Paso 7: se agrega `pantalla -> pantalla : marcarSeleccionEnLista(nroBolsin)` antes de `marcarSeleccion`. | Lo hace el JS de la pantalla al seleccionar. |
| D7 | Se agrega `opt el EB cancela (Obs. 1)`: `cancelar()` y `pantalla -> gestor : finCU()`. | `cancelar()` estaba en el diagrama de clases, pero no en ninguna secuencia. |
| D7 | Nota en `getMapa()`: es la carga de la API de mapas (`cargarGoogleMaps`, o Leaflet sin clave). | No existe un método con ese nombre: es el pedido al servidor externo. |

### `diagrama-clases-diseno.puml`

| # | Clase | Cambio |
|---|---|---|
| C4 | `PantallaConsultarBolsines` | `-mostrarCM(nombreCM: String, model: Model): void`. |
| C4 | Relaciones | Se borra la dependencia `PantallaConsultarBolsines ..> CM`. |
| C4 | `GestorConsultarBolsines` | Nuevo atributo `-nombreCMUsuarioLogueado: String`. |
| C5 | `PantallaConsultarBolsines` | Nuevo método `-marcarSeleccionEnLista(nroBolsin: int): void`. |
| D6 | `PantallaConsultarBolsines` | Nuevos atributos `-lblCantidadBolsines: Label` y `-btnNoEnviar: Button`, que existen en el HTML. `claveGoogleMaps` pasa a `{readOnly}`. |
| D6 | `PantallaConsultarBolsines` | Nota: se implementa con `PantallaConsultarBolsines.java`, `consultarBolsines.html` (los atributos son elementos de la vista con el mismo `id`) y `pantallaConsultarBolsines.js`. |
| D3 | `GestorConsultarBolsines` | Nota: la persistencia no se modela. El gestor recorre bolsines y empleados con `RepositorioBolsin.findAll()` y `RepositorioEmpleado.findAll()`. |
| D4 | Nueva clase | `ServicioGPSException <<exception>>`, con dependencias desde los 3 adaptadores (la capturan) y los 3 servicios (la lanzan). |
| D5 | `InterfazGPSTracker` | Nuevos atributos `-{static} XTR`, `-{static} NAVTRACK`, `-{static} GEOPULSE: String {readOnly}`. `clavesAPI` y `urlsServicios` pasan a `{readOnly}`. |
| D5 | Adaptadores y servicios | `claveAPI` y `restClient` pasan a `{readOnly}` (son `final`). |

### `diagrama-clases-adapter.puml`

Recibe los mismos cambios de D5 que el diagrama de diseño (`{static}` y `{readOnly}`), y además las dos excepciones con
sus dependencias:

- **`UbicacionNoDisponibleException`:** desde `InterfazGPSTracker` y desde los 3 adaptadores.
- **`ServicioGPSException`:** desde los 3 adaptadores y los 3 servicios.

Así los dos diagramas de clases quedan coherentes.

### `diagrama-secuencia-xtr4500l.puml`

Sin cambios: ya coincidía con el código. Con C1, el código hace exactamente esos mensajes en ese orden.

---

## Qué sigue sin modelarse, a propósito

Ninguno de estos elementos aparece en un mensaje del CU:

- **Constructores** de `PantallaConsultarBolsines`, `GestorConsultarBolsines`, `InterfazGPSTracker` y
  `GestorNotificarUbicacionBolsin`. Los crea Spring y no aparecen en la secuencia. Los de los adaptadores y los servicios
  sí se muestran.
- **Atributos `id`** de las entidades (técnicos de JPA) y el logger `log` de `GestorNotificarUbicacionBolsin`.
- **Métodos de las entidades usados fuera del CU 36:** `Bolsin.agregarCambioEstado` (carga de datos),
  `Usuario.getUsuario` y `Empleado.getNombreCompleto` (pantalla de inicio).
- **Funciones auxiliares de presentación del JS:** `cargarCMsDestino`, `formatearHaceCuanto` y `actualizarHaceCuanto` de
  la pantalla, y `cargarGoogleMaps`, `crearMapaGoogle` y `crearMapaLeaflet` de `InterfazGoogleMaps`, junto con sus
  atributos internos.
- **Flujos alternativos de control** sin mensaje propio en la secuencia del flujo básico:
  - el `finCU()` de A1 cuando no hay bolsines enviados;
  - el `try/catch` de A3 en `InterfazGPSTracker`;
  - el mensaje de error de `tomarConfirmEnvioCorreo` si no hay bolsín seleccionado.

## Pendiente antes de entregar

1. **Regenerar los `.svg` y `.png`** de los tres diagramas modificados. Los `.puml` ya están actualizados, pero las
   imágenes del repositorio siguen siendo las anteriores. Desde `docs/diagramas`, con PlantUML y Graphviz instalados:
   ```bash
   plantuml -charset UTF-8 -tsvg -tpng diagrama-clases-diseno.puml diagrama-clases-adapter.puml diagrama-secuencia-cu36.puml
   ```
   O con el `.jar`: `java -jar plantuml.jar -charset UTF-8 -tsvg -tpng <archivos>`. Conviene revisar el resultado: el
   diagrama de diseño tiene dos notas y una clase nueva, y el layout puede moverse.
2. **Reemplazar las imágenes en `docs/entrega/PPAI2026_3K1_G5_E2_Diseno.docx`** y volver a exportar el `.pdf`.
3. **Probar la aplicación a mano** con `eb.cba-01`: que se vea el nombre de la CM en el encabezado y que seleccionar
   desde el marcador y desde la lista siga abriendo el modal. Ese caso cubre C4 y C5.
