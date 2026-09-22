from fastapi import FastAPI, UploadFile, File, Form, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
import shutil
import os
import uuid
import asyncio
from models.virtual_tryon import VirtualTryOnModel

app = FastAPI(title="VirtualFit AI Service")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

os.makedirs("uploads", exist_ok=True)
os.makedirs("generated", exist_ok=True)

# Initialize our VTO Model abstraction
vto_model = VirtualTryOnModel()

@app.get("/api/health")
async def health_check():
    return {"status": "ok", "service": "AI Inference Service"}

@app.post("/api/generate")
async def generate_tryon(
    person: UploadFile = File(...),
    garment: UploadFile = File(None),
    garmentId: str = Form(None)
):
    if not garment and not garmentId:
        raise HTTPException(status_code=400, detail="Garment image or ID is required")
        
    job_id = str(uuid.uuid4())
    person_path = f"uploads/{job_id}_person_{person.filename}"
    
    with open(person_path, "wb") as buffer:
        shutil.copyfileobj(person.file, buffer)
        
    garment_path = None
    if garment:
        garment_path = f"uploads/{job_id}_garment_{garment.filename}"
        with open(garment_path, "wb") as buffer:
            shutil.copyfileobj(garment.file, buffer)
    
    # In a real async job queue (like Celery), you'd dispatch this here.
    # For this implementation, we will simulate the processing pipeline.
    
    try:
        # Step 1: Preprocess Person Image
        processed_person = vto_model.preprocess(person_path)
        
        # Step 2: Generate Body Mask / Parse
        body_mask = vto_model.generate_mask(processed_person)
        
        # Step 3: Prepare Garment
        processed_garment = vto_model.prepare_garment(garment_path or garmentId)
        
        # Step 4: Generate Try-On
        # Simulate heavy computation
        await asyncio.sleep(3) 
        generated_result = vto_model.generate_tryon(processed_person, body_mask, processed_garment)
        
        # Step 5: Postprocess
        final_image = vto_model.postprocess(generated_result)
        
        return JSONResponse({
            "success": True,
            "job_id": job_id,
            "result_url": final_image, # In real app, this would be a URL to the saved generated image
            "model_used": vto_model.model_name
        })
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
