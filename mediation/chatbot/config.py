"""Configuration settings for the Legal Mediation Chatbot"""

import os
from typing import List

class ChatbotConfig:
    """Configuration class for the chatbot"""
    
    # Ollama settings
    DEFAULT_MODEL = "llama3"
    OLLAMA_BASE_URL = os.getenv("OLLAMA_BASE_URL", "http://localhost:11434")
    
    # Logging settings
    LOG_LEVEL = os.getenv("LOG_LEVEL", "INFO")
    
    # Case categories
    VALID_CATEGORIES = [
        "Divorce",
        "Property Dispute", 
        "Business Dispute",
        "Employment Dispute",
        "Family Dispute",
        "Consumer Complaint",
        "Other"
    ]
    
    # Resolution paths
    VALID_RESOLUTION_PATHS = ["Mediation", "Court"]
    
    # Mediation-suitable case keywords
    MEDIATION_KEYWORDS = [
        "family", "custody", "visitation", "support", "property", "boundary",
        "business", "partnership", "employment", "harassment", "discrimination",
        "consumer", "complaint", "divorce", "asset", "neighbor", "contract",
        "negotiation", "dispute resolution"
    ]
    
    # Court-required case keywords
    COURT_KEYWORDS = [
        "criminal", "fraud", "misconduct", "emergency", "injunction",
        "enforcement", "refusal", "violent", "threat"
    ] 