# Use an official Python runtime as a parent image
FROM python:3.10-slim

# Set the working directory in the container
WORKDIR /app

# Copy the requirements file into the container
COPY requirements.txt .

# Install dependencies
RUN pip install --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

# Copy the rest of the code
COPY . .

# Expose the port
ENV PORT=8000

# --- THE FIX ---
# We chain the training script AND the server start command.
# This ensures models are generated every time the app deploys/restarts.
CMD ["sh", "-c", "python -m src.train_global_models && uvicorn src.api:app --host 0.0.0.0 --port ${PORT}"]