# PPAI 2026 – DSI 3K1 – Grupo 5

Implementación del **CU 36 "Consultar seguimiento de bolsines"**, rediseñado con el patrón **Adapter (GoF)**.
Corre el flujo básico y los flujos alternativos **A1** y **A3**, con persistencia en base de datos mediante Hibernate.

## Cómo correrlo

Requiere **JDK 21 o superior** y **Maven**. Son dos aplicaciones que corren al mismo tiempo.

### 1. API de GPS de la cátedra (puerto 8080)

Los tres modelos de GPS Tracker los provee la API de la cátedra,
[dsi-api-bolsines](https://github.com/LiberaeMentis/dsi-api-bolsines). Clonala **fuera** de este proyecto y levantala:

```bash
git clone https://github.com/LiberaeMentis/dsi-api-bolsines
cd dsi-api-bolsines
mvn spring-boot:run
```

Para comprobar que está andando, <http://localhost:8080/api/mock/bolsines> tiene que listar los bolsines 101 a 110.

### 2. Este sistema (puerto 8081)

```bash
mvn spring-boot:run
```

La aplicación queda en <http://localhost:8081>. La primera vez crea la base en `./data/bolsines` y carga los datos de prueba.

Si la API de la cátedra no está levantada, la aplicación arranca igual, pero todos los bolsines aparecen como
"Ubicación no disponible" (A3).

La URL de la API y las API keys de cada modelo están en `src/main/resources/application.properties` (`gps.*`).

### Tests

```bash
mvn test
```

Los tests no usan la base de `./data` ni necesitan la API de la cátedra levantada: usan una base en memoria y un doble
de prueba de la API (`ApiCatedraFalsa`, en `src/test`) con sus mismas rutas, keys, validaciones, formatos y bolsines.

**Mapa.** Sin clave en `google.maps.api-key` se usa Leaflet + OpenStreetMap, que necesitan conexión a Internet. Sin
Internet no se ve el mapa, pero la lista de bolsines sigue funcionando.

**Si la pantalla se ve sin estilos o sin mapa** después de actualizar el proyecto, refrescá una vez con Ctrl+F5: el
navegador puede tener guardada una versión anterior del JS y el CSS.

### Consola H2

Con la aplicación corriendo, entrá a <http://localhost:8081/h2-console> con estos datos:

| Campo | Valor |
|---|---|
| JDBC URL | `jdbc:h2:file:./data/bolsines` |
| User Name | `sa` |
| Password | (vacío) |

Para volver a los datos iniciales, detené la aplicación y borrá la carpeta `data/`.

## Usuarios de prueba

Iniciar sesión es el CU 5, fuera del alcance del grupo, así que la sesión se simula: en <http://localhost:8081/inicio>
elegí un usuario en el desplegable **Ingresar como**. No se pide contraseña. Al elegirlo se crea y persiste una `Sesion`
para ese `Usuario` y aparece la opción **Consultar seguimiento de bolsines**. Para probar con otro usuario, elegilo en el
mismo desplegable: empieza una sesión nueva, sin datos de la anterior.

Los bolsines y los códigos de CM son una copia fiel de los de la API de la cátedra: XTR-4500L valida la CM de origen y
NavTrack QX-7A la de destino, así que ambos sistemas tienen que coincidir. El modelo de GPS de cada bolsín es un dato
propio de este sistema.

| Usuario | CM | Ve | Para probar |
|---|---|---|---|
| `eb.cba-01` | Córdoba | 101 (XTR-4500L), 102 (NavTrack QX-7A), 103 (GeoPulse MTR-900) y 111 | Flujo básico con los tres modelos, filtros y A3 |
| `eb.ba-01` | Buenos Aires | 104 (XTR-4500L), 105 (NavTrack QX-7A) y 106 (GeoPulse MTR-900) | Flujo básico con los tres modelos y filtros |
| `eb.nqn-01`, `eb.mdq-01`, `eb.lpl-01`, `eb.cba-02` | Neuquén, Mar del Plata, La Plata, Córdoba Norte | Un bolsín cada uno (107 a 110) | Flujo básico |
| `eb.juj-01`, `eb.ros-02`, `eb.mdz-03`, `eb.tuc-01`, `eb.sla-01` | Resto de las CM | — | A1 (sin bolsines enviados) |

## Cómo probar cada flujo

### Flujo básico

1. En **Ingresar como** elegí `eb.cba-01` y tocá **Consultar seguimiento de bolsines**.
2. Se ven los bolsines 101, 102, 103 y 111. **No** aparece el 112, que sigue Cerrado.
3. Seleccioná el 101 desde el marcador o desde la lista. Ambos se resaltan y se abre el modal con el resumen.
4. Tocá **Enviar correo**. El CU termina en la pantalla de inicio con el mensaje de éxito.
5. El correo está simulado: en la consola de la aplicación aparece
   `[CU 30 simulado] Mail a gcm.ros-02@bolsines.test...`, con el GCM de la CM destino.

Para ver los otros dos modelos de GPS, seleccioná el 102 (NavTrack QX-7A) o el 103 (GeoPulse MTR-900), o repetí con `eb.ba-01`.

También se puede probar el filtro por precinto y CM destino ("Mostrando X de Y"), **No enviar** (cierra el modal y vuelve
al mapa) y **Cancelar** (termina el CU y vuelve a inicio).

### A1: no hay bolsines enviados

En **Ingresar como** elegí `eb.juj-01` y tocá **Consultar seguimiento de bolsines**. Se ve el mapa sin marcadores, el
mensaje "No hay bolsines enviados desde CM Jujuy" y solo el botón **Volver**.

### A3: el GPS Tracker no informa la ubicación

En **Ingresar como** elegí `eb.cba-01` y tocá **Consultar seguimiento de bolsines**. El bolsín **111** figura como
Enviado en este sistema, pero la API de la cátedra no lo conoce y responde 404. Aparece en la lista con el aviso
"Ubicación no disponible", sin marcador y sin poder seleccionarse. El 101 sigue normal.

## Clase del diagrama → archivo Java

Rutas relativas a `src/main/java/com/g5/bolsines/`, salvo las que empiezan con `static/` o `templates/`, que están en
`src/main/resources/`.

| Estereotipo / rol | Clase del diagrama | Archivo |
|---|---|---|
| Boundary | `PantallaConsultarBolsines` | `boundary/PantallaConsultarBolsines.java` y `templates/consultarBolsines.html` |
| Boundary | `InterfazGoogleMaps` | `static/js/interfazGoogleMaps.js` |
| Boundary, Client del Adapter | `InterfazGPSTracker` | `boundary/InterfazGPSTracker.java` |
| Control | `GestorConsultarBolsines` | `control/GestorConsultarBolsines.java` |
| Control (CU 30 simulado) | `GestorNotificarUbicacionBolsin` | `control/GestorNotificarUbicacionBolsin.java` |
| Target | `IAdaptadorUbicacionBolsin` | `adapter/IAdaptadorUbicacionBolsin.java` |
| Adapter | `AdaptadorUbicacionXTR4500L` | `adapter/AdaptadorUbicacionXTR4500L.java` |
| Adapter | `AdaptadorUbicacionNavTrackQX7A` | `adapter/AdaptadorUbicacionNavTrackQX7A.java` |
| Adapter | `AdaptadorUbicacionGeoPulseMTR900` | `adapter/AdaptadorUbicacionGeoPulseMTR900.java` |
| Adaptee | `ServicioXTR4500L` | `servicio/ServicioXTR4500L.java` |
| Adaptee | `ServicioNavTrackQX7A` | `servicio/ServicioNavTrackQX7A.java` |
| Adaptee | `ServicioGeoPulseMTR900` | `servicio/ServicioGeoPulseMTR900.java` |
| Excepción del Adapter | `UbicacionNoDisponibleException` | `adapter/UbicacionNoDisponibleException.java` |
| Excepción de los Adaptee | `ServicioGPSException` | `servicio/ServicioGPSException.java` |
| Entidad | `Sesion` | `entity/Sesion.java` |
| Entidad | `Usuario` | `entity/Usuario.java` |
| Entidad | `Empleado` | `entity/Empleado.java` |
| Entidad | `Rol` | `entity/Rol.java` |
| Entidad | `CM` | `entity/CM.java` |
| Entidad | `Bolsin` | `entity/Bolsin.java` |
| Entidad | `CambioEstadoBolsin` | `entity/CambioEstadoBolsin.java` |
| Entidad | `Estado` | `entity/Estado.java` |

Fuera del diagrama del CU 36:

| Rol | Clase | Archivo |
|---|---|---|
| Persistencia | `RepositorioBolsin`, `RepositorioCM`, `RepositorioEmpleado`, `RepositorioEstado`, `RepositorioRol`, `RepositorioSesion`, `RepositorioUsuario` | `persistencia/Repositorio*.java` |
| GPS Tracker (sistema externo) | API de la cátedra | Repositorio [dsi-api-bolsines](https://github.com/LiberaeMentis/dsi-api-bolsines), corre aparte |
| Inicio con sesión simulada (CU 5, fuera de alcance) | `PantallaInicio` | `config/PantallaInicio.java` y `templates/inicio.html` |
| Datos de prueba | `CargaDatosIniciales` | `config/CargaDatosIniciales.java` |

## Especificación para el diagrama de clases de diseño

Refleja el código tal como quedó. Visibilidad: `+` público, `-` privado. Los atributos marcados `{readOnly}` son `final`
en Java y los `{static}` son de clase. Los constructores figuran con el estereotipo `<<create>>`.

### InterfazGPSTracker

`<<boundary>>`, paquete `boundary`, `@Component`. Client del Adapter.

| Atributo | Tipo | Modificadores |
|---|---|---|
| `-XTR` | `String` | `{static, readOnly}` = `"XTR-4500L"` |
| `-NAVTRACK` | `String` | `{static, readOnly}` = `"NavTrack QX-7A"` |
| `-GEOPULSE` | `String` | `{static, readOnly}` = `"GeoPulse MTR-900"` |
| `-clavesAPI` | `Map<String, String>` | `{readOnly}` |
| `-urlsServicios` | `Map<String, String>` | `{readOnly}` |

| Método |
|---|
| `<<create>> +InterfazGPSTracker(claveXTR: String, claveNavTrack: String, claveGeoPulse: String, urlXTR: String, urlNavTrack: String, urlGeoPulse: String)` |
| `+buscarUbicacionBolsin(nroBolsin: int, codCMOrigen: String, codCMDestino: String, modeloGPS: String): Object[]` |

### IAdaptadorUbicacionBolsin

`<<interface>>`, paquete `adapter`. Target del Adapter. No tiene atributos.

| Método |
|---|
| `+recibirUbicacionBolsin(nroBolsin: int, codCMOrigen: String, codCMDestino: String): Object[]` |
| `+solicitarUbicacionBolsin(nroBolsin: int, codCMOrigen: String, codCMDestino: String): void` |
| `+adaptarDatosUbicacion(): void` |

`recibirUbicacionBolsin` devuelve `Object[]{Integer nroBolsin, Double latitud, Double longitud, LocalDateTime fechaHora}`.

### AdaptadorUbicacionXTR4500L

Paquete `adapter`. Adapter. Realiza `IAdaptadorUbicacionBolsin`.

| Atributo | Tipo | Modificadores |
|---|---|---|
| `-claveAPI` | `String` | `{readOnly}` |
| `-servicio` | `ServicioXTR4500L` | `{readOnly}` |
| `-datosUbicacionBolsin` | `String` | |
| `-datosUbicacionAdaptados` | `Object[]` | |

| Método |
|---|
| `<<create>> +AdaptadorUbicacionXTR4500L(claveAPI: String, urlServicio: String)` |
| `+recibirUbicacionBolsin(nroBolsin: int, codCMOrigen: String, codCMDestino: String): Object[]` |
| `+solicitarUbicacionBolsin(nroBolsin: int, codCMOrigen: String, codCMDestino: String): void` |
| `+adaptarDatosUbicacion(): void` |

Reenvía al servicio `claveAPI`, `nroBolsin` y `codCMOrigen`.

### AdaptadorUbicacionNavTrackQX7A

Paquete `adapter`. Adapter. Realiza `IAdaptadorUbicacionBolsin`.

| Atributo | Tipo | Modificadores |
|---|---|---|
| `-claveAPI` | `String` | `{readOnly}` |
| `-servicio` | `ServicioNavTrackQX7A` | `{readOnly}` |
| `-datosUbicacionBolsin` | `String` | |
| `-datosUbicacionAdaptados` | `Object[]` | |

| Método |
|---|
| `<<create>> +AdaptadorUbicacionNavTrackQX7A(claveAPI: String, urlServicio: String)` |
| `+recibirUbicacionBolsin(nroBolsin: int, codCMOrigen: String, codCMDestino: String): Object[]` |
| `+solicitarUbicacionBolsin(nroBolsin: int, codCMOrigen: String, codCMDestino: String): void` |
| `+adaptarDatosUbicacion(): void` |

Reenvía al servicio `claveAPI`, `nroBolsin` y `codCMDestino`.

### AdaptadorUbicacionGeoPulseMTR900

Paquete `adapter`. Adapter. Realiza `IAdaptadorUbicacionBolsin`.

| Atributo | Tipo | Modificadores |
|---|---|---|
| `-claveAPI` | `String` | `{readOnly}` |
| `-servicio` | `ServicioGeoPulseMTR900` | `{readOnly}` |
| `-datosUbicacionBolsin` | `Object[][]` | |
| `-datosUbicacionAdaptados` | `Object[]` | |

| Método |
|---|
| `<<create>> +AdaptadorUbicacionGeoPulseMTR900(claveAPI: String, urlServicio: String)` |
| `+recibirUbicacionBolsin(nroBolsin: int, codCMOrigen: String, codCMDestino: String): Object[]` |
| `+solicitarUbicacionBolsin(nroBolsin: int, codCMOrigen: String, codCMDestino: String): void` |
| `+adaptarDatosUbicacion(): void` |

Reenvía al servicio `claveAPI` y `nroBolsin`.

### ServicioXTR4500L

Paquete `servicio`. Adaptee. No es un bean de Spring: lo crea su adaptador.

| Atributo | Tipo | Modificadores |
|---|---|---|
| `-restClient` | `RestClient` | `{readOnly}` |

| Método |
|---|
| `<<create>> +ServicioXTR4500L(urlServicio: String)` |
| `+getBolsinLocation(claveAPI: String, nroBolsin: int, codCMOrigen: String): String` |

### ServicioNavTrackQX7A

Paquete `servicio`. Adaptee. No es un bean de Spring: lo crea su adaptador.

| Atributo | Tipo | Modificadores |
|---|---|---|
| `-restClient` | `RestClient` | `{readOnly}` |

| Método |
|---|
| `<<create>> +ServicioNavTrackQX7A(urlServicio: String)` |
| `+retrieveTrackingData(claveAPI: String, nroBolsin: int, codCMDestino: String): String` |

### ServicioGeoPulseMTR900

Paquete `servicio`. Adaptee. No es un bean de Spring: lo crea su adaptador.

| Atributo | Tipo | Modificadores |
|---|---|---|
| `-restClient` | `RestClient` | `{readOnly}` |

| Método |
|---|
| `<<create>> +ServicioGeoPulseMTR900(urlServicio: String)` |
| `+fetchCargoPositions(claveAPI: String, nroBolsin: int): Object[][]` |

### Excepciones

Ambas son unchecked: extienden `RuntimeException`. No tienen atributos propios.

| Clase | Paquete | Quién la lanza | Métodos |
|---|---|---|---|
| `ServicioGPSException` | `servicio` | Los 3 `Servicio*`, si falla el HTTP o la respuesta viene vacía | `<<create>> +ServicioGPSException(mensaje: String)`, `<<create>> +ServicioGPSException(mensaje: String, causa: Throwable)` |
| `UbicacionNoDisponibleException` | `adapter` | Los 3 adaptadores, al traducir `ServicioGPSException` o si la respuesta es inválida | `<<create>> +UbicacionNoDisponibleException(mensaje: String)`, `<<create>> +UbicacionNoDisponibleException(mensaje: String, causa: Throwable)` |

### GestorConsultarBolsines

`<<control>>`, paquete `control`, `@Service @SessionScope`.

| Atributo | Tipo | Modificadores |
|---|---|---|
| `-repositorioBolsin` | `RepositorioBolsin` | `{readOnly}` |
| `-repositorioEmpleado` | `RepositorioEmpleado` | `{readOnly}` |
| `-interfazGPSTracker` | `InterfazGPSTracker` | `{readOnly}` |
| `-gestorNotificarUbicacionBolsin` | `GestorNotificarUbicacionBolsin` | `{readOnly}` |
| `-cmUsuarioLogueado` | `CM` | |
| `-bolsinesEnviados` | `List<Bolsin>` | |
| `-datosLocBolsines` | `Map<Integer, Object[]>` | |
| `-bolsinSeleccionado` | `Bolsin` | |
| `-mailGCM` | `String` | |

| Método |
|---|
| `<<create>> +GestorConsultarBolsines(repositorioBolsin: RepositorioBolsin, repositorioEmpleado: RepositorioEmpleado, interfazGPSTracker: InterfazGPSTracker, gestorNotificarUbicacionBolsin: GestorNotificarUbicacionBolsin)` |
| `+consultarUbBolsines(sesion: Sesion): Object[]` |
| `-buscarCMUsuarioLogueado(sesion: Sesion): void` |
| `-buscarBolsinesEnviados(): void` |
| `-buscarDatosLocBolsines(): void` |
| `+tomarSelecBolsinMapa(nroBolsin: int): void` |
| `+tomarConfirmEnvioCorreo(): boolean` |
| `-buscarMailGCM(): void` |
| `-llamarCUNotificarUbicacionDeBolsines(): boolean` |
| `+finCU(): void` |

`consultarUbBolsines` devuelve `Object[]{CM cmUsuarioLogueado, List<Map<String, Object>> ubicaciones}`. Cada mapa tiene las
claves `nroBolsin`, `numeroPrecinto`, `cmDestino`, `modeloGPS` y `disponible`; si hay ubicación, también `latitud`,
`longitud` y `fechaHora`.

### Relaciones

**Realización**

| Origen | Destino |
|---|---|
| `AdaptadorUbicacionXTR4500L` | `IAdaptadorUbicacionBolsin` |
| `AdaptadorUbicacionNavTrackQX7A` | `IAdaptadorUbicacionBolsin` |
| `AdaptadorUbicacionGeoPulseMTR900` | `IAdaptadorUbicacionBolsin` |

**Asociación** (dirigida: el origen guarda al destino en un atributo)

| Origen | Rol en el origen | Destino | Multiplicidad destino |
|---|---|---|---|
| `AdaptadorUbicacionXTR4500L` | `servicio` | `ServicioXTR4500L` | 1 |
| `AdaptadorUbicacionNavTrackQX7A` | `servicio` | `ServicioNavTrackQX7A` | 1 |
| `AdaptadorUbicacionGeoPulseMTR900` | `servicio` | `ServicioGeoPulseMTR900` | 1 |
| `GestorConsultarBolsines` | `interfazGPSTracker` | `InterfazGPSTracker` | 1 |
| `GestorConsultarBolsines` | `gestorNotificarUbicacionBolsin` | `GestorNotificarUbicacionBolsin` | 1 |
| `GestorConsultarBolsines` | `repositorioBolsin` | `RepositorioBolsin` | 1 |
| `GestorConsultarBolsines` | `repositorioEmpleado` | `RepositorioEmpleado` | 1 |
| `GestorConsultarBolsines` | `cmUsuarioLogueado` | `CM` | 0..1 |
| `GestorConsultarBolsines` | `bolsinesEnviados` | `Bolsin` | 0..* |
| `GestorConsultarBolsines` | `bolsinSeleccionado` | `Bolsin` | 0..1 |

Cada adaptador crea su servicio en el constructor y es el único que lo referencia, así que esa asociación también admite
modelarse como composición.

**Dependencia** (el origen usa al destino sin guardarlo en un atributo)

| Origen | Destino | Estereotipo | Motivo |
|---|---|---|---|
| `InterfazGPSTracker` | `IAdaptadorUbicacionBolsin` | `<<use>>` | Variable local `adaptador` en `buscarUbicacionBolsin` |
| `InterfazGPSTracker` | `AdaptadorUbicacionXTR4500L` | `<<create>>` | `new` en `buscarUbicacionBolsin` |
| `InterfazGPSTracker` | `AdaptadorUbicacionNavTrackQX7A` | `<<create>>` | `new` en `buscarUbicacionBolsin` |
| `InterfazGPSTracker` | `AdaptadorUbicacionGeoPulseMTR900` | `<<create>>` | `new` en `buscarUbicacionBolsin` |
| `InterfazGPSTracker` | `UbicacionNoDisponibleException` | `<<use>>` | La captura y devuelve `null` (A3) |
| `Adaptador*` (los 3) | `ServicioGPSException` | `<<use>>` | La capturan en `solicitarUbicacionBolsin` |
| `Adaptador*` (los 3) | `UbicacionNoDisponibleException` | `<<create>>` | La lanzan al traducir `ServicioGPSException` o si la respuesta es inválida |
| `Servicio*` (los 3) | `ServicioGPSException` | `<<create>>` | La lanzan si falla el HTTP o la respuesta viene vacía |
| `GestorConsultarBolsines` | `Sesion` | `<<use>>` | Parámetro de `consultarUbBolsines` y `buscarCMUsuarioLogueado` |
| `GestorConsultarBolsines` | `Empleado` | `<<use>>` | Variable local de recorrido en `buscarMailGCM` |

`InterfazGPSTracker` no tiene un atributo de tipo `IAdaptadorUbicacionBolsin`: crea un adaptador por cada consulta y lo usa
como variable local. Por eso la relación Client → Target es una **dependencia**, no una asociación.

`GestorConsultarBolsines` **no** tiene relación con los adaptadores ni con los `Servicio*`.

Entre paquetes, la dependencia va solo de `adapter` a `servicio`: los `Servicio*` no conocen nada del paquete `adapter`.
