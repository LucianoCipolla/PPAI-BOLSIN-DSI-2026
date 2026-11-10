// Boundary con el Servidor Google Maps. Sin clave usa Leaflet + OpenStreetMap detrás de la misma interfaz.
class InterfazGoogleMaps {

    static CENTRO_ARGENTINA = { lat: -33.0, lng: -63.6 };
    static ZOOM_INICIAL = 5;
    static COLOR_NORMAL = "#0b3d91";
    static COLOR_SELECCION = "#c2410c";

    constructor(idContenedor, claveAPI, alSeleccionarBolsin) {
        this.contenedor = document.getElementById(idContenedor);
        this.claveAPI = claveAPI;
        this.alSeleccionarBolsin = alSeleccionarBolsin;
        this.marcadores = new Map();
    }

    // CU36 paso 6
    async mostrarMapaConUbicaciones(ubicaciones) {
        const conUbicacion = ubicaciones.filter((ubicacion) => ubicacion.disponible);
        try {
            if (this.claveAPI) {
                await this.cargarGoogleMaps();
                this.crearMapaGoogle(conUbicacion);
            } else {
                this.crearMapaLeaflet(conUbicacion);
            }
        } catch (error) {
            this.contenedor.textContent = "No se pudo cargar el mapa. La lista de bolsines sigue disponible.";
        }
    }

    // CU36 paso 7
    marcarSeleccion(nroBolsin) {
        this.marcadores.forEach((marcador, nro) => marcador.resaltar(nro === nroBolsin));
    }

    // CU36 paso 6
    filtrarMarcadores(nrosVisibles) {
        this.marcadores.forEach((marcador, nro) => marcador.mostrar(nrosVisibles.includes(nro)));
    }

    cargarGoogleMaps() {
        return new Promise((resolver, rechazar) => {
            const script = document.createElement("script");
            script.src = `https://maps.googleapis.com/maps/api/js?key=${encodeURIComponent(this.claveAPI)}`;
            script.onload = resolver;
            script.onerror = rechazar;
            document.head.append(script);
        });
    }

    crearMapaGoogle(ubicaciones) {
        const mapa = new google.maps.Map(this.contenedor, {
            center: InterfazGoogleMaps.CENTRO_ARGENTINA,
            zoom: InterfazGoogleMaps.ZOOM_INICIAL
        });
        const limites = new google.maps.LatLngBounds();
        const icono = (color, escala) => ({
            path: google.maps.SymbolPath.CIRCLE,
            fillColor: color,
            fillOpacity: 0.9,
            strokeColor: "#ffffff",
            strokeWeight: 2,
            scale: escala
        });

        for (const ubicacion of ubicaciones) {
            const posicion = { lat: ubicacion.latitud, lng: ubicacion.longitud };
            const marcador = new google.maps.Marker({
                position: posicion,
                map: mapa,
                title: `Bolsín ${ubicacion.nroBolsin}`,
                icon: icono(InterfazGoogleMaps.COLOR_NORMAL, 9)
            });
            marcador.addListener("click", () => this.alSeleccionarBolsin(ubicacion.nroBolsin));
            limites.extend(posicion);
            this.marcadores.set(ubicacion.nroBolsin, {
                mostrar: (visible) => marcador.setVisible(visible),
                resaltar: (activo) => marcador.setIcon(activo
                    ? icono(InterfazGoogleMaps.COLOR_SELECCION, 13)
                    : icono(InterfazGoogleMaps.COLOR_NORMAL, 9))
            });
        }
        if (ubicaciones.length > 0) {
            mapa.fitBounds(limites, 40);
        }
    }

    crearMapaLeaflet(ubicaciones) {
        const centro = InterfazGoogleMaps.CENTRO_ARGENTINA;
        const mapa = L.map(this.contenedor).setView([centro.lat, centro.lng], InterfazGoogleMaps.ZOOM_INICIAL);
        L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
            maxZoom: 19,
            attribution: "&copy; OpenStreetMap"
        }).addTo(mapa);

        const estilo = (color, radio) => ({ color: "#ffffff", weight: 2, fillColor: color, fillOpacity: 0.9, radius: radio });
        const limites = [];

        for (const ubicacion of ubicaciones) {
            const posicion = [ubicacion.latitud, ubicacion.longitud];
            const marcador = L.circleMarker(posicion, estilo(InterfazGoogleMaps.COLOR_NORMAL, 9))
                .addTo(mapa)
                .bindTooltip(`Bolsín ${ubicacion.nroBolsin}`)
                .on("click", () => this.alSeleccionarBolsin(ubicacion.nroBolsin));
            limites.push(posicion);
            this.marcadores.set(ubicacion.nroBolsin, {
                mostrar: (visible) => (visible ? marcador.addTo(mapa) : marcador.remove()),
                resaltar: (activo) => marcador.setStyle(activo
                    ? estilo(InterfazGoogleMaps.COLOR_SELECCION, 13)
                    : estilo(InterfazGoogleMaps.COLOR_NORMAL, 9))
            });
        }
        if (limites.length > 0) {
            mapa.fitBounds(limites, { padding: [40, 40] });
        }
    }
}
