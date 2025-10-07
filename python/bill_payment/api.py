from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from .services import bill_payment_service
from .data_store import data_store

app = FastAPI(
    title="CardDemo Bill Payment API",
    description="Python conversion of COBIL00C mainframe bill payment module",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


class ValidateAccountRequest(BaseModel):
    acct_id: str
    
    class Config:
        json_schema_extra = {
            "example": {
                "acct_id": "12345678901"
            }
        }


class ProcessPaymentRequest(BaseModel):
    acct_id: str
    confirm: str
    
    class Config:
        json_schema_extra = {
            "example": {
                "acct_id": "12345678901",
                "confirm": "Y"
            }
        }


@app.on_event("startup")
async def startup_event():
    data_store.add_sample_data()


@app.get("/health")
async def health_check():
    return {"status": "healthy", "service": "bill-payment"}


@app.post("/api/v1/bill-payment/validate")
async def validate_account(request: ValidateAccountRequest):
    result = bill_payment_service.validate_account(request.acct_id)
    if not result["success"]:
        raise HTTPException(status_code=400, detail=result["error"])
    return result


@app.post("/api/v1/bill-payment/process")
async def process_payment(request: ProcessPaymentRequest):
    result = bill_payment_service.process_payment(request.acct_id, request.confirm)
    if not result["success"]:
        raise HTTPException(status_code=400, detail=result["error"])
    return result
