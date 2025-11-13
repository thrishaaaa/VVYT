import pandas as pd
import logging

logging.basicConfig(level=logging.INFO, format='%(levelname)s: %(message)s') # Simplified logging format
logger = logging.getLogger(__name__)

# --- CONFIGURATION ---
FILE_TO_REVIEW = 'case_dataset.json'
NUMBER_TO_REVIEW = 20  # Look at 20 random samples
COLUMNS_TO_DISPLAY = [ # Select columns most relevant for review
    'description',
    'category',
    'sub_category',
    'suggested_resolution_path',
    'predicted_mediation_outcome'
]
# Set pandas display options
pd.set_option('display.max_colwidth', None) # Show full description
pd.set_option('display.width', 1000)        # Allow wider table in terminal
pd.set_option('display.max_rows', 50)       # Show more rows if needed
# ---------------------

def review_data():
    try:
        data = pd.read_json(FILE_TO_REVIEW)
        logger.info(f"Loaded {len(data)} records from {FILE_TO_REVIEW}\n")

        # Check if dataset has the expected columns
        missing_cols = [col for col in COLUMNS_TO_DISPLAY if col not in data.columns]
        if missing_cols:
            logger.error(f"Dataset is missing expected columns: {', '.join(missing_cols)}")
            logger.error("Please ensure the dataset was generated correctly with all required fields.")
            return

        if len(data) == 0:
             logger.warning("The dataset file is empty.")
             return

        display_count = min(NUMBER_TO_REVIEW, len(data))

        if len(data) < NUMBER_TO_REVIEW:
            logger.warning(f"Dataset has only {len(data)} records. Showing all.")
            sample = data[COLUMNS_TO_DISPLAY] # Select specific columns
        else:
            # Get a random sample, selecting only the desired columns
            sample = data.sample(NUMBER_TO_REVIEW, random_state=1)[COLUMNS_TO_DISPLAY]

        print(f"--- Showing {display_count} random samples for review ---")
        # Print the selected columns of the sample DataFrame
        print(sample.to_string()) # .to_string() gives better formatting control

        logger.info(f"\nYour job: Read each 'description' and verify the assigned categories/predictions.")
        logger.info(f"Focus on '{COLUMNS_TO_DISPLAY[1]}', '{COLUMNS_TO_DISPLAY[2]}', '{COLUMNS_TO_DISPLAY[3]}', and '{COLUMNS_TO_DISPLAY[4]}'.")


    except FileNotFoundError:
        logger.error(f"ERROR: Could not find {FILE_TO_REVIEW}.")
        logger.error("Please ensure the 'case_dataset.json' file exists in the same directory.")
    except ValueError as ve:
         logger.error(f"ERROR reading JSON file: {ve}")
         logger.error("The 'case_dataset.json' file might be empty, corrupted, or not valid JSON.")
    except Exception as e:
        logger.error(f"An unexpected error occurred: {e}")

if __name__ == "__main__":
    review_data()