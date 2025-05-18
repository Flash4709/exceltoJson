import React from 'react';
import FileUpload from './components/FileUpload';
import './App.css';

function App() {
  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-4xl mx-auto px-4">
        <header className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Excel to JSON Converter</h1>
          <p className="mt-2 text-gray-600">
            Upload an Excel file and a JSON template to convert and update your data
          </p>
        </header>
        <main>
          <FileUpload />
        </main>
      </div>
    </div>
  );
}

export default App;
