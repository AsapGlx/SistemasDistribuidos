/*
 * Proyecto Final
 * González González Jesús Asael
 * 7CM2
 * Se cambio a txt porque teams no deja subir js pero es .js
 */

// Función para enviar una solicitud de búsqueda al servidor web.
function sendSearchRequest() {
    // Recoge la frase de búsqueda del elemento de entrada en la página web.
    var phrase = document.getElementById('searchPhrase').value;
    var webServerUrl = 'http://localhost:8089/procesar_datos'; // URL del servidor web.

    // Realiza una solicitud HTTP POST al servidor con la frase de búsqueda.
    fetch(webServerUrl, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ searchQuery: phrase })
    })
    .then(response => {
        // Procesa la respuesta del servidor.
        if (response.ok) {
            // Si la solicitud fue exitosa, procesa la respuesta JSON.
            console.log('Solicitud POST enviada correctamente');
            console.log('Frase enviada: ' + phrase);
            return response.json();
        } else {
            // Si hubo un error, muestra un mensaje de error.
            console.error('Error al enviar la solicitud. Código de estado: ' + response.status);
            return Promise.reject('Error en la solicitud');
        }
    })
    .then(data => {
        // Ordena los resultados por 'Score Total' y los pasa a la función 'displayResults'.
        const sortedData = Object.entries(data).sort((a, b) => b[1].scoreTotal - a[1].scoreTotal);
        displayResults(Object.fromEntries(sortedData));
    })
    .catch(error => {
        // Maneja cualquier error que ocurra durante la solicitud.
        console.error('Error:', error);
    });
}

// Función para mostrar los resultados de la búsqueda en una tabla.
function displayResults(data) {
    // Selecciona el elemento div donde se mostrarán los resultados.
    var resultsDiv = document.getElementById('results');
    resultsDiv.innerHTML = ''; // Limpia los resultados anteriores.

    // Crea los elementos de la tabla (tabla, encabezado, cuerpo).
    var table = document.createElement('table');
    var thead = document.createElement('thead');
    var tbody = document.createElement('tbody');

    // Configura los encabezados de la tabla.
    var headerRow = document.createElement('tr');
    var headers = ['Libro', 'Score Total', 'Palabra', 'TF*IDF'];
    headers.forEach(headerText => {
        var header = document.createElement('th');
        header.textContent = headerText;
        headerRow.appendChild(header);
    });
    thead.appendChild(headerRow);

    // Agrega el encabezado y el cuerpo a la tabla.
    table.appendChild(thead);
    table.appendChild(tbody);

    // Llena el cuerpo de la tabla con los datos de los libros.
    Object.entries(data).forEach(([book, content]) => {
        // Crea una fila para cada libro y sus detalles.
        var bookRow = document.createElement('tr');
        bookRow.appendChild(createCell(book));
        bookRow.appendChild(createCell(content.scoreTotal.toFixed(20)));

        var first = true;
        Object.entries(content.detalles).forEach(([word, tfidf]) => {
            // Agrega detalles adicionales para cada palabra en el libro.
            if (first) {
                bookRow.appendChild(createCell(word));
                bookRow.appendChild(createCell(tfidf.toFixed(20)));
                tbody.appendChild(bookRow);
                first = false;
            } else {
                var wordRow = document.createElement('tr');
                wordRow.appendChild(createCell(''));
                wordRow.appendChild(createCell(''));
                wordRow.appendChild(createCell(word));
                wordRow.appendChild(createCell(tfidf.toFixed(20)));
                tbody.appendChild(wordRow);
            }
        });
    });

    // Agrega la tabla completa al div de resultados.
    resultsDiv.appendChild(table);
}

// Función para crear un elemento de celda de tabla con texto dado.
function createCell(text) {
    var cell = document.createElement('td');
    cell.textContent = text;
    return cell;
}

