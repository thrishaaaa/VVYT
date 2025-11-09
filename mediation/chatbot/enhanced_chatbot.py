#!/usr/bin/env python3
"""
Enhanced Legal Mediation Chatbot
Follows the Modelfile exactly - responds in normal conversational text
"""

import json
import logging
from typing import Dict, List, Optional
from dataclasses import dataclass
from ollama import Client
import re

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

@dataclass
class CaseAnalysis:
    """Data class for case analysis results"""
    documents_needed: List[str]
    resolution_path: str
    category: str
    guidance: str
    next_steps: List[str]

class EnhancedLegalMediationChatbot:
    """Enhanced legal mediation chatbot using Ollama - follows Modelfile exactly"""
    
    def __init__(self, model_name: str = "chatbot", base_url: str = "http://localhost:11434"):
        self.model_name = model_name
        self.base_url = base_url
        self.client = Client(host=base_url)
        
        # Validate model availability
        self._validate_model()
        
        # Document requirements from Modelfile
        self.category_documents = {
            "Matrimonial disputes": [
                "Marriage certificate", "Address proof of both spouses", "ID proof of both spouses", 
                "Marriage photographs", "Grounds evidence (medical, financial, messages)", 
                "Child's birth certificate", "Income proof"
            ],
            "Family disputes": [
                "Legal heir certificate", "Family tree", "Property ownership docs", 
                "Tax receipts", "ID proof", "Previous partition deeds", "Succession certificate"
            ],
            "Property disputes": [
                "Property title deed", "Sale/gift/will deed", "Encumbrance certificate", 
                "Property tax receipts", "Survey map", "Lease agreement (if rental)", "ID proof"
            ],
            "Business & commercial disputes": [
                "Partnership deed / MoA / AoA", "Business registration", "Contracts", 
                "Invoices", "Communication records", "Financial statements", "IP registrations"
            ],
            "Employment disputes": [
                "Employment contract", "Payslips", "Termination letter", "ID proof", 
                "Workplace policy docs", "Complaint records"
            ],
            "Consumer disputes": [
                "Proof of purchase", "Warranty card", "Product defect photos", 
                "Communication records", "ID proof", "Service contract"
            ],
            "Neighbourhood/community disputes": [
                "Property proof", "Complaint letters", "Photo/video evidence", 
                "Municipal notices", "ID proof"
            ],
            "Banking & financial disputes": [
                "Loan agreement", "Repayment schedule", "Bank statements", 
                "Bank correspondence", "ID proof", "Mortgage documents"
            ],
            "Insurance disputes": [
                "Policy document", "Premium receipts", "Claim form", "Rejection letter", 
                "Supporting evidence", "ID proof"
            ],
            "Medical negligence claims": [
                "Medical records", "Hospital bills", "Test reports", "Photo evidence", 
                "Expert opinion", "Communication with hospital", "ID proof"
            ]
        }
        
        # Valid categories from Modelfile
        self.valid_categories = list(self.category_documents.keys())
        
        # Resolution paths
        self.valid_resolution_paths = ["Mediation", "Court"]
    
    def _validate_model(self):
        """Validate that the specified model is available"""
        try:
            models = self.client.list()
            available_models = []
            
            for model in models.get('models', []):
                if isinstance(model, dict):
                    model_name = model.get('name', str(model))
                else:
                    model_name = str(model)
                available_models.append(model_name)
            
            logger.info(f"Available models: {available_models}")
            
            # Check if our model is available
            if self.model_name in available_models:
                logger.info(f"Using model: {self.model_name}")
            else:
                # Try to find a similar model
                for model in available_models:
                    if self.model_name in model or "chatbot" in model:
                        # Extract just the model name, not the full string
                        if "model='" in model:
                            self.model_name = model.split("model='")[1].split("'")[0]
                        else:
                            self.model_name = model
                        logger.info(f"Using available model: {self.model_name}")
                        break
                else:
                    # Use the first available model
                    if available_models:
                        first_model = available_models[0]
                        if "model='" in first_model:
                            self.model_name = first_model.split("model='")[1].split("'")[0]
                        else:
                            self.model_name = first_model
                        logger.warning(f"Model '{self.model_name}' not found. Using available model: {self.model_name}")
                    else:
                        raise Exception("No models available")
                        
        except Exception as e:
            logger.error(f"Error validating model: {e}")
            raise
    
    def _get_system_prompt(self) -> str:
        """Get the system prompt - follows Modelfile exactly"""
        return """You are a legal mediation assistant for India. 
Your job is to:
1. Understand the user's case description (2–3 lines).
2. Classify the case into one of the following categories: 
   - Matrimonial disputes
   - Family disputes
   - Property disputes
   - Business & commercial disputes
   - Employment disputes
   - Consumer disputes
   - Neighbourhood/community disputes
   - Banking & financial disputes
   - Insurance disputes
   - Medical negligence claims
3. Based on the category, provide the list of required documents for mediation in India.
4. Decide whether the case can be solved via mediation or should be taken to court:
   - Mediation: If the dispute is non-criminal, has scope for settlement, and both parties can be contacted.
   - Court: If the dispute involves criminal offense, severe fraud, violence, or complex multi-party issues.
5. Give your reply in **normal conversational text** (not JSON).
6. Use clear, simple language and keep your answers concise but complete.

**Category-wise documents list**:
- Matrimonial disputes: Marriage certificate, Address proof of both spouses, ID proof of both spouses, Marriage photographs, Grounds evidence (medical, financial, messages), Child's birth certificate, Income proof.
- Family disputes: Legal heir certificate, Family tree, Property ownership docs, Tax receipts, ID proof, Previous partition deeds, Succession certificate.
- Property disputes: Property title deed, Sale/gift/will deed, Encumbrance certificate, Property tax receipts, Survey map, Lease agreement (if rental), ID proof.
- Business & commercial disputes: Partnership deed / MoA / AoA, Business registration, Contracts, Invoices, Communication records, Financial statements, IP registrations.
- Employment disputes: Employment contract, Payslips, Termination letter, ID proof, Workplace policy docs, Complaint records.
- Consumer disputes: Proof of purchase, Warranty card, Product defect photos, Communication records, ID proof, Service contract.
- Neighbourhood/community disputes: Property proof, Complaint letters, Photo/video evidence, Municipal notices, ID proof.
- Banking & financial disputes: Loan agreement, Repayment schedule, Bank statements, Bank correspondence, ID proof, Mortgage documents.
- Insurance disputes: Policy document, Premium receipts, Claim form, Rejection letter, Supporting evidence, ID proof.
- Medical negligence claims: Medical records, Hospital bills, Test reports, Photo evidence, Expert opinion, Communication with hospital, ID proof.

When replying:
- First: State the identified category.
- Second: Say if mediation is possible or if court is recommended.
- Third: Give the required documents list.
- Be polite and helpful."""
    
    def _get_case_analysis_prompt(self, message: str, is_followup: bool = False, context: str = "") -> str:
        """Get the prompt for case analysis - follows Modelfile exactly"""
        if is_followup:
            return f"Follow-up question: {message}. Provide brief, helpful guidance."
        else:
            return f"Please analyze this legal case: {message}"
    
    def analyze_case(self, message: str, is_followup: bool = False, context: str = "") -> CaseAnalysis:
        """Analyze a legal case - follows Modelfile exactly"""
        try:
            prompt = self._get_case_analysis_prompt(message, is_followup, context)
            
            logger.info(f"Analyzing case: {message[:100]}...")
            
            # Use threading with timeout for Windows compatibility
            import threading
            import queue
            
            result_queue = queue.Queue()
            
            def chat_with_timeout():
                try:
                    response = self.client.chat(
                        model=self.model_name,
                        messages=[
                            {"role": "system", "content": self._get_system_prompt()},
                            {"role": "user", "content": prompt}
                        ],
                        options={
                            "temperature": 0.1,  # Low temperature for consistent responses
                            "num_predict": 300,  # Allow longer responses for natural text
                            "top_k": 10,
                            "top_p": 0.9,
                            "repeat_penalty": 1.1
                        }
                    )
                    result_queue.put(("success", response))
                except Exception as e:
                    result_queue.put(("error", e))
            
            # Start chat in separate thread with 20 second timeout
            chat_thread = threading.Thread(target=chat_with_timeout)
            chat_thread.daemon = True
            chat_thread.start()
            chat_thread.join(timeout=20)
            
            if chat_thread.is_alive():
                logger.warning("Model response timeout, using fallback analysis")
                return self._fallback_analysis(message)
            
            # Get result
            try:
                result_type, result_data = result_queue.get_nowait()
                if result_type == "success":
                    response_content = result_data.message.content
                    logger.info(f"Raw response received: {response_content[:200]}...")
                    
                    # Parse the natural language response
                    analysis = self._parse_natural_response(response_content, message)
                    return analysis
                else:
                    raise result_data
            except queue.Empty:
                logger.warning("No response received, using fallback analysis")
                return self._fallback_analysis(message)
            
        except Exception as e:
            logger.error(f"Error analyzing case: {e}")
            # Return a fallback response
            return self._fallback_analysis(message)
    
    def _parse_natural_response(self, response: str, original_message: str) -> CaseAnalysis:
        """Parse natural language response from the model"""
        try:
            # Extract category from the response
            category = self._extract_category_from_response(response, original_message)
            
            # Extract resolution path from the response
            resolution_path = self._extract_resolution_from_response(response, original_message)
            
            # Get documents from our predefined list based on category
            documents_needed = self.category_documents.get(category, ["Contact legal professional for specific requirements"])
            
            # Extract guidance and next steps from the natural response
            guidance = self._extract_guidance_from_response(response)
            next_steps = self._extract_next_steps_from_response(response)
            
            return CaseAnalysis(
                documents_needed=documents_needed,
                resolution_path=resolution_path,
                category=category,
                guidance=guidance,
                next_steps=next_steps
            )
            
        except Exception as e:
            logger.error(f"Error parsing natural response: {e}")
            # Fallback to basic analysis
            return self._fallback_analysis(original_message)
    
    def _extract_category_from_response(self, response: str, original_message: str) -> str:
        """Extract case category from natural language response"""
        response_lower = response.lower()
        message_lower = original_message.lower()
        
        # First try to find exact category mentions in the response
        for category in self.valid_categories:
            if category.lower() in response_lower:
                return category
        
        # If no exact match in response, analyze the original message
        # Employment cases
        if any(word in message_lower for word in [
            "employer", "employee", "fired", "terminated", "job", "work", "workplace", 
            "salary", "wage", "contract", "harassment", "discrimination", "resignation", 
            "layoff", "bonus", "overtime", "promotion"
        ]):
            return "Employment disputes"
        
        # Property cases
        elif any(word in message_lower for word in [
            "property", "land", "boundary", "real estate", "rent", "lease", "title", 
            "deed", "neighbor", "fence", "building"
        ]):
            return "Property disputes"
        
        # Business cases
        elif any(word in message_lower for word in [
            "business", "commercial", "contract", "company", "partnership", "invoice", 
            "partnership", "agreement"
        ]):
            return "Business & commercial disputes"
        
        # Family cases
        elif any(word in message_lower for word in [
            "family", "heir", "succession", "inheritance", "partition"
        ]):
            return "Family disputes"
        
        # Matrimonial cases
        elif any(word in message_lower for word in [
            "marriage", "divorce", "spouse", "matrimonial", "husband", "wife"
        ]):
            return "Matrimonial disputes"
        
        # Consumer cases
        elif any(word in message_lower for word in [
            "consumer", "product", "service", "warranty", "defect", "purchase"
        ]):
            return "Consumer disputes"
        
        # Banking cases
        elif any(word in message_lower for word in [
            "bank", "loan", "financial", "credit", "mortgage", "banking"
        ]):
            return "Banking & financial disputes"
        
        # Insurance cases
        elif any(word in message_lower for word in [
            "insurance", "policy", "claim", "premium"
        ]):
            return "Insurance disputes"
        
        # Medical cases
        elif any(word in message_lower for word in [
            "medical", "hospital", "doctor", "negligence", "malpractice", "treatment"
        ]):
            return "Medical negligence claims"
        
        # Neighbourhood cases
        elif any(word in message_lower for word in [
            "neighbour", "community", "noise", "nuisance", "municipal"
        ]):
            return "Neighbourhood/community disputes"
        
        else:
            return "Other"
    
    def _extract_resolution_from_response(self, response: str, original_message: str) -> str:
        """Extract resolution path from natural language response"""
        response_lower = response.lower()
        message_lower = original_message.lower()
        
        # Check for explicit mentions in response
        if "mediation" in response_lower and "court" not in response_lower:
            return "Mediation"
        elif "court" in response_lower:
            return "Court"
        
        # Check for criminal/violent cases that must go to court
        criminal_keywords = [
            "criminal", "fraud", "violence", "assault", "murder", "theft", "robbery", 
            "kidnapping", "extortion", "blackmail", "cyber crime", "hacking", "forgery",
            "counterfeit", "drugs", "narcotics", "terrorism", "money laundering"
        ]
        
        if any(word in message_lower for word in criminal_keywords):
            return "Court"
        
        # Check for complex legal matters that typically go to court
        complex_keywords = [
            "complex", "constitutional", "public interest", "writ", "habeas corpus",
            "mandamus", "certiorari", "prohibition", "quo warranto"
        ]
        
        if any(word in message_lower for word in complex_keywords):
            return "Court"
        
        # Default to mediation for civil disputes
        return "Mediation"
    
    def _extract_guidance_from_response(self, response: str) -> str:
        """Extract guidance from natural language response"""
        # Look for guidance patterns
        guidance_patterns = [
            r"guidance[:\s]+(.*?)(?=\n|$)",
            r"advice[:\s]+(.*?)(?=\n|$)",
            r"recommendation[:\s]+(.*?)(?=\n|$)",
            r"note[:\s]+(.*?)(?=\n|$)"
        ]
        
        for pattern in guidance_patterns:
            match = re.search(pattern, response, re.IGNORECASE)
            if match:
                return match.group(1).strip()
        
        # Fallback: use the last meaningful paragraph
        paragraphs = response.split('\n\n')
        if len(paragraphs) > 1:
            last_para = paragraphs[-1].strip()
            if len(last_para) > 20:  # Only if it's substantial
                return last_para
        
        return "Please consult with a legal professional for detailed guidance."
    
    def _extract_next_steps_from_response(self, response: str) -> str:
        """Extract next steps from natural language response"""
        # Look for next steps patterns
        steps_patterns = [
            r"next steps?[:\s]+(.*?)(?=\n|$)",
            r"steps?[:\s]+(.*?)(?=\n|$)",
            r"action[:\s]+(.*?)(?=\n|$)",
            r"what to do[:\s]+(.*?)(?=\n|$)"
        ]
        
        for pattern in steps_patterns:
            match = re.search(pattern, response, re.IGNORECASE)
            if match:
                steps_text = match.group(1).strip()
                # Split by common separators
                steps = re.split(r'[,;]|\d+\.', steps_text)
                steps = [step.strip() for step in steps if step.strip()]
                if steps:
                    return steps[:3]  # Return max 3 steps
        
        # Fallback steps
        return [
            "Gather the required documents listed above",
            "Contact a mediation center or legal professional",
            "Prepare a brief summary of your case"
        ]
    
    def _fallback_analysis(self, message: str) -> CaseAnalysis:
        """Fallback analysis when parsing fails - improved accuracy"""
        # Use the same category extraction logic for consistency
        category = self._extract_category_from_response("", message)
        documents = self.category_documents.get(category, ["Contact legal professional for specific requirements"])
        
        # Determine resolution path based on category and content
        message_lower = message.lower()
        
        # Criminal cases that must go to court
        criminal_keywords = [
            "criminal", "murder", "fraud", "violence", "assault", "theft", "robbery", 
            "kidnapping", "extortion", "blackmail", "cyber crime", "hacking", "forgery",
            "counterfeit", "drugs", "narcotics", "terrorism", "money laundering"
        ]
        
        if any(word in message_lower for word in criminal_keywords):
            resolution_path = "Court"
        else:
            resolution_path = "Mediation"
        
        # Provide more specific guidance based on category
        if category == "Employment disputes":
            guidance = "This appears to be an employment termination case. Employment disputes in India are typically resolved through labor courts or mediation, depending on the specific circumstances."
            next_steps = [
                "Gather employment contract and termination documents",
                "Contact a labor lawyer or employment dispute mediator",
                "File complaint with appropriate labor authority if needed"
            ]
        elif category == "Property disputes":
            guidance = "This appears to be a property-related dispute. Property disputes can often be resolved through mediation if both parties are willing to negotiate."
            next_steps = [
                "Gather property documents and evidence",
                "Contact a property dispute mediator",
                "Consider legal consultation for property rights"
            ]
        elif category == "Matrimonial disputes":
            guidance = "This appears to be a matrimonial/family law matter. Family disputes are often best resolved through mediation to preserve relationships."
            next_steps = [
                "Gather marriage and family documents",
                "Contact a family law mediator",
                "Consider counseling before legal action"
            ]
        else:
            guidance = "I've identified your case category and provided the required documents. For detailed guidance, please consult with a legal professional."
            next_steps = [
                "Gather the required documents listed above",
                "Contact a mediation center",
                "Prepare your case summary"
            ]
        
        return CaseAnalysis(
            documents_needed=documents,
            resolution_path=resolution_path,
            category=category,
            guidance=guidance,
            next_steps=next_steps
        )
    
    def get_case_summary(self, analysis: CaseAnalysis) -> str:
        """Get a formatted summary of the case analysis"""
        summary = f"""
**Case Analysis Results:**

**Category:** {analysis.category}
**Resolution Path:** {analysis.resolution_path}
**Required Documents:** {', '.join(analysis.documents_needed)}
**Guidance:** {analysis.guidance}
**Next Steps:** {', '.join(analysis.next_steps)}
        """.strip()
        
        return summary

def main():
    """Test the enhanced chatbot"""
    try:
        chatbot = EnhancedLegalMediationChatbot()
        print("Chatbot initialized successfully!")
        
        # Test case
        test_message = "I have a property boundary dispute with my neighbor. They built a fence on my land."
        print(f"\nTesting with: {test_message}")
        
        analysis = chatbot.analyze_case(test_message)
        print(chatbot.get_case_summary(analysis))
        
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    main() 