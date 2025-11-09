# Legal Mediation Chatbot

A Python-based legal mediation chatbot that uses Ollama to analyze legal cases and provide structured guidance.

## Features

- **Case Analysis**: Analyzes legal case descriptions and identifies required documents
- **Resolution Path**: Determines whether cases can be resolved through mediation or require court intervention
- **Case Classification**: Categorizes cases into legal types (Divorce, Property Dispute, Business Dispute, etc.)
- **Structured Output**: Returns results in consistent JSON format
- **Easy API Integration**: Designed to be easily wrapped in REST APIs

## Requirements

- Python 3.7+
- Ollama running locally with LLaMA 3 or compatible model
- Internet connection for Ollama model downloads

## Installation

1. Navigate to the chatbot directory:
   ```bash
   cd chatbot
   ```

2. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```

3. Ensure Ollama is running with a compatible model:
   ```bash
   ollama run llama3
   ```

## Usage

### Basic Usage

```python
from legal_mediation_chatbot import LegalMediationChatbot

# Initialize chatbot
chatbot = LegalMediationChatbot()

# Analyze a case
case_description = "My neighbor is building a fence that encroaches on my property."
analysis = chatbot.analyze_case(case_description)

print(f"Category: {analysis.category}")
print(f"Resolution: {analysis.resolution_path}")
print(f"Documents needed: {analysis.documents_needed}")
```

### Getting a Summary

```python
# Get a formatted summary
summary = chatbot.get_case_summary(case_description)
print(summary)
```

### Custom Model

```python
# Use a different Ollama model
chatbot = LegalMediationChatbot(model_name="llama3.2")
```

## API Integration

The chatbot is designed to be easily integrated into APIs:

```python
from flask import Flask, request, jsonify
from legal_mediation_chatbot import LegalMediationChatbot

app = Flask(__name__)
chatbot = LegalMediationChatbot()

@app.route('/analyze', methods=['POST'])
def analyze_case():
    data = request.get_json()
    case_description = data.get('case_description')
    
    if not case_description:
        return jsonify({'error': 'Case description required'}), 400
    
    analysis = chatbot.analyze_case(case_description)
    
    return jsonify({
        'documents_needed': analysis.documents_needed,
        'resolution_path': analysis.resolution_path,
        'category': analysis.category
    })

if __name__ == '__main__':
    app.run(debug=True)
```

## Configuration

Modify `config.py` to customize:
- Default Ollama model
- Server URL
- Logging level
- Valid case categories
- Resolution paths

## Testing

Run the test suite:

```bash
python test_chatbot.py
```

## Output Format

The chatbot returns structured data in this format:

```json
{
    "documents_needed": ["Document 1", "Document 2"],
    "resolution_path": "Mediation",
    "category": "Property Dispute"
}
```

## Error Handling

The chatbot includes robust error handling:
- Invalid JSON responses
- Model unavailability
- Network issues
- Fallback parsing for malformed responses

## Project Structure

```
chatbot/
├── legal_mediation_chatbot.py  # Main chatbot module
├── config.py                   # Configuration settings
├── test_chatbot.py            # Unit tests
├── requirements.txt            # Python dependencies
└── README.md                  # This file
```

## Integration with Spring Boot Project

This chatbot is designed to work alongside your existing Spring Boot legal mediation application. You can:

1. **Run it as a separate service** and communicate via HTTP
2. **Integrate it directly** using Python subprocess calls
3. **Use it as a microservice** in your legal system architecture

## Future Enhancements

- Support for multiple languages
- Case history tracking
- Integration with legal databases
- Confidence scoring for recommendations
- Multi-modal input (text + documents)

## License

This project is provided as-is for educational and development purposes. 