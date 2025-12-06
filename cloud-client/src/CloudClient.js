import React, { useState, useEffect, useCallback } from "react";
import { useDropzone } from "react-dropzone";
import { ToastContainer, toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import "./CloudClient.css";

function CloudClient() {
  const [filesList, setFilesList] = useState([]);
  const SERVER_URL = "http://127.0.0.1:4567";

  // Drag & Drop
  const onDrop = useCallback(async (acceptedFiles) => {
    for (let file of acceptedFiles) {
      const formData = new FormData();
      formData.append("file", file);           // correspond au name "file" côté Spark
      formData.append("filename", file.name);  // nom du fichier

      try {
        const res = await fetch(`${SERVER_URL}/upload`, {
          method: "POST",
          body: formData,
        });

        if (!res.ok) throw new Error(`Erreur upload: ${file.name}`);

        const text = await res.text();
        toast.success(text);
      } catch (err) {
        console.error(err);
        toast.error(`Erreur upload: ${file.name}`);
      }
    }
    fetchFiles(); // actualiser la liste après upload
  }, []);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({ onDrop });

  // Récupérer la liste des fichiers
  const fetchFiles = async () => {
    try {
      const res = await fetch(`${SERVER_URL}/list`);
      const text = await res.text();
      // Convertir la string JSON en tableau JS
      const parsedFiles = JSON.parse(text.replace(/'/g, '"'));
      setFilesList(parsedFiles);
    } catch (err) {
      console.error(err);
      toast.error("Erreur récupération liste fichiers");
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

  useEffect(() => {
    fetchFiles();
  }, []);

  return (
    <div className="cloud-client">
      <div className="header">
        <h1>MiniCloud Client</h1>
      </div>

      {/* Drag & Drop */}
      <div
        {...getRootProps()}
        className={`upload-area ${isDragActive ? 'active' : ''}`}
      >
        <input {...getInputProps()} />
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
        <h2>Fichiers disponibles</h2>
        {filesList.length === 0 ? (
          <p className="no-files">Aucun fichier trouvé.</p>
        ) : (
          <div className="files-grid">
            {filesList.map((f) => (
              <div key={f} className="file-card">
                <div className="file-name">{f}</div>
                <button className="download-btn" onClick={() => downloadFile(f)}>
                  Télécharger
                </button>
                <button className="delete-btn" onClick={() => deleteFile(f)}>
                  Supprimer
                </button>
                <span style={{ fontSize: "0.8em", color: "gray", marginTop: "5px" }}>
                  (path serveur: /{f})
                </span>
              </div>
            ))}
          </div>
        )}
      </div>

      <ToastContainer position="top-right" autoClose={3000} />
    </div>
  );
}

export default CloudClient;
