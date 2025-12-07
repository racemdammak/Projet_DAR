from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from main import summarize_pdf
import os
from pathlib import Path

app = FastAPI()

origins = [
    "http://localhost:3000",  # React dev server
    "http://127.0.0.1:3000",  # React dev server (alternative)
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class SummarizeRequest(BaseModel):
    filename: str

def find_storage_directory():
    """Find the cloud_storage directory in MiniCloud_CORBA folder."""
    # Get the current file's directory (AI folder)
    current = Path(__file__).parent.absolute()
    
    # Go up to project root (parent of AI folder)
    project_root = current.parent
    
    # Path to cloud_storage in MiniCloud_CORBA
    storage_path = project_root / "MiniCloud_CORBA" / "cloud_storage"
    
    # Convert to absolute path string
    return str(storage_path.absolute())

STORAGE_DIR = find_storage_directory()

@app.get("/")
async def read_root():
    return {
        "Hello": "World",
        "storage_dir": STORAGE_DIR,
        "storage_exists": os.path.exists(STORAGE_DIR)
    }

@app.post("/summarize")
async def summarize_pdf_endpoint(request: SummarizeRequest):
    try:
        # Construct the full path to the file
        pdf_path = os.path.join(STORAGE_DIR, request.filename)
        
        # Check if file exists
        if not os.path.exists(pdf_path):
            raise HTTPException(
                status_code=404, 
                detail=f"File not found: {request.filename}. Searched in: {STORAGE_DIR}"
            )
        
        if not os.path.isfile(pdf_path):
            raise HTTPException(
                status_code=400,
                detail=f"Path exists but is not a file: {request.filename}"
            )
        
        summary = summarize_pdf(pdf_path)
        return {"summary": summary}
    except HTTPException:
        raise
    except Exception as e:
        import traceback
        error_detail = f"Error: {str(e)}\nTraceback: {traceback.format_exc()}"
        raise HTTPException(status_code=500, detail=error_detail)