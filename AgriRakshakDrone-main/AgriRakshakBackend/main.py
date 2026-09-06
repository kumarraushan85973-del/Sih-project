from fastapi import FastAPI, File, UploadFile
from fastapi.middleware.cors import CORSMiddleware
import random

app = FastAPI(
    title="AgriRakshak AI API",
    description="Crop Disease Detection API",
    version="1.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/")
def home():
    return {
        "message": "AgriRakshak AI Server is running"
    }


@app.post("/predict")
async def predict(file: UploadFile = File(...)):

    diseases = [
        "Healthy",
        "Leaf Blight",
        "Leaf Spot",
        "Powdery Mildew"
    ]

    disease = random.choice(diseases)

    if disease == "Healthy":
        status = "HEALTHY"
        recommendation = (
            "Crop appears healthy. Continue regular monitoring."
        )
    else:
        status = "DISEASE_DETECTED"
        recommendation = (
            "Affected leaves should be inspected and appropriate "
            "crop protection measures should be considered."
        )

    confidence = round(random.uniform(85.0, 98.0), 2)

    return {
        "status": status,
        "disease": disease,
        "confidence": confidence,
        "recommendation": recommendation
    }