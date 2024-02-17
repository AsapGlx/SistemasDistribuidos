/*
 * Proyecto Final
 * González González Jesús Asael
 * 7CM2
 */

package com.proyectofinal;

/**
 * Clase que representa una solicitud de búsqueda desde el frontend.
 * Esta clase es utilizada para mapear los datos de solicitud enviados desde el cliente.
 */
public class FrontendSearchRequest {
    // Campo para almacenar la consulta de búsqueda
    private String searchQuery;

    /**
     * Obtiene la consulta de búsqueda.
     * 
     * @return La consulta de búsqueda como un String.
     */
    public String getSearchQuery() {
        return searchQuery;
    }

    /**
     * Establece la consulta de búsqueda.
     * 
     * @param searchQuery La consulta de búsqueda a establecer.
     */
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }
}

