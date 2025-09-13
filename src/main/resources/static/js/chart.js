function extractChartData(jsonData, labelKey, valueKey) {
    if (!Array.isArray(jsonData) || jsonData.length === 0) return null;

    const labels = [];
    const data = [];
    for (let i = 0; i < jsonData.length; i++) {
        labels.push(jsonData[i][labelKey]); // x축 라벨
        data.push(jsonData[i][valueKey]);   // y축 값
    }
    return {labels, data};
}

function drawChart({
                       jsonData,         // 원본 데이터 배열
                       labelKey,         // 라벨 키 이름
                       valueKey,         // 값 키 이름
                       chartContainerId, // 차트가 들어갈 요소 ID
                       canvasId,         // canvas ID
                       chartType = "line",   // 차트 타입 (line, bar, pie 등)
                       chartTitle = "",      // 차트 제목
                       datasetLabel = "",    // 데이터셋 이름
                       xAxisLabel = "",      // x축 제목
                       yAxisLabel = "",      // y축 제목
                       noDataMessage = ""    // 데이터 없을 때 표시할 메시지
                   }) {
    const chartData = extractChartData(jsonData, labelKey, valueKey);
    if (!chartData) {
        // 데이터가 없을 경우 안내 메시지 출력
        document.getElementById(chartContainerId).innerHTML = `
          <div style="text-align:center; padding:1rem; color:#888;">
            ${noDataMessage}
          </div>
        `;
        return;
    }

    const ctx = document.getElementById(canvasId).getContext("2d");

    const colorPalette = [
        "rgba(78, 115, 223, 0.6)",
        "rgba(0, 191, 165, 0.6)",
        "rgba(255, 112, 67, 0.6)",
        "rgba(255, 193, 7, 0.6)",
        "rgba(156, 39, 176, 0.6)",
        "rgba(164, 235, 230, 0.7)",
        "rgba(255, 225, 0, 0.7)",
        "rgba(165, 50, 50, 0.7)",
        "rgba(255, 200, 240, 0.7)",
        "rgba(128, 128, 128, 0.6)",
        "rgba(130, 130, 230, 0.7)",
        "rgba(0, 255, 170, 0.6)"
    ];

    // 차트 타입에 따라 색상 설정
    const datasetConfig = {
        label: datasetLabel,
        data: chartData.data,
        backgroundColor: chartType === "pie" || chartType === "doughnut" || chartType === "bar"
            ? colorPalette.slice(0, chartData.data.length) // 항목별 색상
            : "#333",
        borderColor: chartType === "line" ? "#666" : "#fff", // 선 또는 테두리 색상
        borderWidth: 1,
        fill: false, // 선 아래 색 채우기 안함
        pointRadius: chartType === "line" ? 4 : 0,
        pointHoverRadius: chartType === "line" ? 6 : 0
    };

    // Chart.js 인스턴스 생성
    new Chart(ctx, {
        type: chartType,
        data: {
            labels: chartData.labels,
            datasets: [datasetConfig]
        },
        options: {
            responsive: true,               // 반응형
            maintainAspectRatio: false,    // 비율 고정 해제
            plugins: {
                title: {
                    display: !!chartTitle,
                    text: chartTitle,
                    font: {
                        size: 16,
                        weight: "bold"
                    }
                },
                legend: {
                    display:  chartType === "pie" || chartType === "doughnut" ,
                    labels: {
                        color: "#666" // 범례 글씨 색상
                    }
                }
            },
            scales: chartType === "pie" || chartType === "doughnut" ? {} : {
                x: {
                    title: {
                        display: !!xAxisLabel,
                        text: xAxisLabel,
                        font: {
                            size: 14,
                            weight: "bold"
                        }
                    },
                    ticks: {
                        autoSkip: true,
                        maxTicksLimit: 6
                    }
                },
                y: {
                    beginAtZero: true,
                    title: {
                        display: !!yAxisLabel,
                        text: yAxisLabel,
                        font: {
                            size: 14,
                            weight: "bold"
                        }
                    },
                    ticks: {
                        stepSize: 1
                    }
                }
            }
        }
    });
}