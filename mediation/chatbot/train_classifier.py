import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.pipeline import Pipeline
from sklearn.metrics import accuracy_score # Removed classification_report for simplicity, can be added back
from sklearn.multioutput import MultiOutputClassifier
import joblib
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def train_focused_classifier():
    # 1. Load the dataset
    try:
        data = pd.read_json('case_dataset.json')
        logger.info(f"Loaded {len(data)} records from case_dataset.json")
    except Exception as e:
        logger.error(f"Could not read case_dataset.json. Error: {e}")
        return

    # --- Define Features (X) and Focused Targets (y) ---
    X = data['description']

    # *** CHANGED: List only the columns you want the model to predict ***
    target_columns = [
        'category',
        'suggested_resolution_path'
        # Removed: 'sub_category', 'complexity', 'urgency', 'predicted_mediation_outcome'
    ]
    # Check if target columns exist in the loaded data
    missing_cols = [col for col in target_columns if col not in data.columns]
    if missing_cols:
         logger.error(f"Dataset is missing required target columns: {', '.join(missing_cols)}")
         return
         
    y = data[target_columns]
    # ----------------------------------------------------

    # 3. Split the data
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

    # 4. Create a model pipeline with MultiOutputClassifier
    model = Pipeline([
        ('tfidf', TfidfVectorizer(stop_words='english')),
        ('clf', MultiOutputClassifier(LogisticRegression(max_iter=1000, random_state=42)))
    ])

    # 5. Train the model
    logger.info(f"Training the model to predict: {', '.join(target_columns)}...")
    model.fit(X_train, y_train)
    logger.info("Training complete.")

    # 6. Test the model and print accuracy
    logger.info("Evaluating model on test data...")
    y_pred = model.predict(X_test)

    # --- Evaluate each target column separately ---
    print("\n--- Evaluation Results ---")
    for i, col in enumerate(target_columns):
        # Ensure y_test[col] and y_pred[:, i] are valid before scoring
        if col in y_test.columns and y_pred.shape[1] > i:
             col_accuracy = accuracy_score(y_test[col], y_pred[:, i])
             print(f"Accuracy for '{col}': {col_accuracy * 100:.2f}%")
        else:
             logger.error(f"Could not evaluate column '{col}'. Index mismatch or column not found.")
             
    print("-------------------------\n")


    # 7. Save the trained model to a file (using a new name for clarity)
    model_filename = 'focused_classifier.pkl' # <-- SAVED WITH A NEW NAME
    joblib.dump(model, model_filename)
    logger.info(f"Focused multi-output model saved successfully as {model_filename}")

if __name__ == "__main__":
    train_focused_classifier()