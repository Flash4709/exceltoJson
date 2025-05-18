# Excel to JSON Converter

This application allows users to upload an Excel file and a JSON file, and updates the JSON file's content based on the Excel data.

## Project Structure

```
.
├── frontend/           # React frontend application
└── backend/           # Spring Boot backend application
```

## Frontend Setup

The frontend is built with React and Vite. To set it up:

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm run dev
   ```

The frontend will be available at `http://localhost:5173`

## Backend Setup

The backend is built with Spring Boot. To set it up:

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Build the project using Maven:
   ```bash
   ./mvnw clean install
   ```

3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

The backend will be available at `http://localhost:8080`

## Features

- Upload Excel (.xlsx) and JSON files
- Convert Excel data to JSON format
- Download the updated JSON file
- File type validation
- Error handling and user feedback

## API Endpoints

- `POST /api/upload`: Upload Excel and JSON files
- `GET /api/download`: Download the processed JSON file

## Technologies Used

### Frontend
- React
- Vite
- Axios
- Material-UI

### Backend
- Spring Boot
- Apache POI
- Jackson 