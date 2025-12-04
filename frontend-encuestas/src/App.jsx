import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import SurveyPage from './pages/SurveyPage';
import './App.css';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/q/:idEncuesta/:idLead" element={<SurveyPage />} />
        <Route path="*" element={
          <div style={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            height: '100vh',
            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
            color: 'white',
            textAlign: 'center',
            padding: '2rem'
          }}>
            <div>
              <h1>📋 Sistema de Encuestas</h1>
              <p>Por favor, utiliza un enlace válido de encuesta para continuar.</p>
              <p style={{ fontSize: '0.9rem', marginTop: '1rem', opacity: 0.8 }}>
                Formato: /q/[idEncuesta]/[idLead]
              </p>
            </div>
          </div>
        } />
      </Routes>
    </Router>
  );
}

export default App;

