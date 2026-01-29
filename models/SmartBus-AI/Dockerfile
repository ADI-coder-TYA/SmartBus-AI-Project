# Use an official Python runtime as a parent image
FROM python:3.10-slim

# Set the working directory in the container
WORKDIR /app

# Copy the requirements file into the container
COPY requirements.txt .

# Install any needed packages specified in requirements.txt
# We upgrade pip first to avoid installation issues
RUN pip install --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

# Copy the rest of the current directory contents into the container
COPY . .

# Expose the port the app runs on (Render sets the PORT env var automatically)
ENV PORT=8000

# Run uvicorn server
# We use shell form to properly expand the PORT variable
CMD ["sh", "-c", "uvicorn src.api:app --host 0.0.0.0 --port ${PORT}"]
