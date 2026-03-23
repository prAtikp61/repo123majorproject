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

    const today = new Date().toISOString().split('T')[0];
    dateInput.min = today;
    dateInput.value = today;

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
            document.getElementById('optPrice').textContent = `₹${data.price}`;
            document.getElementById('optDuration').textContent = `${data.suggestedDuration} hrs`;
            document.getElementById('optMessage').textContent =
                `${data.message} Online: ${data.onlineBookings}, Walk-ins: ${data.offlineBookings}.`;
            resultCard.classList.remove('hidden');
        } catch (error) {
            alert('Unable to load optimization right now. Make sure the ML service is running.');
        } finally {
            actionButton.disabled = false;
            actionButton.textContent = 'Check Smart Pricing';
        }
    });
});
