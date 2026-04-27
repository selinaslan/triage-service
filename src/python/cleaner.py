import sys
import re
import spacy
from bs4 import BeautifulSoup

# Load English NLP model for Named Entity Recognition (NER)
# Justification: Java doesn't have a native, lightweight equivalent for this.
try:
    nlp = spacy.load("en_core_web_sm")
except OSError:
    # Fallback if model is not downloaded
    nlp = None

def clean_and_redact(text):
    if not text:
        return ""

    # 1. HTML STRIPPING
    # Removes all HTML tags and leaves only the inner text
    soup = BeautifulSoup(text, "html.parser")
    text = soup.get_text(separator=" ")

    # 2. EMAIL REDACTION
    # Regex to catch standard email formats
    email_pattern = r'[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+'
    text = re.sub(email_pattern, '[EMAIL]', text)

    # 3. PHONE NUMBER REDACTION
    # Matches common international and local phone formats
    phone_pattern = r'\b\d{3}[-.\s]??\d{3}[-.\s]??\d{4}\b'
    text = re.sub(phone_pattern, '[PHONE]', text)

    # 4. NAMED ENTITY RECOGNITION (NER)
    # This is where Python justifies itself. It finds names, locations, and organizations.
    if nlp:
        doc = nlp(text)
        # We process in reverse to not mess up indices during replacement
        entities = sorted(doc.ents, key=lambda x: x.start_char, reverse=True)
        for ent in entities:
            if ent.label_ in ["PERSON", "ORG", "GPE"]:
                # Replace names with [PERSON], cities with [GPE], etc.
                text = text[:ent.start_char] + f"[{ent.label_}]" + text[ent.end_char:]

    # 5. FINAL CLEANUP
    # Remove redundant whitespaces and newlines
    text = " ".join(text.split())

    return text

if __name__ == "__main__":
    # Standard boilerplate for receiving input from Java ProcessBuilder
    if len(sys.argv) > 1:
        raw_message = sys.argv[1]
        processed_text = clean_and_redact(raw_message)
        # Print output to stdout for Java to read
        print(processed_text)