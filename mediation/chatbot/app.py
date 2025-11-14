#!/usr/bin/env python3
"""
Legal Mediation Chatbot - NOW USING A TRAINED, FOCUSED (2-output) CLASSIFIER
WITH A CRITICAL KEYWORD SAFETY NET
"""
import logging
from typing import List
from dataclasses import dataclass
import joblib  # For loading the model
from flask import Flask, request, jsonify

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

@dataclass
class CaseAnalysis:
    """Data class for case analysis results (simplified for focused model)"""
    category: str
    suggested_resolution_path: str
    documents_needed: List[str]
    guidance: str
    next_steps: List[str]
    sub_category: str = "N/A"
    complexity: str = "N/A"
    urgency: str = "N/A"
    predicted_mediation_outcome: str = "N/A"


class EnhancedLegalMediationChatbot:
    """
    This class uses a pre-trained model and adds a rule-based safety net
    to override dangerous predictions.
    """
    PREDICTED_COLUMNS = [
        'category',
        'suggested_resolution_path'
    ]

    # --- NEW: CRITICAL KEYWORD LIST ---
    # Any case containing these words will be forced to "Court"
    CRITICAL_KEYWORDS = [
    # Already present
    'killed', 'murder', 'assault', 'violent', 'violence', 'fraudulent',
    'theft', 'criminal', 'weapon', 'police', 'stole', 'robbery', 'arson',
    'fraud', 'extortion', 'blackmail', 'kidnapping', 'kill', 'poison',
    'kidnapped', 'abused',

    # New additions (important)
    'rape', 'sexual assault', 'molestation', 'harassed', 'abuse', 'abusive',
    'domestic violence', 'dowry', 'threat', 'threatened', 'threatening',
    'illegal', 'scam', 'forgery', 'cheating', 'bribery', 'corruption',

    # Injury / bodily harm
    'stabbed', 'beaten', 'hit', 'attacked', 'gun', 'shot', 'shooting',
    'strangled', 'slapped', 'injury', 'injured', 'hurt',

    # Property and public crimes
    'trespassing', 'vandalism', 'smuggling', 'trafficking', 'drug',
    'drugs', 'narcotics', 'contraband',

    # Financial crimes
    'embezzlement', 'money laundering', 'scammed', 'counterfeit',
    'identity theft', 'scamming',

    # Cyber crimes
    'hacked', 'hacking', 'cybercrime', 'data breach', 'phishing',

    # Kid and vulnerable-related
    'child abuse', 'child labour', 'human trafficking',

    # Death / danger words
    'dead', 'death', 'fatal', 'dangerous', 'hazardous',

    # Court-related red flags
    'FIR', 'complaint filed', 'charge sheet', 'arrest', 'arrested'
]

    # ------------------------------------

    def __init__(self, model_path: str = 'focused_classifier.pkl'):
        try:
            self.classifier = joblib.load(model_path)
            logger.info(f"Successfully loaded focused classifier from {model_path}")
        except FileNotFoundError:
            logger.error(f"FATAL: Model file not found at {model_path}.")
            logger.error("Please run train_classifier.py (the focused version) first!")
            raise
        except Exception as e:
            logger.error(f"Error loading model from {model_path}: {e}")
            raise

        # Document list (remains the same)
        self.category_documents = {
            "Matrimonial disputes": ["Marriage certificate", "Address proof", "ID proof", "Marriage photographs", "Income proof"],
            "Family disputes": ["Legal heir certificate", "Family tree", "Property ownership docs", "Tax receipts", "ID proof"],
            "Property disputes": ["Property title deed", "Sale/gift/will deed", "Encumbrance certificate", "Property tax receipts", "Survey map"],
            "Business & commercial disputes": ["Partnership deed / MoA / AoA", "Business registration", "Contracts", "Invoices", "Communication records"],
            "Employment disputes": ["Employment contract", "Payslips", "Termination letter", "ID proof", "Workplace policy docs"],
            "Consumer disputes": ["Proof of purchase", "Warranty card", "Product defect photos", "Communication records", "ID proof"],
            "Neighbourhood/community disputes": ["Property proof", "Complaint letters", "Photo/video evidence", "Municipal notices"],
            "Banking & financial disputes": ["Loan agreement", "Bank statements", "Bank correspondence", "ID proof"],
            "Insurance disputes": ["Policy document", "Premium receipts", "Claim form", "Rejection letter", "ID proof"],
            "Medical negligence claims": ["Medical records", "Hospital bills", "Test reports", "Expert opinion", "ID proof"],
            "Other": ["Please gather all relevant documents, contracts, and communication records related to your case."]
        }
        self.valid_categories = list(self.category_documents.keys())

    def analyze_case(self, message: str) -> CaseAnalysis:
        """
        Analyze a legal case using the model AND the safety net override.
        """
        try:
            # --- 1. Get Model Prediction ---
            prediction_values = self.classifier.predict([message])[0]
            predictions = dict(zip(self.PREDICTED_COLUMNS, prediction_values))
            
            # Get initial model predictions
            category = predictions.get('category', 'Other')
            resolution_path = predictions.get('suggested_resolution_path', 'Mediation')

            # --- 2. !! START OF RULE-BASED OVERRIDE (SAFETY NET) !! ---
            message_lower = message.lower()
            is_critical = any(keyword in message_lower for keyword in self.CRITICAL_KEYWORDS)
            
            if is_critical:
                if resolution_path == "Mediation":
                    logger.warning(f"CRITICAL OVERRIDE: Model predicted 'Mediation' for case with critical keywords. Forcing to 'Court'. Input: {message[:50]}...")
                    resolution_path = "Court" # Force to Court
                
                # Also override inappropriate categories
                # (e.g., "Family disputes" is wrong for a murder, even if "Court" is right)
                inappropriate_categories = [
                    "Family disputes", "Property disputes", "Consumer disputes", 
                    "Neighbourhood/community disputes", "Matrimonial disputes"
                ]
                
                # Check if it's a criminal word AND not a negligence case
                is_medical = "negligence" in message_lower or "medical" in message_lower or "hospital" in message_lower
                
                if category in inappropriate_categories and not is_medical:
                    logger.warning(f"Overriding model's predicted category '{category}' to 'Other' due to critical keywords.")
                    category = "Other" # Force category to 'Other'
            # --- !! END OF RULE-BASED OVERRIDE !! ---

            # --- 3. Finalize values ---
            if category not in self.valid_categories:
                category = "Other"

            documents = self.category_documents.get(category, self.category_documents["Other"])

            # --- 4. Set Guidance & Next Steps ---
            guidance = f"Based on the analysis, this appears to be a case under '{category}'. The suggested initial path is {resolution_path}."
            next_steps = [
                f"Gather the required documents for {category}.",
                f"Prepare a summary of the main issue.",
                f"Contact our platform to discuss the recommended {resolution_path} path."
            ]

            # Special guidance for overridden cases
            if is_critical and resolution_path == "Court":
                guidance = f"Based on the serious nature of the description (Category: '{category}'), this case is not suitable for mediation."
                next_steps = [
                    "We strongly recommend contacting local law enforcement or a legal professional immediately.",
                    "Gather all official documents (e.g., police reports, legal notices).",
                    "This platform is not suitable for resolving matters of this nature."
                ]

            logger.info(f"Final predictions (post-override): {{'category': '{category}', 'resolution_path': '{resolution_path}'}}")

            return CaseAnalysis(
                category=category,
                suggested_resolution_path=resolution_path,
                documents_needed=documents,
                guidance=guidance,
                next_steps=next_steps
            )

        except Exception as e:
            logger.error(f"Error during focused case analysis: {e}")
            return self._fallback_analysis() 

    def _fallback_analysis(self) -> CaseAnalysis:
        return CaseAnalysis(
            category="Other",
            suggested_resolution_path="Pending Review",
            documents_needed=["Please gather all relevant documents."],
            guidance="We were unable to automatically classify your case. Please contact a legal professional.",
            next_steps=["Contact support."]
        )

# --- Flask Server (Stays the same) ---

app = Flask(__name__)
try:
    chatbot = EnhancedLegalMediationChatbot(model_path='focused_classifier.pkl')
except Exception as e:
    logger.error(f"Failed to initialize chatbot on startup: {e}")
    chatbot = None 

@app.route("/analyze", methods=["POST"])
def analyze():
    if chatbot is None:
         return jsonify({"error": "Chatbot service failed to initialize. Cannot analyze case."}), 500

    data = request.get_json()
    if not data or "message" not in data:
        return jsonify({"error": "Invalid request. 'message' is required."}), 400

    message = data["message"]
    analysis = chatbot.analyze_case(message)
    return jsonify(analysis.__dict__)

if __name__ == "__main__":
    app.run(port=5000, debug=True)