document.addEventListener('DOMContentLoaded', () => {
    const widget = document.getElementById('optimizerWidget');
    if (!widget) {
        return;
    }

    const cafeId = widget.dataset.cafeId;
    const dateInput = document.getElementById('optBookingDate');
    const timeSlotInput = document.getElementById('optTimeSlot');
    const actionButton = document.getElementById('runOptimizer');
    const resultCard = document.getElementById('optimizerResult');
    const recommendedBanner = document.getElementById('recommendedWindowBanner');
    const recommendedTitle = document.getElementById('recommendedWindowTitle');
    const recommendedText = document.getElementById('recommendedWindowText');
    const recommendedPrice = document.getElementById('recommendedWindowPrice');
    const recommendedCards = document.getElementById('recommendedWindowCards');

    const today = new Date().toISOString().split('T')[0];
    dateInput.min = today;
    dateInput.value = today;

    function demandTone(demand, currentDemand) {
        if (demand < currentDemand) {
            return 'Low crowd';
        }
        if (demand > currentDemand) {
            return 'Higher crowd';
        }
        return 'Similar crowd';
    }

    function renderRecommendedWindow(windowData, selectedHour, bestHour) {
        if (!recommendedCards) {
            return;
        }

        const selectedSlot = windowData.find(slot => slot.hour === selectedHour);
        const selectedPrice = selectedSlot ? Number(selectedSlot.price) : 0;

        recommendedCards.innerHTML = windowData.map(slot => {
            const isSelected = slot.hour === selectedHour;
            const isBest = slot.hour === bestHour;
            const saveAmount = Math.max(0, selectedPrice - Number(slot.price || 0));
            const borderClass = isBest
                ? 'border-neon-green/40'
                : isSelected
                    ? 'border-neon-cyan/40'
                    : 'border-white/10';

            return `
                <div class="rounded-xl border ${borderClass} bg-dark-bg p-3">
                    <div class="flex items-center justify-between">
                        <p class="font-orbitron text-lg text-white">${slot.label}</p>
                        <span class="text-xs ${isBest ? 'text-neon-green' : isSelected ? 'text-neon-cyan' : 'text-gray-400'}">
                            ${isBest ? 'Best' : isSelected ? 'Selected' : 'Nearby'}
                        </span>
                    </div>
                    <p class="mt-2 text-sm text-gray-400">Demand: <span class="text-white">${slot.realDemand}</span></p>
                    <p class="mt-1 text-sm text-gray-400">Price: <span class="text-neon-green">Rs ${slot.price}</span></p>
                    ${!isSelected && saveAmount > 0
                        ? `<p class="mt-2 inline-flex rounded-full border border-neon-green/30 bg-neon-green/10 px-3 py-1 text-xs font-semibold text-neon-green">You Save Rs ${saveAmount.toFixed(2)}</p>`
                        : ''}
                </div>
            `;
        }).join('');
    }

    async function loadRecommendedWindow() {
        if (!dateInput.value || !timeSlotInput.value || !recommendedBanner) {
            recommendedBanner.classList.add('hidden');
            return;
        }

        try {
            const selectedDate = new Date(`${dateInput.value}T00:00:00`);
            const dayOfWeek = selectedDate.getDay() === 0 ? 7 : selectedDate.getDay();
            const params = new URLSearchParams({
                cafeId,
                timeSlot: timeSlotInput.value,
                day: String(dayOfWeek),
                bookingDate: dateInput.value
            });

            const response = await fetch(`/api/optimize?${params.toString()}`);
            if (!response.ok) {
                throw new Error('Optimizer request failed');
            }

            const data = await response.json();
            const bestWindow = (data.recommendedWindow || []).find(slot => slot.label === data.recommendedHour)
                || (data.recommendedWindow || []).find(slot => slot.hour === data.selectedHour)
                || (data.recommendedWindow || [])[0];
            if (!bestWindow) {
                throw new Error('No nearby window available');
            }

            recommendedTitle.textContent = `Around ${data.selectedHourLabel}, ${data.recommendedHour} looks better`;
            recommendedText.textContent = `${data.message} Approx price near ${data.recommendedHour} is Rs ${data.recommendedPrice}, with ${demandTone(data.recommendedDemand, data.realDemand).toLowerCase()}.`;
            recommendedPrice.textContent = `Rs ${data.recommendedPrice}`;
            renderRecommendedWindow(data.recommendedWindow || [], data.selectedHour, bestWindow.hour);
            recommendedBanner.classList.remove('hidden');
        } catch (error) {
            recommendedBanner.classList.add('hidden');
        }
    }

    actionButton.addEventListener('click', async () => {
        if (!dateInput.value || !timeSlotInput.value) {
            alert('Select booking date and time slot first.');
            return;
        }

        actionButton.disabled = true;
        actionButton.textContent = 'Checking...';

        try {
            const selectedDate = new Date(`${dateInput.value}T00:00:00`);
            const dayOfWeek = selectedDate.getDay() === 0 ? 7 : selectedDate.getDay();
            const params = new URLSearchParams({
                cafeId,
                timeSlot: timeSlotInput.value,
                day: String(dayOfWeek),
                bookingDate: dateInput.value
            });

            const response = await fetch(`/api/optimize?${params.toString()}`);
            if (!response.ok) {
                throw new Error('Optimizer request failed');
            }

            const data = await response.json();
            document.getElementById('optDemandLevel').textContent = data.demandLevel;
            document.getElementById('optPredicted').textContent = data.predictedDemand;
            document.getElementById('optReal').textContent = data.realDemand;
            document.getElementById('optPrice').textContent = `Rs ${data.price}`;
            document.getElementById('optDuration').textContent = `${data.suggestedDuration} hrs`;
            document.getElementById('optBestHour').textContent = data.recommendedHour;
            document.getElementById('optBestPrice').textContent = `Rs ${data.recommendedPrice}`;
            document.getElementById('optMessage').textContent =
                `${data.message} Current actual demand: ${data.actualDemand} (${data.onlineBookings} online, ${data.offlineBookings} walk-ins).`;
            renderRecommendedWindow(
                data.recommendedWindow || [],
                data.selectedHour,
                ((data.recommendedWindow || []).find(slot => slot.label === data.recommendedHour) || {}).hour
            );
            recommendedBanner.classList.remove('hidden');
            recommendedTitle.textContent = `Around ${data.selectedHourLabel}, ${data.recommendedHour} looks better`;
            recommendedText.textContent = `${data.message} Approx price near ${data.recommendedHour} is Rs ${data.recommendedPrice}.`;
            recommendedPrice.textContent = `Rs ${data.recommendedPrice}`;
            resultCard.classList.remove('hidden');
        } catch (error) {
            alert('Unable to load optimization right now. Make sure the ML service is running.');
        } finally {
            actionButton.disabled = false;
            actionButton.textContent = 'Check Smart Pricing';
        }
    });

    dateInput.addEventListener('change', loadRecommendedWindow);
    timeSlotInput.addEventListener('change', loadRecommendedWindow);
});
