// Pantalla del CU 36: mapa, selección sincronizada, filtro en vivo y modal de confirmación.
const listaBolsines = document.getElementById("listaBolsines");
const modalConfirmacionCorreo = document.getElementById("modalConfirmacionCorreo");
const interfazGoogleMaps = new InterfazGoogleMaps("mapaUbicaciones", claveGoogleMaps, seleccionarBolsin);

const MINUTOS_POR_HORA = 60;
const MS_POR_MINUTO = 60000;
const HORAS_POR_DIA = 24;
// CU36 pasos 7 y 8
async function seleccionarBolsin(nroBolsin) {
    marcarSeleccionEnLista(nroBolsin);
    interfazGoogleMaps.marcarSeleccion(nroBolsin);

    const respuesta = await fetch(urlSeleccion, {
        method: "POST",
        body: new URLSearchParams({ nroBolsin })
    });
    if (respuesta.ok) {
        solicitarEnvioCorreo(ubicaciones.find((ubicacion) => ubicacion.nroBolsin === nroBolsin));
    }
}

// CU36 paso 7
function marcarSeleccionEnLista(nroBolsin) {
    for (const item of listaBolsines.children) {
        const seleccionado = Number(item.dataset.nro) === nroBolsin;
        item.classList.toggle("seleccionado", seleccionado);
        if (seleccionado) {
            item.scrollIntoView({ block: "nearest" });
        }
    }
}

// CU36 paso 8
function solicitarEnvioCorreo(ubicacion) {
    document.getElementById("resumenBolsin").textContent = ubicacion.nroBolsin;
    document.getElementById("resumenCMDestino").textContent = ubicacion.cmDestino;
    document.getElementById("resumenCoordenadas").textContent = `${ubicacion.latitud}, ${ubicacion.longitud}`;
    document.getElementById("resumenHora").textContent = new Date(ubicacion.fechaHora)
        .toLocaleString("es-AR", { dateStyle: "short", timeStyle: "short" });
    modalConfirmacionCorreo.showModal();
}

// CU36 paso 6
function tomarFiltroBolsines(precinto, cmDestino) {
    const textoPrecinto = precinto.trim().toLowerCase();
    const nrosVisibles = [];

    for (const item of listaBolsines.children) {
        const coincide = item.dataset.precinto.toLowerCase().includes(textoPrecinto)
            && (cmDestino === "" || item.dataset.cmDestino === cmDestino);
        item.hidden = !coincide;
        if (coincide) {
            nrosVisibles.push(Number(item.dataset.nro));
        }
    }

    interfazGoogleMaps.filtrarMarcadores(nrosVisibles);
    document.getElementById("lblCantidadBolsines").textContent =
        `Mostrando ${nrosVisibles.length} de ${listaBolsines.children.length}`;
}

function cargarCMsDestino(combo) {
    const cmsDestino = [...new Set(ubicaciones.map((ubicacion) => ubicacion.cmDestino))].sort();
    for (const cmDestino of cmsDestino) {
        combo.add(new Option(cmDestino, cmDestino));
    }
}

function formatearHaceCuanto(fechaHora) {
    const minutos = Math.max(0, Math.floor((Date.now() - new Date(fechaHora)) / MS_POR_MINUTO));
    if (minutos < 1) {
        return "hace menos de 1 min";
    }
    if (minutos < MINUTOS_POR_HORA) {
        return `hace ${minutos} min`;
    }
    const horas = Math.floor(minutos / MINUTOS_POR_HORA);
    if (horas < HORAS_POR_DIA) {
        return `hace ${horas} h ${minutos % MINUTOS_POR_HORA} min`;
    }
    const dias = Math.floor(horas / HORAS_POR_DIA);
    return dias === 1 ? "hace 1 día" : `hace ${dias} días`;
}

function actualizarHaceCuanto() {
    for (const etiqueta of listaBolsines.querySelectorAll("[data-fecha-hora]")) {
        etiqueta.textContent = formatearHaceCuanto(etiqueta.dataset.fechaHora);
    }
}

interfazGoogleMaps.mostrarMapaConUbicaciones(ubicaciones);

if (listaBolsines) {
    const txtFiltroPrecinto = document.getElementById("txtFiltroPrecinto");
    const cmbFiltroCMDestino = document.getElementById("cmbFiltroCMDestino");
    const aplicarFiltro = () => tomarFiltroBolsines(txtFiltroPrecinto.value, cmbFiltroCMDestino.value);

    cargarCMsDestino(cmbFiltroCMDestino);
    aplicarFiltro();
    txtFiltroPrecinto.addEventListener("input", aplicarFiltro);
    cmbFiltroCMDestino.addEventListener("change", aplicarFiltro);

    listaBolsines.addEventListener("click", (evento) => {
        const tarjeta = evento.target.closest("button");
        if (tarjeta) {
            seleccionarBolsin(Number(tarjeta.closest("li").dataset.nro));
        }
    });

    actualizarHaceCuanto();
    setInterval(actualizarHaceCuanto, MS_POR_MINUTO);
}

document.getElementById("btnNoEnviar").addEventListener("click", () => modalConfirmacionCorreo.close());
