import { ThemeProvider, createTheme, CssBaseline, Container } from '@mui/material';
import FileUpload from './components/FileUpload';

const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
  },
});

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Container>
        <FileUpload />
      </Container>
    </ThemeProvider>
  );
}

export default App;
