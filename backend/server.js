// server.js
require('dotenv').config();
const express = require('express');
const mongoose = require('mongoose');
const axios = require('axios');
const cors = require('cors');

const app = express();
app.use(express.json());
app.use(cors());

// --- Configuration ---
const PORT = process.env.PORT || 3000;
const PYTHON_SERVICE_URL = process.env.PYTHON_SERVICE_URL; // e.g., https://smartbus-model-api.onrender.com

// --- MongoDB Schemas ---
const historySchema = new mongoose.Schema({
    passengerId: String,
    norm_row: Number,
    norm_col: Number,
    feedback_score: Number
});
const History = mongoose.model('History', historySchema);

const bookingSchema = new mongoose.Schema({
    tripId: String,
    vehicle: Object,
    assignments: Array,
    createdAt: { type: Date, default: Date.now }
});
const Booking = mongoose.model('Booking', bookingSchema);

// --- 1. Endpoints for Android App ---

// Health check
app.get('/health', (req, res) => {
    res.json({ status: "Node.js Backend is running" });
});

// The Main Booking Endpoint (Called by Android)
app.post('/allocate', async (req, res) => {
    try {
        console.log("1. Received booking request from Android");

        // Forward the request to the Python AI Model
        const pythonResponse = await axios.post(`${PYTHON_SERVICE_URL}/allocate`, req.body);

        console.log("2. Got response from Python AI");

        // Return the AI's answer back to Android
        res.json(pythonResponse.data);
    } catch (error) {
        console.error("Error talking to Python AI:", error.message);
        res.status(500).json({ error: "Failed to allocate seats" });
    }
});


// --- 2. Endpoints for Python AI Service (The "Callback" hooks) ---

// AI calls this to learn about a passenger
app.get('/histories/:passengerId', async (req, res) => {
    try {
        const history = await History.find({ passengerId: req.params.passengerId });
        // Return only the fields the AI needs
        const cleanHistory = history.map(h => ({
            norm_row: h.norm_row,
            norm_col: h.norm_col,
            feedback_score: h.feedback_score
        }));
        res.json(cleanHistory);
    } catch (err) {
        res.status(500).json([]);
    }
});

// AI calls this to SAVE the final allocation
app.post('/allocations', async (req, res) => {
    try {
        console.log("3. Python AI asked to save allocation for Trip:", req.body.tripId);

        const newBooking = new Booking(req.body);
        await newBooking.save();

        res.json({ status: "stored", tripId: req.body.tripId });
    } catch (err) {
        console.error("Save failed:", err);
        res.status(500).json({ status: "error" });
    }
});

// AI calls this to get data for re-training
app.get('/feedback', async (req, res) => {
    // For now, return empty data structures so the AI doesn't crash.
    // You can implement complex aggregation here later.
    res.json({
        seattype: [],
        penalty: []
    });
});

// --- Start Server ---
mongoose.connect(process.env.MONGO_URI)
    .then(() => {
        console.log("Connected to MongoDB");
        app.listen(PORT, () => console.log(`Node.js server running on port ${PORT}`));
    })
    .catch(err => console.error("MongoDB connection error:", err));