#!/usr/bin/env python3
"""
Simple API wrapper for the Legal Mediation Chatbot
This demonstrates how to expose the chatbot as a web service
"""

from flask import Flask, request, jsonify
from legal_mediation_chatbot import LegalMediationChatbot
import logging

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Initialize Flask app
app = Flask(__name__)

# Initialize chatbot
try:
    chatbot = LegalMediationChatbot()
    logger.info("Chatbot initialized successfully")
except Exception as e:
    logger.error(f"Failed to initialize chatbot: {e}")
    chatbot = None

@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({
        "status": "healthy",
        "chatbot_available": chatbot is not None
    })

@app.route('/analyze', methods=['POST'])
def analyze_case():
    """
    Analyze a legal case
    
    Expected JSON payload:
    {
        "case_description": "Description of the legal case"
    }
    
    Returns:
    {
        "documents_needed": ["Document 1", "Document 2"],
        "resolution_path": "Mediation",
        "category": "Property Dispute"
    }
    """
    if not chatbot:
        return jsonify({
            "error": "Chatbot not available",
            "status": "error"
        }), 503
    
    try:
        # Get JSON data from request
        data = request.get_json()
        
        if not data:
            return jsonify({
                "error": "No JSON data provided",
                "status": "error"
            }), 400
        
        case_description = data.get('case_description')
        
        if not case_description:
            return jsonify({
                "error": "case_description is required",
                "status": "error"
            }), 400
        
        # Analyze the case
        analysis = chatbot.analyze_case(case_description)
        
        # Return structured response
        return jsonify({
            "documents_needed": analysis.documents_needed,
            "resolution_path": analysis.resolution_path,
            "category": analysis.category,
            "status": "success"
        })
        
    except Exception as e:
        logger.error(f"Error analyzing case: {e}")
        return jsonify({
            "error": "Failed to analyze case",
            "details": str(e),
            "status": "error"
        }), 500

@app.route('/analyze', methods=['GET'])
def analyze_case_get():
    """GET endpoint for testing with query parameters"""
    if not chatbot:
        return jsonify({
            "error": "Chatbot not available",
            "status": "error"
        }), 503
    
    case_description = request.args.get('case_description')
    
    if not case_description:
        return jsonify({
            "error": "case_description query parameter is required",
            "status": "error"
        }), 400
    
    try:
        # Analyze the case
        analysis = chatbot.analyze_case(case_description)
        
        # Return structured response
        return jsonify({
            "documents_needed": analysis.documents_needed,
            "resolution_path": analysis.resolution_path,
            "category": analysis.category,
            "status": "success"
        })
        
    except Exception as e:
        logger.error(f"Error analyzing case: {e}")
        return jsonify({
            "error": "Failed to analyze case",
            "details": str(e),
            "status": "error"
        }), 500

@app.route('/categories', methods=['GET'])
def get_categories():
    """Get available case categories"""
    from config import ChatbotConfig
    
    return jsonify({
        "categories": ChatbotConfig.VALID_CATEGORIES,
        "resolution_paths": ChatbotConfig.VALID_RESOLUTION_PATHS,
        "status": "success"
    })

@app.errorhandler(404)
def not_found(error):
    """Handle 404 errors"""
    return jsonify({
        "error": "Endpoint not found",
        "status": "error"
    }), 404

@app.errorhandler(500)
def internal_error(error):
    """Handle 500 errors"""
    return jsonify({
        "error": "Internal server error",
        "status": "error"
    }), 500

if __name__ == '__main__':
    if chatbot:
        print("Legal Mediation Chatbot API")
        print("=" * 30)
        print("Available endpoints:")
        print("  GET  /health - Health check")
        print("  POST /analyze - Analyze case (JSON payload)")
        print("  GET  /analyze?case_description=... - Analyze case (query param)")
        print("  GET  /categories - Get available categories")
        print()
        print("Starting server on http://localhost:5000")
        print("Press Ctrl+C to stop")
        print()
        
        app.run(debug=True, host='0.0.0.0', port=5000)
    else:
        print("Failed to initialize chatbot. Please check your Ollama setup.")
        print("Make sure Ollama is running and a compatible model is available.") 