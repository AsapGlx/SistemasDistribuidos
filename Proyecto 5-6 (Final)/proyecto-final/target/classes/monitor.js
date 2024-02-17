/*
 * Proyecto Final
 * González González Jesús Asael
 * 7CM2
 * Se cambio a txt porque teams no deja subir js pero es .js
 */

let chartServer1, chartServer2, chartServer3, chartServer4;
let historyCpuLoadServer1 = [];
let historyCpuLoadServer2 = [];
let historyCpuLoadServer3 = [];
let historyCpuLoadServer4 = [];
let historyRamUsageServer1 = [];
let historyRamUsageServer2 = [];
let historyRamUsageServer3 = [];
let historyRamUsageServer4 = [];


// Función para actualizar el historial de métricas para cada servidor.

function updateHistory(data) {
        // Asegura que los valores no excedan 100 y los agrega al historial correspondiente.

    historyCpuLoadServer1.push(Math.min(data[0].cpuUsage, 100));
    historyCpuLoadServer2.push(Math.min(data[1].cpuUsage, 100));
    historyCpuLoadServer3.push(Math.min(data[2].cpuUsage, 100));
    historyCpuLoadServer4.push(Math.min(data[3].cpuUsage, 100));
    historyRamUsageServer1.push(Math.min(data[0].memoryUsage, 100));
    historyRamUsageServer2.push(Math.min(data[1].memoryUsage, 100));
    historyRamUsageServer3.push(Math.min(data[2].memoryUsage, 100));
    historyRamUsageServer4.push(Math.min(data[3].memoryUsage, 100));
}

// Función para crear o actualizar un gráfico para un servidor específico.

function createOrUpdateChart(chartInstance, canvasId, cpuData, ramData, serverLabel) {
        // Obtiene el contexto del canvas para el gráfico.
    const ctx = document.getElementById(canvasId).getContext('2d');
        // Si el gráfico no existe, lo crea con los datos proporcionados.
    if (!chartInstance) {
        chartInstance = new Chart(ctx, {
              // Configuración del gráfico (tipo línea, datos, colores, etc.)
            type: 'line',
            data: {
                labels: [...Array(cpuData.length).keys()],
                datasets: [
                    {
                        label: 'CPU Load ' + serverLabel,
                        data: cpuData,
                        borderColor: 'rgba(255, 99, 132, 1)',
                        borderWidth: 1
                    },
                    {
                        label: 'RAM Usage ' + serverLabel,
                        data: ramData,
                        borderColor: 'rgba(54, 162, 235, 1)',
                        borderWidth: 1
                    }
                ]
            },
            options: {
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
    } else {
        // Si el gráfico ya existe, actualiza sus datos.
        chartInstance.data.labels = [...Array(cpuData.length).keys()];
        chartInstance.data.datasets[0].data = cpuData;
        chartInstance.data.datasets[1].data = ramData;
        chartInstance.update();
    }
    return chartInstance;
}

// Función para actualizar los gráficos de todos los servidores.
function updateChart() {
        // Crea o actualiza el gráfico para cada servidor.
    chartServer1 = createOrUpdateChart(chartServer1, 'chartServer1', historyCpuLoadServer1, historyRamUsageServer1, 'Server 1');
    chartServer2 = createOrUpdateChart(chartServer2, 'chartServer2', historyCpuLoadServer2, historyRamUsageServer2, 'Server 2');
    chartServer3 = createOrUpdateChart(chartServer3, 'chartServer3', historyCpuLoadServer3, historyRamUsageServer3, 'Server 3');
    chartServer4 = createOrUpdateChart(chartServer4, 'chartServer4', historyCpuLoadServer4, historyRamUsageServer4, 'Server 4');
}

// Función para obtener los datos de métricas de los servidores y actualizar los gráficos.
function fetchDataAndUpdateChart() {
    Promise.all([
                // Realiza solicitudes HTTP para obtener las métricas de cada servidor.
        fetch('http://localhost:8089/metrics').then(response => response.json()),
        fetch('http://localhost:8081/metrics').then(response => response.json()),
        fetch('http://localhost:8082/metrics').then(response => response.json()),
        fetch('http://localhost:8083/metrics').then(response => response.json())
    ])
    .then(data => {
                // Cuando se reciben los datos, actualiza el historial y los gráficos.
        console.log("Datos recibidos:", data); // Verifica los datos aquí
        updateHistory(data);
        updateChart();
    })
    .catch(error => {
        console.error('Error al obtener los datos:', error);
        console.error('Asegúrate de que los servidores están corriendo y accesibles');
    });
}
// Ejecuta la función fetchDataAndUpdateChart inmediatamente y luego cada 1000 milisegundos (1 segundo).
fetchDataAndUpdateChart();
setInterval(fetchDataAndUpdateChart, 1000);

  
