import { useState } from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  Typography,
  Alert,
  CircularProgress,
} from '@mui/material';
import { Upload as UploadIcon, Download as DownloadIcon } from '@mui/icons-material';
import axios from 'axios';
import type { UploadResponse } from '../types';

const FileUpload = () => {
  const [excelFile, setExcelFile] = useState<File | null>(null);
  const [jsonFile, setJsonFile] = useState<File | null>(null);
  const [message, setMessage] = useState<string>('');
  const [error, setError] = useState<string>('');
  const [loading, setLoading] = useState(false);
  const [downloadReady, setDownloadReady] = useState(false);

  const handleExcelChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file && file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet') {
      setExcelFile(file);
      setError('');
    } else {
      setError('Please upload a valid Excel (.xlsx) file');
      setExcelFile(null);
    }
  };

  const handleJsonChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file && file.type === 'application/json') {
      setJsonFile(file);
      setError('');
    } else {
      setError('Please upload a valid JSON file');
      setJsonFile(null);
    }
  };

  const handleUpload = async () => {
    if (!excelFile || !jsonFile) {
      setError('Please upload both Excel and JSON files');
      return;
    }

    setLoading(true);
    setError('');
    setMessage('');

    const formData = new FormData();
    formData.append('excelFile', excelFile);
    formData.append('jsonFile', jsonFile);

    try {
      const response = await axios.post<UploadResponse>('http://localhost:8080/api/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      if (response.data.success) {
        setMessage(response.data.message);
        setDownloadReady(true);
      } else {
        setError(response.data.message);
      }
    } catch (err) {
      setError('Error uploading files. Please try again.');
      console.error('Upload error:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleDownload = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/download', {
        responseType: 'blob',
      });

      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'updated_stores.json');
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      setError('Error downloading file. Please try again.');
      console.error('Download error:', err);
    }
  };

  return (
    <Box sx={{ maxWidth: 600, mx: 'auto', mt: 4, p: 2 }}>
      <Card>
        <CardContent>
          <Typography variant="h5" component="h1" gutterBottom>
            Excel to JSON Converter
          </Typography>

          <Box sx={{ mb: 3 }}>
            <input
              accept=".xlsx"
              style={{ display: 'none' }}
              id="excel-file"
              type="file"
              onChange={handleExcelChange}
            />
            <label htmlFor="excel-file">
              <Button
                variant="contained"
                component="span"
                startIcon={<UploadIcon />}
                sx={{ mr: 2 }}
              >
                Upload Excel
              </Button>
            </label>
            {excelFile && (
              <Typography variant="body2" color="text.secondary">
                Selected: {excelFile.name}
              </Typography>
            )}
          </Box>

          <Box sx={{ mb: 3 }}>
            <input
              accept=".json"
              style={{ display: 'none' }}
              id="json-file"
              type="file"
              onChange={handleJsonChange}
            />
            <label htmlFor="json-file">
              <Button
                variant="contained"
                component="span"
                startIcon={<UploadIcon />}
                sx={{ mr: 2 }}
              >
                Upload JSON
              </Button>
            </label>
            {jsonFile && (
              <Typography variant="body2" color="text.secondary">
                Selected: {jsonFile.name}
              </Typography>
            )}
          </Box>

          <Box sx={{ mb: 3 }}>
            <Button
              variant="contained"
              color="primary"
              onClick={handleUpload}
              disabled={!excelFile || !jsonFile || loading}
              sx={{ mr: 2 }}
            >
              {loading ? <CircularProgress size={24} /> : 'Process Files'}
            </Button>

            {downloadReady && (
              <Button
                variant="contained"
                color="secondary"
                onClick={handleDownload}
                startIcon={<DownloadIcon />}
              >
                Download JSON
              </Button>
            )}
          </Box>

          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          {message && (
            <Alert severity="success" sx={{ mb: 2 }}>
              {message}
            </Alert>
          )}
        </CardContent>
      </Card>
    </Box>
  );
};

export default FileUpload; 