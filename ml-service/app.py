from pathlib import Path

import pandas as pd
from flask import Flask, jsonify, request
from sklearn.linear_model import LinearRegression


app = Flask(__name__)
BASE_DIR = Path(__file__).resolve().parent
DATA_FILE = BASE_DIR / "booking_demand.csv"


def train_model():
    data = pd.read_csv(DATA_FILE)
    features = data[["time_slot", "day_of_week"]]
    target = data["bookings"]

    regressor = LinearRegression()
    regressor.fit(features, target)
    return regressor


model = train_model()


@app.post("/predict")
def predict():
    payload = request.get_json(force=True)
    time_slot = int(payload.get("time_slot", 0))
    day = int(payload.get("day", 1))
    prediction = model.predict([[time_slot, day]])[0]
    return jsonify({"predicted": max(0, round(prediction))})


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
