#!/usr/bin/env python3
"""
Integrated Web UI for Legal Mediation Chatbot
Combines chat interface and document management
"""

import json
import logging
import uuid
from datetime import datetime
from http.server import HTTPServer, BaseHTTPRequestHandler
from urllib.parse import urlparse, parse_qs
import threading
import webbrowser
import os
from enhanced_chatbot import EnhancedLegalMediationChatbot
from document_manager import DocumentManager

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Initialize components
try:
    chatbot = EnhancedLegalMediationChatbot()
    doc_manager = DocumentManager()
    logger.info("All components initialized successfully")
except Exception as e:
    logger.error(f"Failed to initialize components: {e}")
    chatbot = None
    doc_manager = None

# Store chat sessions and case data
chat_sessions = {}
case_sessions = {}

class IntegratedChatbotHandler(BaseHTTPRequestHandler):
    """HTTP request handler for the integrated chatbot web interface"""
    
    def do_GET(self):
        """Handle GET requests"""
        parsed_path = urlparse(self.path)
        path = parsed_path.path
        
        if path == '/':
            # Main chat interface
            self.send_response(200)
            self.send_header('Content-type', 'text/html')
            self.end_headers()
            self.wfile.write(self.get_main_html().encode('utf-8'))
            
        elif path == '/documents':
            # Document management page
            self.send_response(200)
            self.send_header('Content-type', 'text/html')
            self.end_headers()
            self.wfile.write(self.get_documents_html().encode('utf-8'))
            
        elif path == '/api/chat/history':
            # Get chat history
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            
            query_params = parse_qs(parsed_path.query)
            session_id = query_params.get('session_id', [None])[0]
            
            if session_id and session_id in chat_sessions:
                response = {'status': 'success', 'messages': chat_sessions[session_id]}
            else:
                response = {'status': 'success', 'messages': []}
            
            self.wfile.write(json.dumps(response).encode('utf-8'))
            
        elif path == '/api/cases':
            # Get all cases
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            
            if doc_manager:
                cases = doc_manager.list_all_cases()
                response = {'status': 'success', 'cases': cases}
            else:
                response = {'status': 'error', 'message': 'Document manager not available'}
            
            self.wfile.write(json.dumps(response).encode('utf-8'))
            
        elif path == '/api/case/documents':
            # Get documents for a specific case
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            
            query_params = parse_qs(parsed_path.query)
            case_id = query_params.get('case_id', [None])[0]
            
            if doc_manager and case_id:
                documents = doc_manager.get_case_documents(case_id)
                response = {'status': 'success', 'documents': documents}
            else:
                response = {'status': 'error', 'message': 'Invalid case ID'}
            
            self.wfile.write(json.dumps(response).encode('utf-8'))
            
        elif path == '/api/health':
            # Health check
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            
            response = {
                'status': 'healthy',
                'chatbot_available': chatbot is not None,
                'document_manager_available': doc_manager is not None,
                'timestamp': datetime.now().isoformat()
            }
            
            self.wfile.write(json.dumps(response).encode('utf-8'))
            
        else:
            self.send_response(404)
            self.send_header('Content-type', 'text/plain')
            self.end_headers()
            self.wfile.write(b'Not Found')
    
    def do_POST(self):
        """Handle POST requests"""
        parsed_path = urlparse(self.path)
        path = parsed_path.path
        
        if path == '/api/chat':
            # Handle chat messages
            self.handle_chat()
            
        elif path == '/api/chat/clear':
            # Clear chat history
            self.handle_chat_clear()
            
        elif path == '/api/case/create':
            # Create new case
            self.handle_case_create()
            
        elif path == '/api/document/upload':
            # Upload document
            self.handle_document_upload()
            
        elif path == '/api/document/delete':
            # Delete document
            self.handle_document_delete()
            
        else:
            self.send_response(404)
            self.send_header('Content-type', 'text/plain')
            self.end_headers()
            self.wfile.write(b'Not Found')
    
    def handle_chat(self):
        """Handle chat message processing"""
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.end_headers()
        
        try:
            content_length = int(self.headers.get('Content-Length', 0))
            post_data = self.rfile.read(content_length)
            data = json.loads(post_data.decode('utf-8'))
            
            message = data.get('message', '').strip()
            session_id = data.get('session_id', str(uuid.uuid4()))
            
            if not message:
                response = {'error': 'Message is required', 'status': 'error'}
            else:
                # Store user message
                if session_id not in chat_sessions:
                    chat_sessions[session_id] = []
                
                user_message = {
                    'id': str(uuid.uuid4()),
                    'type': 'user',
                    'content': message,
                    'timestamp': datetime.now().isoformat()
                }
                chat_sessions[session_id].append(user_message)
                
                # Get chatbot response
                if chatbot:
                    try:
                        analysis = chatbot.analyze_case(message)
                        
                        # Format response with document upload link - improved formatting
                        response_content = f"""# 📋 **Case Analysis Results**

## 🏷️ **Case Category**
{analysis.category}

## ⚖️ **Recommended Resolution Path**
{analysis.resolution_path}

## 📄 **Required Documents**
{chr(10).join([f"• {doc}" for doc in analysis.documents_needed])}

## 💡 **Legal Guidance & Analysis**
{analysis.guidance}

## 🚀 **Recommended Next Steps**
{chr(10).join([f"• {step}" for step in analysis.next_steps])}

---
**📁 Document Management:** [Click here to upload your case documents](/documents)

💡 **Next Action:** After reviewing the analysis above, click the document management link to upload your case documents.""".strip()
                        
                        bot_message = {
                            'id': str(uuid.uuid4()),
                            'type': 'bot',
                            'content': response_content,
                            'analysis': {
                                'category': analysis.category,
                                'resolution_path': analysis.resolution_path,
                                'documents_needed': analysis.documents_needed,
                                'guidance': analysis.guidance,
                                'next_steps': analysis.next_steps
                            },
                            'timestamp': datetime.now().isoformat()
                        }
                        
                    except Exception as e:
                        logger.error(f"Error analyzing case: {e}")
                        response_content = f"""
I apologize, but I encountered an error while analyzing your case. Please try rephrasing your description or contact support if the issue persists.

Error: {str(e)}
                        """.strip()
                        
                        bot_message = {
                            'id': str(uuid.uuid4()),
                            'type': 'bot',
                            'content': response_content,
                            'error': True,
                            'timestamp': datetime.now().isoformat()
                        }
                    
                    # Store bot message
                    chat_sessions[session_id].append(bot_message)
                    
                    response = {
                        'status': 'success',
                        'response': bot_message,
                        'session_id': session_id
                    }
                else:
                    response = {'error': 'Chatbot not available', 'status': 'error'}
            
        except Exception as e:
            logger.error(f"Error in chat endpoint: {e}")
            response = {
                'error': 'Internal server error',
                'details': str(e),
                'status': 'error'
            }
        
        self.wfile.write(json.dumps(response).encode('utf-8'))
    
    def handle_chat_clear(self):
        """Handle chat history clearing"""
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.end_headers()
        
        try:
            content_length = int(self.headers.get('Content-Length', 0))
            post_data = self.rfile.read(content_length)
            data = json.loads(post_data.decode('utf-8'))
            
            session_id = data.get('session_id')
            if session_id and session_id in chat_sessions:
                chat_sessions[session_id] = []
            
            response = {'status': 'success', 'message': 'Chat history cleared'}
            
        except Exception as e:
            response = {'error': 'Failed to clear chat', 'status': 'error'}
        
        self.wfile.write(json.dumps(response).encode('utf-8'))
    
    def handle_case_create(self):
        """Handle case creation"""
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.end_headers()
        
        try:
            content_length = int(self.headers.get('Content-Length', 0))
            post_data = self.rfile.read(content_length)
            data = json.loads(post_data.decode('utf-8'))
            
            case_title = data.get('case_title', '').strip()
            
            if not case_title:
                response = {'error': 'Case title is required', 'status': 'error'}
            elif doc_manager:
                case_id = f"case_{uuid.uuid4().hex[:8]}"
                doc_manager.create_case_folder(case_id, case_title)
                
                response = {
                    'status': 'success',
                    'case_id': case_id,
                    'case_title': case_title,
                    'message': 'Case created successfully'
                }
            else:
                response = {'error': 'Document manager not available', 'status': 'error'}
            
        except Exception as e:
            logger.error(f"Error creating case: {e}")
            response = {'error': 'Failed to create case', 'status': 'error'}
        
        self.wfile.write(json.dumps(response).encode('utf-8'))
    
    def handle_document_upload(self):
        """Handle document upload with multipart form data"""
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.end_headers()
        
        try:
            # Parse multipart form data manually (more reliable than cgi)
            import tempfile
            import os
            
            # Get content type and boundary
            content_type = self.headers.get('Content-Type', '')
            if not content_type.startswith('multipart/form-data'):
                response = {'status': 'error', 'message': 'Invalid content type'}
                self.wfile.write(json.dumps(response).encode('utf-8'))
                return
            
            # Extract boundary from content type
            boundary = None
            for part in content_type.split(';'):
                part = part.strip()
                if part.startswith('boundary='):
                    boundary = part[9:].strip('"')
                    break
            
            if not boundary:
                response = {'status': 'error', 'message': 'No boundary found in content type'}
                self.wfile.write(json.dumps(response).encode('utf-8'))
                return
            
            # Read the entire request body
            content_length = int(self.headers.get('Content-Length', 0))
            if content_length == 0:
                response = {'status': 'error', 'message': 'No content received'}
                self.wfile.write(json.dumps(response).encode('utf-8'))
                return
            
            body = self.rfile.read(content_length)
            
            # Parse multipart data manually
            parts = body.split(b'--' + boundary.encode())
            form_data = {}
            file_data = None
            
            for part in parts:
                if part.strip() == b'' or part.strip() == b'--':
                    continue
                
                # Split part into headers and content
                if b'\r\n\r\n' in part:
                    headers, content = part.split(b'\r\n\r\n', 1)
                    content = content.rstrip(b'\r\n')
                    
                    # Parse headers to find field name and filename
                    field_name = None
                    filename = None
                    
                    for line in headers.decode().split('\r\n'):
                        if line.startswith('Content-Disposition:'):
                            for item in line.split(';'):
                                item = item.strip()
                                if item.startswith('name='):
                                    field_name = item[5:].strip('"')
                                elif item.startswith('filename='):
                                    filename = item[9:].strip('"')
                    
                    if field_name:
                        if filename:
                            # This is a file
                            file_data = {
                                'filename': filename,
                                'content': content
                            }
                        else:
                            # This is a form field
                            form_data[field_name] = content.decode('utf-8', errors='ignore').strip()
            
            # Extract form data from parsed multipart data
            case_title = form_data.get('caseTitle', '').strip()
            case_type = form_data.get('caseType', '').strip()
            document_description = form_data.get('documentDescription', '').strip()
            
            # Validate required fields
            if not case_title or not case_type or not file_data:
                response = {'status': 'error', 'message': 'Missing required fields'}
                self.wfile.write(json.dumps(response).encode('utf-8'))
                return
            
            # Validate file
            if not file_data or not file_data.get('filename'):
                response = {'status': 'error', 'message': 'No file selected'}
                self.wfile.write(json.dumps(response).encode('utf-8'))
                return
            
            # Check file size (10MB limit)
            file_size = len(file_data['content'])
            if file_size > 10 * 1024 * 1024:  # 10MB
                response = {'status': 'error', 'message': 'File size exceeds 10MB limit'}
                self.wfile.write(json.dumps(response).encode('utf-8'))
                return
            
            # Create case if it doesn't exist
            case_id = f"case_{uuid.uuid4().hex[:8]}"
            if doc_manager:
                doc_manager.create_case_folder(case_id, case_title)
                
                # Save the uploaded file
                file_extension = os.path.splitext(file_data['filename'])[1].lower()
                safe_filename = f"{case_type.replace(' ', '_')}_{uuid.uuid4().hex[:8]}{file_extension}"
                
                # Create documents directory if it doesn't exist
                case_dir = os.path.join('documents', case_id)
                os.makedirs(case_dir, exist_ok=True)
                
                # Save file
                file_path = os.path.join(case_dir, safe_filename)
                with open(file_path, 'wb') as f:
                    f.write(file_data['content'])
                
                # Update metadata
                doc_manager.add_document_metadata(case_id, safe_filename, case_type, document_description, file_size)
                
                response = {
                    'status': 'success',
                    'message': 'Document uploaded successfully',
                    'case_id': case_id,
                    'filename': safe_filename
                }
            else:
                response = {'status': 'error', 'message': 'Document manager not available'}
            
        except Exception as e:
            logger.error(f"Error uploading document: {e}")
            response = {'status': 'error', 'message': f'Upload failed: {str(e)}'}
        
        self.wfile.write(json.dumps(response).encode('utf-8'))
    
    def handle_document_delete(self):
        """Handle document deletion"""
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.end_headers()
        
        try:
            content_length = int(self.headers.get('Content-Length', 0))
            post_data = self.rfile.read(content_length)
            data = json.loads(post_data.decode('utf-8'))
            
            case_id = data.get('case_id')
            document_id = data.get('document_id')
            
            if doc_manager and case_id and document_id:
                success = doc_manager.delete_document(case_id, document_id)
                if success:
                    response = {'status': 'success', 'message': 'Document deleted'}
                else:
                    response = {'error': 'Failed to delete document', 'status': 'error'}
            else:
                response = {'error': 'Invalid parameters', 'status': 'error'}
            
        except Exception as e:
            logger.error(f"Error deleting document: {e}")
            response = {'error': 'Failed to delete document', 'status': 'error'}
        
        self.wfile.write(json.dumps(response).encode('utf-8'))
    
    def log_message(self, format, *args):
        """Override to reduce logging noise"""
        logger.info(f"{self.address_string()} - {format % args}")
    
    def get_main_html(self):
        """Get the main chat interface HTML"""
        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Legal Mediation Chatbot - Integrated</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
        }
        
        .header {
            background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
            color: white;
            padding: 20px;
            text-align: center;
        }
        
        .nav-tabs {
            display: flex;
            justify-content: center;
            background: white;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        
        .nav-tab {
            padding: 15px 30px;
            cursor: pointer;
            border: none;
            background: white;
            color: #333;
            font-size: 16px;
            transition: all 0.3s ease;
        }
        
        .nav-tab.active {
            background: #667eea;
            color: white;
        }
        
        .nav-tab:hover:not(.active) {
            background: #f8f9fa;
        }
        
        .content-area {
            max-width: 1200px;
            margin: 20px auto;
            padding: 20px;
        }
        
        .tab-content {
            display: none;
            background: white;
            border-radius: 15px;
            padding: 30px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.1);
        }
        
        .tab-content.active {
            display: block;
        }
        
        .chat-container {
            height: 60vh;
            overflow-y: auto;
            border: 1px solid #e9ecef;
            border-radius: 10px;
            padding: 20px;
            margin-bottom: 20px;
        }
        
        .message {
            margin-bottom: 20px;
            display: flex;
            align-items: flex-start;
        }
        
        .message.user { justify-content: flex-end; }
        .message.bot { justify-content: flex-start; }
        
        .message-content {
            max-width: 70%;
            padding: 15px 20px;
            border-radius: 20px;
            word-wrap: break-word;
        }
        
        .message.user .message-content {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border-bottom-right-radius: 5px;
        }
        
        .message.bot .message-content {
            background: #f8f9fa;
            color: #333;
            border: 1px solid #e9ecef;
            border-bottom-left-radius: 5px;
        }
        
        .chat-input-container {
            display: flex;
            gap: 10px;
        }
        
        .chat-input {
            flex: 1;
            padding: 15px 20px;
            border: 2px solid #e9ecef;
            border-radius: 25px;
            font-size: 16px;
            outline: none;
        }
        
        .send-button {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            padding: 15px 25px;
            border-radius: 25px;
            cursor: pointer;
            font-size: 16px;
            font-weight: 600;
        }
        
        .documents-section {
            margin-top: 20px;
        }
        
        .case-form {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 10px;
            margin-bottom: 20px;
        }
        
        .form-group {
            margin-bottom: 15px;
        }
        
        .form-group label {
            display: block;
            margin-bottom: 5px;
            font-weight: 600;
        }
        
        .form-group input {
            width: 100%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 5px;
        }
        
        .btn {
            background: #667eea;
            color: white;
            border: none;
            padding: 10px 20px;
            border-radius: 5px;
            cursor: pointer;
        }
        
        .cases-list {
            margin-top: 20px;
        }
        
        .case-item {
            background: white;
            border: 1px solid #e9ecef;
            border-radius: 10px;
            padding: 15px;
            margin-bottom: 10px;
        }
    </style>
