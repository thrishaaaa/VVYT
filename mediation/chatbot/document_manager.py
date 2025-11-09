#!/usr/bin/env python3
"""
Document Manager for Legal Mediation Chatbot
Handles file uploads, storage, and management
"""

import os
import json
import logging
import uuid
from datetime import datetime
from pathlib import Path
import shutil
from typing import Dict, List, Optional
import mimetypes

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class DocumentManager:
    """Manages document uploads and storage for legal cases"""
    
    def __init__(self, base_storage_path: str = "documents"):
        self.base_storage_path = Path(base_storage_path)
        self.base_storage_path.mkdir(exist_ok=True)
        self.metadata_file = self.base_storage_path / "metadata.json"
        self.load_metadata()
        
        # Allowed file types for security
        self.allowed_extensions = {
            '.pdf', '.doc', '.docx', '.txt', '.jpg', '.jpeg', '.png', 
            '.gif', '.bmp', '.tiff', '.csv', '.xls', '.xlsx'
        }
        
        # Maximum file size (10MB)
        self.max_file_size = 10 * 1024 * 1024
    
    def load_metadata(self):
        """Load existing metadata or create new"""
        if self.metadata_file.exists():
            try:
                with open(self.metadata_file, 'r') as f:
                    self.metadata = json.load(f)
            except Exception as e:
                logger.error(f"Error loading metadata: {e}")
                self.metadata = {}
        else:
            self.metadata = {}
    
    def save_metadata(self):
        """Save metadata to file"""
        try:
            with open(self.metadata_file, 'w') as f:
                json.dump(self.metadata, f, indent=2, default=str)
        except Exception as e:
            logger.error(f"Error saving metadata: {e}")
    
    def create_case_folder(self, case_id: str, case_title: str) -> str:
        """Create a folder for a new case"""
        case_folder = self.base_storage_path / case_id
        case_folder.mkdir(exist_ok=True)
        
        # Initialize case metadata
        if case_id not in self.metadata:
            self.metadata[case_id] = {
                'case_title': case_title,
                'created_at': datetime.now().isoformat(),
                'documents': {},
                'total_size': 0,
                'document_count': 0
            }
            self.save_metadata()
        
        return str(case_folder)
    
    def upload_document(self, case_id: str, file_data: bytes, filename: str, 
                       description: str = "") -> Dict:
        """Upload a document for a case"""
        try:
            # Validate file extension
            file_ext = Path(filename).suffix.lower()
            if file_ext not in self.allowed_extensions:
                raise ValueError(f"File type {file_ext} not allowed")
            
            # Check file size
            if len(file_data) > self.max_file_size:
                raise ValueError(f"File size exceeds maximum limit of {self.max_file_size // (1024*1024)}MB")
            
            # Create case folder if it doesn't exist
            case_folder = self.base_storage_path / case_id
            case_folder.mkdir(exist_ok=True)
            
            # Generate unique filename
            unique_filename = f"{uuid.uuid4().hex}_{filename}"
            file_path = case_folder / unique_filename
            
            # Save file
            with open(file_path, 'wb') as f:
                f.write(file_data)
            
            # Get file info
            file_size = len(file_data)
            mime_type = mimetypes.guess_type(filename)[0] or 'application/octet-stream'
            
            # Create document record
            doc_id = str(uuid.uuid4())
            document_info = {
                'id': doc_id,
                'original_filename': filename,
                'stored_filename': unique_filename,
                'file_path': str(file_path),
                'file_size': file_size,
                'mime_type': mime_type,
                'description': description,
                'uploaded_at': datetime.now().isoformat(),
                'status': 'uploaded'
            }
            
            # Update metadata
            if case_id not in self.metadata:
                self.metadata[case_id] = {
                    'case_title': f"Case {case_id}",
                    'created_at': datetime.now().isoformat(),
                    'documents': {},
                    'total_size': 0,
                    'document_count': 0
                }
            
            self.metadata[case_id]['documents'][doc_id] = document_info
            self.metadata[case_id]['total_size'] += file_size
            self.metadata[case_id]['document_count'] += 1
            
            self.save_metadata()
            
            logger.info(f"Document uploaded successfully: {filename} for case {case_id}")
            
            return {
                'success': True,
                'document_id': doc_id,
                'filename': filename,
                'file_size': file_size,
                'message': 'Document uploaded successfully'
            }
            
        except Exception as e:
            logger.error(f"Error uploading document: {e}")
            return {
                'success': False,
                'error': str(e),
                'message': 'Failed to upload document'
            }
    
    def get_case_documents(self, case_id: str) -> List[Dict]:
        """Get all documents for a case"""
        if case_id in self.metadata:
            return list(self.metadata[case_id]['documents'].values())
        return []
    
    def get_document_info(self, case_id: str, document_id: str) -> Optional[Dict]:
        """Get information about a specific document"""
        if (case_id in self.metadata and 
            document_id in self.metadata[case_id]['documents']):
            return self.metadata[case_id]['documents'][document_id]
        return None
    
    def download_document(self, case_id: str, document_id: str) -> Optional[bytes]:
        """Download a document file"""
        doc_info = self.get_document_info(case_id, document_id)
        if doc_info:
            try:
                file_path = Path(doc_info['file_path'])
                if file_path.exists():
                    with open(file_path, 'rb') as f:
                        return f.read()
            except Exception as e:
                logger.error(f"Error downloading document: {e}")
        return None
    
    def delete_document(self, case_id: str, document_id: str) -> bool:
        """Delete a document"""
        try:
            doc_info = self.get_document_info(case_id, document_id)
            if doc_info:
                # Remove file
                file_path = Path(doc_info['file_path'])
                if file_path.exists():
                    file_path.unlink()
                
                # Update metadata
                file_size = doc_info['file_size']
                self.metadata[case_id]['total_size'] -= file_size
                self.metadata[case_id]['document_count'] -= 1
                del self.metadata[case_id]['documents'][document_id]
                
                self.save_metadata()
                logger.info(f"Document deleted: {document_id}")
                return True
        except Exception as e:
            logger.error(f"Error deleting document: {e}")
        return False
    
    def get_case_summary(self, case_id: str) -> Dict:
        """Get summary information for a case"""
        if case_id in self.metadata:
            case_info = self.metadata[case_id]
            return {
                'case_id': case_id,
                'case_title': case_info['case_title'],
                'created_at': case_info['created_at'],
                'document_count': case_info['document_count'],
                'total_size': case_info['total_size'],
                'total_size_mb': round(case_info['total_size'] / (1024 * 1024), 2),
                'documents': list(case_info['documents'].values())
            }
        return {}
    
    def list_all_cases(self) -> List[Dict]:
        """List all cases with summary information"""
        cases = []
        for case_id, case_info in self.metadata.items():
            cases.append({
                'case_id': case_id,
                'case_title': case_info['case_title'],
                'created_at': case_info['created_at'],
                'document_count': case_info['document_count'],
                'total_size_mb': round(case_info['total_size'] / (1024 * 1024), 2)
            })
        return cases
    
    def cleanup_case(self, case_id: str) -> bool:
        """Remove all documents and metadata for a case"""
        try:
            if case_id in self.metadata:
                # Remove all files
                case_folder = self.base_storage_path / case_id
                if case_folder.exists():
                    shutil.rmtree(case_folder)
                
                # Remove metadata
                del self.metadata[case_id]
                self.save_metadata()
                
                logger.info(f"Case cleaned up: {case_id}")
                return True
        except Exception as e:
            logger.error(f"Error cleaning up case: {e}")
        return False
    
    def add_document_metadata(self, case_id: str, filename: str, document_type: str, description: str, file_size: int):
        """Add document metadata to the case"""
        try:
            # Load existing metadata
            metadata = self._load_metadata()
            
            # Find or create case metadata
            case_metadata = None
            for case in metadata.get('cases', []):
                if case['case_id'] == case_id:
                    case_metadata = case
                    break
            
            if not case_metadata:
                case_metadata = {
                    'case_id': case_id,
                    'case_title': filename.split('_')[0].replace('_', ' '),
                    'created_at': datetime.now().isoformat(),
                    'documents': [],
                    'total_size_bytes': 0
                }
                metadata['cases'].append(case_metadata)
            
            # Add document metadata
            document_metadata = {
                'document_id': str(uuid.uuid4()),
                'filename': filename,
                'document_type': document_type,
                'description': description,
                'file_size_bytes': file_size,
                'uploaded_at': datetime.now().isoformat()
            }
            
            case_metadata['documents'].append(document_metadata)
            case_metadata['total_size_bytes'] += file_size
            
            # Save updated metadata
            self._save_metadata(metadata)
            
        except Exception as e:
            logger.error(f"Error adding document metadata: {e}")
    
    def _remove_document_metadata(self, case_id: str, document_id: str):
        """Remove document metadata from a case"""
        try:
            metadata = self._load_metadata()
            
            for case in metadata.get('cases', []):
                if case['case_id'] == case_id:
                    # Find and remove the document
                    for doc in case['documents'][:]:
                        if document_id in doc.get('document_id', ''):
                            case['total_size_bytes'] -= doc.get('file_size_bytes', 0)
                            case['documents'].remove(doc)
                            break
                    break
            
            self._save_metadata(metadata)
            
        except Exception as e:
            logger.error(f"Error removing document metadata: {e}")
    
    def get_storage_stats(self) -> Dict:
        """Get overall storage statistics"""
        total_cases = len(self.metadata)
        total_documents = sum(case['document_count'] for case in self.metadata.values())
        total_size = sum(case['total_size'] for case in self.metadata.values())
        
        return {
            'total_cases': total_cases,
            'total_documents': total_documents,
            'total_size_bytes': total_size,
            'total_size_mb': round(total_size / (1024 * 1024), 2),
            'storage_path': str(self.base_storage_path)
        }

# Example usage
if __name__ == "__main__":
    doc_manager = DocumentManager()
    
    # Create a test case
    case_id = "test_case_001"
    case_title = "Property Boundary Dispute"
    
    print("Document Manager Test")
    print("=" * 30)
    
    # Create case folder
    doc_manager.create_case_folder(case_id, case_title)
    print(f"Created case folder for: {case_title}")
    
    # Get storage stats
    stats = doc_manager.get_storage_stats()
    print(f"Storage stats: {stats}")
    
    # List cases
    cases = doc_manager.list_all_cases()
    print(f"Cases: {cases}") 