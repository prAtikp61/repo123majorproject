document.addEventListener('DOMContentLoaded', () => {
    const cafeSelect = document.getElementById('insightCafeSelect');
    if (!cafeSelect) {
        return;
    }

    const sourceSelect = document.getElementById('insightSourceSelect');
    const graphSourceSelect = document.getElementById('graphSourceSelect');
    const peakHour = document.getElementById('insightPeakHour');
    const lowHour = document.getElementById('insightLowHour');
    const bestSlot = document.getElementById('insightBestSlot');
    const revenueLabel = document.getElementById('insightRevenueLabel');
    const revenue = document.getElementById('insightRevenue');
    const occupancy = document.getElementById('insightOccupancy');
    const suggestion = document.getElementById('insightSuggestion');
    const chartCanvas = document.getElementById('ownerHourlyInsightsChart');
    let hourlyChart;

    function updateRevenueLabel(source) {
        if (!revenueLabel) {
            return;
        }

        if (source === 'online') {
            revenueLabel.textContent = 'Online Revenue Share';
            return;
        }

        if (source === 'offline') {
            revenueLabel.textContent = 'Offline Revenue Share';
            return;
        }

        revenueLabel.textContent = 'Total Expected Revenue';
    }

    function renderChart(hourlyData, source) {
        if (!chartCanvas || typeof Chart === 'undefined') {
            return;
        }

        if (hourlyChart) {
            hourlyChart.destroy();
        }

        hourlyChart = new Chart(chartCanvas, {
            type: 'line',
            data: {
                labels: hourlyData.map(item => item.label),
                datasets: [
                    {
                        label: `${source.charAt(0).toUpperCase()}${source.slice(1)} Demand`,
                        data: hourlyData.map(item => item.realDemand),
                        borderColor: '#00ffff',
                        backgroundColor: 'rgba(0, 255, 255, 0.18)',
                        tension: 0.35,
                        fill: true,
                        pointBackgroundColor: '#8b5cf6',
                        pointBorderColor: '#00ffff'
                    },
                    {
                        label: 'Revenue',
                        data: hourlyData.map(item => item.revenue),
                        borderColor: '#10b981',
                        backgroundColor: 'rgba(16, 185, 129, 0.12)',
                        tension: 0.35,
                        fill: false,
                        yAxisID: 'y1',
                        pointBackgroundColor: '#10b981',
                        pointBorderColor: '#10b981'
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        labels: {
                            color: '#e5e7eb'
                        }
                    }
                },
                scales: {
                    x: {
                        ticks: { color: '#9ca3af' },
                        grid: { color: 'rgba(255,255,255,0.05)' }
                    },
                    y: {
                        ticks: { color: '#9ca3af' },
                        grid: { color: 'rgba(255,255,255,0.05)' }
                    },
                    y1: {
                        position: 'right',
                        ticks: { color: '#86efac' },
                        grid: { display: false }
                    }
                }
            }
        });
    }

    async function loadInsights() {
        const cafeId = cafeSelect.value;
        const source = graphSourceSelect ? graphSourceSelect.value : (sourceSelect ? sourceSelect.value : 'both');
        if (!cafeId) {
            return;
        }

        updateRevenueLabel(source);
        suggestion.textContent = 'Loading insights...';

        try {
            const [summaryResponse, hourlyResponse] = await Promise.all([
                fetch(`/api/owner-insights?cafeId=${cafeId}&source=${source}`),
                fetch(`/api/owner-hourly-insights?cafeId=${cafeId}&source=${source}`)
            ]);

            if (!summaryResponse.ok || !hourlyResponse.ok) {
                throw new Error('Failed to load insights');
            }

            const summary = await summaryResponse.json();
            const hourly = await hourlyResponse.json();

            peakHour.textContent = summary.peakHour;
            lowHour.textContent = summary.lowHour;
            bestSlot.textContent = hourly.bestRevenueHour || summary.bestSlot;
            revenue.textContent = `Rs ${summary.expectedRevenue}`;
            occupancy.textContent = summary.occupancy;
            suggestion.textContent = summary.pricingSuggestion;
            renderChart(hourly.hourlyData || [], source);
        } catch (error) {
            peakHour.textContent = '--';
            lowHour.textContent = '--';
            bestSlot.textContent = '--';
            revenue.textContent = '--';
            occupancy.textContent = '--';
            updateRevenueLabel(source);
            suggestion.textContent = 'Unable to load owner insights right now.';
            renderChart([], source);
        }
    }

    cafeSelect.addEventListener('change', loadInsights);
    if (sourceSelect) {
        sourceSelect.addEventListener('change', () => {
            if (graphSourceSelect) {
                graphSourceSelect.value = sourceSelect.value;
            }
            loadInsights();
        });
    }
    if (graphSourceSelect) {
        graphSourceSelect.value = sourceSelect ? sourceSelect.value : 'both';
        graphSourceSelect.addEventListener('change', () => {
            if (sourceSelect) {
                sourceSelect.value = graphSourceSelect.value;
            }
            loadInsights();
        });
    }
    loadInsights();
});
