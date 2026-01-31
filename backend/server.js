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
        console.log(pythonResponse.data);

        // Return the AI's answer back to Android
        res.json(pythonResponse.data);
    } catch (error) {
        console.error("Error talking to Python AI:", error.message);
        res.status(500).json({ error: "Failed to allocate seats" });
    }
});

app.get('/bookings', async (req, res) => {
    try {
        const bookings = await Booking.find({}).sort({ createdAt: -1 });
        res.json(bookings);
    } catch (err) {
        console.error("Fetch bookings failed:", err);
        res.status(500).json([]);
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

// AI calls this to download training data
app.get('/feedback', async (req, res) => {
    try {
        console.log("Python AI requesting training data...");

        // 1. Fetch all history records
        const allHistory = await History.find({});

        // 2. Format for "Seat Type" Model
        // (Maps raw history to features needed for seat classification)
        const seatTypeData = allHistory.map(h => {
            // Reconstruct features based on what we saved.
            // Note: Since History schema is simple, we might infer some defaults
            // or you might need to enrich the History schema later.
            // For now, we return basic features available.
            return [
                {
                    "is_priority": 0, // Placeholder if not in DB
                    "is_female": 0,   // Placeholder
                    "is_in_group": 0, // Placeholder
                    "age": 30,        // Placeholder default
                    "norm_row": h.norm_row,
                    "norm_col": h.norm_col,
                    "disability": "None" // Placeholder
                },
                h.feedback_score >= 4 ? "Standard" : "Priority" // Infer label from score
            ];
        });

        // 3. Format for "Penalty" Model
        const penaltyData = allHistory.map(h => {
            return [
                {
                    "is_priority": 0,
                    "is_female": 0,
                    "is_in_group": 0,
                    "age": 30,
                    "norm_row": h.norm_row,
                    "norm_col": h.norm_col,
                    "group_distance": 0,
                    "disability": "None"
                },
                h.feedback_score // The target value
            ];
        });

        // 4. Return the structure Python expects
        res.json({
            seattype: seatTypeData,
            penalty: penaltyData
        });

    } catch (err) {
        console.error("Failed to serve training data:", err);
        // Return empty structure so Python falls back to synthetic data instead of crashing
        res.json({ seattype: [], penalty: [] });
    }
});

// AI calls this to SAVE the final allocation
app.post('/allocations', async (req, res) => {
    try {
        console.log("3. Python AI asked to save allocation for Trip:", req.body.tripId);

        const newBooking = new Booking(req.body);
        await newBooking.save();

        res.json({ status: "stored", trip_id: req.body.tripId });
    } catch (err) {
        console.error("Save failed:", err);
        res.status(500).json({ status: "error" });
    }
});

// --- FEEDBACK ENDPOINT ---
// App sends: { passengerId, rating, seatLabel, totalRows, totalCols }
app.post('/feedback', async (req, res) => {
    try {
        const { passengerId, rating, seatLabel, totalRows, totalCols } = req.body;
        
        // 1. Parse Seat Label (e.g., "1A") back to coordinates
        // Assuming format "1A", "10B"
        const rowPart = seatLabel.match(/\d+/)[0]; // "1"
        const colPart = seatLabel.match(/[A-Z]+/)[0]; // "A"
        
        const row = parseInt(rowPart) - 1; 
        const col = colPart.charCodeAt(0) - 65; // 'A' -> 0, 'B' -> 1

        // 2. Normalize (0.0 to 1.0) for the ML model
        const norm_row = row / Math.max(totalRows - 1, 1);
        const norm_col = col / Math.max(totalCols - 1, 1);

        // 3. Save to History
        const newHistory = new History({
            passengerId: passengerId,
            norm_row: norm_row,
            norm_col: norm_col,
            feedback_score: parseInt(rating) // 1 to 5
        });

        await newHistory.save();
        console.log(`Feedback saved for ${passengerId}: Rating ${rating}`);
        res.json({ status: "success" });

    } catch (err) {
        console.error("Feedback error:", err);
        res.status(500).json({ error: "Failed to save feedback" });
    }
});

// --- Start Server ---
mongoose.connect(process.env.MONGO_URI)
    .then(() => {
        console.log("Connected to MongoDB");
        app.listen(PORT, () => console.log(`Node.js server running on port ${PORT}`));
    })
    .catch(err => console.error("MongoDB connection error:", err));
    