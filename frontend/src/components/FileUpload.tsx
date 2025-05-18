import React, { useState } from 'react';
import axios from 'axios';

const FileUpload: React.FC = () => {
    const [excelFile, setExcelFile] = useState<File | null>(null);
    const [jsonFile, setJsonFile] = useState<File | null>(null);
    const [message, setMessage] = useState<{ text: string; type: 'success' | 'error' } | null>(null);
    const [isLoading, setIsLoading] = useState(false);

    const handleExcelChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const file = event.target.files?.[0];
        if (file && file.name.endsWith('.xlsx')) {
            setExcelFile(file);
            setMessage(null);
        } else {
            setMessage({ text: 'Please select a valid Excel (.xlsx) file', type: 'error' });
        }
    };

    const handleJsonChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const file = event.target.files?.[0];
        if (file && file.name.endsWith('.json')) {
            setJsonFile(file);
            setMessage(null);
        } else {
            setMessage({ text: 'Please select a valid JSON (.json) file', type: 'error' });
        }
    };

    const handleSubmit = async (event: React.FormEvent) => {
        event.preventDefault();
        if (!excelFile || !jsonFile) {
            setMessage({ text: 'Please select both Excel and JSON files', type: 'error' });
            return;
        }

        setIsLoading(true);
        const formData = new FormData();
        formData.append('excelFile', excelFile);
        formData.append('jsonFile', jsonFile);

        try {
            const response = await axios.post('http://localhost:8080/api/upload', formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            });
            setMessage({ text: 'Files processed successfully!', type: 'success' });
            // Enable download button
            setExcelFile(null);
            setJsonFile(null);
        } catch (error: any) {
            setMessage({
                text: error.response?.data?.message || 'Error processing files. Please try again.',
                type: 'error'
            });
        } finally {
            setIsLoading(false);
        }
    };

    const handleDownload = async () => {
        try {
            const response = await axios.get('http://localhost:8080/api/download', {
                responseType: 'blob'
            });
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', 'updated.json');
            document.body.appendChild(link);
            link.click();
            link.remove();
        } catch (error) {
            setMessage({
                text: 'Error downloading file. Please try again.',
                type: 'error'
            });
        }
    };

    return (
        <div className="max-w-2xl mx-auto p-6">
            <form onSubmit={handleSubmit} className="space-y-6">
                <div className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700">
                            Excel File (.xlsx)
                        </label>
                        <input
                            type="file"
                            accept=".xlsx"
                            onChange={handleExcelChange}
                            className="mt-1 block w-full text-sm text-gray-500
                                file:mr-4 file:py-2 file:px-4
                                file:rounded-md file:border-0
                                file:text-sm file:font-semibold
                                file:bg-blue-50 file:text-blue-700
                                hover:file:bg-blue-100"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700">
                            JSON File (.json)
                        </label>
                        <input
                            type="file"
                            accept=".json"
                            onChange={handleJsonChange}
                            className="mt-1 block w-full text-sm text-gray-500
                                file:mr-4 file:py-2 file:px-4
                                file:rounded-md file:border-0
                                file:text-sm file:font-semibold
                                file:bg-blue-50 file:text-blue-700
                                hover:file:bg-blue-100"
                        />
                    </div>
                </div>

                {message && (
                    <div className={`p-4 rounded-md ${
                        message.type === 'success' ? 'bg-green-50 text-green-700' : 'bg-red-50 text-red-700'
                    }`}>
                        {message.text}
                    </div>
                )}

                <div className="flex space-x-4">
                    <button
                        type="submit"
                        disabled={isLoading || !excelFile || !jsonFile}
                        className={`px-4 py-2 rounded-md text-white font-medium
                            ${isLoading || !excelFile || !jsonFile
                                ? 'bg-gray-400 cursor-not-allowed'
                                : 'bg-blue-600 hover:bg-blue-700'
                            }`}
                    >
                        {isLoading ? 'Processing...' : 'Upload Files'}
                    </button>
                    {message?.type === 'success' && (
                        <button
                            type="button"
                            onClick={handleDownload}
                            className="px-4 py-2 rounded-md text-white font-medium bg-green-600 hover:bg-green-700"
                        >
                            Download Updated JSON
                        </button>
                    )}
                </div>
            </form>
        </div>
    );
};

export default FileUpload; 