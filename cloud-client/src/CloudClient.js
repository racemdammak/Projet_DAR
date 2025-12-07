import React, { useState, useEffect, useCallback } from "react";
import { useDropzone } from "react-dropzone";
import { ToastContainer, toast } from "react-toastify";
import ReactMarkdown from "react-markdown";
import "react-toastify/dist/ReactToastify.css";
import "./CloudClient.css";

function CloudClient() {
  const [filesList, setFilesList] = useState([]);
  const [selectedFile, setSelectedFile] = useState(null);
  const [summary, setSummary] = useState("");
  const [isLoadingSummary, setIsLoadingSummary] = useState(false);
  const [showSummaryModal, setShowSummaryModal] = useState(false);
  const [serverConnected, setServerConnected] = useState(true);
  const SERVER_URL = "http://127.0.0.1:4567";
  const AI_SERVER_URL = "http://127.0.0.1:8000"; // FastAPI server

  // Drag & Drop
  const onDrop = useCallback(async (acceptedFiles) => {
    for (let file of acceptedFiles) {
      const formData = new FormData();
      formData.append("file", file);           // correspond au name "file" côté Spark
      formData.append("filename", file.name);  // nom du fichier

      try {
        const res = await fetch(`${SERVER_URL}/upload?filename=${encodeURIComponent(file.name)}`, {
          method: "POST",
          body: formData,
          // Ne pas définir Content-Type, le navigateur le fera automatiquement avec la boundary
        });

        if (!res.ok) {
          const errorText = await res.text();
          throw new Error(errorText || `Erreur upload: ${file.name}`);
        }

        const text = await res.text();
        toast.success(text);
        setServerConnected(true);
      } catch (err) {
        console.error(err);
        setServerConnected(false);
        if (err.name === 'TypeError' && (err.message.includes('Failed to fetch') || err.message.includes('ERR_CONNECTION_RESET'))) {
          toast.error(`Serveur non accessible. Assurez-vous que le serveur REST Java est démarré sur le port 4567.`);
        } else {
          toast.error(`Erreur upload: ${err.message || file.name}`);
        }
      }
    }
    fetchFiles(); // actualiser la liste après upload
  }, []);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({ onDrop });

  // Récupérer la liste des fichiers
  const fetchFiles = async () => {
    try {
      const res = await fetch(`${SERVER_URL}/list`);
      if (!res.ok) throw new Error("Erreur serveur");
      const text = await res.text();
      // Convertir la string JSON en tableau JS
      const parsedFiles = JSON.parse(text.replace(/'/g, '"'));
      setFilesList(parsedFiles);
      setServerConnected(true);
    } catch (err) {
      console.error(err);
      setServerConnected(false);
      if (err.name === 'TypeError' && err.message.includes('Failed to fetch')) {
        // Ne pas afficher de toast à chaque fois pour éviter le spam
        console.warn("Serveur REST non accessible");
      } else {
        toast.error("Erreur récupération liste fichiers");
      }
    }
  };

  // Supprimer un fichier
  const deleteFile = async (filename) => {
    try {
      const res = await fetch(`${SERVER_URL}/delete/${filename}`, {
        method: "DELETE",
      });
      if (!res.ok) throw new Error("Erreur suppression");
      toast.success(`Supprimé: ${filename}`);
      fetchFiles(); // Met à jour la liste
    } catch (err) {
      console.error(err);
      toast.error(`Erreur suppression: ${filename}`);
    }
  };


  // Télécharger un fichier
  const downloadFile = async (filename) => {
    try {
      const res = await fetch(`${SERVER_URL}/download/${filename}`);
      if (!res.ok) throw new Error("Fichier non trouvé");

      const blob = await res.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = filename;
      a.click();
      window.URL.revokeObjectURL(url);

      toast.success(`Téléchargé: ${filename}`);
    } catch (err) {
      console.error(err);
      toast.error(`Erreur téléchargement: ${filename}`);
    }
  };

  // Résumer un fichier avec IA
  const summarizeFile = async (filename) => {
    setIsLoadingSummary(true);
    setSelectedFile(filename);
    setShowSummaryModal(true);
    setSummary("");

    try {
      const res = await fetch(`${AI_SERVER_URL}/summarize`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ filename: filename }),
      });

      if (!res.ok) {
        const errorData = await res.json();
        throw new Error(errorData.detail || "Erreur lors du résumé");
      }

      const data = await res.json();
      setSummary(data.summary || "Aucun résumé disponible");
      toast.success("Résumé généré avec succès");
    } catch (err) {
      console.error(err);
      setSummary(`Erreur: ${err.message}`);
      toast.error(`Erreur lors du résumé: ${err.message}`);
    } finally {
      setIsLoadingSummary(false);
    }
  };

  // Fermer le modal de résumé
  const closeSummaryModal = () => {
    setShowSummaryModal(false);
    setSummary("");
    setSelectedFile(null);
  };

  useEffect(() => {
    fetchFiles();
  }, []);

  return (
    <div className="cloud-client">
      <div className="header">
        <h1 className="main-title">
          <span className="title-gradient">MiniCloud</span>
          <span className="title-subtitle">Client</span>
        </h1>
        <div className="header-accent"></div>
        {!serverConnected && (
          <div className="server-status-error">
            ⚠️ Serveur REST non accessible. Vérifiez que le serveur Java est démarré sur le port 4567.
          </div>
        )}
      </div>

      {/* Drag & Drop */}
      <div
        {...getRootProps()}
        className={`upload-area ${isDragActive ? 'active' : ''}`}
      >
        <input {...getInputProps()} />
        <div className="upload-icon">📁</div>
        <p className="upload-text">
          {isDragActive ? (
            "Déposez vos fichiers ici ..."
          ) : (
            "Glissez-déposez des fichiers ici ou cliquez pour sélectionner"
          )}
        </p>
      </div>

      {/* Liste des fichiers */}
      <div className="files-section">
        <h2 className="section-title">
          <span className="section-title-line"></span>
          <span>Fichiers disponibles</span>
          <span className="section-title-line"></span>
        </h2>
        {filesList.length === 0 ? (
          <p className="no-files">Aucun fichier trouvé.</p>
        ) : (
          <div className="files-grid">
            {filesList.map((f) => (
              <div key={f} className="file-card">
                <div className="file-icon">📄</div>
                <div className="file-name">{f}</div>
                <div className="file-actions">
                  <button 
                    className="ai-summary-btn" 
                    onClick={() => summarizeFile(f)}
                    title="Résumer avec IA"
                  >
                    <span className="ai-icon">🤖</span>
                    Résumer avec IA
                  </button>
                  <button className="download-btn" onClick={() => downloadFile(f)}>
                    Télécharger
                  </button>
                  <button className="delete-btn" onClick={() => deleteFile(f)}>
                    Supprimer
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Modal de résumé IA */}
      {showSummaryModal && (
        <div className="summary-modal-overlay" onClick={closeSummaryModal}>
          <div className="summary-modal" onClick={(e) => e.stopPropagation()}>
            <div className="summary-modal-header">
              <h3 className="summary-title">
                <span className="ai-icon-large">🤖</span>
                Résumé IA - {selectedFile}
              </h3>
              <button className="close-btn" onClick={closeSummaryModal}>
                ✕
              </button>
            </div>
            <div className="summary-content">
              {isLoadingSummary ? (
                <div className="loading-container">
                  <div className="loading-spinner"></div>
                  <p>Génération du résumé en cours...</p>
                </div>
              ) : (
                <div className="summary-text">
                  <ReactMarkdown>{summary}</ReactMarkdown>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      <ToastContainer 
        position="top-right" 
        autoClose={3000}
        theme="dark"
        toastClassName="futuristic-toast"
      />
    </div>
  );
}

export default CloudClient;
