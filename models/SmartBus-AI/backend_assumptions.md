**GET /histories/{passengerId}**

[
  {"norm_row": 0.8, "norm_col": 0.0, "feedback_score": 4.8},
  {"norm_row": 0.9, "norm_col": 0.0, "feedback_score": 4.9},
  ...
]<br>
*Deliver normalized coords (bus-agnostic):*<br>
*norm_row = seat_row / (rows-1) (0 if rows==1)*<br>
*norm_col = seat_col / (cols-1) (0 if cols==1)*<br>
*Return at least 3 rows to enable a personal model; fewer rows are fine (we just skip personalization).*

---
**POST /allocations**<br>
*Request*<br>
{
  "tripId": "T-2025-08-23-123",
  "vehicle": {"rows": 12, "columns": 5, "vehicleType": "AC Seater"},
  "assignments": [
    {
      "passengerId": "uuid-2002",
      "groupId": "grpA",
      "seatId": "2A",
      "groupDistance": 0,
      "seatType": "window",
      "normRow": 0.09,
      "normCol": 0.0,
      "explanation": "..."
    }
  ]
}<br>
*Response*

{"status":"stored","tripId":"T-2025-08-23-123"}

---
**GET /feedback?since=YYYY-MM-DD**<br>
*Response*

{
  "seattype": [
    [
      {
        "is_priority": 1,
        "is_female": 0,
        "is_in_group": 0,
        "age": 72,
        "norm_row": 0.0,
        "norm_col": 0.0,
        "disability": "Wheelchair"
      },
      "front"
    ]
  ],
  "penalty": [
    [
      {
        "is_priority": 0,
        "is_female": 1,
        "is_in_group": 1,
        "age": 25,
        "norm_row": 0.8,
        "norm_col": 0.0,
        "disability": "None",
        "group_distance": 3
      },
      3.5
    ]
  ]
}
