from fastapi import FastAPI

app = FastAPI()

@app.get("/Seats")
def read_root():
    return "hello world"