</head>
<body>
    <div class="header">
        <h1>🤖 Legal Mediation Chatbot - Integrated</h1>
        <p>Chat with AI + Manage Your Case Documents</p>
    </div>
    
    <div class="nav-tabs">
        <button class="nav-tab active" onclick="showTab('chat')">💬 Chat Assistant</button>
        <button class="nav-tab" onclick="showTab('documents')">📁 Document Manager</button>
    </div>
    
    <div class="content-area">
        <!-- Chat Tab -->
        <div id="chat-tab" class="tab-content active">
            <h2>Legal Case Assistant</h2>
            <p>Describe your legal case and get instant guidance, then upload your documents.</p>
            
            <div class="chat-container" id="chatMessages">
                <div style="text-align: center; color: #666; margin: 40px 0;">
                    <h3>Welcome to Legal Mediation Assistant!</h3>
                    <p>Describe your legal situation and I'll help you with:</p>
                    <ul style="text-align: left; display: inline-block; margin-top: 10px;">
                        <li>📋 Required documents for your case</li>
                        <li>⚖️ Whether mediation or court is appropriate</li>
                        <li>🏷️ Case classification and recommendations</li>
                        <li>📁 Document upload and management</li>
                    </ul>
                </div>
            </div>
            
            <div class="chat-input-container">
                <input type="text" class="chat-input" id="messageInput" placeholder="Describe your legal case here...">
                <button class="send-button" id="sendButton" onclick="sendMessage()">Send</button>
            </div>
            
            <div style="text-align: center; margin-top: 20px;">
                <button class="btn" onclick="clearChat()">Clear Chat</button>
                <button class="btn" onclick="loadChatHistory()">Load History</button>
            </div>
        </div>
        
        <!-- Documents Tab -->
        <div id="documents-tab" class="tab-content">
            <h2>Document Management</h2>
            <p>Create cases and manage your legal documents</p>
            
            <div class="case-form">
                <h3>Create New Case</h3>
                <div class="form-group">
                    <label for="caseTitle">Case Title:</label>
                    <input type="text" id="caseTitle" placeholder="e.g., Property Boundary Dispute">
                </div>
                <button class="btn" onclick="createCase()">Create Case</button>
            </div>
            
            <div class="cases-list" id="casesList">
                <h3>Your Cases</h3>
                <div id="casesContainer">Loading cases...</div>
            </div>
        </div>
    </div>

    <script>
        let currentTab = 'chat';
        let sessionId = localStorage.getItem('chatSessionId') || generateSessionId();
        localStorage.setItem('chatSessionId', sessionId);
        
        function generateSessionId() {
            return 'session_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
        }
        
        function showTab(tabName) {
            currentTab = tabName;
            
            // Update tab buttons
            document.querySelectorAll('.nav-tab').forEach(tab => {
                tab.classList.remove('active');
            });
            
            // Find the clicked button and make it active
            const clickedButton = document.querySelector(`[onclick="showTab('${tabName}')"]`);
            if (clickedButton) {
                clickedButton.classList.add('active');
            }
            
            // Update tab content
            document.querySelectorAll('.tab-content').forEach(content => {
                content.classList.remove('active');
            });
            document.getElementById(tabName + '-tab').classList.add('active');
            
            // Load data for documents tab
            if (tabName === 'documents') {
                loadCases();
            }
        }
        
        function sendMessage() {
            const message = document.getElementById('messageInput').value.trim();
            if (!message) return;
            
            console.log('Sending message:', message);
            
            addMessage(message, 'user');
            document.getElementById('messageInput').value = '';
            
            fetch('/api/chat', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({ message: message, session_id: sessionId })
            })
            .then(response => {
                console.log('Response status:', response.status);
                return response.json();
            })
            .then(data => {
                console.log('Response data:', data);
                if (data.status === 'success') {
                    addMessage(data.response.content, 'bot', data.response.analysis);
                } else {
                    addMessage('Sorry, I encountered an error: ' + (data.error || 'Unknown error'), 'bot');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                addMessage('Sorry, I encountered an error. Please try again.', 'bot');
            });
        }
        
        function addMessage(content, type, analysis = null) {
            const messageDiv = document.createElement('div');
            messageDiv.className = `message ${type}`;
            
            const messageContent = document.createElement('div');
            messageContent.className = 'message-content';
            
            if (type === 'bot' && analysis) {
                messageContent.innerHTML = formatBotMessage(content, analysis);
            } else {
                messageContent.textContent = content;
            }
            
            messageDiv.appendChild(messageContent);
            document.getElementById('chatMessages').appendChild(messageDiv);
            document.getElementById('chatMessages').scrollTop = document.getElementById('chatMessages').scrollHeight;
        }
        
        function formatBotMessage(content, analysis) {
            let formatted = content
                .replace(/^# (.*$)/gim, '<h1 style="color: #2c3e50; margin: 20px 0 15px 0; font-size: 24px;">$1</h1>')
                .replace(/^## (.*$)/gim, '<h2 style="color: #34495e; margin: 15px 0 10px 0; font-size: 20px; border-bottom: 2px solid #3498db; padding-bottom: 5px;">$1</h2>')
                .replace(/\*\*(.*?)\*\*/g, '<strong style="color: #2c3e50;">$1</strong>')
                .replace(/\*(.*?)\*/g, '<em style="color: #7f8c8d;">$1</em>')
                .replace(/^• (.*$)/gim, '<li style="margin: 8px 0; padding-left: 10px;">$1</li>')
                .replace(/^---$/gim, '<hr style="border: none; border-top: 2px solid #ecf0f1; margin: 20px 0;">')
                .replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" style="color: #3498db; text-decoration: none; font-weight: 600; padding: 8px 16px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; border-radius: 20px; display: inline-block; margin: 10px 0;">$1</a>');
            
            // Convert bullet points to proper lists - improved regex
            formatted = formatted.replace(/(<li.*?<\/li>(\s*<li.*?<\/li>)*)/gs, '<ul style="margin: 10px 0; padding-left: 20px;">$1</ul>');
            
            if (analysis) {
                const highlightHtml = `
                    <div style="background: linear-gradient(135deg, #e3f2fd 0%, #f3e5f5 100%); border-left: 4px solid #2196f3; padding: 20px; margin: 20px 0; border-radius: 10px; box-shadow: 0 4px 15px rgba(0,0,0,0.1);">
                        <h4 style="color: #1976d2; margin: 0 0 15px 0; font-size: 18px;">📊 Case Analysis Summary</h4>
                        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 15px;">
                            <div>
                                <p style="margin: 8px 0;"><strong style="color: #2c3e50;">Category:</strong> <span style="color: #34495e;">${analysis.category}</span></p>
                                <p style="margin: 8px 0;"><strong style="color: #2c3e50;">Resolution Path:</strong> <span style="color: #34495e;">${analysis.resolution_path}</span></p>
                            </div>
                            <div>
                                <p style="margin: 8px 0;"><strong style="color: #2c3e50;">Documents:</strong> <span style="color: #34495e;">${analysis.documents_needed.length}</span></p>
                                <p style="margin: 8px 0;"><strong style="color: #2c3e50;">Next Steps:</strong> <span style="color: #34495e;">${analysis.next_steps.length}</span></p>
                            </div>
                        </div>
                    </div>
                `;
                formatted += highlightHtml;
            }
            
            return formatted;
        }
        
        function clearChat() {
            fetch('/api/chat/clear', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({ session_id: sessionId })
            })
            .then(() => {
                document.getElementById('chatMessages').innerHTML = `
                    <div style="text-align: center; color: #666; margin: 40px 0;">
                        <h3>Welcome to Legal Mediation Assistant!</h3>
                        <p>Describe your legal situation and I'll help you with:</p>
                        <ul style="text-align: left; display: inline-block; margin-top: 10px;">
                            <li>📋 Required documents for your case</li>
                            <li>⚖️ Whether mediation or court is appropriate</li>
                            <li>🏷️ Case classification and recommendations</li>
                            <li>📁 Document upload and management</li>
                        </ul>
                    </div>
                `;
            })
            .catch(error => console.error('Error clearing chat:', error));
        }
        
        function loadChatHistory() {
            fetch(`/api/chat/history?session_id=${sessionId}`)
                .then(response => response.json())
                .then(data => {
                    if (data.status === 'success' && data.messages.length > 0) {
                        document.getElementById('chatMessages').innerHTML = '';
                        data.messages.forEach(msg => {
                            addMessage(msg.content, msg.type, msg.analysis);
                        });
                    }
                })
                .catch(error => console.error('Error loading chat history:', error));
        }
        
        function createCase() {
            const caseTitle = document.getElementById('caseTitle').value.trim();
            if (!caseTitle) {
                alert('Please enter a case title');
                return;
            }
            
            console.log('Creating case:', caseTitle);
            
            fetch('/api/case/create', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({ case_title: caseTitle })
            })
            .then(response => {
                console.log('Create case response status:', response.status);
                return response.json();
            })
            .then(data => {
                console.log('Create case response data:', data);
                if (data.status === 'success') {
                    alert(`Case "${caseTitle}" created successfully!`);
                    document.getElementById('caseTitle').value = '';
                    loadCases();
                } else {
                    alert('Error creating case: ' + (data.error || 'Unknown error'));
                }
            })
            .catch(error => {
                console.error('Error creating case:', error);
                alert('Error creating case: ' + error.message);
            });
        }
        
        function loadCases() {
            console.log('Loading cases...');
            
            fetch('/api/cases')
                .then(response => {
                    console.log('Load cases response status:', response.status);
                    return response.json();
                })
                .then(data => {
                    console.log('Load cases response data:', data);
                    if (data.status === 'success') {
                        displayCases(data.cases);
                    } else {
                        document.getElementById('casesContainer').innerHTML = 'Error loading cases: ' + (data.message || 'Unknown error');
                    }
                })
                .catch(error => {
                    console.error('Error loading cases:', error);
                    document.getElementById('casesContainer').innerHTML = 'Error loading cases: ' + error.message;
                });
        }
        
        function displayCases(cases) {
            if (cases.length === 0) {
                document.getElementById('casesContainer').innerHTML = '<p>No cases created yet.</p>';
                return;
            }
            
            const casesHtml = cases.map(caseItem => `
                <div class="case-item">
                    <h4>${caseItem.case_title}</h4>
                    <p><strong>Case ID:</strong> ${caseItem.case_id}</p>
                    <p><strong>Created:</strong> ${new Date(caseItem.created_at).toLocaleDateString()}</p>
                    <p><strong>Documents:</strong> ${caseItem.document_count}</p>
                    <p><strong>Size:</strong> ${caseItem.total_size_mb} MB</p>
                    <button class="btn" onclick="viewCaseDocuments('${caseItem.case_id}')">View Documents</button>
                </div>
            `).join('');
            
            document.getElementById('casesContainer').innerHTML = casesHtml;
        }
        
        function viewCaseDocuments(caseId) {
            // This would open a modal or navigate to document view
            alert(`Viewing documents for case: ${caseId}`);
        }
        
        // Handle Enter key in chat input
        document.getElementById('messageInput').addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                sendMessage();
            }
        });
        
        // Load initial data
        document.addEventListener('DOMContentLoaded', function() {
            console.log('Page loaded, initializing...');
            loadChatHistory();
            
            // Test basic functionality
            console.log('Testing basic functionality...');
            console.log('Session ID:', sessionId);
            console.log('Chat messages element:', document.getElementById('chatMessages'));
            console.log('Message input element:', document.getElementById('messageInput'));
            console.log('Send button element:', document.getElementById('sendButton'));
        });
    </script>
