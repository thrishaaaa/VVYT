#!/usr/bin/env python3
"""
Test multiple case types to verify chatbot accuracy
"""

from enhanced_chatbot import EnhancedLegalMediationChatbot

def test_multiple_cases():
    """Test the chatbot with different case types"""
    try:
        print("Initializing chatbot...")
        chatbot = EnhancedLegalMediationChatbot()
        
        # Test cases covering different categories
        test_cases = [
            "I have a property boundary dispute with my neighbor. They built a fence on my land.",
            "My employer terminated me without notice and I want to file a complaint.",
            "I bought a defective product and the company is refusing to refund my money.",
            "My spouse and I are getting divorced and need to divide our assets.",
            "I have a business partnership dispute over profit sharing."
        ]
        
        print("\n" + "="*60)
        print("TESTING MULTIPLE CASE TYPES")
        print("="*60)
        
        for i, case in enumerate(test_cases, 1):
            print(f"\n--- Test Case {i} ---")
            print(f"Input: {case}")
            print("-" * 50)
            
            # Time the response
            import time
            start_time = time.time()
            
            analysis = chatbot.analyze_case(case)
            
            end_time = time.time()
            response_time = end_time - start_time
            
            print(f"Response Time: {response_time:.2f} seconds")
            print(f"Category: {analysis.category}")
            print(f"Resolution Path: {analysis.resolution_path}")
            print(f"Documents Needed: {len(analysis.documents_needed)} documents")
            print(f"Documents: {', '.join(analysis.documents_needed)}")
            print(f"Guidance: {analysis.guidance[:100]}...")
            print(f"Next Steps: {', '.join(analysis.next_steps)}")
            
            print("-" * 50)
        
        print("\n" + "="*60)
        print("ALL TESTS COMPLETED")
        print("="*60)
        
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    test_multiple_cases() 