</body>
</html>
        """
    
    def get_documents_html(self):
        """Get the documents management HTML"""
        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Document Management - Legal Mediation</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            color: #333;
        }
        
        .header {
            background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
            color: white;
            padding: 20px;
            text-align: center;
        }
        
        .container {
            max-width: 1200px;
            margin: 20px auto;
            padding: 20px;
        }
        
        .back-button {
            background: #3498db;
            color: white;
            border: none;
            padding: 10px 20px;
            border-radius: 5px;
            cursor: pointer;
            margin-bottom: 20px;
            text-decoration: none;
            display: inline-block;
        }
        
        .back-button:hover {
            background: #2980b9;
        }
        
        .upload-section {
            background: white;
            border-radius: 15px;
            padding: 30px;
            margin-bottom: 30px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.1);
        }
        
        .upload-section h2 {
            color: #2c3e50;
            margin-bottom: 20px;
            border-bottom: 3px solid #3498db;
            padding-bottom: 10px;
        }
        
        .form-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
            margin-bottom: 20px;
        }
        
        .form-group {
            margin-bottom: 20px;
        }
        
        .form-group.full-width {
            grid-column: 1 / -1;
        }
        
        .form-group label {
            display: block;
            margin-bottom: 8px;
            font-weight: 600;
            color: #2c3e50;
        }
        
        .form-group input, .form-group select, .form-group textarea {
            width: 100%;
            padding: 12px;
            border: 2px solid #e9ecef;
            border-radius: 8px;
            font-size: 14px;
            transition: border-color 0.3s ease;
        }
        
        .form-group input:focus, .form-group select:focus, .form-group textarea:focus {
            outline: none;
            border-color: #3498db;
            box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
        }
        
        .file-upload-area {
            border: 2px dashed #3498db;
            border-radius: 8px;
            padding: 40px;
            text-align: center;
            background: #f8f9fa;
            transition: all 0.3s ease;
            cursor: pointer;
        }
        
        .file-upload-area:hover {
            background: #e3f2fd;
            border-color: #2980b9;
        }
        
        .file-upload-area.dragover {
            background: #e3f2fd;
            border-color: #2980b9;
            transform: scale(1.02);
        }
        
        .file-upload-icon {
            font-size: 48px;
            color: #3498db;
            margin-bottom: 15px;
        }
        
        .upload-btn {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            padding: 15px 30px;
            border-radius: 25px;
            cursor: pointer;
            font-size: 16px;
            font-weight: 600;
            transition: all 0.3s ease;
            margin-top: 20px;
        }
        
        .upload-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(0,0,0,0.2);
        }
        
        .upload-btn:disabled {
            background: #95a5a6;
            cursor: not-allowed;
            transform: none;
        }
        
        .documents-section {
            background: white;
            border-radius: 15px;
            padding: 30px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.1);
        }
        
        .documents-section h2 {
            color: #2c3e50;
            margin-bottom: 20px;
            border-bottom: 3px solid #3498db;
            padding-bottom: 10px;
        }
        
        .document-item {
            background: #f8f9fa;
            border: 1px solid #e9ecef;
            border-radius: 10px;
            padding: 20px;
            margin-bottom: 15px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .document-info h4 {
            color: #2c3e50;
            margin-bottom: 8px;
        }
        
        .document-info p {
            color: #7f8c8d;
            margin-bottom: 5px;
        }
        
        .document-actions {
            display: flex;
            gap: 10px;
        }
        
        .btn-small {
            padding: 8px 16px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 12px;
            font-weight: 600;
        }
        
        .btn-view {
            background: #3498db;
            color: white;
        }
        
        .btn-delete {
            background: #e74c3c;
            color: white;
        }
        
        .progress-bar {
            width: 100%;
            height: 6px;
            background: #e9ecef;
            border-radius: 3px;
            overflow: hidden;
            margin-top: 10px;
        }
        
        .progress-fill {
            height: 100%;
            background: linear-gradient(90deg, #3498db, #2ecc71);
            width: 0%;
            transition: width 0.3s ease;
        }
        
        .alert {
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 20px;
            display: none;
        }
        
        .alert-success {
            background: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }
        
        .alert-error {
            background: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }
        
        .loading {
            display: none;
            text-align: center;
            padding: 20px;
        }
        
        .spinner {
            border: 4px solid #f3f3f3;
            border-top: 4px solid #3498db;
            border-radius: 50%;
            width: 40px;
            height: 40px;
            animation: spin 1s linear infinite;
            margin: 0 auto 15px;
        }
        
                 @keyframes spin {
             0% { transform: rotate(0deg); }
             100% { transform: rotate(360deg); }
         }
         
         .selected-files {
             background: #f8f9fa;
             border: 1px solid #e9ecef;
             border-radius: 8px;
             padding: 20px;
             margin-top: 15px;
         }
         
         .selected-files h4 {
             color: #2c3e50;
             margin-bottom: 15px;
             font-size: 16px;
         }
         
         .file-item {
             background: white;
             border: 1px solid #e9ecef;
             border-radius: 6px;
             padding: 15px;
             margin-bottom: 10px;
             display: flex;
             justify-content: space-between;
             align-items: center;
         }
         
         .file-item:last-child {
             margin-bottom: 0;
         }
         
         .file-info {
             flex: 1;
         }
         
         .file-name {
             font-weight: 600;
             color: #2c3e50;
             margin-bottom: 5px;
         }
         
         .file-size {
             color: #7f8c8d;
             font-size: 12px;
         }
         
         .file-description {
             margin-top: 8px;
         }
         
         .file-description input {
             width: 100%;
             padding: 8px;
             border: 1px solid #ddd;
             border-radius: 4px;
             font-size: 12px;
         }
         
         .remove-file {
             background: #e74c3c;
             color: white;
             border: none;
             padding: 6px 12px;
             border-radius: 4px;
             cursor: pointer;
             font-size: 12px;
             margin-left: 10px;
         }
         
         .remove-file:hover {
             background: #c0392b;
         }
    </style>
</head>
<body>
    <div class="header">
        <h1>📁 Document Management System</h1>
        <p>Upload and manage your legal case documents securely</p>
    </div>
    
    <div class="container">
        <a href="/" class="back-button">← Back to Chat</a>
        
        <!-- Upload Section -->
        <div class="upload-section">
            <h2>📤 Upload New Document</h2>
            
            <div id="alert" class="alert"></div>
            
            <form id="uploadForm" enctype="multipart/form-data">
                <div class="form-grid">
                    <div class="form-group">
                        <label for="caseTitle">Case Title *</label>
                        <input type="text" id="caseTitle" name="caseTitle" required placeholder="e.g., Property Boundary Dispute">
                    </div>
                    
                                         <div class="form-group">
                         <label for="documentType">Case Type *</label>
                         <select id="documentType" name="documentType" required>
                             <option value="">Select case type</option>
                             <option value="Property disputes">Property disputes</option>
                             <option value="Employment disputes">Employment disputes</option>
                             <option value="Matrimonial disputes">Matrimonial disputes</option>
                             <option value="Family disputes">Family disputes</option>
                             <option value="Business & commercial disputes">Business & commercial disputes</option>
                             <option value="Consumer disputes">Consumer disputes</option>
                             <option value="Neighbourhood/community disputes">Neighbourhood/community disputes</option>
                             <option value="Banking & financial disputes">Banking & financial disputes</option>
                             <option value="Insurance disputes">Insurance disputes</option>
                             <option value="Medical negligence claims">Medical negligence claims</option>
                             <option value="Other">Other</option>
                         </select>
                     </div>
                    
                                         <div class="form-group full-width">
                         <label for="documentDescription">General Case Description</label>
                         <textarea id="documentDescription" name="documentDescription" rows="3" placeholder="Brief description of your case (optional)"></textarea>
                     </div>
                    
                                         <div class="form-group full-width">
                         <label>File Upload *</label>
                         <div class="file-upload-area" id="fileUploadArea">
                             <div class="file-upload-icon">📄</div>
                             <p><strong>Click to select files</strong> or drag and drop here</p>
                             <p style="color: #7f8c8d; font-size: 14px;">Supports: PDF, DOC, DOCX, TXT, JPG, PNG (Max: 10MB per file)</p>
                             <input type="file" id="documentFile" name="documentFile" accept=".pdf,.doc,.docx,.txt,.jpg,.jpeg,.png" multiple style="display: none;" required>
                         </div>
                         <div id="selectedFiles" class="selected-files" style="display: none;">
                             <h4>Selected Files:</h4>
                             <div id="filesList"></div>
                         </div>
                         <div class="progress-bar" id="progressBar" style="display: none;">
                             <div class="progress-fill" id="progressFill"></div>
                         </div>
                     </div>
                </div>
                
                <button type="submit" class="upload-btn" id="uploadBtn">
                    <span id="uploadBtnText">📤 Upload Document</span>
                </button>
            </form>
        </div>
        
        <!-- Documents List Section -->
        <div class="documents-section">
            <h2>📋 Your Uploaded Documents</h2>
            
            <div class="loading" id="loading">
                <div class="spinner"></div>
                <p>Loading documents...</p>
            </div>
            
            <div id="documentsContainer">
                <p style="text-align: center; color: #7f8c8d; padding: 40px;">
                    No documents uploaded yet. Start by uploading your first document above.
                </p>
            </div>
        </div>
    </div>
    
    <script>
                 // File upload handling
         const fileUploadArea = document.getElementById('fileUploadArea');
         const fileInput = document.getElementById('documentFile');
         const uploadForm = document.getElementById('uploadForm');
         const progressBar = document.getElementById('progressBar');
         const progressFill = document.getElementById('progressFill');
         const uploadBtn = document.getElementById('uploadBtn');
         const uploadBtnText = document.getElementById('uploadBtnText');
         const alert = document.getElementById('alert');
         const selectedFiles = document.getElementById('selectedFiles');
         const filesList = document.getElementById('filesList');
         
         let selectedFilesArray = [];
         
         // Drag and drop functionality
         fileUploadArea.addEventListener('click', () => fileInput.click());
         
         fileUploadArea.addEventListener('dragover', (e) => {
             e.preventDefault();
             fileUploadArea.classList.add('dragover');
         });
         
         fileUploadArea.addEventListener('dragleave', () => {
             fileUploadArea.classList.remove('dragover');
         });
         
         fileUploadArea.addEventListener('drop', (e) => {
             e.preventDefault();
             fileUploadArea.classList.remove('dragover');
             const files = e.dataTransfer.files;
             if (files.length > 0) {
                 addFilesToSelection(files);
             }
         });
         
         fileInput.addEventListener('change', (e) => {
             if (e.target.files.length > 0) {
                 addFilesToSelection(e.target.files);
             }
         });
         
         function addFilesToSelection(files) {
             Array.from(files).forEach(file => {
                 // Check if file is already selected
                 if (!selectedFilesArray.find(f => f.name === file.name && f.size === file.size)) {
                     selectedFilesArray.push(file);
                 }
             });
             updateFileDisplay();
         }
         
         function updateFileDisplay() {
             if (selectedFilesArray.length === 0) {
                 selectedFiles.style.display = 'none';
                 fileUploadArea.innerHTML = `
                     <div class="file-upload-icon">📄</div>
                     <p><strong>Click to select files</strong> or drag and drop here</p>
                     <p style="color: #7f8c8d; font-size: 14px;">Supports: PDF, DOC, DOCX, TXT, JPG, PNG (Max: 10MB per file)</p>
                 `;
                 return;
             }
             
             selectedFiles.style.display = 'block';
             
             const filesHtml = selectedFilesArray.map((file, index) => {
                 const fileName = file.name;
                 const fileSize = (file.size / (1024 * 1024)).toFixed(2);
                 return `
                     <div class="file-item">
                         <div class="file-info">
                             <div class="file-name">${fileName}</div>
                             <div class="file-size">Size: ${fileSize} MB</div>
                             <div class="file-description">
                                 <input type="text" placeholder="Brief description for ${fileName}" 
                                        data-file-index="${index}" class="file-description-input">
                             </div>
                         </div>
                         <button type="button" class="remove-file" onclick="removeFile(${index})">Remove</button>
                     </div>
                 `;
             }).join('');
             
             filesList.innerHTML = filesHtml;
             
             // Update upload area to show count
             fileUploadArea.innerHTML = `
                 <div class="file-upload-icon">✅</div>
                 <p><strong>${selectedFilesArray.length} file(s) selected</strong></p>
                 <p style="color: #7f8c8d; font-size: 14px;">Click to add more files or drag and drop</p>
             `;
         }
         
         function removeFile(index) {
             selectedFilesArray.splice(index, 1);
             updateFileDisplay();
         }
        
                 // Form submission
         uploadForm.addEventListener('submit', async (e) => {
             e.preventDefault();
             
             if (selectedFilesArray.length === 0) {
                 showAlert('Please select at least one file to upload', 'error');
                 return;
             }
             
             // Show progress
             progressBar.style.display = 'block';
             uploadBtn.disabled = true;
             uploadBtnText.textContent = `📤 Uploading ${selectedFilesArray.length} file(s)...`;
             
             try {
                 let successCount = 0;
                 let errorCount = 0;
                 
                                      // Upload each file individually
                     for (let i = 0; i < selectedFilesArray.length; i++) {
                         const file = selectedFilesArray[i];
                         const fileDescription = document.querySelector(`[data-file-index="${i}"]`)?.value || '';
                         
                         const formData = new FormData();
                         formData.append('caseTitle', document.getElementById('caseTitle').value);
                         formData.append('caseType', document.getElementById('documentType').value);
                         formData.append('documentDescription', document.getElementById('documentDescription').value || fileDescription);
                         formData.append('documentFile', file);
                     
                     try {
                         const response = await fetch('/api/document/upload', {
                             method: 'POST',
                             body: formData
                         });
                         
                         const result = await response.json();
                         
                         if (result.status === 'success') {
                             successCount++;
                         } else {
                             errorCount++;
                             console.error(`Failed to upload ${file.name}:`, result.error);
                         }
                     } catch (error) {
                         errorCount++;
                         console.error(`Error uploading ${file.name}:`, error);
                     }
                     
                     // Update progress
                     const progress = ((i + 1) / selectedFilesArray.length) * 100;
                     progressFill.style.width = progress + '%';
                 }
                 
                 // Show final result
                 if (errorCount === 0) {
                     showAlert(`All ${successCount} files uploaded successfully!`, 'success');
                 } else if (successCount > 0) {
                     showAlert(`${successCount} files uploaded successfully, ${errorCount} failed.`, 'success');
                 } else {
                     showAlert('All file uploads failed. Please try again.', 'error');
                 }
                 
                 // Reset form and clear selection
                 uploadForm.reset();
                 selectedFilesArray = [];
                 updateFileDisplay();
                 loadDocuments();
                 
             } catch (error) {
                 showAlert('Upload failed: ' + error.message, 'error');
             } finally {
                 progressBar.style.display = 'none';
                 uploadBtn.disabled = false;
                 uploadBtnText.textContent = '📤 Upload Document';
                 progressFill.style.width = '0%';
             }
         });
        
        function showAlert(message, type) {
            alert.textContent = message;
            alert.className = `alert alert-${type}`;
            alert.style.display = 'block';
            
            setTimeout(() => {
                alert.style.display = 'none';
            }, 5000);
        }
        
        // Load documents on page load
        document.addEventListener('DOMContentLoaded', () => {
            loadDocuments();
        });
        
        async function loadDocuments() {
            const loading = document.getElementById('loading');
            const container = document.getElementById('documentsContainer');
            
            loading.style.display = 'block';
            container.style.display = 'none';
            
            try {
                const response = await fetch('/api/cases');
                const result = await response.json();
                
                if (result.status === 'success') {
                    displayDocuments(result.cases);
                } else {
                    container.innerHTML = '<p style="color: #e74c3c;">Error loading documents: ' + (result.error || 'Unknown error') + '</p>';
                }
            } catch (error) {
                container.innerHTML = '<p style="color: #e74c3c;">Error loading documents: ' + error.message + '</p>';
            } finally {
                loading.style.display = 'none';
                container.style.display = 'block';
            }
        }
        
        function displayDocuments(cases) {
            const container = document.getElementById('documentsContainer');
            
            if (cases.length === 0) {
                container.innerHTML = '<p style="text-align: center; color: #7f8c8d; padding: 40px;">No documents uploaded yet. Start by uploading your first document above.</p>';
                return;
            }
            
            let html = '';
            cases.forEach(caseItem => {
                html += `
                    <div class="document-item">
                        <div class="document-info">
                            <h4>${caseItem.case_title}</h4>
                            <p><strong>Case ID:</strong> ${caseItem.case_id}</p>
                            <p><strong>Documents:</strong> ${caseItem.document_count}</p>
                            <p><strong>Size:</strong> ${caseItem.total_size_mb} MB</p>
                        </div>
                        <div class="document-actions">
                            <button class="btn-small btn-view" onclick="viewCaseDocuments('${caseItem.case_id}')">View</button>
                            <button class="btn-small btn-delete" onclick="deleteCase('${caseItem.case_id}')">Delete</button>
                        </div>
                    </div>
                `;
            });
            
            container.innerHTML = html;
        }
        
        function viewCaseDocuments(caseId) {
            // This would open a modal or navigate to document view
            alert('Viewing documents for case: ' + caseId);
        }
        
        function deleteCase(caseId) {
            if (confirm('Are you sure you want to delete this case and all its documents?')) {
                // Implement delete functionality
                alert('Delete functionality will be implemented');
            }
        }
    </script>
</body>
</html>
        """

def start_server(port=5000):
    """Start the integrated HTTP server"""
    server_address = ('', port)
    httpd = HTTPServer(server_address, IntegratedChatbotHandler)
    
    print("Legal Mediation Chatbot - Integrated Web UI")
    print("=" * 50)
    print(f"Starting web server on port {port}...")
    print(f"Open your browser and go to: http://localhost:{port}")
    print("Features:")
    print("- 💬 AI Chat Assistant with follow-up questions")
    print("- 📁 Document Management System")
    print("- 🏷️ Case Creation and Organization")
    print("Press Ctrl+C to stop")
    print()
    
    try:
        # Open browser automatically
        webbrowser.open(f'http://localhost:{port}')
        
        # Start server
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("\nShutting down server...")
        httpd.shutdown()

if __name__ == '__main__':
    if not chatbot or not doc_manager:
        print("Failed to initialize components. Please check your setup.")
        print("Make sure Ollama is running and all dependencies are installed.")
    else:
        start_server() 